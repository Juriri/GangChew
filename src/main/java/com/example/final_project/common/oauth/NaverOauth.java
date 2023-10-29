package com.example.final_project.common.oauth;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.src.user.model.naver.NaverOAuthToken;
import com.example.final_project.src.user.model.naver.NaverUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static com.example.final_project.common.response.BaseResponseStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverOauth implements SocialOauth {
    @Value("${spring.security.oauth2.client.provider.naver.authorization-uri}")
    private String NAVER_SNS_URL;
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String NAVER_SNS_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String NAVER_SNS_CLIENT_SECRET;
    @Value("${spring.security.oauth2.client.registration.naver.authorization-grant-type}")
    private String NAVER_SNS_GRANT_TYPE;

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String NAVER_TOKEN_REQUEST_URL;;
    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String NAVER_USERINFO_REQUEST_URL;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String NAVER_SNS_CALLBACK_LOGIN_URL;

    @Value("${spring.OAuth2.naver.logout.url}")
    private String NAVER_SNS_LOGOUT_URL;
    @Value("${spring.OAuth2.naver.logout.callback-logout-url}")
    private String NAVER_SNS_CALLBACK_LOGOUT_URL;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String getOauthRedirectURL(Constant.SocialActionType socialActionType) {
        String redirectURL = null;
        switch (socialActionType) {
            case LOGIN -> {
                redirectURL = NAVER_SNS_URL +
                        "?client_id=" + NAVER_SNS_CLIENT_ID +
                        "&client_secret=" + NAVER_SNS_CLIENT_SECRET +
                        "&redirect_uri=" + NAVER_SNS_CALLBACK_LOGIN_URL +
                        "&response_type=code";
            }
            case LOGOUT -> {
                // code 요청
                redirectURL = NAVER_SNS_URL +
                        "?client_id=" + NAVER_SNS_CLIENT_ID +
                        "&client_secret=" + NAVER_SNS_CLIENT_SECRET +
                        "&redirect_uri=" + NAVER_SNS_CALLBACK_LOGOUT_URL +
                        "&response_type=code";
            }
            default -> {
                throw new BaseException(INVALID_OAUTH_ACTION_TYPE);
            }
        }

        return redirectURL;
    }

    public ResponseEntity<String> requestAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", NAVER_SNS_GRANT_TYPE);
        params.add("client_id", NAVER_SNS_CLIENT_ID);
        params.add("client_secret", NAVER_SNS_CLIENT_SECRET);
        params.add("redirect_uri", NAVER_SNS_CALLBACK_LOGIN_URL);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                NAVER_TOKEN_REQUEST_URL,
                HttpMethod.POST,
                requestEntity,
                String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity;
        }
        return null;
    }

    public NaverOAuthToken getAccessToken(ResponseEntity<String> response) throws IOException {
        NaverOAuthToken naverOAuthToken = objectMapper.readValue(response.getBody(), NaverOAuthToken.class);
        return naverOAuthToken;
    }

    public ResponseEntity<String> requestUserInfo(NaverOAuthToken oAuthToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                NAVER_USERINFO_REQUEST_URL,
                HttpMethod.GET,
                request,
                String.class);

        //log.info("response.getBody() = {}", response.getBody());

        return response;
    }

    public NaverUser getUserInfo(ResponseEntity<String> userInfoRes) throws IOException {
        NaverUser naverUser = objectMapper.readValue(userInfoRes.getBody(), NaverUser.class);
        return naverUser;
    }


    public ResponseEntity<String> requestLogout(NaverOAuthToken oAuthToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String Url = NAVER_SNS_LOGOUT_URL + "?grant_type=delete&client_id=" + NAVER_SNS_CLIENT_ID
                + "&client_secret=" + NAVER_SNS_CLIENT_SECRET + "&access_token=" + oAuthToken.getAccess_token()
                + "&service_provider=NAVER";

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                Url,
                HttpMethod.POST,
                requestEntity,
                String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity;
        }
        return null;
    }
}