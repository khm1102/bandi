package kr.ac.tukorea.bandi.domain.member.sso;

/**
 * 학교 자격증명은 요청 처리 중에만 유지한다.
 * record를 사용하지 않아 자동 toString에 민감정보가 노출되는 경로를 차단한다.
 */
public final class SchoolCredentials {

    private final String studentNo;
    private final String password;

    public SchoolCredentials(String studentNo, String password) {
        this.studentNo = requireText(studentNo, "studentNo");
        this.password = requireText(password, "password");
    }

    public String studentNo() {
        return studentNo;
    }

    public String password() {
        return password;
    }

    @Override
    public String toString() {
        return "SchoolCredentials[redacted]";
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
