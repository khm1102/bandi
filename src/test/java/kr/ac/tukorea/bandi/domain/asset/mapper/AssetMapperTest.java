package kr.ac.tukorea.bandi.domain.asset.mapper;

import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUnit;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MapperTest
class AssetMapperTest {

    private final AssetMapper assetMapper;

    @Autowired
    AssetMapperTest(AssetMapper assetMapper) {
        this.assetMapper = assetMapper;
    }

    @Test
    void 품목을_저장하고_잠금_조회한다() {
        AssetItem item = AssetItem.register("케이블", "EQUIPMENT",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                10, "창고", null, null);

        assetMapper.insertItem(item);

        assertThat(assetMapper.lookupItemByIdForUpdate(item.getAssetItemId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getName()).isEqualTo("케이블");
                    assertThat(found.getTotalQuantity()).isEqualTo(10);
                });
    }

    @Test
    void 개별_장비를_관리번호와_함께_저장한다() {
        AssetItem item = AssetItem.register("카메라", "VIDEO",
                AssetTrackingType.INDIVIDUAL, AssetOwnerType.CLUB, null, null,
                1, "영상팀", null, null);
        assetMapper.insertItem(item);
        AssetUnit unit = AssetUnit.register(item.getAssetItemId(), "CAM-001",
                "영상팀");

        assetMapper.insertUnit(unit);

        assertThat(assetMapper.lookupUnitByIdForUpdate(unit.getAssetUnitId()))
                .isPresent()
                .get()
                .extracting(AssetUnit::getManagementNo)
                .isEqualTo("CAM-001");
    }
}
