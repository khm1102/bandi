package kr.ac.tukorea.bandi.domain.resource.mapper;

import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceManageSearchCondition;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceReadableSearchCondition;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceFileLinkResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceFile;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class ResourceMapperTest {

    private final ResourceMapper resourceMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long stageTeamId;
    private Long operatorTeamId;
    private Long adminMemberId;
    private Long stageMemberId;

    @Autowired
    ResourceMapperTest(ResourceMapper resourceMapper, TeamMapper teamMapper,
                       CohortMapper cohortMapper, MemberMapper memberMapper,
                       JdbcTemplate jdbcTemplate) {
        this.resourceMapper = resourceMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        List<Team> teams = teamMapper.searchAll();
        stageTeamId = teamId(teams, "무대팀");
        operatorTeamId = teamId(teams, "오퍼팀");
        Cohort cohort = new Cohort(null, "26-자료실", (short) 2026,
                CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        adminMemberId = insertMember("2026000101", "자료관리자", stageTeamId,
                cohort.getCohortId(), ClubRole.ADMIN);
        stageMemberId = insertMember("2026000102", "무대부원", stageTeamId,
                cohort.getCohortId(), ClubRole.MEMBER);
    }

    @Test
    void 자료를_저장하고_운영_상세를_조회한다() {
        Resource resource = draft(ResourceTargetScope.ALL, null, "SCRIPT", "전체 대본");

        resourceMapper.insert(resource);

        assertThat(resource.getResourceId()).isNotNull();
        assertThat(resourceMapper.lookupById(resource.getResourceId()))
                .isPresent().get()
                .extracting(Resource::getTitle)
                .isEqualTo("전체 대본");
        assertThat(resourceMapper.lookupManageContent(resource.getResourceId()))
                .isPresent().get()
                .extracting("createdByName")
                .isEqualTo("자료관리자");
    }

    @Test
    void 파일_revision은_기존을_보존하고_최대값을_현재로_조회한다() {
        Resource resource = insertDraft(ResourceTargetScope.ALL, null,
                "SCRIPT", "버전 대본");
        Long oldFileId = insertStoredFile("old.pdf", "a");
        Long currentFileId = insertStoredFile("current.pdf", "b");
        resourceMapper.insertFile(ResourceFile.create(resource.getResourceId(),
                oldFileId, 1, 0, adminMemberId));
        resourceMapper.insertFile(ResourceFile.create(resource.getResourceId(),
                currentFileId, 2, 0, adminMemberId));

        assertThat(resourceMapper.lookupMaxRevisionForUpdate(resource.getResourceId()))
                .contains(2);
        assertThat(resourceMapper.searchCurrentFileLinks(resource.getResourceId()))
                .extracting(ResourceFileLinkResponse::storedFileId)
                .containsExactly(currentFileId);
        assertThat(resourceMapper.searchFileLinks(resource.getResourceId()))
                .extracting(ResourceFileLinkResponse::revisionNo)
                .containsExactly(2, 1);
    }

    @Test
    void MEMBER는_전체와_소속_팀만_ADMIN은_모든_팀_게시_자료를_조회한다() {
        publishWithFile(insertDraft(ResourceTargetScope.ALL, null,
                "SCRIPT", "전체 자료"), "all.pdf", "c");
        publishWithFile(insertDraft(ResourceTargetScope.TEAM, stageTeamId,
                "MINUTES", "무대 자료"), "stage.pdf", "d");
        publishWithFile(insertDraft(ResourceTargetScope.TEAM, operatorTeamId,
                "VIDEO", "오퍼 자료"), "operator.pdf", "e");

        List<ResourceSummaryResponse> memberResult = resourceMapper.searchReadable(
                new ResourceReadableSearchCondition(null, null, stageTeamId,
                        false, 0, 20));
        List<ResourceSummaryResponse> adminResult = resourceMapper.searchReadable(
                new ResourceReadableSearchCondition(null, null, stageTeamId,
                        true, 0, 20));

        assertThat(memberResult).extracting(ResourceSummaryResponse::title)
                .containsExactlyInAnyOrder("전체 자료", "무대 자료");
        assertThat(adminResult).extracting(ResourceSummaryResponse::title)
                .containsExactlyInAnyOrder("전체 자료", "무대 자료", "오퍼 자료");
    }

    @Test
    void 초안과_보관_자료와_파일_없는_게시_자료는_읽기에서_제외한다() {
        insertDraft(ResourceTargetScope.ALL, null, "SCRIPT", "초안");
        Resource archived = insertDraft(ResourceTargetScope.ALL, null,
                "SCRIPT", "보관").archive(adminMemberId);
        resourceMapper.update(archived);
        Resource withoutFile = insertDraft(ResourceTargetScope.ALL, null,
                "SCRIPT", "빈 게시").publish(adminMemberId);
        resourceMapper.update(withoutFile);

        assertThat(resourceMapper.searchReadable(new ResourceReadableSearchCondition(
                null, null, stageTeamId, false, 0, 20))).isEmpty();
        assertThat(resourceMapper.lookupReadableContent(withoutFile.getResourceId(),
                stageTeamId, false)).isEmpty();
    }

    @Test
    void 중요도와_카테고리와_키워드로_자료를_검색한다() {
        Resource normal = insertDraft(ResourceTargetScope.ALL, null,
                "MINUTES", "일반 회의록");
        publishWithFile(normal, "minutes.pdf", "f");
        Resource pinned = Resource.draft(ResourceTargetScope.ALL, null,
                "SCRIPT", "중요 대본", "검색 대상 본문", true, adminMemberId);
        resourceMapper.insert(pinned);
        publishWithFile(pinned, "script.pdf", "1");

        List<ResourceSummaryResponse> result = resourceMapper.searchReadable(
                new ResourceReadableSearchCondition("검색 대상", "SCRIPT",
                        stageTeamId, false, 0, 20));

        assertThat(result).extracting(ResourceSummaryResponse::title)
                .containsExactly("중요 대본");
        assertThat(result.get(0).pinned()).isTrue();
    }

    @Test
    void 다운로드_권한은_현재_revision과_대상_범위를_함께_검증한다() {
        Resource resource = insertDraft(ResourceTargetScope.TEAM, stageTeamId,
                "SCRIPT", "무대 대본");
        Long oldFileId = insertStoredFile("old-download.pdf", "2");
        Long currentFileId = insertStoredFile("current-download.pdf", "3");
        resourceMapper.insertFile(ResourceFile.create(resource.getResourceId(),
                oldFileId, 1, 0, adminMemberId));
        resourceMapper.insertFile(ResourceFile.create(resource.getResourceId(),
                currentFileId, 2, 0, adminMemberId));
        resourceMapper.update(resource.publish(adminMemberId));

        assertThat(resourceMapper.existsReadableCurrentFile(resource.getResourceId(),
                currentFileId, stageTeamId, false)).isTrue();
        assertThat(resourceMapper.existsReadableCurrentFile(resource.getResourceId(),
                oldFileId, stageTeamId, false)).isFalse();
        assertThat(resourceMapper.existsReadableCurrentFile(resource.getResourceId(),
                currentFileId, operatorTeamId, false)).isFalse();
    }

    @Test
    void DB는_대상_범위와_revision_제약을_강제한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO resource (
                    target_scope_code, team_id, category_code, title, description,
                    status_code, created_by_member_id, updated_by_member_id
                ) VALUES ('ALL', ?, 'SCRIPT', '오류', '설명', 'DRAFT', ?, ?)
                """, stageTeamId, adminMemberId, adminMemberId))
                .isInstanceOf(DataAccessException.class);

        Resource resource = insertDraft(ResourceTargetScope.ALL, null,
                "SCRIPT", "제약 자료");
        Long fileId = insertStoredFile("constraint.pdf", "4");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO resource_file (
                    resource_id, stored_file_id, revision_no, display_order,
                    uploaded_by_member_id
                ) VALUES (?, ?, 0, 0, ?)
                """, resource.getResourceId(), fileId, adminMemberId))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 논리_삭제된_자료는_운영과_읽기_조회에서_제외한다() {
        Resource resource = insertDraft(ResourceTargetScope.ALL, null,
                "SCRIPT", "삭제 자료");
        jdbcTemplate.update("UPDATE resource SET deleted_dttm = NOW(6) WHERE resource_id = ?",
                resource.getResourceId());

        assertThat(resourceMapper.lookupById(resource.getResourceId())).isEmpty();
        assertThat(resourceMapper.searchManageable(new ResourceManageSearchCondition(
                null, null, null, null, null, 0, 20))).isEmpty();
    }

    private Resource draft(ResourceTargetScope scope, Long teamId,
                           String category, String title) {
        return Resource.draft(scope, teamId, category, title,
                "자료 설명", false, adminMemberId);
    }

    private Resource insertDraft(ResourceTargetScope scope, Long teamId,
                                 String category, String title) {
        Resource resource = draft(scope, teamId, category, title);
        resourceMapper.insert(resource);
        return resource;
    }

    private void publishWithFile(Resource resource, String fileName, String hashSeed) {
        if (resource.getResourceId() == null) {
            resourceMapper.insert(resource);
        }
        Long fileId = insertStoredFile(fileName, hashSeed);
        resourceMapper.insertFile(ResourceFile.create(resource.getResourceId(),
                fileId, 1, 0, adminMemberId));
        resourceMapper.update(resource.publish(adminMemberId));
    }

    private Long teamId(List<Team> teams, String name) {
        return teams.stream().filter(team -> team.getName().equals(name))
                .findFirst().orElseThrow().getTeamId();
    }

    private Long insertMember(String studentNo, String name, Long teamId,
                              Long cohortId, ClubRole role) {
        Member member = new Member(null, studentNo, name, null, null, null,
                teamId, cohortId, role, MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, adminMemberId);
        memberMapper.insert(member);
        return member.getMemberId();
    }

    private Long insertStoredFile(String originalName, String hashSeed) {
        String storageKey = "resource/" + originalName;
        jdbcTemplate.update("""
                INSERT INTO stored_file (
                    original_name, storage_scope_code, storage_key, content_type,
                    size_bytes, sha256_hash, object_etag, uploaded_by_member_id,
                    upload_status_code
                ) VALUES (?, 'PRIVATE', ?, 'application/pdf', 1024, ?, 'etag', ?, 'READY')
                """, originalName, storageKey, hashSeed.repeat(64), adminMemberId);
        return jdbcTemplate.queryForObject("""
                SELECT stored_file_id FROM stored_file WHERE storage_key = ?
                """, Long.class, storageKey);
    }
}
