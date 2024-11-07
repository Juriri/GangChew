package com.educationplatform.gangchew.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.common.exceptions.BaseException;
import com.educationplatform.gangchew.common.jwt.JwtAuthenticationFilter;
import com.educationplatform.gangchew.common.response.BaseResponseStatus;
import com.educationplatform.gangchew.common.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 정적 리소스는 보안 필터를 통과하지 않도록 설정
        web.ignoring().antMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico/**", "/icon/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        // token을 사용하는 방식이기 때문에 csrf를 disable합니다.
        http.csrf().disable();
        http.cors();
        http.headers().frameOptions().disable();

        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()

                .antMatchers( "/authenticate/**", "/login/**", "/signup", "/logout/**", "/authenticate", "/", "/introduce").permitAll()
                .antMatchers("/error/**", "/swagger-ui/**","/api-docs/**", "/favicon.ico").permitAll()
                .antMatchers("/funding/**", "/studentrequest/**", "/fundingList", "/my/**").permitAll()
                .anyRequest().authenticated();
                /*.anyRequest().permitAll();*/

        // OAuth 로그인 경로 설정 (예: /oauth2/**)
        http
                .oauth2Login()
                .loginPage("/login"); // 로그인 페이지 지정
    }

}