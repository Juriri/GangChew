package com.example.final_project.src.user.model;

import com.example.final_project.src.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUserRes {
    private Long id;
    private String username;

    public PostUserRes(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}
