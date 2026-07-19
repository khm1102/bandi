package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastAssignParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastChangeParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundCastResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceContentNotFoundException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceContentMapper;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceRoundCastMapper;
import kr.ac.tukorea.bandi.domain.performance.model.CastAction;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCastHistory;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCharacter;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundCast;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceRoundCastService {

    private final PerformanceRoundCastMapper roundCastMapper;
    private final PerformanceContentMapper contentMapper;
    private final MemberService memberService;
    private final PerformanceRoundService roundService;
    private final PublicProfileService publicProfileService;
    private final Clock clock;

    @Transactional
    public Long assign(Long actorMemberId,
                       PerformanceRoundCastAssignParam param) {
        validateAdmin(actorMemberId);
        roundService.validateExists(actorMemberId,
                param.performanceRoundId(),
                param.performanceProjectId());
        validateCharacter(param.performanceCharacterId(),
                param.performanceProjectId());
        validatePublicProfile(param.publicProfileId());
        PerformanceRoundCast cast = PerformanceRoundCast.assign(
                param.performanceProjectId(), param.performanceRoundId(),
                param.performanceCharacterId(), param.publicProfileId(),
                param.castType());
        try {
            roundCastMapper.insert(cast);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceContentException("roundCast");
        }
        contentMapper.insertCastHistory(PerformanceCastHistory.round(
                param.performanceProjectId(), param.performanceRoundId(),
                param.performanceCharacterId(), null,
                param.publicProfileId(), null, param.castType(),
                CastAction.ASSIGN, param.reason(), actorMemberId, now()));
        return cast.getPerformanceRoundCastId();
    }

    @Transactional
    public void change(Long actorMemberId,
                       PerformanceRoundCastChangeParam param) {
        validateAdmin(actorMemberId);
        PerformanceRoundCast current = lock(param.performanceRoundCastId());
        validatePublicProfile(param.publicProfileId());
        PerformanceRoundCast changed = current.change(
                param.publicProfileId(), param.castType());
        roundCastMapper.update(changed);
        contentMapper.insertCastHistory(PerformanceCastHistory.round(
                current.getPerformanceProjectId(),
                current.getPerformanceRoundId(),
                current.getPerformanceCharacterId(),
                current.getPublicProfileId(), changed.getPublicProfileId(),
                current.getCastType(), changed.getCastType(),
                CastAction.CHANGE, param.reason(), actorMemberId, now()));
    }

    @Transactional
    public void remove(Long actorMemberId, Long performanceRoundCastId,
                       String reason) {
        validateAdmin(actorMemberId);
        PerformanceRoundCast current = lock(performanceRoundCastId);
        roundCastMapper.remove(performanceRoundCastId);
        contentMapper.insertCastHistory(PerformanceCastHistory.round(
                current.getPerformanceProjectId(),
                current.getPerformanceRoundId(),
                current.getPerformanceCharacterId(),
                current.getPublicProfileId(), null,
                current.getCastType(), null, CastAction.REMOVE,
                reason, actorMemberId, now()));
    }

    public List<PerformanceRoundCastResponse> search(
            Long actorMemberId, Long performanceRoundId) {
        validateAdmin(actorMemberId);
        return roundCastMapper.searchByRound(performanceRoundId);
    }

    public List<PublicPerformanceRoundCastResponse> searchPublic(
            String slug, Long performanceRoundId) {
        boolean publicRound = roundService.searchPublicRounds(slug).stream()
                .anyMatch(round -> round.performanceRoundId()
                        .equals(performanceRoundId));
        if (!publicRound) {
            throw new PerformanceContentNotFoundException(
                    "performanceRoundId=" + performanceRoundId);
        }
        List<PublicPerformanceRoundCastResponse> result =
                new ArrayList<>();
        for (PerformanceRoundCastResponse cast
                : roundCastMapper.searchByRound(performanceRoundId)) {
            publicProfileService.lookupPublicCandidate(
                            cast.publicProfileId())
                    .map(profile -> PublicPerformanceRoundCastResponse.from(
                            cast, profile))
                    .ifPresent(result::add);
        }
        return result;
    }

    private PerformanceRoundCast lock(Long performanceRoundCastId) {
        return roundCastMapper.lookupByIdForUpdate(performanceRoundCastId)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "performanceRoundCastId="
                                + performanceRoundCastId));
    }

    private void validateCharacter(Long performanceCharacterId,
                                   Long performanceProjectId) {
        PerformanceCharacter character = contentMapper
                .lookupCharacterForUpdate(performanceCharacterId)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "performanceCharacterId="
                                + performanceCharacterId));
        character.validateProject(performanceProjectId);
    }

    private void validatePublicProfile(Long publicProfileId) {
        if (publicProfileService.lookupPublicCandidate(
                publicProfileId).isEmpty()) {
            throw new InvalidPerformanceContentException(
                    "publicProfileConsent");
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
