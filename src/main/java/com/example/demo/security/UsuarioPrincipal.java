package com.example.demo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UsuarioPrincipal implements UserDetails {
    private final Long id;
    private final String email;
    private final String rol;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioPrincipal(Long id, String email, String rol) {
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.authorities = rol != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + rol), new SimpleGrantedAuthority(rol))
                : List.of();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return email;
    }
}
