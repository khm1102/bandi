package kr.ac.tukorea.bandi.domain.asset.mapper;

import kr.ac.tukorea.bandi.domain.asset.model.AssetHistory;
import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUnit;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchCondition;

import java.util.List;
import java.util.Optional;

public interface AssetMapper {

    List<AssetItem> searchItems(AssetSearchCondition condition);

    List<AssetUnit> searchUnitsByItemId(Long assetItemId);

    List<AssetHistory> searchHistoriesByItemId(Long assetItemId);

    Optional<AssetItem> lookupItemById(Long assetItemId);

    Optional<AssetItem> lookupItemByIdForUpdate(Long assetItemId);

    Optional<AssetUnit> lookupUnitByIdForUpdate(Long assetUnitId);

    int insertItem(AssetItem item);

    int insertUnit(AssetUnit unit);

    int insertHistory(AssetHistory history);

    int updateItem(AssetItem item);

    int updateUnit(AssetUnit unit);

}
