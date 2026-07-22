package kr.ac.tukorea.bandi.domain.notice.mapper;

import kr.ac.tukorea.bandi.domain.file.mapper.StoredFileMapper;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
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
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeAdminSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNotice;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;
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
class PublicNoticeMapperTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    private final PublicNoticeMapper publicNoticeMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final StoredFileMapper storedFileMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long actorMemberId;

    @Autowired
    PublicNoticeMapperTest(PublicNoticeMapper publicNoticeMapper, TeamMapper teamMapper,
                           CohortMapper cohortMapper, MemberMapper memberMapper,
                           StoredFileMapper storedFileMapper, JdbcTemplate jdbcTemplate) {
        this.publicNoticeMapper = publicNoticeMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.storedFileMapper = storedFileMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        List<Team> teams = teamMapper.searchAll();
        Long stageTeamId = teams.stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst()
                .orElseThrow()
                .getTeamId();
        Cohort cohort = new Cohort(null, "26-2기", (short) 2026, CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        Member actor = new Member(null, "2021184099", "이서준", null, null, null,
                stageTeamId, cohort.getCohortId(), ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(actor);
        actorMemberId = actor.getMemberId();
    }

    @Test
    void 공시_초안을_저장하고_단건_조회한다() {
        PublicNotice notice = draft("신입 부원 모집", true);

        publicNoticeMapper.insert(notice);
        PublicNotice found = publicNoticeMapper.lookupById(notice.getPublicNoticeId())
                .orElseThrow();

        assertThat(notice.getPublicNoticeId()).isNotNull();
        assertThat(found.getStatus()).isEqualTo(PublicNoticeStatus.DRAFT);
        assertThat(found.getTitle()).isEqualTo("신입 부원 모집");
        assertThat(found.getCreatedDttm()).isNotNull();
    }

    @Test
    void 공개_목록은_게시_시각이_도달한_공시만_중요도와_최신순으로_조회한다() {
        PublicNotice oldPinned = publish(draft("중요 모집 안내", true), NOW.minusDays(3), null);
        PublicNotice newNormal = publish(draft("정기 운영 안내", false), NOW.minusDays(1), null);
        publish(draft("미래 예약 안내", true), NOW.plusDays(1), null);
        publish(draft("만료 안내", true), NOW.minusDays(3), NOW);

        List<PublicNoticeSummaryResponse> found = publicNoticeMapper.searchPublic(
                new PublicNoticeSearchCondition(null, NOW, 0, 20));

        assertThat(found).extracting(PublicNoticeSummaryResponse::publicNoticeId)
                .containsExactly(oldPinned.getPublicNoticeId(), newNormal.getPublicNoticeId());
        assertThat(found).extracting(PublicNoticeSummaryResponse::createdByName)
                .containsOnly("이서준");
    }

    @Test
    void 공개_목록은_제목과_본문을_검색하고_페이지를_제한한다() {
        publish(draft("여름 운영 안내", false), NOW.minusDays(3), null);
        PublicNotice bodyMatched = PublicNotice.draft(
                "GENERAL", "결과 안내", "운영 안내 대상", false, actorMemberId);
        publish(bodyMatched, NOW.minusDays(2), null);
        publish(draft("신입 모집", false), NOW.minusDays(1), null);

        List<PublicNoticeSummaryResponse> firstPage = publicNoticeMapper.searchPublic(
                new PublicNoticeSearchCondition("안내", NOW, 0, 1));
        List<PublicNoticeSummaryResponse> secondPage = publicNoticeMapper.searchPublic(
                new PublicNoticeSearchCondition("안내", NOW, 1, 1));

        assertThat(firstPage).hasSize(1);
        assertThat(secondPage).hasSize(1);
        assertThat(firstPage.get(0).publicNoticeId())
                .isNotEqualTo(secondPage.get(0).publicNoticeId());
    }

    @Test
    void 공개_상세는_작성자와_수정자_이름을_포함한다() {
        PublicNotice notice = publish(draft("정기 운영 안내", true), NOW.minusDays(1), null);

        PublicNoticeContentResponse found = publicNoticeMapper.lookupPublicContent(
                        notice.getPublicNoticeId(), NOW)
                .orElseThrow();

        assertThat(found.title()).isEqualTo("정기 운영 안내");
        assertThat(found.createdByName()).isEqualTo("이서준");
        assertThat(found.updatedByName()).isEqualTo("이서준");
    }

    @Test
    void 운영_목록은_임시_공시를_상태와_검색어로_조회한다() {
        PublicNotice target = draft("신입 부원 모집", true);
        publicNoticeMapper.insert(target);
        PublicNotice other = draft("정기 운영 안내", false);
        publicNoticeMapper.insert(other);
        publish(draft("게시된 모집 결과", false), NOW.minusDays(1), null);

        List<PublicNoticeAdminSummaryResponse> found = publicNoticeMapper.searchAdmin(
                new PublicNoticeAdminSearchCondition(
                        "모집", PublicNoticeStatus.DRAFT, 0, 20));

        assertThat(found).extracting(PublicNoticeAdminSummaryResponse::publicNoticeId)
                .containsExactly(target.getPublicNoticeId());
        assertThat(found.get(0).status()).isEqualTo(PublicNoticeStatus.DRAFT);
        assertThat(found.get(0).updatedByName()).isEqualTo("이서준");
    }

    @Test
    void 운영_상세는_공개되지_않은_초안도_조회한다() {
        PublicNotice notice = draft("임시 모집 안내", false);
        publicNoticeMapper.insert(notice);

        PublicNoticeAdminContentResponse found = publicNoticeMapper.lookupAdminContent(
                        notice.getPublicNoticeId())
                .orElseThrow();

        assertThat(found.status()).isEqualTo(PublicNoticeStatus.DRAFT);
        assertThat(found.title()).isEqualTo("임시 모집 안내");
        assertThat(found.createdByName()).isEqualTo("이서준");
    }

    @Test
    void 임시_종료_보관_삭제된_공시는_공개_조회에서_제외한다() {
        PublicNotice draft = draft("임시 안내", false);
        publicNoticeMapper.insert(draft);
        PublicNotice closed = publish(draft("종료 안내", false), NOW.minusDays(1), null)
                .close(actorMemberId);
        publicNoticeMapper.update(closed);
        PublicNotice archived = draft("보관 안내", false).archive(actorMemberId);
        publicNoticeMapper.insert(archived);
        PublicNotice deleted = publish(draft("삭제 안내", false), NOW.minusDays(1), null);
        jdbcTemplate.update("UPDATE public_notice SET deleted_dttm = ? WHERE public_notice_id = ?",
                NOW, deleted.getPublicNoticeId());

        List<PublicNoticeSummaryResponse> found = publicNoticeMapper.searchPublic(
                new PublicNoticeSearchCondition(null, NOW, 0, 20));

        assertThat(found).isEmpty();
        assertThat(publicNoticeMapper.lookupPublicContent(deleted.getPublicNoticeId(), NOW))
                .isEmpty();
    }

    @Test
    void 첨부를_표시_순서대로_조회하고_공개_연결_여부를_확인한다() {
        PublicNotice notice = publish(draft("활동 자료", false), NOW.minusDays(1), null);
        Long firstFileId = insertReadyFile("poster.pdf", "1".repeat(64));
        Long secondFileId = insertReadyFile("schedule.pdf", "2".repeat(64));
        publicNoticeMapper.insertAttachment(
                PublicNoticeAttachment.create(notice.getPublicNoticeId(), secondFileId, 1));
        publicNoticeMapper.insertAttachment(
                PublicNoticeAttachment.create(notice.getPublicNoticeId(), firstFileId, 0));

        List<Long> found = publicNoticeMapper.searchAttachmentFileIds(notice.getPublicNoticeId());

        assertThat(found).containsExactly(firstFileId, secondFileId);
        assertThat(publicNoticeMapper.existsPublicAttachment(
                notice.getPublicNoticeId(), firstFileId, NOW)).isTrue();
        assertThat(publicNoticeMapper.existsPublicAttachment(
                notice.getPublicNoticeId(), 999999L, NOW)).isFalse();
    }

    @Test
    void 같은_파일이나_표시_순서를_한_공시에_중복_연결할_수_없다() {
        PublicNotice notice = draft("첨부 안내", false);
        publicNoticeMapper.insert(notice);
        Long firstFileId = insertReadyFile("poster.pdf", "3".repeat(64));
        Long secondFileId = insertReadyFile("schedule.pdf", "4".repeat(64));
        publicNoticeMapper.insertAttachment(
                PublicNoticeAttachment.create(notice.getPublicNoticeId(), firstFileId, 0));

        assertThatThrownBy(() -> publicNoticeMapper.insertAttachment(
                PublicNoticeAttachment.create(notice.getPublicNoticeId(), firstFileId, 1)))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> publicNoticeMapper.insertAttachment(
                PublicNoticeAttachment.create(notice.getPublicNoticeId(), secondFileId, 0)))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 첨부_연결을_하드_삭제하면_파일_메타데이터는_유지된다() {
        PublicNotice notice = draft("첨부 안내", false);
        publicNoticeMapper.insert(notice);
        Long storedFileId = insertReadyFile("poster.pdf", "5".repeat(64));
        publicNoticeMapper.insertAttachment(
                PublicNoticeAttachment.create(notice.getPublicNoticeId(), storedFileId, 0));

        int affected = publicNoticeMapper.removeAttachments(notice.getPublicNoticeId());

        assertThat(affected).isEqualTo(1);
        assertThat(publicNoticeMapper.searchAttachmentFileIds(notice.getPublicNoticeId()))
                .isEmpty();
        assertThat(storedFileMapper.lookupById(storedFileId)).isPresent();
    }

    @Test
    void DB도_상태와_게시기간과_초안_불변조건을_거부한다() {
        assertThatThrownBy(() -> insertRawNotice("UNKNOWN", null, null, null))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> insertRawNotice(
                "PUBLISHED", NOW, NOW.minusDays(1), actorMemberId))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> insertRawNotice(
                "DRAFT", NOW, null, actorMemberId))
                .isInstanceOf(DataAccessException.class);
    }

    private PublicNotice draft(String title, boolean pinned) {
        return PublicNotice.draft("GENERAL", title, "공시 본문", pinned, actorMemberId);
    }

    private PublicNotice publish(PublicNotice draft, LocalDateTime start, LocalDateTime end) {
        publicNoticeMapper.insert(draft);
        PublicNotice published = draft.publish(start, end, actorMemberId, NOW);
        publicNoticeMapper.update(published);
        return published;
    }

    private Long insertReadyFile(String originalName, String hash) {
        StoredFile file = StoredFile.pending(originalName, StorageScope.PRIVATE,
                "notice/2026/07/" + originalName, "application/pdf", 1024L,
                hash, actorMemberId);
        storedFileMapper.insert(file);
        storedFileMapper.updateReady(file.getStoredFileId(), "etag-" + originalName);
        return file.getStoredFileId();
    }

    private void insertRawNotice(String status, LocalDateTime start, LocalDateTime end,
                                 Long publishedByMemberId) {
        jdbcTemplate.update("""
                INSERT INTO public_notice (
                    category_code, title, body, status_code, is_pinned,
                    publish_start_dttm, publish_end_dttm, created_by_member_id,
                    updated_by_member_id, published_by_member_id
                ) VALUES ('GENERAL', '제목', '본문', ?, 0, ?, ?, ?, ?, ?)
                """, status, start, end, actorMemberId, actorMemberId,
                publishedByMemberId);
    }
}
