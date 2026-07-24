package kr.ac.tukorea.bandi.domain.notice.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeUpdateParam;
import kr.ac.tukorea.bandi.domain.notice.mapper.InternalNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
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
class InternalNoticeServiceTransactionTest {

    private static final Long MISSING_FILE_ID = 9_999_999L;

    private final InternalNoticeService internalNoticeService;
    private final InternalNoticeMapper internalNoticeMapper;
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
    private Long internalNoticeId;
    private Long oldFileId;

    @Autowired
    InternalNoticeServiceTransactionTest(InternalNoticeService internalNoticeService,
                                         InternalNoticeMapper internalNoticeMapper,
                                         TeamMapper teamMapper, CohortMapper cohortMapper,
                                         MemberMapper memberMapper,
                                         JdbcTemplate jdbcTemplate) {
        this.internalNoticeService = internalNoticeService;
        this.internalNoticeMapper = internalNoticeMapper;
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
        Cohort cohort = new Cohort(null, "공지트랜잭션기수", true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        Member actor = new Member(null, "2997000001", "공지관리자", null,
                null, null, teamId, cohortId, ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(actor);
        actorMemberId = actor.getMemberId();
        oldFileId = insertStoredFile();

        InternalNotice notice = InternalNotice.draft(InternalNoticeTargetScope.ALL,
                null, "기존 제목", "기존 본문", false, actorMemberId);
        internalNoticeMapper.insert(notice);
        internalNoticeId = notice.getInternalNoticeId();
        internalNoticeMapper.insertAttachment(InternalNoticeAttachment.create(
                internalNoticeId, oldFileId, 0));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM internal_notice_read WHERE internal_notice_id = ?",
                internalNoticeId);
        jdbcTemplate.update("DELETE FROM internal_notice_attachment WHERE internal_notice_id = ?",
                internalNoticeId);
        jdbcTemplate.update("DELETE FROM internal_notice WHERE internal_notice_id = ?",
                internalNoticeId);
        jdbcTemplate.update("DELETE FROM stored_file WHERE stored_file_id = ?", oldFileId);
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", actorMemberId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 새_첨부_연결이_실패하면_본문과_기존_첨부가_함께_롤백된다() {
        given(memberService.lookupAccessContext(actorMemberId))
                .willReturn(new MemberAccessContext(actorMemberId, null,
                        true, false, true));
        given(fileService.lookupPrivateReady(MISSING_FILE_ID))
                .willReturn(new FileReferenceResponse(MISSING_FILE_ID, "없는.pdf",
                        "application/pdf", 1024L));
        InternalNoticeUpdateParam param = new InternalNoticeUpdateParam(
                internalNoticeId, InternalNoticeTargetScope.ALL, null,
                "변경 제목", "변경 본문", true, List.of(MISSING_FILE_ID));

        assertThatThrownBy(() -> internalNoticeService.update(actorMemberId, param))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(internalNoticeMapper.lookupById(internalNoticeId))
                .isPresent()
                .get()
                .extracting(InternalNotice::getTitle)
                .isEqualTo("기존 제목");
        assertThat(internalNoticeMapper.searchAttachmentFileIds(internalNoticeId))
                .containsExactly(oldFileId);
    }

    private Long insertStoredFile() {
        jdbcTemplate.update("""
                INSERT INTO stored_file (
                    original_name, storage_scope_code, storage_key, content_type,
                    size_bytes, sha256_hash, object_etag, uploaded_by_member_id,
                    upload_status_code
                ) VALUES (
                    '기존.pdf', 'PRIVATE', 'notice/transaction-old',
                    'application/pdf', 1024, ?, 'etag', ?, 'READY'
                )
                """, "c".repeat(64), actorMemberId);
        return jdbcTemplate.queryForObject("""
                SELECT stored_file_id
                FROM stored_file
                WHERE storage_key = 'notice/transaction-old'
                """, Long.class);
    }
}
