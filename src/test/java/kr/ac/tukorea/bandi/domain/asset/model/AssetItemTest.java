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
}
