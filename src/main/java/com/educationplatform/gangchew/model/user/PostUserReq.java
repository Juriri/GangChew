package com.educationplatform.gangchew.model.user;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.entity.user.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUserReq {
    private String username;
    private String nickname;
    private String fullname;
    private String email;
    private String address;
    private String password;


    public User toEntity() {
        return User.builder()
                .username(this.username)
                .nickname(this.nickname)
                .fullname(this.fullname)
                .email(this.email)
                .address(this.address)
                .password(this.password)
                .oauthProvider(Constant.SocialLoginType.LOCAL)
                .build();
    }

}
