package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileConsentResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileResponse;
import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfile;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileConsent;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PublicProfileMapper {

    Optional<PublicProfile> lookupProfileByIdForUpdate(Long publicProfileId);

    Optional<PublicProfile> lookupPublishedById(Long publicProfileId);

    Optional<PublicProfileConsent> lookupConsentForUpdate(
            @Param("publicProfileId") Long publicProfileId,
            @Param("policyDocumentVersionId") Long policyDocumentVersionId,
            @Param("consentScope") ConsentScope consentScope);

    Optional<PublicProfileConsent> lookupConsentByIdForUpdate(
            Long publicProfileConsentId);

    Set<ConsentScope> searchLatestAgreedScopes(
            @Param("publicProfileId") Long publicProfileId,
            @Param("currentDttm") LocalDateTime currentDttm);

    List<PublicProfileResponse> searchProfiles(
            PublicProfileSearchCondition condition);

    List<PublicProfileConsentResponse> searchConsents(Long publicProfileId);

    int insertProfile(PublicProfile profile);

    int updateProfile(PublicProfile profile);

    int insertConsent(PublicProfileConsent consent);

    int updateConsent(PublicProfileConsent consent);
}
