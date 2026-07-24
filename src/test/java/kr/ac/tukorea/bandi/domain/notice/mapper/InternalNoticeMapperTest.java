package kr.ac.tukorea.bandi.domain.notice.mapper;

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
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeReadableSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeReadStatusResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeReadFilter;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
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
class InternalNoticeMapperTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    private final InternalNoticeMapper internalNoticeMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long stageTeamId;
    private Long operatorTeamId;
    private Long adminMemberId;
    private Long stageMemberId;
    private Long operatorMemberId;

    @Autowired
    InternalNoticeMapperTest(InternalNoticeMapper internalNoticeMapper,
                             TeamMapper teamMapper, CohortMapper cohortMapper,
                             MemberMapper memberMapper, JdbcTemplate jdbcTemplate) {
        this.internalNoticeMapper = internalNoticeMapper;
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
        Cohort cohort = new Cohort(null, "26-내부공지", (short) 2026,
                CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        adminMemberId = insertMember("2026000001", "관리자", stageTeamId,
                cohort.getCohortId(), ClubRole.ADMIN);
        stageMemberId = insertMember("2026000002", "무대부원", stageTeamId,
                cohort.getCohortId(), ClubRole.MEMBER);
        operatorMemberId = insertMember("2026000003", "오퍼부원", operatorTeamId,
                cohort.getCohortId(), ClubRole.MEMBER);
    }

    @Test
    void 공지를_저장하고_운영_상세를_조회한다() {
        InternalNotice notice = draft(InternalNoticeTargetScope.ALL, null, "전체 공지");

        internalNoticeMapper.insert(notice);

        assertThat(notice.getInternalNoticeId()).isNotNull();
        assertThat(internalNoticeMapper.lookupById(notice.getInternalNoticeId()))
                .isPresent()
                .get()
                .extracting(InternalNotice::getTitle)
                .isEqualTo("전체 공지");
        assertThat(internalNoticeMapper.lookupManageContent(notice.getInternalNoticeId()))
                .isPresent()
                .get()
                .extracting("createdByName")
                .isEqualTo("관리자");
        List<InternalNoticeManageSummaryResponse> summaries =
                internalNoticeMapper.searchManageable(
                        new InternalNoticeManageSearchCondition(null, null, null,
                                null, 0, 20));
        assertThat(summaries).singleElement()
                .extracting(InternalNoticeManageSummaryResponse::createdByName,
                        InternalNoticeManageSummaryResponse::updatedByName)
                .containsExactly("관리자", "관리자");
    }

    @Test
    void MEMBER는_전체와_소속_팀만_ADMIN은_모든_팀_공지를_조회한다() {
        publish(draft(InternalNoticeTargetScope.ALL, null, "전체 공지"));
        publish(draft(InternalNoticeTargetScope.TEAM, stageTeamId, "무대 공지"));
        publish(draft(InternalNoticeTargetScope.TEAM, operatorTeamId, "오퍼 공지"));

        List<InternalNoticeSummaryResponse> memberResult = internalNoticeMapper.searchReadable(
                readableCondition(stageMemberId, stageTeamId, false));
        List<InternalNoticeSummaryResponse> adminResult = internalNoticeMapper.searchReadable(
                readableCondition(adminMemberId, stageTeamId, true));

        assertThat(memberResult).extracting(InternalNoticeSummaryResponse::title)
                .containsExactlyInAnyOrder("전체 공지", "무대 공지");
        assertThat(memberResult).extracting(InternalNoticeSummaryResponse::createdByName)
                .containsOnly("관리자");
        assertThat(adminResult).extracting(InternalNoticeSummaryResponse::title)
                .containsExactlyInAnyOrder("전체 공지", "무대 공지", "오퍼 공지");
    }

    @Test
    void 예약_전과_종료_후와_게시_종료_공지는_읽기_목록에서_제외한다() {
        InternalNotice future = draft(InternalNoticeTargetScope.ALL, null, "예약 전")
                .publish(NOW.plusHours(1), null, adminMemberId, NOW);
        InternalNotice expired = draft(InternalNoticeTargetScope.ALL, null, "종료 후")
                .publish(NOW.minusDays(2), NOW.minusDays(1), adminMemberId,
                        NOW.minusDays(3));
        InternalNotice closed = draft(InternalNoticeTargetScope.ALL, null, "게시 종료")
                .publish(NOW.minusDays(1), null, adminMemberId, NOW.minusDays(2))
                .close(adminMemberId);
        internalNoticeMapper.insert(future);
        internalNoticeMapper.insert(expired);
        internalNoticeMapper.insert(closed);

        assertThat(internalNoticeMapper.searchReadable(
                readableCondition(stageMemberId, stageTeamId, false))).isEmpty();
    }

    @Test
    void 중요_공지와_키워드와_읽음_상태를_조회한다() {
        InternalNotice normal = publish(draft(InternalNoticeTargetScope.ALL,
                null, "일반 안내"));
        InternalNotice important = publish(InternalNotice.draft(
                InternalNoticeTargetScope.ALL, null, "중요 안내", "본문 검색어",
                true, adminMemberId));
        internalNoticeMapper.upsertRead(important.getInternalNoticeId(),
                stageMemberId, NOW.minusMinutes(10));

        InternalNoticeReadableSearchCondition condition =
                new InternalNoticeReadableSearchCondition("검색어", NOW,
                        stageMemberId, stageTeamId, false, 0, 20);

        List<InternalNoticeSummaryResponse> result =
                internalNoticeMapper.searchReadable(condition);

        assertThat(result).extracting(InternalNoticeSummaryResponse::title)
                .containsExactly("중요 안내");
        assertThat(internalNoticeMapper.countReadable(condition)).isEqualTo(result.size());
        assertThat(result.get(0).read()).isTrue();
        assertThat(normal.getInternalNoticeId()).isNotNull();
    }

    @Test
    void 미확인과_대상_범위_필터를_함께_적용한다() {
        InternalNotice allNotice = publish(draft(InternalNoticeTargetScope.ALL,
                null, "전체 미확인 공지"));
        InternalNotice teamNotice = publish(draft(InternalNoticeTargetScope.TEAM,
                stageTeamId, "팀 미확인 공지"));
        internalNoticeMapper.upsertRead(allNotice.getInternalNoticeId(), stageMemberId, NOW);

        InternalNoticeReadableSearchCondition condition =
                new InternalNoticeReadableSearchCondition(null, NOW, stageMemberId,
                        stageTeamId, false, InternalNoticeReadFilter.UNREAD,
                        InternalNoticeTargetScope.TEAM, 0, 20);

        List<InternalNoticeSummaryResponse> result =
                internalNoticeMapper.searchReadable(condition);

        assertThat(result).extracting(InternalNoticeSummaryResponse::title)
                .containsExactly(teamNotice.getTitle());
        assertThat(internalNoticeMapper.countReadable(condition)).isEqualTo(result.size());
    }

    @Test
    void 읽음_upsert는_최초_시각을_보존하고_최근_시각만_갱신한다() {
        InternalNotice notice = publish(draft(
                InternalNoticeTargetScope.ALL, null, "읽음 공지"));
        LocalDateTime first = NOW.minusHours(1);

        internalNoticeMapper.upsertRead(notice.getInternalNoticeId(), stageMemberId, first);
        internalNoticeMapper.upsertRead(notice.getInternalNoticeId(), stageMemberId, NOW);

        List<InternalNoticeReadStatusResponse> result =
                internalNoticeMapper.searchReadStatuses(notice.getInternalNoticeId(),
                        InternalNoticeTargetScope.ALL, null);
        InternalNoticeReadStatusResponse stageRead = result.stream()
                .filter(status -> status.memberId().equals(stageMemberId))
                .findFirst().orElseThrow();
        assertThat(stageRead.firstReadDttm()).isEqualTo(first);
        assertThat(stageRead.lastReadDttm()).isEqualTo(NOW);
    }

    @Test
    void 게시_중인_공지의_공유_토큰만_공개_조회하고_중단하면_즉시_숨긴다() {
        InternalNotice notice = draft(InternalNoticeTargetScope.ALL, null, "공유 공지")
                .publish(NOW.minusHours(1), NOW.plusDays(1), adminMemberId, NOW);
        internalNoticeMapper.insert(notice);
        String shareToken = "A0a1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0";

        internalNoticeMapper.updateShareToken(notice.getInternalNoticeId(), shareToken);

        assertThat(internalNoticeMapper.lookupShareTokenForUpdate(
                notice.getInternalNoticeId())).contains(shareToken);
        assertThat(internalNoticeMapper.lookupPublicShare(shareToken, NOW))
                .isPresent()
                .get()
                .extracting("title")
                .isEqualTo("공유 공지");
        assertThat(internalNoticeMapper.lookupReadableContent(notice.getInternalNoticeId(),
                NOW, stageTeamId, false)).isPresent().get()
                .extracting("createdByMemberId")
                .isEqualTo(adminMemberId);

        internalNoticeMapper.updateShareToken(notice.getInternalNoticeId(), null);

        assertThat(internalNoticeMapper.lookupPublicShare(shareToken, NOW)).isEmpty();
    }

    @Test
    void 팀_공지_읽음_현황은_해당_팀의_활성_멤버만_포함한다() {
        InternalNotice notice = publish(draft(InternalNoticeTargetScope.TEAM,
                stageTeamId, "무대 읽음"));

        List<InternalNoticeReadStatusResponse> result =
                internalNoticeMapper.searchReadStatuses(notice.getInternalNoticeId(),
                        InternalNoticeTargetScope.TEAM, stageTeamId);

        assertThat(result).extracting(InternalNoticeReadStatusResponse::memberId)
                .containsExactlyInAnyOrder(adminMemberId, stageMemberId)
                .doesNotContain(operatorMemberId);
        assertThat(result).allMatch(status -> !status.read());
    }

    @Test
    void 첨부는_표시_순서대로_조회하고_읽기_권한을_검증한다() {
        InternalNotice notice = publish(draft(InternalNoticeTargetScope.TEAM,
                stageTeamId, "첨부 공지"));
        Long firstFileId = insertStoredFile("첫번째.pdf", "a");
        Long secondFileId = insertStoredFile("두번째.pdf", "b");
        internalNoticeMapper.insertAttachment(InternalNoticeAttachment.create(
                notice.getInternalNoticeId(), secondFileId, 1));
        internalNoticeMapper.insertAttachment(InternalNoticeAttachment.create(
                notice.getInternalNoticeId(), firstFileId, 0));

        assertThat(internalNoticeMapper.searchAttachmentFileIds(
                notice.getInternalNoticeId()))
                .containsExactly(firstFileId, secondFileId);
        assertThat(internalNoticeMapper.existsReadableAttachment(
                notice.getInternalNoticeId(), firstFileId, NOW,
                stageTeamId, false)).isTrue();
        assertThat(internalNoticeMapper.existsReadableAttachment(
                notice.getInternalNoticeId(), firstFileId, NOW,
                operatorTeamId, false)).isFalse();
    }

    @Test
    void DB는_공지_대상과_팀의_불일치를_거부한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO internal_notice (
                    target_scope_code, team_id, title, body, status_code,
                    created_by_member_id, updated_by_member_id
                ) VALUES ('ALL', ?, '잘못된 공지', '본문', 'DRAFT', ?, ?)
                """, stageTeamId, adminMemberId, adminMemberId))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 논리_삭제된_공지는_운영과_읽기_조회에서_제외한다() {
        InternalNotice notice = publish(draft(
                InternalNoticeTargetScope.ALL, null, "삭제 공지"));
        jdbcTemplate.update("""
                UPDATE internal_notice
                SET deleted_dttm = ?
                WHERE internal_notice_id = ?
                """, NOW, notice.getInternalNoticeId());

        assertThat(internalNoticeMapper.lookupById(notice.getInternalNoticeId())).isEmpty();
        assertThat(internalNoticeMapper.searchManageable(
                new InternalNoticeManageSearchCondition(null, null, null,
                        null, 0, 20))).isEmpty();
        assertThat(internalNoticeMapper.searchReadable(
                readableCondition(stageMemberId, stageTeamId, false))).isEmpty();
    }

    @Test
    void 초안을_소프트_삭제하면_조회에서_제외하고_첨부_연결은_유지한다() {
        InternalNotice notice = draft(InternalNoticeTargetScope.ALL, null, "삭제할 초안");
        internalNoticeMapper.insert(notice);
        Long fileId = insertStoredFile("초안.pdf", "c");
        internalNoticeMapper.insertAttachment(InternalNoticeAttachment.create(
                notice.getInternalNoticeId(), fileId, 0));

        int affected = internalNoticeMapper.delete(notice.getInternalNoticeId(),
                adminMemberId, NOW);

        assertThat(affected).isEqualTo(1);
        assertThat(internalNoticeMapper.lookupById(notice.getInternalNoticeId())).isEmpty();
        assertThat(internalNoticeMapper.searchAttachmentFileIds(notice.getInternalNoticeId()))
                .containsExactly(fileId);
    }

    private InternalNotice draft(InternalNoticeTargetScope scope, Long teamId,
                                 String title) {
        return InternalNotice.draft(scope, teamId, title, "공지 본문",
                false, adminMemberId);
    }

    private InternalNotice publish(InternalNotice draft) {
        InternalNotice published = draft.publish(NOW.minusHours(1),
                NOW.plusDays(1), adminMemberId, NOW.minusHours(2));
        internalNoticeMapper.insert(published);
        return published;
    }

    private InternalNoticeReadableSearchCondition readableCondition(
            Long memberId, Long teamId, boolean admin) {
        return new InternalNoticeReadableSearchCondition(null, NOW, memberId,
                teamId, admin, 0, 20);
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
        jdbcTemplate.update("""
                INSERT INTO stored_file (
                    original_name, storage_scope_code, storage_key, content_type,
                    size_bytes, sha256_hash, object_etag, uploaded_by_member_id,
                    upload_status_code
                ) VALUES (?, 'PRIVATE', ?, 'application/pdf', 1024, ?, 'etag', ?, 'READY')
                """, originalName, "notice/" + originalName,
                hashSeed.repeat(64), adminMemberId);
        return jdbcTemplate.queryForObject("""
                SELECT stored_file_id
                FROM stored_file
                WHERE storage_key = ?
                """, Long.class, "notice/" + originalName);
    }
}
