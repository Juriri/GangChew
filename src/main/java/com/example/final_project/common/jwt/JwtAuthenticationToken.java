package com.example.final_project.common.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String accessToken;

    public JwtAuthenticationToken(String accessToken) {
        super(null);
        this.accessToken = accessToken;
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}