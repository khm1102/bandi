package kr.ac.tukorea.bandi.domain.asset.mapper;

import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUnit;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchCondition;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

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

    @Test
    void 품목과_개별_장비의_변경을_저장한다() {
        AssetItem item = AssetItem.register("카메라", "VIDEO",
                AssetTrackingType.INDIVIDUAL, AssetOwnerType.CLUB, null, null,
                1, "영상팀", null, null);
        assetMapper.insertItem(item);
        AssetUnit unit = AssetUnit.register(item.getAssetItemId(), "CAM-002",
                "영상팀");
        assetMapper.insertUnit(unit);

        assetMapper.updateItem(item.edit("공연 카메라", "VIDEO",
                AssetOwnerType.CLUB, null, null, 1, "장비실",
                null, "점검 완료"));
        assetMapper.updateUnit(unit.edit(AssetStatus.REPAIR, "수리 업체"));

        assertThat(assetMapper.lookupItemById(item.getAssetItemId()))
                .isPresent().get()
                .extracting(AssetItem::getStorageLocation)
                .isEqualTo("장비실");
        assertThat(assetMapper.lookupUnitByIdForUpdate(unit.getAssetUnitId()))
                .isPresent().get()
                .satisfies(found -> {
                    assertThat(found.getStatus()).isEqualTo(AssetStatus.REPAIR);
                    assertThat(found.getStorageLocation()).isEqualTo("수리 업체");
                });
        assertThat(assetMapper.searchHistoriesByItemId(item.getAssetItemId()))
                .isEmpty();
    }

    @Test
    void 품목을_페이지_단위로_조회하고_삭제_상태를_분리한다() {
        AssetItem active = AssetItem.register("가방", "PROP",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                1, "창고", null, null);
        AssetItem deleted = AssetItem.register("조명", "LIGHTING",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                1, "장비실", null, null);
        assetMapper.insertItem(active);
        assetMapper.insertItem(deleted);
        assetMapper.deleteItem(deleted.getAssetItemId(),
                LocalDateTime.of(2026, 7, 24, 12, 0));

        AssetSearchCondition activeCondition = new AssetSearchCondition(
                null, null, null, null, false, 0, 20);
        AssetSearchCondition deletedCondition = new AssetSearchCondition(
                null, null, null, null, true, 0, 20);

        assertThat(assetMapper.searchItems(activeCondition))
                .extracting(AssetItem::getAssetItemId)
                .contains(active.getAssetItemId())
                .doesNotContain(deleted.getAssetItemId());
        assertThat(assetMapper.countItems(activeCondition)).isPositive();
        assertThat(assetMapper.searchItems(deletedCondition))
                .extracting(AssetItem::getAssetItemId)
                .contains(deleted.getAssetItemId());
        assertThat(assetMapper.lookupDeletedItemByIdForUpdate(
                deleted.getAssetItemId())).isPresent();

        assetMapper.restoreItem(deleted.getAssetItemId());

        assertThat(assetMapper.lookupItemById(deleted.getAssetItemId()))
                .isPresent();
    }
}
