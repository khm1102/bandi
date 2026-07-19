package kr.ac.tukorea.bandi.domain.fee.mapper;

import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeChargeResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeChargeHistoryResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeItemResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeSummaryResponse;
import kr.ac.tukorea.bandi.domain.fee.model.FeeCharge;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeHistory;
import kr.ac.tukorea.bandi.domain.fee.model.FeeItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface FeeMapper {

    Optional<FeeItem> lookupItemByIdForUpdate(Long feeItemId);

    List<FeeCharge> searchChargesByIdsForUpdate(
            @Param("feeItemId") Long feeItemId,
            @Param("feeChargeIds") List<Long> feeChargeIds);

    List<FeeCharge> searchChargesByItemForUpdate(Long feeItemId);

    List<FeeItemResponse> searchItems();

    List<FeeChargeResponse> searchCharges(Long feeItemId);

    List<FeeChargeHistoryResponse> searchChargeHistories(Long feeChargeId);

    List<MemberFeeResponse> searchMemberFees(Long memberId);

    MemberFeeSummaryResponse lookupMemberSummary(Long memberId);

    int insertItem(FeeItem item);

    int updateItem(FeeItem item);

    int insertCharges(@Param("charges") List<FeeCharge> charges);

    int updateCharge(FeeCharge charge);

    int insertChargeHistory(FeeChargeHistory history);
}
