package kr.ac.tukorea.bandi.domain.notice.model;

import java.util.Arrays;

public enum PublicNoticeCategory {

    GENERAL,
    RECRUITMENT;

    public static boolean isSupported(String categoryCode) {
        if (categoryCode == null) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(category -> category.name().equals(categoryCode.strip()));
    }
}
