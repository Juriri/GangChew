package com.example.final_project.src.user;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.jwt.JwtUtil;
import com.example.final_project.common.oauth.KakaoOauth;
import com.example.final_project.common.oauth.NaverOauth;
import com.example.final_project.common.response.BaseResponseStatus;
import com.example.final_project.security.CustomAuthenticationProvider;
import com.example.final_project.src.funding_cart.repository.FundingCartRepository;
import com.example.final_project.src.funding_cart.entity.FundingCart;
import com.example.final_project.src.user.entity.User;
import com.example.final_project.src.user.model.*;
import com.example.final_project.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.example.final_project.common.response.BaseResponseStatus.*;


// Service Create, Update, Delete 의 로직 처리
@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {
    //private final FacebookOauth facebookOauth;
    private final KakaoOauth kakaoOauth;
    private final NaverOauth naverOauth;
    private final HttpServletResponse response;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FundingCartRepository fundingCartRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomAuthenticationProvider authenticationProvider;

    @Transactional
    public PostUserRes createUser(PostUserReq postUserReq) {
        //회원 ID 중복 체크
        Optional<User> checkUserByusername = userRepository.findUserByUsername(postUserReq.getUsername());
        if(checkUserByusername.isPresent()){
            throw new BaseException(POST_USERS_EXISTS_USERID);
        }

        //회원 email 중복 체크
        Optional<User> checkUserByemail = userRepository.findUserByEmail(postUserReq.getEmail());
        if (checkUserByemail.isPresent()) {
            String email = postUserReq.getEmail();
            Optional<User> checkLocalOauth = userRepository.findUserByEmailAndOauthProvider(email, Constant.SocialLoginType.LOCAL);
            Optional<User> checkKakaoOauth = userRepository.findUserByEmailAndOauthProvider(email, Constant.SocialLoginType.KAKAO);
            Optional<User> checkNaverOauth = userRepository.findUserByEmailAndOauthProvider(email, Constant.SocialLoginType.NAVER);
            Optional<User> checkFacebookOauth = userRepository.findUserByEmailAndOauthProvider(email, Constant.SocialLoginType.FACEBOOK);

            //1. 로컬 회원으로 이미 가입된 경우
            if (checkLocalOauth.isPresent())
                throw new BaseException(POST_USERS_EXISTS_EMAIL);
                //2. 카카오 소셜 회원으로 이미 가입된 경우
            else if (checkKakaoOauth.isPresent())
                throw new BaseException(POST_USERS_EXISTS_KAKAO);
                //3. 네이버 소셜 회원으로 이미 가입된 경우
            else if (checkNaverOauth.isPresent())
                throw new BaseException(POST_USERS_EXISTS_NAVER);
                //4. 페이스북 소셜 회원으로 이미 가입된 경우
            else if (checkFacebookOauth.isPresent())
                throw new BaseException(POST_USERS_EXISTS_FACEBOOK);
        }

        // 입력한 패스워드 암호화 (BCryptPasswordEncoder 사용)
        String encryptPwd;
        try {
            encryptPwd = new BCryptPasswordEncoder().encode(postUserReq.getPassword());
            postUserReq.setPassword(encryptPwd);
        } catch (Exception exception) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        // user 객체를 DB에 저장
        User saveUser = userRepository.save(postUserReq.toEntity());
        // refreshToken 생성
        String refreshToken = jwtUtil.createRefreshToken();
        saveUser.updateRefreshToken(refreshToken);

        // user의 장바구니 객체 생성 및 DB 저장
        FundingCart fundingCart = new FundingCart(saveUser);
        fundingCartRepository.save(fundingCart);

        return new PostUserRes(saveUser);

    }


    @Transactional
    public PostUserRes createOAuthUser(User user) {
        // refreshToken 생성
        String refreshToken = jwtUtil.createRefreshToken();

        // user 객체를 DB에 저장
        User saveUser = userRepository.save(user);
        saveUser.updateRefreshToken(refreshToken);

        // user의 장바구니 객체 생성 및 DB 저장
        FundingCart fundingCart = new FundingCart(saveUser);
        fundingCartRepository.save(fundingCart);

        return new PostUserRes(saveUser);
    }


    @Transactional
    public PostLoginRes localLogin(HttpServletResponse response, PostLoginReq postLoginReq) {
        // 클라이언트단에서 입력받은 username, password
        String username = postLoginReq.getUsername();
        String password = postLoginReq.getPassword();

        try {
            // 입력받은 username과 password를 이용하여 DB의 user 객체 조회 및 패스워드 일치 확인
            authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            throw new BaseException(BaseResponseStatus.FAILED_TO_LOGIN);
        }

        // 유저 인증 마치어 jwt 토큰 생성
        String jwtToken = jwtUtil.generateTokenByUsername(username);

        // 브라우저 쿠키에 "jwtToken" 저장
        // 아래 코드로 쿠키를 생성하고 브라우저에 전달합니다.
        CookieUtil.addCookie(response, "jwtToken", jwtToken, jwtUtil.getExpirationDateFromToken(jwtToken));
        return new PostLoginRes(postLoginReq.getUsername(), jwtToken);
    }

    @Transactional(readOnly = true)
    public String checkUsername(String username) {
        //회원 username 중복 체크
        Optional<User> checkUserByusername = userRepository.findUserByUsername(username);
        if(checkUserByusername.isPresent()){
            throw new BaseException(POST_USERS_EXISTS_USERID);
        }
        return "사용 가능한 아이디입니다.";
    }


    @Transactional(readOnly = true)
    public String checkEmail(String email) {
        //회원 email 중복 체크
        Optional<User> checkUserByEmail = userRepository.findUserByEmail(email);
        if(checkUserByEmail.isPresent()){
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }
        return "사용 가능한 이메일입니다.";
    }


    @Transactional(readOnly = true)
    public String checkLoginstatus(HttpServletRequest request) {
        // 브라우저에서 토큰 값 찾기
        CookieUtil.getCookie(request, "jwtToken");
        Optional<String> jwtCookie = CookieUtil.getCookie(request, "jwtToken");

        String jwtToken = null;
        if(jwtCookie.isPresent()){
            jwtToken = jwtCookie.get();
            return jwtToken;
        }

        else {
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        /*// 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        } else{
            return jwtToken;
        }*/

    }



    @Transactional
    public String LogoutRedirectUrl(HttpServletRequest request, HttpServletResponse response) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        log.info("logout 서비스, 헤더 jwt 토큰 정보", jwtToken);
        // 브라우저에서 jwt 토큰 값 찾기
       /* CookieUtil.getCookie(request, "jwtToken");
        Optional<String> jwtCookie = CookieUtil.getCookie(request, "jwtToken");

        String jwtToken = null;
        if(jwtCookie.isPresent()){
            jwtToken = jwtCookie.get();
        }
        // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
        else {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }*/

        // 유저네임을 이용하여 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        log.info("UserService user 로그아웃 실행={}", user.toString());
        Constant.SocialLoginType socialLoginType = user.getOauthProvider();
        String redirectUrl = null;

        switch(socialLoginType){
            case LOCAL:{
                // 쿠키값 제거
                CookieUtil.deleteCookie(request, response, "jwtToken");
                return "로그아웃 되었습니다.";
            }

            case KAKAO:{
                // 쿠키값 제거
                CookieUtil.deleteCookie(request, response, "jwtToken");
                redirectUrl = kakaoOauth.getOauthRedirectURL(Constant.SocialActionType.LOGOUT);
            } break;

            case NAVER:{
                // 쿠키값 제거
                CookieUtil.deleteCookie(request, response, "jwtToken");
                redirectUrl = naverOauth.getOauthRedirectURL(Constant.SocialActionType.LOGOUT);
            } break;

            /*case FACEBOOK:{
                redirectUrl = facebookOauth.getOauthRedirectURL(Constant.SocialActionType.LOGOUT);
            } break;*/

            default: {
                throw new BaseException(INVALID_OAUTH_TYPE);
            }
        }

        // kakao, naver, facebook Logout 리다이렉트 url 반환
        log.info("userService redirectUrl={} ", redirectUrl);
        return redirectUrl;
    }

    // 유저 정보 가져오기
    @Transactional(readOnly = true)
    public UserInfoRes UserInfoGet(HttpServletRequest request) {
        /*// 브라우저에서 jwt 토큰 값 찾기
        CookieUtil.getCookie(request, "jwtToken");
        Optional<String> jwtCookie = CookieUtil.getCookie(request, "jwtToken");

        String jwtToken = null;
        if(jwtCookie.isPresent()){
            jwtToken = jwtCookie.get();
        }
        // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
        else {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }*/

        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 로그인 상태까지 확인되어, jwtToken에서 username 추출
        String username = jwtUtil.extractUsername(jwtToken);

        // 추출한 username으로 User 객체 조회
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        return new UserInfoRes(username, user.getFullname(), user.getNickname(), user.getEmail(), user.getAddress(), user.getOauthProvider());
    }


    // 유저 주소 정보만 업데이트
    @Transactional
    public GetUserRes UserAddressUpdate(HttpServletRequest request, String address) {
        /*// 브라우저에서 jwt 토큰 값 찾기
        CookieUtil.getCookie(request, "jwtToken");
        Optional<String> jwtCookie = CookieUtil.getCookie(request, "jwtToken");

        String jwtToken = null;
        if(jwtCookie.isPresent()){
            jwtToken = jwtCookie.get();
        }
        // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
        else {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }*/


        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        String username = jwtUtil.extractUsername(jwtToken);

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));


        Long userId = user.getId();
        log.info("UserService user 주소 업데이트 실행={}", user.toString());
        userRepository.updateUserAddressById(userId, address);

        log.info("UserService user 주소 목록 가져오기 실행={}", user.toString());
        userRepository.updateUserAddressById(userId, address);

        return new GetUserRes(user.getId(), jwtToken);
    }



    // 유저 정보 업데이트
    @Transactional
    public GetUserRes UserInfoUpdate(HttpServletRequest request, UserInfoRes userInfoRes) {
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        String username = jwtUtil.extractUsername(jwtToken);

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        Long userId = user.getId();
        log.info("UserService user 업데이트 실행={}", user.toString());

        String fullname = userInfoRes.getFullname();
        String nickname = userInfoRes.getNickname();
        String email = userInfoRes.getEmail();
        String address = userInfoRes.getAddress();

        userRepository.updateUserDetailsById(userId, fullname, nickname, email, address);

        return new GetUserRes(user.getId(), jwtToken);
    }



    // 유저 회원 탈퇴
    @Transactional
    public GetUserRes UserDelete(HttpServletRequest request) {
        /*// 브라우저에서 jwt 토큰 값 찾기
        CookieUtil.getCookie(request, "jwtToken");
        Optional<String> jwtCookie = CookieUtil.getCookie(request, "jwtToken");

        String jwtToken = null;
        if(jwtCookie.isPresent()){
            jwtToken = jwtCookie.get();
        }
        // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
        else {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }*/

        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        String username = jwtUtil.extractUsername(jwtToken);

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        log.info("UserService user 탈퇴 실행={}", user.toString());
        userRepository.delete(user);

        return new GetUserRes(user.getId(), jwtToken);
    }
}
