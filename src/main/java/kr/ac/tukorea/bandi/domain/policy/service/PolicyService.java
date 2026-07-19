package kr.ac.tukorea.bandi.domain.policy.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyDocumentCreateParam;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyVersionPublishParam;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyDocumentResponse;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyVersionResponse;
import kr.ac.tukorea.bandi.domain.policy.exception.DuplicatePolicyVersionException;
import kr.ac.tukorea.bandi.domain.policy.exception.InvalidPolicyVersionException;
import kr.ac.tukorea.bandi.domain.policy.exception.PolicyAccessDeniedException;
import kr.ac.tukorea.bandi.domain.policy.exception.PolicyDocumentNotFoundException;
import kr.ac.tukorea.bandi.domain.policy.exception.PolicyVersionNotFoundException;
import kr.ac.tukorea.bandi.domain.policy.mapper.PolicyMapper;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocument;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocumentVersion;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyAudience;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyService {

    private final PolicyMapper policyMapper;
    private final MemberService memberService;
    private final Clock clock;

    @Transactional
    public Long createDocument(Long actorMemberId,
                               PolicyDocumentCreateParam param) {
        validateAdmin(actorMemberId);
        PolicyDocument document = PolicyDocument.create(param.policyType(),
                param.title(), param.audience());
        policyMapper.insertDocument(document);
        return document.getPolicyDocumentId();
    }

    @Transactional
    public void changeActive(Long actorMemberId, Long policyDocumentId,
                             boolean active) {
        validateAdmin(actorMemberId);
        PolicyDocument document = lock(policyDocumentId);
        PolicyDocument changed = document.changeActive(active);
        policyMapper.updateDocumentActive(policyDocumentId,
                changed.isActive());
    }

    @Transactional
    public Long publishVersion(Long actorMemberId,
                               PolicyVersionPublishParam param) {
        validateAdmin(actorMemberId);
        PolicyDocument document = lock(param.policyDocumentId());
        if (!document.isActive()) {
            throw new InvalidPolicyVersionException("inactiveDocument");
        }
        LocalDateTime publishedDttm = now();
        PolicyDocumentVersion version = PolicyDocumentVersion.publish(
                param.policyDocumentId(),
                policyMapper.lookupNextVersionNo(param.policyDocumentId()),
                param.body(), publishedDttm, param.effectiveFromDttm(),
                param.required(), actorMemberId);
        try {
            policyMapper.insertVersion(version);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePolicyVersionException(
                    param.policyDocumentId());
        }
        return version.getPolicyDocumentVersionId();
    }

    public void validateEffectiveVersion(Long policyDocumentVersionId) {
        if (policyDocumentVersionId == null
                || !policyMapper.existsEffectiveVersion(
                policyDocumentVersionId, now())) {
            throw new InvalidPolicyVersionException(
                    "policyDocumentVersionId=" + policyDocumentVersionId);
        }
    }

    public void validateEffectiveVersion(
            Long policyDocumentVersionId, PolicyType policyType) {
        if (policyDocumentVersionId == null || policyType == null
                || !policyMapper.existsEffectiveVersionOfType(
                policyDocumentVersionId, policyType, now())) {
            throw new InvalidPolicyVersionException(
                    "policyDocumentVersionId=" + policyDocumentVersionId);
        }
    }

    public void validateReservationPrivacyVersion(
            Long policyDocumentVersionId) {
        validateEffectiveVersion(policyDocumentVersionId,
                PolicyType.RESERVATION_PRIVACY);
    }

    public List<PolicyDocumentResponse> searchDocuments(Long actorMemberId) {
        validateAdmin(actorMemberId);
        return policyMapper.searchDocuments();
    }

    public List<PolicyVersionResponse> searchVersions(
            Long actorMemberId, Long policyDocumentId) {
        validateAdmin(actorMemberId);
        return policyMapper.searchVersions(policyDocumentId);
    }

    public PolicyVersionResponse lookupCurrentReservationPrivacy() {
        return policyMapper.lookupCurrentEffectiveVersion(
                        PolicyType.RESERVATION_PRIVACY,
                        PolicyAudience.VISITOR, now())
                .orElseThrow(() -> new PolicyVersionNotFoundException(
                        "policyType=RESERVATION_PRIVACY,audience=VISITOR"));
    }

    private PolicyDocument lock(Long policyDocumentId) {
        return policyMapper.lookupDocumentByIdForUpdate(policyDocumentId)
                .orElseThrow(() -> new PolicyDocumentNotFoundException(
                        policyDocumentId));
    }

    private void validateAdmin(Long actorMemberId) {
        MemberAccessContext access = memberService
                .lookupAccessContext(actorMemberId);
        if (!access.canManageGlobal()) {
            throw new PolicyAccessDeniedException();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
