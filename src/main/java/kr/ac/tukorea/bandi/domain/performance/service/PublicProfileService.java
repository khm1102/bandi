package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileConsentParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileCreateParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileUpdateParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileVisibilityParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileConsentResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePublicProfileException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPublicProfileConsentException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.exception.PublicProfileConsentNotFoundException;
import kr.ac.tukorea.bandi.domain.performance.exception.PublicProfileNotFoundException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PublicProfileMapper;
import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfile;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileConsent;
import kr.ac.tukorea.bandi.domain.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicProfileService {

    private final PublicProfileMapper publicProfileMapper;
    private final MemberService memberService;
    private final FileService fileService;
    private final PolicyService policyService;
    private final Clock clock;

    @Transactional
    public Long create(Long actorMemberId, PublicProfileCreateParam param) {
        validateAdmin(actorMemberId);
        if (param.memberId() != null) {
            memberService.lookupAccessContext(param.memberId());
        }
        validatePublicFile(param.profileFileId());
        PublicProfile profile = PublicProfile.draft(param.memberId(),
                param.publicName(), param.bio(), param.profileFileId(),
                param.socialUrl());
        try {
            publicProfileMapper.insertProfile(profile);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePublicProfileException(param.memberId());
        }
        return profile.getPublicProfileId();
    }

    @Transactional
    public void update(Long actorMemberId, PublicProfileUpdateParam param) {
        validateAdmin(actorMemberId);
        PublicProfile profile = lockProfile(param.publicProfileId());
        validatePublicFile(param.profileFileId());
        publicProfileMapper.updateProfile(profile.edit(param.publicName(),
                param.bio(), param.profileFileId(), param.socialUrl()));
    }

    @Transactional
    public void changeVisibility(Long actorMemberId,
                                 PublicProfileVisibilityParam param) {
        validateAdmin(actorMemberId);
        PublicProfile profile = lockProfile(param.publicProfileId());
        publicProfileMapper.updateProfile(profile.changeVisibility(
                param.visibilityStatus()));
    }

    @Transactional
    public void agree(Long actorMemberId, PublicProfileConsentParam param) {
        validateAdmin(actorMemberId);
        PublicProfile profile = lockProfile(param.publicProfileId());
        profile.validateConsentScope(param.consentScope());
        policyService.validateEffectiveVersion(
                param.policyDocumentVersionId());
        if (publicProfileMapper.lookupConsentForUpdate(
                param.publicProfileId(), param.policyDocumentVersionId(),
                param.consentScope()).isPresent()) {
            throw new InvalidPublicProfileConsentException("duplicate");
        }
        PublicProfileConsent consent = PublicProfileConsent.agree(
                param.publicProfileId(), param.policyDocumentVersionId(),
                param.consentScope(), actorMemberId, now());
        try {
            publicProfileMapper.insertConsent(consent);
        } catch (DuplicateKeyException exception) {
            throw new InvalidPublicProfileConsentException("duplicate");
        }
    }

    @Transactional
    public void revoke(Long actorMemberId, Long publicProfileConsentId) {
        validateAdmin(actorMemberId);
        PublicProfileConsent consent = publicProfileMapper
                .lookupConsentByIdForUpdate(publicProfileConsentId)
                .orElseThrow(() -> new PublicProfileConsentNotFoundException(
                        publicProfileConsentId));
        publicProfileMapper.updateConsent(consent.revoke(
                actorMemberId, now()));
    }

    public List<PublicProfileResponse> search(
            Long actorMemberId, PublicProfileSearchCondition condition) {
        validateAdmin(actorMemberId);
        return publicProfileMapper.searchProfiles(condition);
    }

    public List<PublicProfileConsentResponse> searchConsents(
            Long actorMemberId, Long publicProfileId) {
        validateAdmin(actorMemberId);
        return publicProfileMapper.searchConsents(publicProfileId);
    }

    public PublicProfileViewResponse lookupPublic(Long publicProfileId) {
        PublicProfile profile = publicProfileMapper
                .lookupPublishedById(publicProfileId)
                .orElseThrow(() -> new PublicProfileNotFoundException(
                        publicProfileId));
        Set<ConsentScope> scopes = publicProfileMapper
                .searchLatestAgreedScopes(publicProfileId, now());
        return PublicProfileViewResponse.from(profile, scopes);
    }

    public Optional<PublicProfileViewResponse> lookupPublicCandidate(
            Long publicProfileId) {
        PublicProfile profile = publicProfileMapper
                .lookupPublishedById(publicProfileId).orElse(null);
        if (profile == null) {
            return Optional.empty();
        }
        Set<ConsentScope> scopes = publicProfileMapper
                .searchLatestAgreedScopes(publicProfileId, now());
        if (!scopes.contains(ConsentScope.NAME)) {
            return Optional.empty();
        }
        return Optional.of(PublicProfileViewResponse.from(profile, scopes));
    }

    private PublicProfile lockProfile(Long publicProfileId) {
        return publicProfileMapper.lookupProfileByIdForUpdate(publicProfileId)
                .orElseThrow(() -> new PublicProfileNotFoundException(
                        publicProfileId));
    }

    private void validatePublicFile(Long storedFileId) {
        if (storedFileId != null) {
            fileService.validatePublicImageReady(storedFileId);
        }
    }

    private void validateAdmin(Long actorMemberId) {
        MemberAccessContext access = memberService
                .lookupAccessContext(actorMemberId);
        if (!access.canManageGlobal()) {
            throw new PerformanceAccessDeniedException();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
