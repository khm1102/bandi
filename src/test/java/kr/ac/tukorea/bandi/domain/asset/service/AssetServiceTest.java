package kr.ac.tukorea.bandi.domain.asset.service;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUsageCreateParam;
import kr.ac.tukorea.bandi.domain.asset.exception.AssetStockUnavailableException;
import kr.ac.tukorea.bandi.domain.asset.mapper.AssetMapper;
import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUsage;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUsageStatus;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long ITEM_ID = 2L;

    @Mock
    private AssetMapper assetMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private PerformanceProjectService performanceProjectService;
    @Mock
    private FileService fileService;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetMapper, memberService,
                performanceProjectService, fileService, Clock.fixed(
                Instant.parse("2026-07-19T05:00:00Z"), ZoneOffset.UTC));
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(
                new MemberAccessContext(ACTOR_ID, 3L, true, false, true));
    }

    @Test
    void 수량형_예약은_가용_수량을_초과할_수_없다() {
        given(assetMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(quantityItem()));
        given(assetMapper.sumActiveUsageQuantity(ITEM_ID)).willReturn(8);
        AssetUsageCreateParam param = new AssetUsageCreateParam(ITEM_ID,
                null, 10L, 3L, 3, LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0), null);

        assertThatThrownBy(() -> assetService.reserve(ACTOR_ID, param))
                .isInstanceOf(AssetStockUnavailableException.class);
        verify(assetMapper, never()).insertUsage(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 수량형_예약은_사용량과_이력을_저장한다() {
        given(assetMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(quantityItem()));
        given(assetMapper.sumActiveUsageQuantity(ITEM_ID)).willReturn(6);
        AssetUsageCreateParam param = new AssetUsageCreateParam(ITEM_ID,
                null, 10L, 3L, 3, LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0), "공연 사용");

        assetService.reserve(ACTOR_ID, param);

        verify(performanceProjectService).validateExists(ACTOR_ID, 10L);
        verify(memberService).validateActiveTeam(3L);
        verify(assetMapper).insertUsage(org.mockito.ArgumentMatchers.any());
        verify(assetMapper).insertHistory(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 사용_기록을_반납하면_상태와_이력을_함께_저장한다() {
        AssetUsage usage = new AssetUsage(30L, ITEM_ID, null, 10L, 3L,
                2, AssetUsageStatus.RESERVED,
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0), null, ACTOR_ID,
                ACTOR_ID, null);
        given(assetMapper.lookupUsageByIdForUpdate(30L))
                .willReturn(Optional.of(usage));

        assetService.returnUsage(ACTOR_ID, 30L);

        ArgumentCaptor<AssetUsage> captor =
                ArgumentCaptor.forClass(AssetUsage.class);
        verify(assetMapper).updateUsage(captor.capture());
        assertThat(captor.getValue().getStatus())
                .isEqualTo(AssetUsageStatus.RETURNED);
        assertThat(captor.getValue().getReturnedDttm())
                .isEqualTo(LocalDateTime.of(2026, 7, 19, 5, 0));
        verify(assetMapper).insertHistory(org.mockito.ArgumentMatchers.any());
    }

    private AssetItem quantityItem() {
        return new AssetItem(ITEM_ID, "케이블", "EQUIPMENT",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                10, "창고", kr.ac.tukorea.bandi.domain.asset.model.AssetStatus.AVAILABLE,
                null, null);
    }
}
