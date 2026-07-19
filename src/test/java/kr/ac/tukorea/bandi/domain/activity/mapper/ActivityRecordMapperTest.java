package kr.ac.tukorea.bandi.domain.activity.mapper;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecord;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordFile;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordRevision;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReviewHistory;
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
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class ActivityRecordMapperTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 20, 0);

    private final ActivityRecordMapper mapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long stageTeamId;
    private Long operatorTeamId;
    private Long adminId;
    private Long memberId;

    @Autowired
    ActivityRecordMapperTest(ActivityRecordMapper mapper, TeamMapper teamMapper,
                             CohortMapper cohortMapper, MemberMapper memberMapper,
                             JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
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
        Cohort cohort = new Cohort(null, "26-활동", (short) 2026,
                CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        adminId = insertMember("2026000201", "운영진", stageTeamId,
                cohort.getCohortId(), ClubRole.ADMIN);
        memberId = insertMember("2026000202", "작성자", stageTeamId,
                cohort.getCohortId(), ClubRole.MEMBER);
    }

    @Test
    void 초안을_저장하고_운영_상세를_조회한다() {
        ActivityRecord record = insertDraft(stageTeamId, "무대 연습");

        assertThat(mapper.lookupById(record.getActivityRecordId()))
                .isPresent().get().extracting(ActivityRecord::getTitle)
                .isEqualTo("무대 연습");
        assertThat(mapper.lookupManageContent(record.getActivityRecordId()))
                .isPresent().get().extracting("createdByName")
                .isEqualTo("작성자");
    }

    @Test
    void 현재_사진_교체는_과거_연결을_보존한다() {
        ActivityRecord record = insertDraft(stageTeamId, "사진 교체");
        Long oldFileId = insertStoredFile("old.jpg", "a");
        Long newFileId = insertStoredFile("new.jpg", "b");
        ActivityRecordFile old = ActivityRecordFile.create(record.getActivityRecordId(),
                oldFileId, ActivityFileRole.EVIDENCE, 0, memberId);
        mapper.insertFile(old);
        ActivityRecordFile replacement = ActivityRecordFile.create(
                record.getActivityRecordId(), newFileId,
                ActivityFileRole.EVIDENCE, 0, memberId);
        mapper.insertFile(replacement);
        mapper.updateFile(old.markReplaced(replacement.getActivityRecordFileId(),
                memberId, NOW));

        assertThat(mapper.searchCurrentFileLinks(record.getActivityRecordId()))
                .extracting("storedFileId").containsExactly(newFileId);
        assertThat(mapper.lookupFileByIdForUpdate(old.getActivityRecordFileId()))
                .isPresent().get().satisfies(file -> {
                    assertThat(file.isCurrent()).isFalse();
                    assertThat(file.getReplacedByActivityRecordFileId())
                            .isEqualTo(replacement.getActivityRecordFileId());
                });
    }

    @Test
    void 제출과_승인의_revision과_검수_이력을_조회한다() {
        ActivityRecord record = insertDraft(stageTeamId, "승인 기록");
        ActivityRecord submitted = record.submit(memberId, NOW.minusHours(1));
        mapper.insertRevision(ActivityRecordRevision.snapshot(record, 1,
                memberId, NOW.minusHours(1), null));
        mapper.update(submitted);
        mapper.insertReviewHistory(ActivityReviewHistory.change(record.getActivityRecordId(),
                ActivityRecordStatus.DRAFT, ActivityRecordStatus.SUBMITTED,
                null, memberId, NOW.minusHours(1)));
        ActivityRecord approved = submitted.approve(adminId, NOW);
        mapper.update(approved);
        mapper.insertReviewHistory(ActivityReviewHistory.change(record.getActivityRecordId(),
                ActivityRecordStatus.SUBMITTED, ActivityRecordStatus.APPROVED,
                null, adminId, NOW));

        assertThat(mapper.searchRevisions(record.getActivityRecordId()))
                .extracting("revisionNo").containsExactly(1);
        assertThat(mapper.searchReviewHistories(record.getActivityRecordId()))
                .extracting("newStatus")
                .containsExactly(ActivityRecordStatus.APPROVED,
                        ActivityRecordStatus.SUBMITTED);
    }

    @Test
    void 승인_목록은_팀과_기간을_필터링하고_대표_증빙을_제공한다() {
        ActivityRecord stage = insertApproved(stageTeamId, "무대 승인", "stage.jpg", "c");
        insertApproved(operatorTeamId, "오퍼 승인", "operator.jpg", "d");

        List<ActivityRecordSummaryResponse> result = mapper.searchApproved(
                new ActivityRecordSearchCondition(stageTeamId,
                        NOW.minusDays(1), NOW.plusDays(1), 0, 20));

        assertThat(result).extracting(ActivityRecordSummaryResponse::title)
                .containsExactly("무대 승인");
        assertThat(result.get(0).representativeStoredFileId()).isNotNull();
        assertThat(mapper.lookupApprovedContent(stage.getActivityRecordId())).isPresent();
    }

    @Test
    void 관리_목록은_팀과_상태와_작성자를_필터링한다() {
        ActivityRecord own = insertDraft(stageTeamId, "내 기록");
        insertDraft(operatorTeamId, "다른 팀 기록");

        List<ActivityRecordSummaryResponse> result = mapper.searchManageable(
                new ActivityManageSearchCondition(stageTeamId,
                        ActivityRecordStatus.DRAFT, memberId, 0, 20));

        assertThat(result).extracting(ActivityRecordSummaryResponse::activityRecordId)
                .containsExactly(own.getActivityRecordId());
    }

    @Test
    void DB는_참여인원과_상태시각과_파일교체_제약을_강제한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO activity_record (
                    team_id, activity_dttm, title, body, participant_count,
                    status_code, created_by_member_id, updated_by_member_id
                ) VALUES (?, NOW(6), '오류', '내용', 0, 'DRAFT', ?, ?)
                """, stageTeamId, memberId, memberId))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO activity_record (
                    team_id, activity_dttm, title, body, participant_count,
                    status_code, created_by_member_id, updated_by_member_id
                ) VALUES (?, NOW(6), '오류', '내용', 1, 'SUBMITTED', ?, ?)
                """, stageTeamId, memberId, memberId))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 논리_삭제된_기록은_모든_조회에서_제외한다() {
        ActivityRecord record = insertDraft(stageTeamId, "삭제 기록");
        jdbcTemplate.update("UPDATE activity_record SET deleted_dttm=NOW(6) WHERE activity_record_id=?",
                record.getActivityRecordId());

        assertThat(mapper.lookupById(record.getActivityRecordId())).isEmpty();
        assertThat(mapper.searchManageable(new ActivityManageSearchCondition(
                null, null, null, 0, 20))).isEmpty();
    }

    private ActivityRecord insertDraft(Long teamId, String title) {
        ActivityRecord record = ActivityRecord.draft(teamId, NOW.minusHours(2),
                title, "활동 내용", 8, memberId);
        mapper.insert(record);
        return record;
    }

    private ActivityRecord insertApproved(Long teamId, String title,
                                          String fileName, String hashSeed) {
        ActivityRecord record = insertDraft(teamId, title);
        Long fileId = insertStoredFile(fileName, hashSeed);
        mapper.insertFile(ActivityRecordFile.create(record.getActivityRecordId(),
                fileId, ActivityFileRole.EVIDENCE, 0, memberId));
        ActivityRecord submitted = record.submit(memberId, NOW.minusHours(1));
        mapper.update(submitted);
        mapper.update(submitted.approve(adminId, NOW));
        return record;
    }

    private Long teamId(List<Team> teams, String name) {
        return teams.stream().filter(team -> team.getName().equals(name))
                .findFirst().orElseThrow().getTeamId();
    }

    private Long insertMember(String studentNo, String name, Long teamId,
                              Long cohortId, ClubRole role) {
        Member member = new Member(null, studentNo, name, null, null, null,
                teamId, cohortId, role, MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, adminId);
        memberMapper.insert(member);
        return member.getMemberId();
    }

    private Long insertStoredFile(String name, String hashSeed) {
        String key = "activity/" + name;
        jdbcTemplate.update("""
                INSERT INTO stored_file (
                    original_name, storage_scope_code, storage_key, content_type,
                    size_bytes, sha256_hash, object_etag, uploaded_by_member_id,
                    upload_status_code
                ) VALUES (?, 'PRIVATE', ?, 'image/jpeg', 1024, ?, 'etag', ?, 'READY')
                """, name, key, hashSeed.repeat(64), memberId);
        return jdbcTemplate.queryForObject(
                "SELECT stored_file_id FROM stored_file WHERE storage_key=?",
                Long.class, key);
    }
}
