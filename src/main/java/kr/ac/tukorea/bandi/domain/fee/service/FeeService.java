package kr.ac.tukorea.bandi.domain.fee.service;

import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeChargeProcessParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeItemUpdateParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeItemWriteParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeOpenParam;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeChargeHistoryResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeChargeResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeItemResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeSummaryResponse;
import kr.ac.tukorea.bandi.domain.fee.exception.FeeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.fee.exception.FeeChargeNotFoundException;
import kr.ac.tukorea.bandi.domain.fee.exception.FeeItemNotFoundException;
import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import kr.ac.tukorea.bandi.domain.fee.mapper.FeeMapper;
import kr.ac.tukorea.bandi.domain.fee.model.FeeCharge;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeHistory;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;
import kr.ac.tukorea.bandi.domain.fee.model.FeeItem;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeeService {

    private final FeeMapper feeMapper;
    private final MemberService memberService;
    private final Clock clock;

    @Transactional
    public Long create(Long actorMemberId, FeeItemWriteParam param) {
        validateAdmin(actorMemberId);
        FeeItem item = FeeItem.draft(param.name(), param.description(),
                param.referenceYear(), param.referenceTermCode(),
                param.amount(), param.dueDate(), actorMemberId);
        feeMapper.insertItem(item);
        log.info("회비 항목 생성 - feeItemId={}, actorMemberId={}",
                item.getFeeItemId(), actorMemberId);
        return item.getFeeItemId();
    }

    @Transactional
    public void update(Long actorMemberId, FeeItemUpdateParam param) {
        validateAdmin(actorMemberId);
        FeeItem item = lockItem(param.feeItemId());
        FeeItem changed = item.edit(param.name(), param.description(),
                param.referenceYear(), param.referenceTermCode(),
                param.amount(), param.dueDate(), actorMemberId);
        feeMapper.updateItem(changed);
    }

    @Transactional
    public int open(Long actorMemberId, FeeOpenParam param) {
        validateAdmin(actorMemberId);
        FeeItem item = lockItem(param.feeItemId());
        FeeItem opened = item.open(actorMemberId);
        List<Long> memberIds = resolveTargetMemberIds(param.selectedMemberIds());
        List<FeeCharge> charges = memberIds.stream()
                .map(memberId -> FeeCharge.unpaid(
                        item.getFeeItemId(), memberId, item.getAmount()))
                .toList();
        feeMapper.insertCharges(charges);
        feeMapper.updateItem(opened);
        log.info("회비 항목 열기 - feeItemId={}, targetCount={}, actorMemberId={}",
                item.getFeeItemId(), charges.size(), actorMemberId);
        return charges.size();
    }

    @Transactional
    public void close(Long actorMemberId, Long feeItemId) {
        validateAdmin(actorMemberId);
        feeMapper.updateItem(lockItem(feeItemId).close(actorMemberId));
    }

    @Transactional
    public void cancel(Long actorMemberId, Long feeItemId, String reason) {
        validateAdmin(actorMemberId);
        FeeItem item = lockItem(feeItemId);
        LocalDateTime currentDttm = now();
        List<FeeChargeChange> changes = feeMapper
                .searchChargesByItemForUpdate(feeItemId).stream()
                .filter(charge -> !charge.isCancelled())
                .map(charge -> changeCharge(charge, FeeChargeStatus.CANCELLED,
                        actorMemberId, currentDttm, reason))
                .toList();
        persistChargeChanges(changes, actorMemberId, currentDttm, reason);
        feeMapper.updateItem(item.cancel(actorMemberId));
        log.info("회비 항목 취소 - feeItemId={}, actorMemberId={}",
                feeItemId, actorMemberId);
    }

    @Transactional
    public int processCharges(Long actorMemberId,
                              FeeChargeProcessParam param) {
        validateAdmin(actorMemberId);
        FeeItem item = lockItem(param.feeItemId());
        item.validateChargeProcessing();
        List<Long> chargeIds = validateChargeIds(
                param.feeChargeIds(), param.status());
        List<FeeCharge> charges = feeMapper.searchChargesByIdsForUpdate(
                param.feeItemId(), chargeIds);
        if (charges.size() != chargeIds.size()) {
            throw new FeeChargeNotFoundException(
                    findMissingChargeId(chargeIds, charges));
        }
        LocalDateTime currentDttm = now();
        List<FeeChargeChange> changes = charges.stream()
                .map(charge -> changeCharge(charge, param.status(),
                        actorMemberId, currentDttm, param.reason()))
                .toList();
        persistChargeChanges(changes, actorMemberId, currentDttm,
                param.reason());
        log.info("회비 수납 상태 변경 - feeItemId={}, targetCount={}, actorMemberId={}",
                param.feeItemId(), changes.size(), actorMemberId);
        return changes.size();
    }

    public List<FeeItemResponse> searchItems(Long actorMemberId) {
        validateAdmin(actorMemberId);
        return feeMapper.searchItems();
    }

    public List<FeeChargeResponse> searchCharges(Long actorMemberId,
                                                  Long feeItemId) {
        validateAdmin(actorMemberId);
        return feeMapper.searchCharges(feeItemId);
    }

    public List<FeeChargeHistoryResponse> searchChargeHistories(
            Long actorMemberId, Long feeChargeId) {
        validateAdmin(actorMemberId);
        return feeMapper.searchChargeHistories(feeChargeId);
    }

    public List<MemberFeeResponse> searchMyFees(Long actorMemberId) {
        validateInternal(actorMemberId);
        return feeMapper.searchMemberFees(actorMemberId);
    }

    public MemberFeeSummaryResponse lookupMySummary(Long actorMemberId) {
        validateInternal(actorMemberId);
        return feeMapper.lookupMemberSummary(actorMemberId);
    }

    private FeeItem lockItem(Long feeItemId) {
        return feeMapper.lookupItemByIdForUpdate(feeItemId)
                .orElseThrow(() -> new FeeItemNotFoundException(feeItemId));
    }

    private List<Long> resolveTargetMemberIds(List<Long> selectedMemberIds) {
        List<Long> activeMemberIds = memberService.searchActiveMemberIds(null);
        if (selectedMemberIds == null || selectedMemberIds.isEmpty()) {
            return validateResolvedTargets(activeMemberIds);
        }
        validateSelectedMemberIds(selectedMemberIds);
        Set<Long> activeMemberIdSet = new HashSet<>(activeMemberIds);
        if (!activeMemberIdSet.containsAll(selectedMemberIds)) {
            throw new InvalidFeeException("inactive-selected-target");
        }
        return List.copyOf(selectedMemberIds);
    }

    private List<Long> validateResolvedTargets(List<Long> memberIds) {
        if (memberIds.isEmpty()) {
            throw new InvalidFeeException("empty-targets");
        }
        return List.copyOf(memberIds);
    }

    private void validateSelectedMemberIds(List<Long> memberIds) {
        if (memberIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(memberIds).size() != memberIds.size()) {
            throw new InvalidFeeException("selected-targets");
        }
    }

    private List<Long> validateChargeIds(List<Long> chargeIds,
                                         FeeChargeStatus status) {
        if (status == null || chargeIds == null || chargeIds.isEmpty()
                || chargeIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(chargeIds).size() != chargeIds.size()) {
            throw new InvalidFeeException("charge-processing");
        }
        return List.copyOf(chargeIds);
    }

    private FeeChargeChange changeCharge(FeeCharge charge,
                                         FeeChargeStatus status,
                                         Long actorMemberId,
                                         LocalDateTime currentDttm,
                                         String reason) {
        return new FeeChargeChange(charge, charge.changeStatus(status,
                actorMemberId, currentDttm, reason));
    }

    private void persistChargeChanges(List<FeeChargeChange> changes,
                                      Long actorMemberId,
                                      LocalDateTime currentDttm,
                                      String reason) {
        for (FeeChargeChange change : changes) {
            feeMapper.updateCharge(change.changed());
            feeMapper.insertChargeHistory(FeeChargeHistory.change(
                    change.previous().getFeeChargeId(),
                    change.previous().getStatus(),
                    change.changed().getStatus(),
                    change.changed().getChargedAmount(), reason,
                    actorMemberId, currentDttm));
        }
    }

    private Long findMissingChargeId(List<Long> chargeIds,
                                     List<FeeCharge> charges) {
        Set<Long> foundIds = charges.stream()
                .map(FeeCharge::getFeeChargeId)
                .collect(Collectors.toSet());
        return chargeIds.stream()
                .filter(chargeId -> !foundIds.contains(chargeId))
                .findFirst()
                .orElse(chargeIds.get(0));
    }

    private void validateAdmin(Long actorMemberId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        if (!access.canManageGlobal()) {
            throw new FeeAccessDeniedException();
        }
    }

    private void validateInternal(Long actorMemberId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        if (!access.canReadInternal()) {
            throw new FeeAccessDeniedException();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private record FeeChargeChange(FeeCharge previous, FeeCharge changed) {
    }
}
