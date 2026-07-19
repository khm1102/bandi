package kr.ac.tukorea.bandi.domain.member.model;

public enum AcademicStatus {
    ENROLLED,
    LEAVE_OF_ABSENCE,
    GRADUATED,
    UNKNOWN;

    public static AcademicStatus fromPortalLabel(String label) {
        if (label == null) {
            return UNKNOWN;
        }
        return switch (label.trim()) {
            case "재학생" -> ENROLLED;
            case "휴학생" -> LEAVE_OF_ABSENCE;
            case "졸업생" -> GRADUATED;
            default -> UNKNOWN;
        };
    }

    public boolean isLoginAllowed() {
        return this == ENROLLED;
    }
}
