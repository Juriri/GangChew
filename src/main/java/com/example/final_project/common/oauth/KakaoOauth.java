package com.example.final_project.common.oauth;

import com.example.final_project.common.Constant;
import com.example.final_project.common.Constant.*;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.src.user.model.PostUserRes;
import com.example.final_project.src.user.model.kakao.KakaoOAuthToken;
import com.example.final_project.src.user.model.kakao.KakaoUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.final_project.common.response.BaseResponseStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOauth implements SocialOauth {
    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private String KAKAO_SNS_URL;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_SNS_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.kakao.scope}")
    private String KAKAO_SNS_SCOPE;
    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String KAKAO_SNS_GRANT_TYPE;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String KAKAO_TOKEN_REQUEST_URL;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String KAKAO_USERINFO_REQUEST_URL;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String KAKAO_SNS_CALLBACK_LOGIN_URL;

    @Value("${spring.OAuth2.kakao.logout.url}")
    private String KAKAO_SNS_LOGOUT_URL;
    @Value("${spring.OAuth2.kakao.logout.callback-logout-url}")
    private String KAKAO_SNS_CALLBACK_LOGOUT_URL;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String getOauthRedirectURL(Constant.SocialActionType socialActionType) {
        String redirectURL = null;
        switch (socialActionType) {
            case LOGIN -> {
                redirectURL = KAKAO_SNS_URL +
                        "?client_id=" + KAKAO_SNS_CLIENT_ID +
                        "&redirect_uri=" + KAKAO_SNS_CALLBACK_LOGIN_URL +
                        "&response_type=code" +
                        "&scope=" + KAKAO_SNS_SCOPE;
            }
            case LOGOUT -> {
                redirectURL = KAKAO_SNS_LOGOUT_URL +
                        "?client_id=" + KAKAO_SNS_CLIENT_ID +
                        "&logout_redirect_uri=" + KAKAO_SNS_CALLBACK_LOGOUT_URL;
            }
            default -> {
                throw new BaseException(INVALID_OAUTH_ACTION_TYPE);
            }
        }

        log.info("KAKAO "+socialActionType+" redirectURL = {}", redirectURL);
        return redirectURL;
    }

    public ResponseEntity<String> requestAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", KAKAO_SNS_GRANT_TYPE);
        params.add("client_id", KAKAO_SNS_CLIENT_ID);
        params.add("redirect_uri", KAKAO_SNS_CALLBACK_LOGIN_URL);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                KAKAO_TOKEN_REQUEST_URL,
                HttpMethod.POST,
                requestEntity,
                String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity;
        }
        return null;
    }

    public KakaoOAuthToken getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
        KakaoOAuthToken kakaoOAuthToken = objectMapper.readValue(response.getBody(), KakaoOAuthToken.class);
        return kakaoOAuthToken;
    }

    public ResponseEntity<String> requestUserInfo(KakaoOAuthToken oAuthToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_USERINFO_REQUEST_URL,
                HttpMethod.GET,
                request,
                String.class);

        //log.info("response.getBody() = {}", response.getBody());

        return response;
    }

    public KakaoUser getUserInfo(ResponseEntity<String> userInfoRes) throws JsonProcessingException {
        return objectMapper.readValue(userInfoRes.getBody(), KakaoUser.class);
    }

    public PostUserRes logout() {
        try {
            // URL 연결 생성 및 설정
            URL url = new URL(KAKAO_SNS_LOGOUT_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer YOUR_ACCESS_TOKEN");

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            // 응답 데이터 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();

            // 카카오 API 응답을 처리하고 결과 객체를 생성하여 반환
            return new PostUserRes();
        } catch (IOException e) {
            // 예외 처리
            e.printStackTrace();
            return null; // 예외 발생 시에는 적절한 처리를 하셔야 합니다.
        }
    }

    public ResponseEntity<String> requestPayInfo(KakaoOAuthToken oAuthToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://open-api.kakaopay.com/face-recognition/-face/detect",
                HttpMethod.POST,
                request,
                String.class);

        log.info("response.getBody() = {}", response.getBody());

        return response;
    }
}