package kr.ac.tukorea.bandi.domain.policy.mapper;

import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyDocumentResponse;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyVersionResponse;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocument;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocumentVersion;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PolicyMapper {

    Optional<PolicyDocument> lookupDocumentByIdForUpdate(Long policyDocumentId);

    Optional<PolicyVersionResponse> lookupVersionById(
            Long policyDocumentVersionId);

    int lookupNextVersionNo(Long policyDocumentId);

    boolean existsEffectiveVersion(
            @Param("policyDocumentVersionId") Long policyDocumentVersionId,
            @Param("currentDttm") LocalDateTime currentDttm);

    List<PolicyDocumentResponse> searchDocuments();

    List<PolicyVersionResponse> searchVersions(Long policyDocumentId);

    int insertDocument(PolicyDocument document);

    int updateDocumentActive(
            @Param("policyDocumentId") Long policyDocumentId,
            @Param("active") boolean active);

    int insertVersion(PolicyDocumentVersion version);
}
