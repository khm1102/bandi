package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.service.MarkdownRenderer;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceWriteRequest;
import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import kr.ac.tukorea.bandi.domain.resource.exception.ResourceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.resource.mapper.ResourceMapper;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long ADMIN_ID = 2L;
    private static final Long RESOURCE_ID = 10L;
    private static final Long FILE_ID = 30L;

    @Mock
    private ResourceMapper resourceMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;
    @Mock
    private MarkdownRenderer markdownRenderer;
    @Mock
    private ResourceLinkPreviewFetcher linkPreviewFetcher;
    @Mock
    private ResourceLinkPreviewRetirementService linkPreviewRetirementService;

    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(resourceMapper, memberService, fileService,
                markdownRenderer, linkPreviewFetcher, linkPreviewRetirementService);
    }

    @Test
    void 활성_멤버는_자신의_자료를_작성한다() {
        given(memberService.lookupAccessContext(AUTHOR_ID)).willReturn(activeMember(AUTHOR_ID));
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), RESOURCE_ID);
            return 1;
        }).given(resourceMapper).insert(any(Resource.class));

        Long resourceId = resourceService.create(AUTHOR_ID,
                new ResourceWriteRequest("연습 자료", "# 연습", List.of(FILE_ID)));

        assertThat(resourceId).isEqualTo(RESOURCE_ID);
        verify(fileService).validatePrivateReadyOwnedBy(FILE_ID, AUTHOR_ID);
        verify(resourceMapper).insertFile(any());
    }

    @Test
    void 작성자가_아닌_팀장은_다른_사람_자료를_수정할_수_없다() {
        given(memberService.lookupAccessContext(ADMIN_ID))
                .willReturn(new MemberAccessContext(ADMIN_ID, 4L, false, true, true));
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID)).willReturn(Optional.of(resource()));

        assertThatThrownBy(() -> resourceService.update(ADMIN_ID, RESOURCE_ID,
                new ResourceWriteRequest("수정", "본문", List.of())))
                .isInstanceOf(ResourceAccessDeniedException.class);

        verify(resourceMapper, never()).update(any());
    }

    @Test
    void 관리자는_작성자가_아닌_자료도_수정할_수_있다() {
        given(memberService.lookupAccessContext(ADMIN_ID))
                .willReturn(new MemberAccessContext(ADMIN_ID, 4L, true, false, true));
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID)).willReturn(Optional.of(resource()));

        resourceService.update(ADMIN_ID, RESOURCE_ID,
                new ResourceWriteRequest("수정", "본문", List.of()));

        ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
        verify(resourceMapper).update(captor.capture());
        assertThat(captor.getValue().getUpdatedByMemberId()).isEqualTo(ADMIN_ID);
    }

    @Test
    void 중복된_첨부_파일은_파일_검증_전에_거부한다() {
        given(memberService.lookupAccessContext(AUTHOR_ID)).willReturn(activeMember(AUTHOR_ID));

        assertThatThrownBy(() -> resourceService.create(AUTHOR_ID,
                new ResourceWriteRequest("자료", "본문", List.of(FILE_ID, FILE_ID))))
                .isInstanceOf(InvalidResourceException.class);

        verify(fileService, never()).validatePrivateReadyOwnedBy(FILE_ID, AUTHOR_ID);
    }

    @Test
    void 비활성_작성자는_자신의_자료도_수정할_수_없다() {
        given(memberService.lookupAccessContext(AUTHOR_ID))
                .willReturn(new MemberAccessContext(AUTHOR_ID, 4L, false, false, false));

        assertThatThrownBy(() -> resourceService.update(AUTHOR_ID, RESOURCE_ID,
                new ResourceWriteRequest("수정", "본문", List.of())))
                .isInstanceOf(ResourceAccessDeniedException.class);

        verify(resourceMapper, never()).lookupByIdForUpdate(RESOURCE_ID);
    }

    private MemberAccessContext activeMember(Long memberId) {
        return new MemberAccessContext(memberId, 4L, false, false, true);
    }

    private Resource resource() {
        return new Resource(RESOURCE_ID, "기존 자료", "본문", AUTHOR_ID, AUTHOR_ID,
                null, null, null);
    }

    private void assignId(Resource resource, Long resourceId) throws ReflectiveOperationException {
        Field field = Resource.class.getDeclaredField("resourceId");
        field.setAccessible(true);
        field.set(resource, resourceId);
    }
}
