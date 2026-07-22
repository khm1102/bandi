package kr.ac.tukorea.bandi.global.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

public record LoginPrincipal(Long memberId, String role) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Set<String> ROLES = Set.of(
            "ADMIN", "LEADER", "MEMBER");

    public LoginPrincipal {
        if (memberId == null || role == null || !ROLES.contains(role)) {
            throw new IllegalArgumentException("invalid login principal");
        }
    }

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
