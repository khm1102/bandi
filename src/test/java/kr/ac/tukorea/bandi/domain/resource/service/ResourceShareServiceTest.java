package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.service.MarkdownRenderer;
import kr.ac.tukorea.bandi.domain.resource.exception.ResourceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.resource.mapper.ResourceMapper;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import kr.ac.tukorea.bandi.domain.share.service.ShareTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResourceShareServiceTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;
    private static final Long RESOURCE_ID = 10L;
    private static final String SHARE_TOKEN = "A0a1B2b3C4d5E6f7G8h9I0j1K2l3M4n5O6p7Q8r9S0";

    @Mock
    private ResourceMapper resourceMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;
    @Mock
    private ResourceLinkPreviewFetcher linkPreviewFetcher;
    @Mock
    private ResourceLinkPreviewRetirementService linkPreviewRetirementService;
    @Mock
    private ShareTokenGenerator shareTokenGenerator;

    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(resourceMapper, memberService, fileService,
                new MarkdownRenderer(), linkPreviewFetcher,
                linkPreviewRetirementService, shareTokenGenerator);
    }

    @Test
    void 작성자는_제목_공개_공유_토큰을_발급한다() {
        given(memberService.lookupAccessContext(AUTHOR_ID)).willReturn(active(AUTHOR_ID));
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID)).willReturn(Optional.of(resource()));
        given(resourceMapper.lookupShareTokenForUpdate(RESOURCE_ID)).willReturn(Optional.empty());
        given(shareTokenGenerator.generate()).willReturn(SHARE_TOKEN);

        String result = resourceService.issuePublicShare(AUTHOR_ID, RESOURCE_ID);

        assertThat(result).isEqualTo(SHARE_TOKEN);
        verify(resourceMapper).updateShareToken(RESOURCE_ID, SHARE_TOKEN);
    }

    @Test
    void 같은_자료의_공유_링크는_기존_토큰을_재사용한다() {
        given(memberService.lookupAccessContext(AUTHOR_ID)).willReturn(active(AUTHOR_ID));
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID)).willReturn(Optional.of(resource()));
        given(resourceMapper.lookupShareTokenForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(SHARE_TOKEN));

        String result = resourceService.issuePublicShare(AUTHOR_ID, RESOURCE_ID);

        assertThat(result).isEqualTo(SHARE_TOKEN);
        verify(shareTokenGenerator, never()).generate();
        verify(resourceMapper, never()).updateShareToken(RESOURCE_ID, SHARE_TOKEN);
    }

    @Test
    void 작성자가_아닌_일반_멤버는_자료_공유_링크를_발급하지_못한다() {
        given(memberService.lookupAccessContext(OTHER_MEMBER_ID)).willReturn(active(OTHER_MEMBER_ID));
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID)).willReturn(Optional.of(resource()));

        assertThatThrownBy(() -> resourceService.issuePublicShare(OTHER_MEMBER_ID, RESOURCE_ID))
                .isInstanceOf(ResourceAccessDeniedException.class);

        verify(resourceMapper, never()).updateShareToken(RESOURCE_ID, SHARE_TOKEN);
    }

    private MemberAccessContext active(Long memberId) {
        return new MemberAccessContext(memberId, 4L, false, false, true);
    }

    private Resource resource() {
        return new Resource(RESOURCE_ID, "자료", "본문", AUTHOR_ID, AUTHOR_ID,
                null, null, null);
    }
}
