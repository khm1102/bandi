package kr.ac.tukorea.bandi.domain.notice.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.exception.InternalNoticeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.notice.mapper.InternalNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.share.service.ShareTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InternalNoticeShareServiceTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long LEADER_ID = 2L;
    private static final Long OTHER_MEMBER_ID = 3L;
    private static final Long NOTICE_ID = 10L;
    private static final Long TEAM_ID = 4L;
    private static final String SHARE_TOKEN = "A0a1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 24, 10, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-24T01:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private InternalNoticeMapper internalNoticeMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;
    @Mock
    private ShareTokenGenerator shareTokenGenerator;

    private InternalNoticeService internalNoticeService;

    @BeforeEach
    void setUp() {
        internalNoticeService = new InternalNoticeService(internalNoticeMapper,
                memberService, fileService, new MarkdownRenderer(), CLOCK,
                shareTokenGenerator);
    }

    @Test
    void 작성자는_게시_중인_공지의_제목_공개_공유_토큰을_발급한다() {
        given(memberService.lookupAccessContext(AUTHOR_ID)).willReturn(author());
        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(teamNotice()));
        given(internalNoticeMapper.lookupShareTokenForUpdate(NOTICE_ID))
                .willReturn(Optional.empty());
        given(shareTokenGenerator.generate()).willReturn(SHARE_TOKEN);

        String result = internalNoticeService.issuePublicShare(AUTHOR_ID, NOTICE_ID);

        assertThat(result).isEqualTo(SHARE_TOKEN);
        verify(internalNoticeMapper).updateShareToken(NOTICE_ID, SHARE_TOKEN);
    }

    @Test
    void 해당_팀장은_팀_공지의_공유_링크를_발급한다() {
        given(memberService.lookupAccessContext(LEADER_ID)).willReturn(leader());
        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(teamNotice()));
        given(internalNoticeMapper.lookupShareTokenForUpdate(NOTICE_ID))
                .willReturn(Optional.of(SHARE_TOKEN));

        String result = internalNoticeService.issuePublicShare(LEADER_ID, NOTICE_ID);

        assertThat(result).isEqualTo(SHARE_TOKEN);
        verify(shareTokenGenerator, never()).generate();
    }

    @Test
    void 일반_멤버는_다른_작성자의_공지_공유_링크를_발급할_수_없다() {
        given(memberService.lookupAccessContext(OTHER_MEMBER_ID)).willReturn(member());
        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(teamNotice()));

        assertThatThrownBy(() -> internalNoticeService.issuePublicShare(
                OTHER_MEMBER_ID, NOTICE_ID))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);

        verify(internalNoticeMapper, never()).updateShareToken(NOTICE_ID, SHARE_TOKEN);
    }

    @Test
    void 작성자는_공유_중단으로_토큰을_제거한다() {
        given(memberService.lookupAccessContext(AUTHOR_ID)).willReturn(author());
        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(teamNotice()));

        internalNoticeService.revokePublicShare(AUTHOR_ID, NOTICE_ID);

        verify(internalNoticeMapper).updateShareToken(NOTICE_ID, null);
    }

    private MemberAccessContext author() {
        return new MemberAccessContext(AUTHOR_ID, TEAM_ID, false, false, true);
    }

    private MemberAccessContext leader() {
        return new MemberAccessContext(LEADER_ID, TEAM_ID, false, true, true);
    }

    private MemberAccessContext member() {
        return new MemberAccessContext(OTHER_MEMBER_ID, TEAM_ID, false, false, true);
    }

    private InternalNotice teamNotice() {
        return new InternalNotice(NOTICE_ID, InternalNoticeTargetScope.TEAM, TEAM_ID,
                "공지", "본문", InternalNoticeStatus.PUBLISHED, false,
                NOW.minusHours(1), NOW.plusDays(1), AUTHOR_ID, AUTHOR_ID,
                AUTHOR_ID, NOW.minusDays(1), NOW.minusHours(1), null);
    }
}
