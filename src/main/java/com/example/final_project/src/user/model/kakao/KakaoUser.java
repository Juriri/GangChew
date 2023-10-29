package com.example.final_project.src.user.model.kakao;

import com.example.final_project.common.Constant;
import com.example.final_project.src.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoUser {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("properties")
    private KakaoUserProperties properties;

    @JsonProperty("kakao_account")
    private KakaoUserAccount kakaoAccount;

    @Data
    public static class KakaoUserProperties {
        @JsonProperty("nickname")
        private String nickname;

        // 다른 사용자 속성들 추가 가능
    }

    @Data
    public static class KakaoUserAccount {
        @JsonProperty("email")
        private String email;

        // 다른 사용자 계정 정보들 추가 가능
    }

    public User toEntity() {
        return User.builder()
                .email(this.kakaoAccount.getEmail())
                .password("NONE")
                .fullname(this.properties.getNickname())
                .nickname(this.properties.getNickname()+"@kakao")
                .oauthProvider(Constant.SocialLoginType.KAKAO)
                .username(String.valueOf(this.id))
                .build();
    }


}