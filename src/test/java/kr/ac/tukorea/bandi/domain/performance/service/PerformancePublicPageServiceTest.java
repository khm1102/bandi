package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformancePublicPageStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformancePublicPageWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceViewingGuideWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformancePublicPageException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformancePublicPageException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformancePublicPageMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformancePublicPage;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceViewingGuide;
import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PerformancePublicPageServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long PAGE_ID = 20L;
    private static final Long HERO_FILE_ID = 31L;
    private static final Long POSTER_FILE_ID = 32L;
    private static final Long OG_FILE_ID = 33L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 18, 23, 45);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-18T14:45:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private PerformancePublicPageMapper publicPageMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private PerformanceProjectService performanceProjectService;
    @Mock
    private FileService fileService;

    private PerformancePublicPageService service;

    @BeforeEach
    void setUp() {
        service = new PerformancePublicPageService(publicPageMapper,
                memberService, performanceProjectService, fileService,
                CLOCK);
    }

    @Test
    void 운영진은_프로젝트의_공개_페이지를_생성하고_이미지를_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0),
                    "performancePublicPageId", PAGE_ID);
            return 1;
        }).given(publicPageMapper).insertPage(any());

        Long result = service.create(ACTOR_ID, writeParam());

        assertThat(result).isEqualTo(PAGE_ID);
        verify(performanceProjectService)
                .validateExists(ACTOR_ID, PROJECT_ID);
        verify(fileService).validatePublicImageReady(HERO_FILE_ID);
        verify(fileService).validatePublicImageReady(POSTER_FILE_ID);
        verify(fileService).validatePublicImageReady(OG_FILE_ID);
    }

    @Test
    void 운영진이_아니면_공개_페이지를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext());

        assertThatThrownBy(() -> service.create(ACTOR_ID, writeParam()))
                .isInstanceOf(PerformanceAccessDeniedException.class);

        verify(publicPageMapper, never()).insertPage(any());
    }

    @Test
    void 같은_프로젝트나_슬러그_중복을_도메인_예외로_변환한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicPageMapper.insertPage(any()))
                .willThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> service.create(ACTOR_ID, writeParam()))
                .isInstanceOf(DuplicatePerformancePublicPageException.class);
    }

    @Test
    void 공개_페이지_내용과_상태를_변경한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicPageMapper.lookupPageByIdForUpdate(PAGE_ID))
                .willReturn(Optional.of(page(PublicPageStatus.DRAFT)));

        service.update(ACTOR_ID, writeParam());
        service.changeStatus(ACTOR_ID,
                new PerformancePublicPageStatusParam(
                        PAGE_ID, PublicPageStatus.PUBLISHED));

        verify(publicPageMapper,
                org.mockito.Mockito.times(2)).updatePage(any());
    }

    @Test
    void 수정_요청의_프로젝트가_기존_페이지와_다르면_거부한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicPageMapper.lookupPageByIdForUpdate(PAGE_ID))
                .willReturn(Optional.of(page(PublicPageStatus.DRAFT)));
        PerformancePublicPageWriteParam original = writeParam();
        PerformancePublicPageWriteParam mismatched =
                new PerformancePublicPageWriteParam(
                        original.performancePublicPageId(), 999L,
                        original.slug(), original.shortDescription(),
                        original.synopsis(), original.directorNote(),
                        original.genre(), original.ageRating(),
                        original.runtimeMinutes(),
                        original.intermissionMinutes(),
                        original.admissionFee(), original.heroFileId(),
                        original.posterFileId(), original.accentColor(),
                        original.contactName(), original.contactChannel(),
                        original.organizerName(), original.ogTitle(),
                        original.ogDescription(), original.ogImageFileId(),
                        original.publishStartDttm(),
                        original.publishEndDttm());

        assertThatThrownBy(() -> service.update(ACTOR_ID, mismatched))
                .isInstanceOf(InvalidPerformancePublicPageException.class);

        verify(publicPageMapper, never()).updatePage(any());
    }

    @Test
    void 관람_안내는_프로젝트당_하나를_생성하거나_수정한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicPageMapper.lookupGuideByProjectForUpdate(PROJECT_ID))
                .willReturn(Optional.empty(), Optional.of(guide()));

        service.saveViewingGuide(ACTOR_ID, guideParam());
        service.saveViewingGuide(ACTOR_ID, guideParam());

        verify(publicPageMapper).insertGuide(any());
        verify(publicPageMapper).updateGuide(any());
        verify(performanceProjectService,
                org.mockito.Mockito.times(2))
                .validateExists(ACTOR_ID, PROJECT_ID);
    }

    @Test
    void 외부_공개_페이지와_관람_안내는_로그인_없이_조회한다() {
        given(publicPageMapper.lookupPublicBySlug("hamlet-2026", NOW))
                .willReturn(Optional.of(response()));

        assertThat(service.lookupPublic("hamlet-2026").slug())
                .isEqualTo("hamlet-2026");
        assertThat(service.lookupPublicViewingGuide(PROJECT_ID)).isEmpty();

        verify(memberService, never()).lookupAccessContext(anyLong());
    }

    private PerformancePublicPageWriteParam writeParam() {
        return new PerformancePublicPageWriteParam(
                PAGE_ID, PROJECT_ID, "hamlet-2026", "짧은 소개",
                "상세 시놉시스", "연출 의도", "비극", "12세 이상",
                120, 15, 0L, HERO_FILE_ID, POSTER_FILE_ID,
                "#0F6F5D", "공연 문의", "bandi@example.com", "Bandi",
                "햄릿 2026", "공연 소개", OG_FILE_ID, null, null);
    }

    private PerformanceViewingGuideWriteParam guideParam() {
        return new PerformanceViewingGuideWriteParam(PROJECT_ID,
                "공연 30분 전 입장", "지연 입장은 안내에 따름",
                "촬영 및 녹음 금지", "공연 전날까지 취소",
                "휠체어 접근 가능", "정문에서 도보 5분", "교내 주차장");
    }

    private PerformancePublicPage page(PublicPageStatus status) {
        PerformancePublicPage page = PerformancePublicPage.draft(
                PROJECT_ID, "hamlet-2026", "짧은 소개", "상세 시놉시스",
                "연출 의도", "비극", "12세 이상", 120, 15, 0L,
                HERO_FILE_ID, POSTER_FILE_ID, "#0F6F5D", "공연 문의",
                "bandi@example.com", "Bandi", "햄릿 2026", "공연 소개",
                OG_FILE_ID, null, null);
        assignId(page, "performancePublicPageId", PAGE_ID);
        if (status == PublicPageStatus.PUBLISHED) {
            return page.changeStatus(status);
        }
        return page;
    }

    private PerformanceViewingGuide guide() {
        return new PerformanceViewingGuide(40L, PROJECT_ID,
                "공연 30분 전 입장", "지연 입장은 안내에 따름",
                "촬영 및 녹음 금지", "공연 전날까지 취소",
                "휠체어 접근 가능", "정문에서 도보 5분", "교내 주차장");
    }

    private PerformancePublicPageResponse response() {
        return new PerformancePublicPageResponse(PAGE_ID, PROJECT_ID,
                "햄릿", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3), "소극장", "hamlet-2026",
                PublicPageStatus.PUBLISHED, "짧은 소개", "상세 시놉시스",
                "연출 의도", "비극", "12세 이상", 120, 15, 0L,
                HERO_FILE_ID, POSTER_FILE_ID, "#0F6F5D", "공연 문의",
                "bandi@example.com", "Bandi", "햄릿 2026",
                "공연 소개", OG_FILE_ID, null, null);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
                true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
                false, false, true);
    }

    private void assignId(Object target, String fieldName, Long value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
