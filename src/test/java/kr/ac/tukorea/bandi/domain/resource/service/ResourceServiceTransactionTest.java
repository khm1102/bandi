package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceRevisionParam;
import kr.ac.tukorea.bandi.domain.resource.mapper.ResourceMapper;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceFile;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
class ResourceServiceTransactionTest {

    private static final Long MISSING_FILE_ID = 9_999_999L;

    private final ResourceService resourceService;
    private final ResourceMapper resourceMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private FileService fileService;

    private Long actorMemberId;
    private Long cohortId;
    private Long resourceId;
    private Long oldFileId;
    private Long newFileId;

    @Autowired
    ResourceServiceTransactionTest(ResourceService resourceService,
                                   ResourceMapper resourceMapper,
                                   TeamMapper teamMapper, CohortMapper cohortMapper,
                                   MemberMapper memberMapper,
                                   JdbcTemplate jdbcTemplate) {
        this.resourceService = resourceService;
        this.resourceMapper = resourceMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "자료트랜잭션기수", (short) 2996,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        Member actor = new Member(null, "2996000001", "자료관리자", null,
                null, null, teamId, cohortId, ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(actor);
        actorMemberId = actor.getMemberId();
        oldFileId = insertStoredFile("resource/transaction-old", "a");
        newFileId = insertStoredFile("resource/transaction-new", "b");

        Resource resource = Resource.draft(ResourceTargetScope.ALL, null,
                "SCRIPT", "기존 자료", "기존 설명", false, actorMemberId);
        resourceMapper.insert(resource);
        resourceId = resource.getResourceId();
        resourceMapper.insertFile(ResourceFile.create(resourceId, oldFileId,
                1, 0, actorMemberId));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM resource_file WHERE resource_id = ?", resourceId);
        jdbcTemplate.update("DELETE FROM resource WHERE resource_id = ?", resourceId);
        jdbcTemplate.update("DELETE FROM stored_file WHERE stored_file_id IN (?, ?)",
                oldFileId, newFileId);
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", actorMemberId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void revision의_일부_파일_연결이_실패하면_새_revision_전체를_롤백한다() {
        given(memberService.lookupAccessContext(actorMemberId))
                .willReturn(new MemberAccessContext(actorMemberId, null,
                        true, false, true));
        given(fileService.lookupPrivateReady(newFileId))
                .willReturn(fileReference(newFileId, "new.pdf"));
        given(fileService.lookupPrivateReady(MISSING_FILE_ID))
                .willReturn(fileReference(MISSING_FILE_ID, "missing.pdf"));

        assertThatThrownBy(() -> resourceService.replaceFiles(actorMemberId,
                new ResourceRevisionParam(resourceId,
                        List.of(newFileId, MISSING_FILE_ID))))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(resourceMapper.lookupMaxRevisionForUpdate(resourceId)).contains(1);
        assertThat(resourceMapper.searchCurrentFileLinks(resourceId))
                .extracting("storedFileId")
                .containsExactly(oldFileId);
    }

    private FileReferenceResponse fileReference(Long storedFileId, String name) {
        return new FileReferenceResponse(storedFileId, name,
                "application/pdf", 1024L);
    }

    private Long insertStoredFile(String storageKey, String hashSeed) {
        jdbcTemplate.update("""
                INSERT INTO stored_file (
                    original_name, storage_scope_code, storage_key, content_type,
                    size_bytes, sha256_hash, object_etag, uploaded_by_member_id,
                    upload_status_code
                ) VALUES (?, 'PRIVATE', ?, 'application/pdf', 1024, ?, 'etag', ?, 'READY')
                """, storageKey + ".pdf", storageKey, hashSeed.repeat(64), actorMemberId);
        return jdbcTemplate.queryForObject("""
                SELECT stored_file_id FROM stored_file WHERE storage_key = ?
                """, Long.class, storageKey);
    }
}
