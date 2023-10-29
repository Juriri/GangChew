package com.example.final_project.src.user.model.naver;

import com.example.final_project.common.Constant;
import com.example.final_project.src.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NaverUser {
    @JsonProperty("response")
    private NaverUser.NaverUserResponse response;

    @Data
    public static class NaverUserResponse {
        @JsonProperty("id")
        private String userId;

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("email")
        private String email;

        @JsonProperty("name")
        private String name;
    }

    public User toEntity() {
        return User.builder()
                .email(this.response.getEmail())
                .password("NONE")
                .fullname(this.response.getName())
                .nickname(this.response.getName()+"@naver")
                .oauthProvider(Constant.SocialLoginType.NAVER)
                .username(this.response.getUserId())
                .build();
    }
}

