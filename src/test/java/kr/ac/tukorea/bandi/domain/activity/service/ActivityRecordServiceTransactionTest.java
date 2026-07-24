package kr.ac.tukorea.bandi.domain.activity.service;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileReplaceParam;
import kr.ac.tukorea.bandi.domain.activity.mapper.ActivityRecordMapper;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecord;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordFile;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
@ActiveProfiles("test")
class ActivityRecordServiceTransactionTest {

    private final ActivityRecordService service;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ActivityRecordMapper mapper;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private FileService fileService;

    private Long memberId;
    private Long cohortId;
    private Long recordId;
    private Long oldFileId;
    private Long newFileId;
    private Long oldRecordFileId;

    @Autowired
    ActivityRecordServiceTransactionTest(ActivityRecordService service,
                                         TeamMapper teamMapper,
                                         CohortMapper cohortMapper,
                                         MemberMapper memberMapper,
                                         JdbcTemplate jdbcTemplate) {
        this.service = service;
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
        Cohort cohort = new Cohort(null, "활동트랜잭션기수", true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        Member member = new Member(null, "2995000001", "활동작성자", null,
                null, null, teamId, cohortId, ClubRole.MEMBER, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(member);
        memberId = member.getMemberId();
        given(memberService.lookupAccessContext(memberId))
                .willReturn(new MemberAccessContext(memberId, teamId,
                        false, false, true));

        ActivityRecord record = ActivityRecord.draft(teamId,
                java.time.LocalDateTime.of(2026, 7, 20, 18, 0),
                "트랜잭션 연습", "활동 내용", 8, memberId);
        mapper.insert(record);
        recordId = record.getActivityRecordId();
        oldFileId = insertStoredFile("activity/transaction-old", "a");
        newFileId = insertStoredFile("activity/transaction-new", "b");
        ActivityRecordFile oldFile = ActivityRecordFile.create(recordId,
                oldFileId, ActivityFileRole.EVIDENCE, 0, memberId);
        mapper.insertFile(oldFile);
        oldRecordFileId = oldFile.getActivityRecordFileId();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM activity_review_history WHERE activity_record_id=?", recordId);
        jdbcTemplate.update("DELETE FROM activity_record_revision WHERE activity_record_id=?", recordId);
        jdbcTemplate.update("UPDATE activity_record_file SET replaced_by_activity_record_file_id=NULL WHERE activity_record_id=?", recordId);
        jdbcTemplate.update("DELETE FROM activity_record_file WHERE activity_record_id=?", recordId);
        jdbcTemplate.update("DELETE FROM activity_record WHERE activity_record_id=?", recordId);
        jdbcTemplate.update("DELETE FROM stored_file WHERE stored_file_id IN (?,?)", oldFileId, newFileId);
        jdbcTemplate.update("DELETE FROM member WHERE member_id=?", memberId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id=?", cohortId);
    }

    @Test
    void 검수_이력_저장이_실패하면_제출_상태와_revision을_롤백한다() {
        willThrow(new IllegalStateException("검수 이력 실패"))
                .given(mapper).insertReviewHistory(any());

        assertThatThrownBy(() -> service.submit(memberId, recordId, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mapper.lookupById(recordId)).isPresent().get()
                .extracting(ActivityRecord::getStatus)
                .isEqualTo(ActivityRecordStatus.DRAFT);
        assertThat(mapper.searchRevisions(recordId)).isEmpty();
        assertThat(mapper.searchReviewHistories(recordId)).isEmpty();
    }

    @Test
    void 기존_파일_교체_갱신이_실패하면_새_파일_연결도_롤백한다() {
        given(fileService.lookupPrivateReady(newFileId))
                .willReturn(new FileReferenceResponse(newFileId, "new.jpg",
                        "image/jpeg", 1024L, memberId));
        willThrow(new IllegalStateException("기존 연결 갱신 실패"))
                .given(mapper).updateFile(any());

        assertThatThrownBy(() -> service.replaceFile(memberId,
                new ActivityFileReplaceParam(oldRecordFileId, newFileId)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mapper.searchCurrentFileLinks(recordId))
                .extracting("storedFileId").containsExactly(oldFileId);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM activity_record_file WHERE activity_record_id=?",
                Integer.class, recordId);
        assertThat(count).isEqualTo(1);
    }

    private Long insertStoredFile(String key, String hashSeed) {
        jdbcTemplate.update("""
                INSERT INTO stored_file (
                    original_name, storage_scope_code, storage_key, content_type,
                    size_bytes, sha256_hash, object_etag, uploaded_by_member_id,
                    upload_status_code
                ) VALUES (?, 'PRIVATE', ?, 'image/jpeg', 1024, ?, 'etag', ?, 'READY')
                """, key + ".jpg", key, hashSeed.repeat(64), memberId);
        return jdbcTemplate.queryForObject(
                "SELECT stored_file_id FROM stored_file WHERE storage_key=?",
                Long.class, key);
    }
}
