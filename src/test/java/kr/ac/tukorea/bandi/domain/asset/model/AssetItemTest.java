package kr.ac.tukorea.bandi.domain.asset.model;

import kr.ac.tukorea.bandi.domain.asset.exception.InvalidAssetException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetItemTest {

    @Test
    void 수량형_동아리_품목을_등록한다() {
        AssetItem item = AssetItem.register("케이블", "EQUIPMENT",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                10, "창고 A", null, null);

        assertThat(item.getStatus()).isEqualTo(AssetStatus.AVAILABLE);
        assertThat(item.getTotalQuantity()).isEqualTo(10);
    }

    @Test
    void 개인_소유는_멤버_식별자가_필수다() {
        assertThatThrownBy(() -> AssetItem.register("카메라", "VIDEO",
                AssetTrackingType.INDIVIDUAL, AssetOwnerType.MEMBER, null,
                null, 1, "영상팀", null, null))
                .isInstanceOf(InvalidAssetException.class);
    }

    @Test
    void 외부_소유는_외부_소유자명이_필수다() {
        assertThatThrownBy(() -> AssetItem.register("조명", "LIGHT",
                AssetTrackingType.INDIVIDUAL, AssetOwnerType.EXTERNAL, null,
                " ", 1, "무대", null, null))
                .isInstanceOf(InvalidAssetException.class);
    }

    @Test
    void 품목_정보를_수정하면_추적_방식과_상태를_보존한다() {
        AssetItem item = new AssetItem(10L, "케이블", "EQUIPMENT",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                10, "창고 A", AssetStatus.REPAIR, null, null);

        AssetItem changed = item.edit("전원 케이블", "CABLE",
                AssetOwnerType.EXTERNAL, null, "대여 업체", 12,
                "창고 B", 20L, "수량 보충");

        assertThat(changed.getAssetItemId()).isEqualTo(10L);
        assertThat(changed.getTrackingType()).isEqualTo(AssetTrackingType.QUANTITY);
        assertThat(changed.getStatus()).isEqualTo(AssetStatus.REPAIR);
        assertThat(changed.getTotalQuantity()).isEqualTo(12);
        assertThat(changed.getStorageLocation()).isEqualTo("창고 B");
    }

    @Test
    void 품목_수정도_소유_구분의_필수값을_검증한다() {
        AssetItem item = AssetItem.register("카메라", "VIDEO",
                AssetTrackingType.INDIVIDUAL, AssetOwnerType.CLUB, null, null,
                1, "영상팀", null, null);

        assertThatThrownBy(() -> item.edit("카메라", "VIDEO",
                AssetOwnerType.MEMBER, null, null, 1,
                "영상팀", null, null))
                .isInstanceOf(InvalidAssetException.class);
    }

    @Test
    void 품목_상태는_null로_변경할_수_없다() {
        AssetItem item = AssetItem.register("카메라", "VIDEO",
                AssetTrackingType.INDIVIDUAL, AssetOwnerType.CLUB, null, null,
                1, "영상팀", null, null);

        assertThatThrownBy(() -> item.changeStatus(null))
                .isInstanceOf(InvalidAssetException.class);
        assertThat(item.changeStatus(AssetStatus.LOST).getStatus())
                .isEqualTo(AssetStatus.LOST);
    }

    @Test
    void 개별_장비의_위치와_상태를_수정한다() {
        AssetUnit unit = AssetUnit.register(10L, "CAM-001", "영상팀");

        AssetUnit changed = unit.edit(AssetStatus.REPAIR, "수리 업체");

        assertThat(changed.getStatus()).isEqualTo(AssetStatus.REPAIR);
        assertThat(changed.getStorageLocation()).isEqualTo("수리 업체");
        assertThatThrownBy(() -> unit.edit(null, " "))
                .isInstanceOf(InvalidAssetException.class);
    }
}
