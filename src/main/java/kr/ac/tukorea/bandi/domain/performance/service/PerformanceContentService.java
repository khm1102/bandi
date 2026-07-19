package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastAssignParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastChangeParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCharacterWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceMediaWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.ProductionCreditWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastHistoryResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCharacterResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceMediaResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.ProductionCreditResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProductionCreditResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceContentNotFoundException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceContentMapper;
import kr.ac.tukorea.bandi.domain.performance.model.CastAction;
import kr.ac.tukorea.bandi.domain.performance.model.MediaType;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCast;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCastHistory;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCharacter;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceMedia;
import kr.ac.tukorea.bandi.domain.performance.model.ProductionCredit;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceContentService {

    private final PerformanceContentMapper contentMapper;
    private final MemberService memberService;
    private final PerformanceProjectService projectService;
    private final PerformancePublicPageService publicPageService;
    private final PublicProfileService publicProfileService;
    private final FileService fileService;
    private final Clock clock;

    @Transactional
    public Long createCharacter(Long actorMemberId,
                                PerformanceCharacterWriteParam param) {
        validateAdmin(actorMemberId);
        projectService.validateExists(
                actorMemberId, param.performanceProjectId());
        PerformanceCharacter character = PerformanceCharacter.create(
                param.performanceProjectId(), param.name(),
                param.description(), param.importance(),
                param.displayOrder());
        contentMapper.insertCharacter(character);
        return character.getPerformanceCharacterId();
    }

    @Transactional
    public void updateCharacter(Long actorMemberId,
                                PerformanceCharacterWriteParam param) {
        validateAdmin(actorMemberId);
        PerformanceCharacter character = lockCharacter(
                param.performanceCharacterId());
        character.validateProject(param.performanceProjectId());
        contentMapper.updateCharacter(character.edit(param.name(),
                param.description(), param.importance(),
                param.displayOrder()));
    }

    @Transactional
    public void removeCharacter(Long actorMemberId,
                                Long performanceCharacterId) {
        validateAdmin(actorMemberId);
        lockCharacter(performanceCharacterId);
        if (contentMapper.existsCastByCharacter(performanceCharacterId)
                || contentMapper.existsCastHistoryByCharacter(
                performanceCharacterId)) {
            throw new InvalidPerformanceContentException("activeCast");
        }
        contentMapper.removeCharacter(performanceCharacterId);
    }

    @Transactional
    public Long assignCast(Long actorMemberId,
                           PerformanceCastAssignParam param) {
        validateAdmin(actorMemberId);
        PerformanceCharacter character = lockCharacter(
                param.performanceCharacterId());
        character.validateProject(param.performanceProjectId());
        validatePublicProfile(param.publicProfileId());
        PerformanceCast cast = PerformanceCast.assign(
                param.performanceProjectId(),
                param.performanceCharacterId(), param.publicProfileId(),
                param.castType(), param.displayOrder());
        try {
            contentMapper.insertCast(cast);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceContentException("cast");
        }
        contentMapper.insertCastHistory(PerformanceCastHistory.project(
                param.performanceProjectId(),
                param.performanceCharacterId(), null,
                param.publicProfileId(), null, param.castType(),
                CastAction.ASSIGN, param.reason(), actorMemberId, now()));
        return cast.getPerformanceCastId();
    }

    @Transactional
    public void changeCast(Long actorMemberId,
                           PerformanceCastChangeParam param) {
        validateAdmin(actorMemberId);
        PerformanceCast current = lockCast(param.performanceCastId());
        validatePublicProfile(param.publicProfileId());
        PerformanceCast changed = current.change(param.publicProfileId(),
                param.castType(), param.displayOrder());
        try {
            contentMapper.updateCast(changed);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceContentException("cast");
        }
        contentMapper.insertCastHistory(PerformanceCastHistory.project(
                current.getPerformanceProjectId(),
                current.getPerformanceCharacterId(),
                current.getPublicProfileId(), changed.getPublicProfileId(),
                current.getCastType(), changed.getCastType(),
                CastAction.CHANGE, param.reason(), actorMemberId, now()));
    }

    @Transactional
    public void removeCast(Long actorMemberId, Long performanceCastId,
                           String reason) {
        validateAdmin(actorMemberId);
        PerformanceCast current = lockCast(performanceCastId);
        contentMapper.removeCast(performanceCastId);
        contentMapper.insertCastHistory(PerformanceCastHistory.project(
                current.getPerformanceProjectId(),
                current.getPerformanceCharacterId(),
                current.getPublicProfileId(), null,
                current.getCastType(), null, CastAction.REMOVE,
                reason, actorMemberId, now()));
    }

    @Transactional
    public Long createCredit(Long actorMemberId,
                             ProductionCreditWriteParam param) {
        validateAdmin(actorMemberId);
        projectService.validateExists(
                actorMemberId, param.performanceProjectId());
        validateOptionalPublicProfile(param.publicProfileId());
        ProductionCredit credit = ProductionCredit.create(
                param.performanceProjectId(), param.creditRole(),
                param.publicName(), param.publicProfileId(),
                param.displayOrder());
        contentMapper.insertCredit(credit);
        return credit.getProductionCreditId();
    }

    @Transactional
    public void updateCredit(Long actorMemberId,
                             ProductionCreditWriteParam param) {
        validateAdmin(actorMemberId);
        ProductionCredit current = lockCredit(param.productionCreditId());
        if (!current.getPerformanceProjectId().equals(
                param.performanceProjectId())) {
            throw new InvalidPerformanceContentException(
                    "performanceProjectId");
        }
        validateOptionalPublicProfile(param.publicProfileId());
        contentMapper.updateCredit(current.edit(param.creditRole(),
                param.publicName(), param.publicProfileId(),
                param.displayOrder()));
    }

    @Transactional
    public void removeCredit(Long actorMemberId, Long productionCreditId) {
        validateAdmin(actorMemberId);
        lockCredit(productionCreditId);
        contentMapper.removeCredit(productionCreditId);
    }

    @Transactional
    public Long createMedia(Long actorMemberId,
                            PerformanceMediaWriteParam param) {
        validateAdmin(actorMemberId);
        projectService.validateExists(
                actorMemberId, param.performanceProjectId());
        validateMediaFile(param);
        PerformanceMedia media = PerformanceMedia.create(
                param.performanceProjectId(), param.storedFileId(),
                param.mediaType(), param.title(), param.description(),
                param.altText(), param.creditText(), param.externalUrl(),
                param.displayOrder());
        contentMapper.insertMedia(media);
        return media.getPerformanceMediaId();
    }

    @Transactional
    public void updateMedia(Long actorMemberId,
                            PerformanceMediaWriteParam param) {
        validateAdmin(actorMemberId);
        PerformanceMedia current = lockMedia(param.performanceMediaId());
        if (!current.getPerformanceProjectId().equals(
                param.performanceProjectId())) {
            throw new InvalidPerformanceContentException(
                    "performanceProjectId");
        }
        current.validateFile(param.storedFileId());
        validateMediaFile(param);
        contentMapper.updateMedia(current.edit(param.mediaType(),
                param.title(), param.description(), param.altText(),
                param.creditText(), param.externalUrl(),
                param.displayOrder()));
    }

    @Transactional
    public void changeMediaPublished(Long actorMemberId,
                                     Long performanceMediaId,
                                     boolean published) {
        validateAdmin(actorMemberId);
        PerformanceMedia current = lockMedia(performanceMediaId);
        contentMapper.updateMedia(current.changePublished(published));
    }

    @Transactional
    public void removeMedia(Long actorMemberId, Long performanceMediaId) {
        validateAdmin(actorMemberId);
        lockMedia(performanceMediaId);
        contentMapper.removeMedia(performanceMediaId);
    }

    public List<PerformanceCharacterResponse> searchCharacters(
            Long actorMemberId, Long performanceProjectId) {
        validateAdmin(actorMemberId);
        return contentMapper.searchCharacters(performanceProjectId);
    }

    public List<PerformanceCastResponse> searchCasts(
            Long actorMemberId, Long performanceProjectId) {
        validateAdmin(actorMemberId);
        return contentMapper.searchCasts(performanceProjectId);
    }

    public List<PerformanceCastHistoryResponse> searchCastHistories(
            Long actorMemberId, Long performanceProjectId) {
        validateAdmin(actorMemberId);
        return contentMapper.searchCastHistories(performanceProjectId);
    }

    public List<ProductionCreditResponse> searchCredits(
            Long actorMemberId, Long performanceProjectId) {
        validateAdmin(actorMemberId);
        return contentMapper.searchCredits(performanceProjectId);
    }

    public List<PerformanceMediaResponse> searchMedia(
            Long actorMemberId, Long performanceProjectId) {
        validateAdmin(actorMemberId);
        return contentMapper.searchMedia(performanceProjectId, false);
    }

    public List<PublicPerformanceCastResponse> searchPublicCasts(
            String slug) {
        Long projectId = publicPageService.lookupPublic(slug)
                .performanceProjectId();
        List<PublicPerformanceCastResponse> result = new ArrayList<>();
        for (PerformanceCastResponse cast
                : contentMapper.searchCasts(projectId)) {
            publicProfileService.lookupPublicCandidate(
                            cast.publicProfileId())
                    .map(profile -> PublicPerformanceCastResponse.from(
                            cast, profile))
                    .ifPresent(result::add);
        }
        return result;
    }

    public List<PublicProductionCreditResponse> searchPublicCredits(
            String slug) {
        Long projectId = publicPageService.lookupPublic(slug)
                .performanceProjectId();
        List<PublicProductionCreditResponse> result = new ArrayList<>();
        for (ProductionCreditResponse credit
                : contentMapper.searchCredits(projectId)) {
            if (credit.publicProfileId() == null) {
                result.add(PublicProductionCreditResponse.from(
                        credit, null));
                continue;
            }
            Optional<PublicProfileViewResponse> profile =
                    publicProfileService.lookupPublicCandidate(
                            credit.publicProfileId());
            profile.map(value -> PublicProductionCreditResponse.from(
                            credit, value))
                    .ifPresent(result::add);
        }
        return result;
    }

    public List<PerformanceMediaResponse> searchPublicMedia(String slug) {
        Long projectId = publicPageService.lookupPublic(slug)
                .performanceProjectId();
        return contentMapper.searchMedia(projectId, true);
    }

    private PerformanceCharacter lockCharacter(Long id) {
        return contentMapper.lookupCharacterForUpdate(id)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "performanceCharacterId=" + id));
    }

    private PerformanceCast lockCast(Long id) {
        return contentMapper.lookupCastForUpdate(id)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "performanceCastId=" + id));
    }

    private ProductionCredit lockCredit(Long id) {
        return contentMapper.lookupCreditForUpdate(id)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "productionCreditId=" + id));
    }

    private PerformanceMedia lockMedia(Long id) {
        return contentMapper.lookupMediaForUpdate(id)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "performanceMediaId=" + id));
    }

    private void validatePublicProfile(Long publicProfileId) {
        if (publicProfileService.lookupPublicCandidate(
                publicProfileId).isEmpty()) {
            throw new InvalidPerformanceContentException(
                    "publicProfileConsent");
        }
    }

    private void validateOptionalPublicProfile(Long publicProfileId) {
        if (publicProfileId != null) {
            validatePublicProfile(publicProfileId);
        }
    }

    private void validateMediaFile(PerformanceMediaWriteParam param) {
        if (param.mediaType() == MediaType.VIDEO) {
            fileService.validatePublicReady(param.storedFileId());
            return;
        }
        fileService.validatePublicImageReady(param.storedFileId());
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
