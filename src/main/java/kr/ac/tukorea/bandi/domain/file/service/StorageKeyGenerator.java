package kr.ac.tukorea.bandi.domain.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class StorageKeyGenerator {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("[a-z][a-z0-9-]{1,29}");

    private final Clock clock;

    public String generate(String domain) {
        if (domain == null || !DOMAIN_PATTERN.matcher(domain).matches()) {
            throw new IllegalArgumentException("domain must be a lowercase feature identifier");
        }
        LocalDate today = LocalDate.now(clock);
        return "%s/%04d/%02d/%s".formatted(
                domain, today.getYear(), today.getMonthValue(), UUID.randomUUID());
    }
}
