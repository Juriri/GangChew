package com.educationplatform.gangchew.common.oauth;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.common.Constant.*;
import com.educationplatform.gangchew.common.exceptions.BaseException;
import com.educationplatform.gangchew.common.jwt.JwtUtil;
import com.educationplatform.gangchew.common.security.CustomUserDetailsService;
import com.educationplatform.gangchew.common.utils.CookieUtil;
import com.educationplatform.gangchew.entity.user.User;
import com.educationplatform.gangchew.model.user.GetUserRes;
import com.educationplatform.gangchew.model.user.PostUserRes;
import com.educationplatform.gangchew.model.user.kakao.KakaoOAuthToken;
import com.educationplatform.gangchew.model.user.kakao.KakaoUser;
import com.educationplatform.gangchew.model.user.naver.NaverOAuthToken;
import com.educationplatform.gangchew.model.user.naver.NaverUser;
import com.educationplatform.gangchew.repository.user.UserRepository;
import com.educationplatform.gangchew.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

import static com.educationplatform.gangchew.common.response.BaseResponseStatus.*;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthService {
    //private final FacebookOauth facebookOauth;
    private final KakaoOauth kakaoOauth;
    private final NaverOauth naverOauth;
    private final HttpServletResponse response;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    public GetUserRes oAuthLoginOrJoin(SocialLoginType socialLoginType, String code) throws IOException {

        switch (socialLoginType) {
            case KAKAO -> {  // 카카오 로그인 처리를 위한 분기 추가
                ResponseEntity<String> accessTokenResponse = kakaoOauth.requestAccessToken(code);
                KakaoOAuthToken oAuthToken = kakaoOauth.getAccessToken(accessTokenResponse);

                ResponseEntity<String> userInfoResponse = kakaoOauth.requestUserInfo(oAuthToken);
                KakaoUser kakaoUser = kakaoOauth.getUserInfo(userInfoResponse);

                String username = String.valueOf(kakaoUser.getId());
                // 기존 사용자 정보 조회
                Optional<User> existingUser = userRepository.findUserByUsername(username);
                Long Id = null;
                // 신규 사용자 처리
                if (existingUser.isEmpty()) {
                    User kakaouser = kakaoUser.toEntity();
                    PostUserRes postUserRes = userService.createOAuthUser(kakaouser);
                    Id = postUserRes.getId();

                }
                // 기존 사용자 정보를 바탕으로 JWT 토큰 생성 및 응답 객체 생성
                else {
                    // 유저 정보 조회
                    User user = existingUser.get();
                    Id = user.getId();
                }

                // JWT 토큰 생성
                /* UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);*/
                String jwtToken = jwtUtil.generateTokenByUsername(username);
                CookieUtil.addCookie(response, "jwtToken", jwtToken, jwtUtil.getExpirationDateFromToken(jwtToken));

                return new GetUserRes(Id, jwtToken);
            }

            // 네이버 로그인 처리 부분 추가
            case NAVER -> {
                ResponseEntity<String> accessTokenResponseNaver = naverOauth.requestAccessToken(code);
                NaverOAuthToken naverOAuthToken = naverOauth.getAccessToken(accessTokenResponseNaver);
                log.info("naverOAuthToken ={}", naverOAuthToken);
                ResponseEntity<String> userInfoResponseNaver = naverOauth.requestUserInfo(naverOAuthToken);
                log.info("userInfoResponseNaver ={}", userInfoResponseNaver);
                NaverUser naverUser = naverOauth.getUserInfo(userInfoResponseNaver);
                log.info("naverUser ={}", naverUser.toString());
                //log.info("oAuthLoginOrJoin naverUser={}", naverUser);
                String username = naverUser.getResponse().getUserId();
                // 기존 사용자 정보 조회
                Optional<User> existingNaverUser = userRepository.findUserByUsername(username);
                Long Id = null;

                // 신규 사용자 처리
                if (existingNaverUser.isEmpty()) {
                    User naveruser = naverUser.toEntity();
                    PostUserRes postUserRes = userService.createOAuthUser(naveruser);
                    Id = postUserRes.getId();
                }
                // 기존 사용자 정보를 바탕으로 JWT 토큰 생성 및 응답 객체 생성
                else {
                    // 유저 정보 조회
                    User user = existingNaverUser.get();
                    Id = user.getId();
                }

                // JWT 토큰 생성
                String jwtToken = jwtUtil.generateTokenByUsername(username);
                log.info("소셜 로그인 인증 완료, jwt 쿠키 저장 시도");
                CookieUtil.addCookie(response, "jwtToken", jwtToken, jwtUtil.getExpirationDateFromToken(jwtToken));
                return new GetUserRes(Id, jwtToken);
            }
            default -> {
                throw new BaseException(INVALID_OAUTH_TYPE);
            }

        }
    }

    public void oAuthLogout(SocialLoginType socialLoginType, String code) throws IOException {

        switch (socialLoginType) {
            /*case KAKAO: {  // 카카오 로그인 처리를 위한 분기 추가
                ResponseEntity<String> accessTokenResponse = kakaoOauth.requestAccessToken(code);
                log.info("oAuthLogout accessTokenResponse={}",accessTokenResponse );
                KakaoOAuthToken oAuthToken = kakaoOauth.getAccessToken(accessTokenResponse);
                log.info("oAuthLogout oAuthToken={}",oAuthToken );
                ResponseEntity<String> userInfoResponse = kakaoOauth.requestUserInfo(oAuthToken);
                log.info("oAuthLogout userInfoResponse={}",userInfoResponse );
            } break;*/

            // 네이버 로그인 처리 부분 추가
            case NAVER: {
                ResponseEntity<String> accessTokenResponseNaver = naverOauth.requestAccessToken(code);

                // access Token 획득
                NaverOAuthToken naverOAuthToken = naverOauth.getAccessToken(accessTokenResponseNaver);

                // delete 실행
                log.info("NAVER accessToken 폐기 실행 완료, {}", naverOauth.requestLogout(naverOAuthToken));
            }
            break;

            default: {
                throw new BaseException(INVALID_OAUTH_TYPE);
            }

        }
    }

    public String oAuthPayment(String code) throws IOException {

        ResponseEntity<String> accessTokenResponse = kakaoOauth.requestAccessToken(code);
        KakaoOAuthToken oAuthToken = kakaoOauth.getAccessToken(accessTokenResponse);

        ResponseEntity<String> payInfoResponse = kakaoOauth.requestPayInfo(oAuthToken);
        log.info("payInfoResponse =  {}", payInfoResponse);

        return null;
    }

}
