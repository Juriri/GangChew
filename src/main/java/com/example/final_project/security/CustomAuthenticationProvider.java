package com.example.final_project.security;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.jwt.JwtAuthenticationToken;
import com.example.final_project.src.user.UserRepository;
import com.example.final_project.src.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.example.final_project.common.response.BaseResponseStatus.*;

@Component
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 사용자가 입력한 패스워드
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 패스워드 일치 확인
        UserDetails userDetails = null;
        if (customAuthenticationLogic(username, password)){
            // 사용자를 인증하고 UserDetails를 가져옵니다.
            userDetails = customUserDetailsService.loadUserByUsername(username);
            log.info("userDetails={}", userDetails.toString());
        }

        //log.info("Authentication authenticate {}, ", userDetails);
        // 인증이 성공하면 UsernamePasswordAuthenticationToken을 반환합니다.
        assert userDetails != null;
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class) ||
                authentication.equals(JwtAuthenticationToken.class);
    }

    // 사용자 정의 로그인 로직을 구현
    private boolean customAuthenticationLogic(String username, String password) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        if (new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            // 비밀번호가 일치하면 true 반환.
            return true;
        } else {
            // 비밀번호가 일치하지 않으면 로그인 실패 예외를 던짐.
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

}
