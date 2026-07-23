package kr.ac.tukorea.bandi.domain.notice.service;

import java.util.Objects;

public final class SafeMarkdownHtml {

    private final String value;

    SafeMarkdownHtml(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public String getValue() {
        return value;
    }
}
