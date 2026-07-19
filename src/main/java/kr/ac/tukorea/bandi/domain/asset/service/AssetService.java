package kr.ac.tukorea.bandi.domain.asset.service;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUsageCreateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemCreateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchCondition;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUnitCreateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetItemResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetUnitResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetUsageResponse;
import kr.ac.tukorea.bandi.domain.asset.exception.AssetAccessDeniedException;
import kr.ac.tukorea.bandi.domain.asset.exception.AssetItemNotFoundException;
import kr.ac.tukorea.bandi.domain.asset.exception.AssetUnitNotFoundException;
import kr.ac.tukorea.bandi.domain.asset.exception.AssetUsageNotFoundException;
import kr.ac.tukorea.bandi.domain.asset.exception.InvalidAssetException;
import kr.ac.tukorea.bandi.domain.asset.mapper.AssetMapper;
import kr.ac.tukorea.bandi.domain.asset.model.AssetHistory;
import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUnit;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUsage;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetMapper assetMapper;
    private final MemberService memberService;
    private final PerformanceProjectService performanceProjectService;
    private final FileService fileService;
    private final Clock clock;

    @Transactional
    public Long registerItem(Long actorMemberId, AssetItemCreateParam param) {
        validateAdmin(actorMemberId);
        if (param.photoFileId() != null) {
            fileService.validatePrivateReady(param.photoFileId());
        }
        AssetItem item = AssetItem.register(param.name(), param.categoryCode(),
                param.trackingType(), param.ownerType(), param.ownerMemberId(),
                param.externalOwnerName(), param.totalQuantity(),
                param.storageLocation(), param.photoFileId(), param.note());
        assetMapper.insertItem(item);
        assetMapper.insertHistory(new AssetHistory(null, item.getAssetItemId(),
                null, kr.ac.tukorea.bandi.domain.asset.model.AssetAction.REGISTER,
                item.getTotalQuantity(), null, item.getStatus(), item.getNote(),
                actorMemberId, now()));
        return item.getAssetItemId();
    }

    @Transactional
    public Long registerUnit(Long actorMemberId, AssetUnitCreateParam param) {
        validateAdmin(actorMemberId);
        AssetItem item = lockItem(param.assetItemId());
        if (item.getTrackingType() != AssetTrackingType.INDIVIDUAL) {
            throw new InvalidAssetException();
        }
        AssetUnit unit = AssetUnit.register(param.assetItemId(),
                param.managementNo(), param.storageLocation());
        assetMapper.insertUnit(unit);
        assetMapper.insertHistory(new AssetHistory(null, item.getAssetItemId(),
                unit.getAssetUnitId(),
                kr.ac.tukorea.bandi.domain.asset.model.AssetAction.REGISTER,
                1, null, unit.getStatus(), null, actorMemberId, now()));
        return unit.getAssetUnitId();
    }

    public List<AssetItemResponse> searchItems(Long actorMemberId,
                                                AssetSearchCondition condition) {
        validateInternal(actorMemberId);
        return assetMapper.searchItems(condition).stream()
                .map(AssetItemResponse::from)
                .toList();
    }

    public List<AssetUnitResponse> searchUnits(Long actorMemberId,
                                                Long assetItemId) {
        validateInternal(actorMemberId);
        findItem(assetItemId);
        return assetMapper.searchUnitsByItemId(assetItemId).stream()
                .map(AssetUnitResponse::from)
                .toList();
    }

    public List<AssetUsageResponse> searchUsages(Long actorMemberId,
                                                  Long assetItemId) {
        validateInternal(actorMemberId);
        findItem(assetItemId);
        return assetMapper.searchUsagesByItemId(assetItemId).stream()
                .map(AssetUsageResponse::from)
                .toList();
    }

    @Transactional
    public Long reserve(Long actorMemberId, AssetUsageCreateParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(
                actorMemberId);
        if (!access.canManageTeam(param.teamId())) {
            throw new AssetAccessDeniedException();
        }
        memberService.validateActiveTeam(param.teamId());
        performanceProjectService.validateExists(actorMemberId,
                param.performanceProjectId());
        AssetItem item = lockItem(param.assetItemId());
        int activeQuantity = assetMapper.sumActiveUsageQuantity(
                param.assetItemId());
        item.validateReservation(param.assetUnitId(), param.quantity(),
                activeQuantity);
        AssetUnit reservedUnit = validateUnit(item, param.assetUnitId());
        AssetUsage usage = AssetUsage.reserve(param.assetItemId(),
                param.assetUnitId(), param.performanceProjectId(),
                param.teamId(), param.quantity(), param.startDttm(),
                param.expectedReturnDttm(), actorMemberId, param.note());
        assetMapper.insertUsage(usage);
        if (reservedUnit != null) {
            assetMapper.updateUnitStatus(
                    reservedUnit.changeStatus(AssetStatus.IN_USE));
        }
        assetMapper.insertHistory(AssetHistory.reserved(usage, now()));
        log.info("소품·장비 사용 예약 - assetUsageId={}, assetItemId={}, teamId={}",
                usage.getAssetUsageId(), param.assetItemId(), param.teamId());
        return usage.getAssetUsageId();
    }

    @Transactional
    public void returnUsage(Long actorMemberId, Long assetUsageId) {
        AssetUsage usage = assetMapper.lookupUsageByIdForUpdate(assetUsageId)
                .orElseThrow(() -> new AssetUsageNotFoundException(
                        assetUsageId));
        MemberAccessContext access = memberService.lookupAccessContext(
                actorMemberId);
        if (!access.canManageTeam(usage.getTeamId())) {
            throw new AssetAccessDeniedException();
        }
        AssetUsage returned = usage.returned(actorMemberId, now());
        assetMapper.updateUsage(returned);
        if (usage.getAssetUnitId() != null) {
            AssetUnit unit = assetMapper.lookupUnitByIdForUpdate(
                            usage.getAssetUnitId())
                    .orElseThrow(() -> new AssetUnitNotFoundException(
                            usage.getAssetUnitId()));
            assetMapper.updateUnitStatus(
                    unit.changeStatus(AssetStatus.AVAILABLE));
        }
        assetMapper.insertHistory(AssetHistory.returned(returned, now()));
    }

    private AssetUnit validateUnit(AssetItem item, Long assetUnitId) {
        if (item.getTrackingType() != AssetTrackingType.INDIVIDUAL) {
            return null;
        }
        AssetUnit unit = assetMapper.lookupUnitByIdForUpdate(assetUnitId)
                .orElseThrow(() -> new AssetUnitNotFoundException(assetUnitId));
        unit.validateReservation(item.getAssetItemId(),
                assetMapper.existsActiveUsageByUnitId(assetUnitId));
        return unit;
    }

    private AssetItem lockItem(Long assetItemId) {
        return assetMapper.lookupItemByIdForUpdate(assetItemId)
                .orElseThrow(() -> new AssetItemNotFoundException(assetItemId));
    }

    private AssetItem findItem(Long assetItemId) {
        return assetMapper.lookupItemById(assetItemId)
                .orElseThrow(() -> new AssetItemNotFoundException(assetItemId));
    }

    private void validateAdmin(Long actorMemberId) {
        if (!memberService.lookupAccessContext(actorMemberId)
                .canManageGlobal()) {
            throw new AssetAccessDeniedException();
        }
    }

    private void validateInternal(Long actorMemberId) {
        if (!memberService.lookupAccessContext(actorMemberId)
                .canReadInternal()) {
            throw new AssetAccessDeniedException();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
