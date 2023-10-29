package com.example.final_project.src.user;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.jwt.JwtUtil;
import com.example.final_project.common.oauth.OAuthService;
import com.example.final_project.src.funding.model.FundingInsertReq;
import com.example.final_project.src.user.model.*;
import com.example.final_project.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.example.final_project.common.response.BaseResponseStatus.*;
import static com.example.final_project.utils.ValidationRegex.isRegexEmail;
import com.example.final_project.common.response.BaseResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 브라우저 "jwtToken" 쿠키값 상태 확인 API
     * [GET] /login/cookie
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/login/cookie")
    public BaseResponse<String> checkCookieStatus(HttpServletRequest request) {
        return new BaseResponse<>(userService.checkLoginstatus(request));
    }



    /**
     * 아이디, 이메일 중복 확인 API
     * [Post] /login?check-username={username}
     * @param username (Optional) 유저 아이디
     * @param email (Optional) 유저 이매일
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/login")
    public BaseResponse<String> checkUsername(@RequestParam(name = "check-username", required = false) String username,
                                              @RequestParam(name = "check-email", required = false) String email) {
        // username 조건으로 중복 확인
        if (username != null){
            return new BaseResponse<>(userService.checkUsername(username));
        }

        else if (email != null) {
            return new BaseResponse<>(userService.checkEmail(email));
        }

        else {
            throw new BaseException(USERS_EMPTY_USERID);
        }

    }

    /**
     * 회원가입 API
     * [POST] /signup
     * @param postUserReq 회원가입 모델
     * @return BaseResponse<PostUserRes>
     */

    @ResponseBody
    @PostMapping("/signup")
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq) {
        if (validatePostUserReq(postUserReq)!= null) {
            return validatePostUserReq(postUserReq);
        } else {
            return new BaseResponse<>(userService.createUser(postUserReq));
        }
    }

    //회원가입 정보 공란과 이메일 정규식 여부 확인
    private BaseResponse<PostUserRes> validatePostUserReq(PostUserReq postUserReq) {
        if (postUserReq.getUsername() == null) {
            throw new BaseException(USERS_EMPTY_USERID);
        }
        if (postUserReq.getEmail() == null) {
            throw new BaseException(USERS_EMPTY_EMAIL);
        }
        //이메일 정규표현
        if(!isRegexEmail(postUserReq.getEmail())){
            throw new BaseException(POST_USERS_INVALID_EMAIL);
        }

        if (postUserReq.getPassword() == null) {
            throw new BaseException(USERS_EMPTY_PASSWORD);
        }
        if (postUserReq.getFullname() == null) {
            throw new BaseException(USERS_EMPTY_NAME);
        }
        return null; // 유효성 검사 통과
    }



    /**
     * 로그인 API
     * [POST] /authenticate
     * @param postLoginReq 로컬 로그인 모델
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/authenticate")
    public BaseResponse<String> LoginLocalUser(HttpServletResponse response, @RequestBody PostLoginReq postLoginReq) {
        if (validatePostLoginReq(postLoginReq)!= null) {
            return validatePostLoginReq(postLoginReq);
        } else {
            PostLoginRes postLoginRes = userService.localLogin(response, postLoginReq);
            return new BaseResponse<>(postLoginRes.getJwtToken());
        }
    }


    //로그인 정보 공란 확인
    private BaseResponse<String> validatePostLoginReq(PostLoginReq postLoginReq) {
        if (postLoginReq.getUsername() == null) {
            throw new BaseException(USERS_EMPTY_USERID);
        }

        if (postLoginReq.getPassword() == null) {
            throw new BaseException(USERS_EMPTY_PASSWORD);
        }

        return null; // 유효성 검사 통과
    }


    /**
     * 로컬 로그아웃 API
     * [Get] /logout
     * @param request, response
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/authenticate/logout")
    public BaseResponse<String> LogoutRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Kakao -> /home 리다이렉트, Naver -> /logout/{socialLoginType}/callback 로그아웃 URL로 리디렉션
        return new BaseResponse<>(userService.LogoutRedirectUrl(request, response));
    }



    /**
     * 소셜(카카오, 네이버) 로그인 콜백 API
     * [GET] /login/{socialLoginType}/callback
     * @param socialLoginPath kakao, naver, facebook
     * @param code 액세스 코드
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping(value = "/login/{socialLoginType}/callback")
    public BaseResponse<GetUserRes> socialLoginCallback(
            HttpServletResponse response,
            @PathVariable(name = "socialLoginType") String socialLoginPath,
            @RequestParam(name = "code") String code) throws IOException, BaseException {
        log.info("social "+socialLoginPath+ " callback login code={}", code);
        Constant.SocialLoginType socialLoginType = Constant.SocialLoginType.valueOf(socialLoginPath.toUpperCase());
        // 콜백 URL로부터 전달된 인증 코드(code)를 사용하여 액세스 토큰을 얻는 로직
        GetUserRes getUserRes = oAuthService.oAuthLoginOrJoin(socialLoginType, code);
        String jwtToken = getUserRes.getJwtToken();
        // 결과 반환
        CookieUtil.addCookie(response, "jwtToken", jwtToken, jwtUtil.getExpirationDateFromToken(jwtToken));
        /*String redirectUrl = "http://localhost:9000/home";
        response.sendRedirect(redirectUrl);*/
        return new BaseResponse<>(getUserRes);
    }

    /**
     * 소셜(카카오) 로그아웃 콜백 API
     * [GET] /logout/{socialLoginType}/callback
     * @param response HttpServletResponse
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping(value = "/logout/kakao/callback")
    public void socialKakaoLogoutCallback(HttpServletResponse response) throws IOException, BaseException {
        // 결과 반환
        /*return new BaseResponse<>("로그아웃 되었습니다.");*/
        response.sendRedirect("http://localhost:3000");
    }


    /**
     * 소셜(네이버) 로그아웃 콜백 API
     * [GET] /logout/{socialLoginType}/callback
     * @param code 액세스 코드
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping(value = "/logout/naver/callback")
    public void socialNaverLogoutCallback(
            @RequestParam(name = "code", required = false) String code, HttpServletResponse response) throws IOException, BaseException {

        Constant.SocialLoginType socialLoginType = Constant.SocialLoginType.valueOf("naver".toUpperCase());
        oAuthService.oAuthLogout(socialLoginType, code);
        // 결과 반환
        /*return new BaseResponse<>("로그아웃 되었습니다.");*/
        response.sendRedirect("http://localhost:3000");
    }


    /**
     * 소셜(페이스북) 로그아웃 콜백 API
     * [GET] /logout/{socialLoginType}/callback
     * @param code 액세스 코드
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping(value = "/logout/facebook/callback")
    public BaseResponse<String> socialFacebookLogoutCallback(
            @RequestParam(name = "code", required = false) String code, HttpServletResponse response) throws IOException, BaseException {

        Constant.SocialLoginType socialLoginType = Constant.SocialLoginType.valueOf("facebook".toUpperCase());
        oAuthService.oAuthLogout(socialLoginType, code);
        // 결과 반환
        return new BaseResponse<>("로그아웃 되었습니다.");
    }


    /**
     * 유저 정보 가져오기 API
     * [GET] /user/update
     * @return BaseResponse<UserInfoRes>
     */

    @ResponseBody
    @PostMapping(value = "/user/myinfo")
    public BaseResponse<UserInfoRes> MyInfoGet(HttpServletRequest request) throws IOException, BaseException {
        return new BaseResponse<>(userService.UserInfoGet(request));
    }





    /**
     * 유저 정보 업데이트 API
     * [POST] /user/update
     * @param address (optional) 업데이트할 주소
     * @body UserInfoRes 업데이트 모델. 이 모델은 유저 정보 업데이트에 사용되며, 사용자의 개인 정보를 담고 있습니다.
     *                 (username, fullname, nickname, email, address, SocialLoginType.oauthProvider)
     * @return BaseResponse<GetUserRes>
     */
    @ResponseBody
    @PostMapping(value = "/user/update")
    public BaseResponse<GetUserRes> MyInfoUpdate(@RequestParam(name = "address", required = false) String address,
                                                 @RequestBody UserInfoRes userInfoRes, HttpServletRequest request) throws IOException, BaseException {

        log.info("{}, ", userInfoRes.toString());
        if (validateUserUpdateReq(userInfoRes)!= null) {
            return validateUserUpdateReq(userInfoRes);
        } else {
            if (address == null || address.isEmpty()){
                return new BaseResponse<>(userService.UserInfoUpdate(request, userInfoRes));
            }
            else {
                return new BaseResponse<>(userService.UserAddressUpdate(request, address));
            }
        }
    }


    //업데이트 정보 공란 확인
    private BaseResponse<GetUserRes> validateUserUpdateReq(UserInfoRes userInfoRes) {
        // fullname 공란
        if (userInfoRes.getFullname() == null || userInfoRes.getFullname().isEmpty()) {
            throw new BaseException(USERS_EMPTY_NAME);
        }

        // email 공란
        if (userInfoRes.getEmail() == null || userInfoRes.getEmail().isEmpty()) {
            throw new BaseException(USERS_EMPTY_EMAIL);
        }

        // nickname 공란
        if (userInfoRes.getNickname() == null || userInfoRes.getNickname().isEmpty()) {
            throw new BaseException(USERS_EMPTY_NAME);
        }

        // address 공란
        if (userInfoRes.getAddress() == null || userInfoRes.getAddress().isEmpty()) {
            throw new BaseException(USERS_EMPTY_ADDRESS);
        }

        return null; // 유효성 검사 통과
    }

    /**
     * 회원탈퇴 API
     * [GET] /user/delete
     * @return BaseResponse<GetUserRes>
     */
    @ResponseBody
    @PostMapping(value = "/user/delete")
    public BaseResponse<GetUserRes> UserDelete(HttpServletRequest request) throws IOException, BaseException {
        return new BaseResponse<>(userService.UserDelete(request));
    }
}
