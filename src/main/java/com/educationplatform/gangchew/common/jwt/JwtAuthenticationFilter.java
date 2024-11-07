package com.educationplatform.gangchew.common.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.educationplatform.gangchew.common.exceptions.BaseException;
import com.educationplatform.gangchew.common.response.BaseResponseStatus;
import com.educationplatform.gangchew.common.security.CustomAuthenticationProvider;
import com.educationplatform.gangchew.common.security.CustomUserDetailsService;
import com.educationplatform.gangchew.common.utils.CookieUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.educationplatform.gangchew.common.response.BaseResponseStatus.NOT_FIND_LOGIN_SESSION;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain) throws ServletException, IOException {
        try {
            // 헤더에서 jwtToken 추출
            String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

            log.info("요청 URL = {}", request.getRequestURI());

            if (jwtToken != null && jwtUtil.isRefreshTokenExpired(jwtToken)) {
                sendErrorResponse(request, response, BaseResponseStatus.EXPIRED_REFRESHTOKEN);
            } else {
                if (jwtToken != null && !jwtUtil.isTokenExpired(jwtToken)) {
                    String username = jwtUtil.extractUsername(jwtToken);

                    long expirationTime = jwtUtil.getExpirationDateFromToken(jwtToken).getTime();
                    long currentTime = System.currentTimeMillis();
                    long MinuteInMillis = 3 * 60 * 1000; // 3분을 밀리초로 표현

                    if (expirationTime - currentTime <= MinuteInMillis) {
                        // 토큰 만료가 3분 이내인 경우
                        String newAccessToken = jwtUtil.generateTokenByUsername(username);
                        log.info("요청 토큰 만료가 3분 이내 = {}", newAccessToken);
                        if (newAccessToken != null) {
                            // 클라이언트의 쿠키 갱신
                            Date newExp = jwtUtil.getExpirationDateFromToken(newAccessToken);
                            CookieUtil.deleteCookie(request, response, "jwtToken");
                            CookieUtil.addCookie(response, "jwtToken", newAccessToken, newExp);
                        }
                    }

                    // Authentication 객체 갱신
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    //log.info("Authentication userDetails= {}", userDetails);
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    //log.info("Authentication usernamePasswordAuthenticationToken= {}", usernamePasswordAuthenticationToken);
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }


            }
            chain.doFilter(request, response);

        } catch (IllegalArgumentException e) {
            sendErrorResponse(request, response, BaseResponseStatus.INVALID_USER_JWT);
        } catch (ExpiredJwtException e) {
            sendErrorResponse(request, response, BaseResponseStatus.EXPIRED_JWT);
        } catch (MalformedJwtException e) {
            sendErrorResponse(request, response, BaseResponseStatus.MALFORMEDJWT);
        }
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, BaseResponseStatus status) throws IOException {
        CookieUtil.deleteCookie(request, response, "jwtToken");
        String errors = status.getMessage();
        log.info("errors = {}", status);
        //request.setAttribute("errors", errors);
        response.setCharacterEncoding("utf-8");
        /*response.sendRedirect("/error?msg=" + status);*/
        response.sendRedirect("/error?msg=" + status);
    }
}