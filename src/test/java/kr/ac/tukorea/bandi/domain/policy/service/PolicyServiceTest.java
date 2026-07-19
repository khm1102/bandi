package kr.ac.tukorea.bandi.domain.policy.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyDocumentCreateParam;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyVersionPublishParam;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyVersionResponse;
import kr.ac.tukorea.bandi.domain.policy.exception.InvalidPolicyVersionException;
import kr.ac.tukorea.bandi.domain.policy.exception.PolicyAccessDeniedException;
import kr.ac.tukorea.bandi.domain.policy.mapper.PolicyMapper;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyAudience;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocument;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocumentVersion;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyType;
import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long DOCUMENT_ID = 10L;
    private static final Long VERSION_ID = 20L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 18, 22, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-18T13:00:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private PolicyMapper policyMapper;
    @Mock
    private MemberService memberService;

    private PolicyService service;

    @BeforeEach
    void setUp() {
        service = new PolicyService(policyMapper, memberService, CLOCK);
    }

    @Test
    void 운영진은_활성_정책_문서를_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), "policyDocumentId", DOCUMENT_ID);
            return 1;
        }).given(policyMapper).insertDocument(any());

        Long result = service.createDocument(ACTOR_ID,
                new PolicyDocumentCreateParam(PolicyType.PRIVACY,
                        "공개 프로필 동의", PolicyAudience.MEMBER));

        assertThat(result).isEqualTo(DOCUMENT_ID);
    }

    @Test
    void 운영진이_아니면_정책을_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext());

        assertThatThrownBy(() -> service.createDocument(ACTOR_ID,
                new PolicyDocumentCreateParam(PolicyType.PRIVACY,
                        "공개 프로필 동의", PolicyAudience.MEMBER)))
                .isInstanceOf(PolicyAccessDeniedException.class);

        verify(policyMapper, never()).insertDocument(any());
    }

    @Test
    void 정책_문서를_잠그고_다음_버전_번호로_발행한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(policyMapper.lookupDocumentByIdForUpdate(DOCUMENT_ID))
                .willReturn(Optional.of(document(true)));
        given(policyMapper.lookupNextVersionNo(DOCUMENT_ID)).willReturn(3);
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0),
                    "policyDocumentVersionId", VERSION_ID);
            return 1;
        }).given(policyMapper).insertVersion(any());

        Long result = service.publishVersion(ACTOR_ID,
                new PolicyVersionPublishParam(DOCUMENT_ID, "동의 본문",
                        NOW.plusDays(1), true));

        assertThat(result).isEqualTo(VERSION_ID);
        ArgumentCaptor<PolicyDocumentVersion> captor =
                ArgumentCaptor.forClass(PolicyDocumentVersion.class);
        verify(policyMapper).insertVersion(captor.capture());
        assertThat(captor.getValue().getVersionNo()).isEqualTo(3);
        assertThat(captor.getValue().getPublishedDttm()).isEqualTo(NOW);
    }

    @Test
    void 비활성_정책에는_새_버전을_발행할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(policyMapper.lookupDocumentByIdForUpdate(DOCUMENT_ID))
                .willReturn(Optional.of(document(false)));

        assertThatThrownBy(() -> service.publishVersion(ACTOR_ID,
                new PolicyVersionPublishParam(DOCUMENT_ID, "동의 본문",
                        NOW, true)))
                .isInstanceOf(InvalidPolicyVersionException.class);
    }

    @Test
    void 동의에는_현재_발효된_활성_정책_버전만_사용한다() {
        given(policyMapper.existsEffectiveVersion(VERSION_ID, NOW))
                .willReturn(true, false);

        service.validateEffectiveVersion(VERSION_ID);
        assertThatThrownBy(() -> service.validateEffectiveVersion(VERSION_ID))
                .isInstanceOf(InvalidPolicyVersionException.class);
    }

    @Test
    void 관람_신청은_현재_발효된_관람객_정책_버전만_사용한다() {
        given(policyMapper.existsEffectiveVersionOfType(
                VERSION_ID, PolicyType.RESERVATION_PRIVACY, NOW))
                .willReturn(true, false);

        service.validateEffectiveVersion(
                VERSION_ID, PolicyType.RESERVATION_PRIVACY);
        assertThatThrownBy(() -> service.validateEffectiveVersion(
                VERSION_ID, PolicyType.RESERVATION_PRIVACY))
                .isInstanceOf(InvalidPolicyVersionException.class);
    }

    @Test
    void 관람객에게_현재_유효한_관람_신청_개인정보_정책을_제공한다() {
        PolicyVersionResponse current = new PolicyVersionResponse(
                VERSION_ID, DOCUMENT_ID, 2, "수집 동의", NOW,
                ACTOR_ID, NOW, true);
        given(policyMapper.lookupCurrentEffectiveVersion(
                PolicyType.RESERVATION_PRIVACY,
                PolicyAudience.VISITOR, NOW))
                .willReturn(Optional.of(current));

        PolicyVersionResponse result =
                service.lookupCurrentReservationPrivacy();

        assertThat(result).isEqualTo(current);
    }

    @Test
    void 현재_유효한_관람_신청_정책이_없으면_찾을_수_없다() {
        given(policyMapper.lookupCurrentEffectiveVersion(
                PolicyType.RESERVATION_PRIVACY,
                PolicyAudience.VISITOR, NOW))
                .willReturn(Optional.empty());

        assertThatThrownBy(service::lookupCurrentReservationPrivacy)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.POLICY_VERSION_NOT_FOUND));
    }

    private PolicyDocument document(boolean active) {
        return new PolicyDocument(DOCUMENT_ID, PolicyType.PRIVACY,
                "공개 프로필 동의", PolicyAudience.MEMBER,
                active, null, null);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
                true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
                false, false, true);
    }

    private void assignId(Object target, String fieldName, Long value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
