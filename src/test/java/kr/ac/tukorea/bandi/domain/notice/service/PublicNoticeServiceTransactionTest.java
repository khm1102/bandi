package kr.ac.tukorea.bandi.domain.notice.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.mapper.StoredFileMapper;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeUpdateParam;
import kr.ac.tukorea.bandi.domain.notice.mapper.PublicNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNotice;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeAttachment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
class PublicNoticeServiceTransactionTest {

    private static final Long MISSING_FILE_ID = Long.MAX_VALUE;

    private final PublicNoticeService publicNoticeService;
    private final PublicNoticeMapper publicNoticeMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final StoredFileMapper storedFileMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoBean
    private FileService fileService;

    private Long actorMemberId;
    private Long cohortId;
    private Long publicNoticeId;
    private Long originalFileId;

    @Autowired
    PublicNoticeServiceTransactionTest(
            PublicNoticeService publicNoticeService,
            PublicNoticeMapper publicNoticeMapper,
            TeamMapper teamMapper,
            CohortMapper cohortMapper,
            MemberMapper memberMapper,
            StoredFileMapper storedFileMapper,
            JdbcTemplate jdbcTemplate) {
        this.publicNoticeService = publicNoticeService;
        this.publicNoticeMapper = publicNoticeMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.storedFileMapper = storedFileMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        Long stageTeamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst()
                .map(Team::getTeamId)
                .orElseThrow();
        Cohort cohort = new Cohort(null, "공시트랜잭션기수", (short) 2997,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();

        Member admin = Member.preRegister("2997184001", "공시관리자", stageTeamId,
                cohortId, ClubRole.ADMIN, null);
        memberMapper.insert(admin);
        actorMemberId = admin.getMemberId();
        memberMapper.updateStatus(actorMemberId, MemberStatus.ACTIVE);

        PublicNotice notice = PublicNotice.draft("PERFORMANCE", "원래 제목",
                "원래 본문", false, actorMemberId);
        publicNoticeMapper.insert(notice);
        publicNoticeId = notice.getPublicNoticeId();

        StoredFile file = StoredFile.pending("original.pdf", StorageScope.PRIVATE,
                "notice/transaction/original", "application/pdf", 1024L,
                "a".repeat(64), actorMemberId);
        storedFileMapper.insert(file);
        originalFileId = file.getStoredFileId();
        storedFileMapper.updateReady(originalFileId, "etag-original");
        publicNoticeMapper.insertAttachment(
                PublicNoticeAttachment.create(publicNoticeId, originalFileId, 0));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM public_notice_attachment WHERE public_notice_id = ?",
                publicNoticeId);
        jdbcTemplate.update("DELETE FROM public_notice WHERE public_notice_id = ?",
                publicNoticeId);
        jdbcTemplate.update("DELETE FROM stored_file WHERE stored_file_id = ?", originalFileId);
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", actorMemberId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 새_첨부_삽입이_실패하면_본문_수정과_기존_첨부_삭제도_롤백된다() {
        given(fileService.lookupPrivateReady(MISSING_FILE_ID))
                .willReturn(new FileReferenceResponse(MISSING_FILE_ID, "missing.pdf",
                        "application/pdf", 100L));
        PublicNoticeUpdateParam param = new PublicNoticeUpdateParam(publicNoticeId,
                "PERFORMANCE", "바뀐 제목", "바뀐 본문", true,
                List.of(MISSING_FILE_ID));

        assertThatThrownBy(() -> publicNoticeService.update(actorMemberId, param))
                .isInstanceOf(DataAccessException.class);

        assertThat(publicNoticeMapper.lookupById(publicNoticeId))
                .isPresent()
                .get()
                .extracting(PublicNotice::getTitle)
                .isEqualTo("원래 제목");
        assertThat(publicNoticeMapper.searchAttachmentFileIds(publicNoticeId))
                .containsExactly(originalFileId);
    }
}
