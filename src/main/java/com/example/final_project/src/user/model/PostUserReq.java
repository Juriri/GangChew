package com.example.final_project.src.user.model;

import com.example.final_project.common.Constant;
import com.example.final_project.src.user.entity.User;
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
