package com.scholarshiphub.security;

import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.UserStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security adapter around {@link User}. Kept out of the entity layer
 * so JPA entities stay free of framework concerns.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final RoleName role;
    private final GrantedAuthority authority;
    private final UserStatus status;
    private final boolean emailVerified;
    private final Instant lockedUntil;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole().getName();
        this.authority = new SimpleGrantedAuthority("ROLE_" + role.name());
        this.status = user.getStatus();
        this.emailVerified = user.isEmailVerified();
        this.lockedUntil = user.getLockedUntil();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED
                && (lockedUntil == null || lockedUntil.isBefore(Instant.now()));
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
