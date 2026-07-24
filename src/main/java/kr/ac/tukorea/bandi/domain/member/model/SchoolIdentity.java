package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.SchoolIdentityMismatchException;

import java.text.Normalizer;
import java.util.Objects;

/** 학교 포털 원문에서 서비스에 필요한 최소 신원만 정규화한 값. */
public record SchoolIdentity(
        String studentNo,
        String name,
        String department,
        AcademicStatus academicStatus,
        String phoneNumber
) {

    public SchoolIdentity {
        studentNo = requireText(studentNo, "studentNo");
        name = normalizeName(requireText(name, "name"));
        department = normalizeNullable(department);
        Objects.requireNonNull(academicStatus, "academicStatus");
        phoneNumber = normalizePhoneNumber(phoneNumber);
    }

    public SchoolIdentity(String studentNo, String name, String department,
                          AcademicStatus academicStatus) {
        this(studentNo, name, department, academicStatus, null);
    }

    public boolean hasSameStudentNo(String registeredStudentNo) {
        return studentNo.equals(registeredStudentNo);
    }

    public void validateStudentNo(String expectedStudentNo) {
        if (!hasSameStudentNo(expectedStudentNo)) {
            throw new SchoolIdentityMismatchException();
        }
    }

    public boolean hasSameName(String registeredName) {
        return name.equals(normalizeName(registeredName));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private static String normalizeName(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFC);
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String normalizePhoneNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.matches("01[016789]\\d{7,8}") ? digits : null;
    }
}
