package com.educationplatform.gangchew.model.user;

import com.educationplatform.gangchew.common.Constant;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserInfoRes {
    private String username;
    private String fullname;
    private String nickname;
    private String email;
    private String address;
    private Constant.SocialLoginType oauthProvider;
}
