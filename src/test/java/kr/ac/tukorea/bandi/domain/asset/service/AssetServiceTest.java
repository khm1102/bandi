package kr.ac.tukorea.bandi.domain.asset.service;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemUpdateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUnitUpdateParam;
import kr.ac.tukorea.bandi.domain.asset.exception.AssetAccessDeniedException;
import kr.ac.tukorea.bandi.domain.asset.mapper.AssetMapper;
import kr.ac.tukorea.bandi.domain.asset.model.AssetAction;
import kr.ac.tukorea.bandi.domain.asset.model.AssetHistory;
import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUnit;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private FileService fileService;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetMapper, memberService, fileService,
                Clock.fixed(Instant.parse("2026-07-19T05:00:00Z"),
                        ZoneOffset.UTC));
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(
                new MemberAccessContext(ACTOR_ID, 3L, true, false, true));
    }

    @Test
    void 관리자는_품목_수량과_위치를_수정하고_각각_이력을_남긴다() {
        given(assetMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(quantityItem()));

        assetService.updateItem(ACTOR_ID, ITEM_ID,
                new AssetItemUpdateParam("전원 케이블", "CABLE",
                        AssetOwnerType.CLUB, null, null, 12,
                        "창고 B", 30L, "재고 정리"));

        ArgumentCaptor<AssetHistory> captor =
                ArgumentCaptor.forClass(AssetHistory.class);
        verify(assetMapper).updateItem(org.mockito.ArgumentMatchers.any());
        verify(fileService).validatePrivateReadyOwnedBy(30L, ACTOR_ID);
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

    private AssetItem quantityItem() {
        return new AssetItem(ITEM_ID, "케이블", "EQUIPMENT",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                10, "창고", AssetStatus.AVAILABLE, null, null);
    }
}
