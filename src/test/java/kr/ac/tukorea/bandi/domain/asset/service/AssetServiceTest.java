package kr.ac.tukorea.bandi.domain.asset.service;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUsageCreateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemUpdateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUnitUpdateParam;
import kr.ac.tukorea.bandi.domain.asset.exception.AssetAccessDeniedException;
import kr.ac.tukorea.bandi.domain.asset.exception.AssetStockUnavailableException;
import kr.ac.tukorea.bandi.domain.asset.mapper.AssetMapper;
import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetAction;
import kr.ac.tukorea.bandi.domain.asset.model.AssetHistory;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUnit;
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

    @Test
    void 관리자는_품목_수량과_위치를_수정하고_각각_이력을_남긴다() {
        given(assetMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(quantityItem()));
        AssetItemUpdateParam param = new AssetItemUpdateParam("전원 케이블",
                "CABLE", AssetOwnerType.CLUB, null, null, 12,
                "창고 B", null, "재고 정리");

        assetService.updateItem(ACTOR_ID, ITEM_ID, param);

        ArgumentCaptor<AssetHistory> captor =
                ArgumentCaptor.forClass(AssetHistory.class);
        verify(assetMapper).updateItem(org.mockito.ArgumentMatchers.any());
        verify(assetMapper, org.mockito.Mockito.times(2))
                .insertHistory(captor.capture());
        assertThat(captor.getAllValues()).extracting(AssetHistory::action)
                .containsExactly(AssetAction.ADJUST, AssetAction.MOVE);
    }

    @Test
    void 전역_관리자가_아니면_품목을_수정할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(
                new MemberAccessContext(ACTOR_ID, 3L, false, false, true));

        assertThatThrownBy(() -> assetService.updateItem(ACTOR_ID, ITEM_ID,
                new AssetItemUpdateParam("케이블", "CABLE",
                        AssetOwnerType.CLUB, null, null, 10,
                        "창고", null, null)))
                .isInstanceOf(AssetAccessDeniedException.class);
        verify(assetMapper, never()).updateItem(
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 사용_중인_수량보다_총수량을_낮출_수_없다() {
        given(assetMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(quantityItem()));
        given(assetMapper.sumActiveUsageQuantity(ITEM_ID)).willReturn(8);

        assertThatThrownBy(() -> assetService.updateItem(ACTOR_ID, ITEM_ID,
                new AssetItemUpdateParam("케이블", "CABLE",
                        AssetOwnerType.CLUB, null, null, 7,
                        "창고", null, null)))
                .isInstanceOf(AssetStockUnavailableException.class);
        verify(assetMapper, never()).updateItem(
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 품목의_동일한_상태_변경은_갱신과_이력을_생략한다() {
        given(assetMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(quantityItem()));

        assetService.changeItemStatus(ACTOR_ID, ITEM_ID,
                AssetStatus.AVAILABLE, "중복 요청");

        verify(assetMapper, never()).updateItem(
                org.mockito.ArgumentMatchers.any());
        verify(assetMapper, never()).insertHistory(
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 품목의_상태가_바뀌면_갱신하고_상태_이력을_남긴다() {
        given(assetMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(quantityItem()));

        assetService.changeItemStatus(ACTOR_ID, ITEM_ID,
                AssetStatus.REPAIR, "피복 손상");

        ArgumentCaptor<AssetItem> itemCaptor =
                ArgumentCaptor.forClass(AssetItem.class);
        ArgumentCaptor<AssetHistory> historyCaptor =
                ArgumentCaptor.forClass(AssetHistory.class);
        verify(assetMapper).updateItem(itemCaptor.capture());
        verify(assetMapper).insertHistory(historyCaptor.capture());
        assertThat(itemCaptor.getValue().getStatus())
                .isEqualTo(AssetStatus.REPAIR);
        assertThat(historyCaptor.getValue().action())
                .isEqualTo(AssetAction.REPAIR);
    }

    @Test
    void 활성_대여가_있는_개별_장비는_직접_수정할_수_없다() {
        AssetUnit unit = new AssetUnit(20L, ITEM_ID, "CAM-001",
                AssetStatus.IN_USE, "영상팀");
        given(assetMapper.lookupUnitByIdForUpdate(20L))
                .willReturn(Optional.of(unit));
        given(assetMapper.existsActiveUsageByUnitId(20L)).willReturn(true);

        assertThatThrownBy(() -> assetService.updateUnit(ACTOR_ID,
                new AssetUnitUpdateParam(20L, AssetStatus.REPAIR,
                        "수리 업체", "점검")))
                .isInstanceOf(AssetStockUnavailableException.class);
        verify(assetMapper, never()).updateUnit(
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 개별_장비의_위치와_상태가_모두_바뀌면_이력을_각각_남긴다() {
        AssetUnit unit = new AssetUnit(20L, ITEM_ID, "CAM-001",
                AssetStatus.AVAILABLE, "영상팀");
        given(assetMapper.lookupUnitByIdForUpdate(20L))
                .willReturn(Optional.of(unit));

        assetService.updateUnit(ACTOR_ID, new AssetUnitUpdateParam(20L,
                AssetStatus.REPAIR, "수리 업체", "렌즈 점검"));

        ArgumentCaptor<AssetHistory> captor =
                ArgumentCaptor.forClass(AssetHistory.class);
        verify(assetMapper, org.mockito.Mockito.times(2))
                .insertHistory(captor.capture());
        assertThat(captor.getAllValues()).extracting(AssetHistory::action)
                .containsExactly(AssetAction.REPAIR, AssetAction.MOVE);
    }

    @Test
    void 내부_멤버는_품목_사진의_다운로드_주소를_받는다() {
        AssetItem item = new AssetItem(ITEM_ID, "카메라", "VIDEO",
                AssetTrackingType.INDIVIDUAL, AssetOwnerType.CLUB, null, null,
                1, "영상팀", AssetStatus.AVAILABLE, 50L, null);
        given(assetMapper.lookupItemById(ITEM_ID)).willReturn(Optional.of(item));
        given(fileService.createPrivateDownloadUrl(50L,
                kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision.GRANTED))
                .willReturn("http://minio/photo");

        String url = assetService.createPhotoDownloadUrl(ACTOR_ID, ITEM_ID);

        assertThat(url).isEqualTo("http://minio/photo");
    }

    private AssetItem quantityItem() {
        return new AssetItem(ITEM_ID, "케이블", "EQUIPMENT",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                10, "창고", kr.ac.tukorea.bandi.domain.asset.model.AssetStatus.AVAILABLE,
                null, null);
    }
}
