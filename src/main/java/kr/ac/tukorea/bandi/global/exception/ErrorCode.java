package kr.ac.tukorea.bandi.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러의 단일 출처 (컨벤션 9.4).
 * 코드 접두사는 feature별 고정 — C 공통, A auth, M member, CA calendar, FI file,
 * PN public notice, NI internal notice, RS resource, AR activity record,
 * EV event, F fee, PO policy, P performance.
 * message는 사용자에게 그대로 보여줄 문장으로 작성하고 내부 사정을 노출하지 않는다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 (C)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C002", "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "일시적인 오류가 발생했습니다."),

    // member (M)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 멤버입니다."),
    DUPLICATE_STUDENT_NO(HttpStatus.CONFLICT, "M002", "이미 등록된 학번입니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "M003", "존재하지 않는 팀입니다."),
    COHORT_NOT_FOUND(HttpStatus.NOT_FOUND, "M004", "존재하지 않는 기수입니다."),
    INACTIVE_TEAM(HttpStatus.BAD_REQUEST, "M005", "비활성화된 팀에는 멤버를 배정할 수 없습니다."),
    NO_CHANGE(HttpStatus.BAD_REQUEST, "M006", "변경 전후 값이 같습니다."),
    LAST_ACTIVE_ADMIN(HttpStatus.CONFLICT, "M007", "마지막 운영진의 권한과 상태는 변경할 수 없습니다."),
    SELF_ROLE_DEMOTION(HttpStatus.FORBIDDEN, "M008", "본인의 운영진 권한은 다른 운영진만 변경할 수 있습니다."),
    DUPLICATE_TEAM_NAME(HttpStatus.CONFLICT, "M009", "이미 등록된 팀명입니다."),
    DUPLICATE_COHORT(HttpStatus.CONFLICT, "M010", "이미 등록된 기수입니다."),
    CHANGE_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "M011", "변경 사유를 입력해 주세요."),
    INACTIVE_COHORT(HttpStatus.BAD_REQUEST, "M012", "비활성화된 기수에는 멤버를 배정할 수 없습니다."),
    INVALID_MEMBER_STATUS_TRANSITION(HttpStatus.CONFLICT, "M013", "현재 상태에서는 요청한 상태로 변경할 수 없습니다."),
    SCHOOL_IDENTITY_MISMATCH(HttpStatus.CONFLICT, "M014", "학교 인증 정보와 등록 정보가 일치하지 않습니다."),

    // auth (A)
    SCHOOL_MEMBER_NOT_REGISTERED(HttpStatus.FORBIDDEN, "A001", "등록된 동아리 멤버가 아닙니다."),
    SCHOOL_ACADEMIC_STATUS_DENIED(HttpStatus.FORBIDDEN, "A002", "재학생만 로그인할 수 있습니다."),
    SCHOOL_IDENTITY_REVIEW_REQUIRED(HttpStatus.FORBIDDEN, "A003", "등록 정보 확인이 필요합니다. 운영진에게 문의해 주세요."),
    MEMBER_LOGIN_DENIED(HttpStatus.FORBIDDEN, "A004", "현재 동아리 활동 상태로는 로그인할 수 없습니다."),
    SCHOOL_CREDENTIALS_INVALID(HttpStatus.UNAUTHORIZED, "A005", "학교 계정 정보를 확인해 주세요."),
    SCHOOL_SSO_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "A006", "학교 인증 시스템에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요."),
    SCHOOL_SSO_RESPONSE_CHANGED(HttpStatus.BAD_GATEWAY, "A007", "학교 인증 응답을 확인할 수 없습니다. 운영진에게 문의해 주세요."),

    // file (FI)
    INVALID_FILE(HttpStatus.BAD_REQUEST, "FI001", "업로드할 수 없는 파일입니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "FI002", "파일 용량이 허용 범위를 초과했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FI003", "존재하지 않는 파일입니다."),
    INVALID_FILE_STATE(HttpStatus.CONFLICT, "FI004", "현재 상태에서는 파일을 사용할 수 없습니다."),
    INVALID_FILE_SCOPE(HttpStatus.CONFLICT, "FI005", "요청한 저장 범위에서 파일을 사용할 수 없습니다."),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FI006", "파일에 접근할 권한이 없습니다."),
    FILE_STORAGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "FI007", "파일 저장소를 일시적으로 사용할 수 없습니다."),

    // calendar (CA)
    CALENDAR_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CA001", "존재하지 않는 일정입니다."),

    // public notice (PN)
    PUBLIC_NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "PN001", "존재하지 않는 공시입니다."),
    INVALID_PUBLIC_NOTICE_STATE(HttpStatus.CONFLICT, "PN002", "현재 상태에서는 요청한 공시 작업을 처리할 수 없습니다."),

    // internal notice (NI)
    INTERNAL_NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NI001", "존재하지 않는 공지입니다."),
    INVALID_INTERNAL_NOTICE_STATE(HttpStatus.CONFLICT, "NI002", "현재 상태에서는 요청한 공지 작업을 처리할 수 없습니다."),

    // resource (RS)
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RS001", "존재하지 않는 자료입니다."),
    INVALID_RESOURCE_STATE(HttpStatus.CONFLICT, "RS002", "현재 상태에서는 요청한 자료 작업을 처리할 수 없습니다."),

    // activity record (AR)
    ACTIVITY_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "AR001", "존재하지 않는 활동 기록입니다."),
    INVALID_ACTIVITY_RECORD_STATE(HttpStatus.CONFLICT, "AR002", "현재 상태에서는 요청한 활동 기록 작업을 처리할 수 없습니다."),
    ACTIVITY_RECORD_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "AR003", "존재하지 않는 활동 기록 파일입니다."),

    // event (EV)
    CLUB_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EV001", "존재하지 않는 행사입니다."),
    EVENT_ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "EV002", "존재하지 않는 출석 대상입니다."),
    INVALID_CLUB_EVENT_STATE(HttpStatus.CONFLICT, "EV003", "현재 상태에서는 행사를 변경할 수 없습니다."),

    // fee (F)
    FEE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "존재하지 않는 회비 항목입니다."),
    FEE_CHARGE_NOT_FOUND(HttpStatus.NOT_FOUND, "F002", "존재하지 않는 회비 부과 내역입니다."),
    INVALID_FEE_STATE(HttpStatus.CONFLICT, "F003", "현재 상태에서는 회비를 변경할 수 없습니다."),

    // policy (PO)
    POLICY_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PO001", "존재하지 않는 정책 문서입니다."),
    POLICY_VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "PO002", "존재하지 않는 정책 버전입니다."),
    DUPLICATE_POLICY_VERSION(HttpStatus.CONFLICT, "PO003", "이미 등록된 정책 버전입니다."),
    INVALID_POLICY_VERSION(HttpStatus.CONFLICT, "PO004", "동의에 사용할 수 없는 정책 버전입니다."),

    // performance (P)
    PERFORMANCE_PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "존재하지 않는 공연 프로젝트입니다."),
    DUPLICATE_PERFORMANCE_TERM(HttpStatus.CONFLICT, "P002", "해당 학기에 이미 공연 프로젝트가 있습니다."),
    INVALID_PERFORMANCE_PROJECT_STATE(HttpStatus.CONFLICT, "P003", "현재 상태에서는 공연 프로젝트를 변경할 수 없습니다."),
    PRODUCTION_TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "P004", "존재하지 않는 제작 업무입니다."),
    INVALID_PRODUCTION_TASK_STATE(HttpStatus.CONFLICT, "P005", "현재 상태에서는 제작 업무를 변경할 수 없습니다."),
    PUBLIC_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "P006", "존재하지 않는 공개 프로필입니다."),
    DUPLICATE_PUBLIC_PROFILE(HttpStatus.CONFLICT, "P007", "이미 공개 프로필이 등록된 멤버입니다."),
    PUBLIC_PROFILE_CONSENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P008", "존재하지 않는 공개 동의입니다."),
    INVALID_PUBLIC_PROFILE_STATE(HttpStatus.CONFLICT, "P009", "현재 상태에서는 공개 프로필을 변경할 수 없습니다."),
    PERFORMANCE_PUBLIC_PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "P010", "존재하지 않는 공연 공개 페이지입니다."),
    DUPLICATE_PERFORMANCE_PUBLIC_PAGE(HttpStatus.CONFLICT, "P011", "공연 공개 페이지나 슬러그가 이미 등록되어 있습니다."),
    PERFORMANCE_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P012", "존재하지 않는 공연 콘텐츠입니다."),
    DUPLICATE_PERFORMANCE_CONTENT(HttpStatus.CONFLICT, "P013", "이미 등록된 공연 콘텐츠입니다."),
    CHECKLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "P014", "존재하지 않는 체크리스트 항목입니다."),
    INVALID_CHECKLIST_ITEM_STATE(HttpStatus.CONFLICT, "P015", "현재 상태에서는 체크리스트를 변경할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
