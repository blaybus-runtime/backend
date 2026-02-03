package com.blaybus.backend.global.security;

import com.blaybus.backend.domain.user.User;

import java.util.Collection;
import java.util.List;
import com.blaybus.backend.global.enum_type.Role;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) { this.user = user; }

    public User getUser() { return user; }
    public Long getUserId() { return user.getId(); }
    public Role getRole() { return user.getRole(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().getKey())); // ROLE_MENTOR
    }

    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getUsername(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
