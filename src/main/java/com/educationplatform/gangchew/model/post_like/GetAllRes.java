package com.educationplatform.gangchew.model.post_like;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.student_request.StudentRequest;
import com.educationplatform.gangchew.entity.user.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetAllRes {
    private String username;
    private String type;
    private Long type_id;
    private Long viewCount;
    private String title;

    public GetAllRes(User user, String type, Long type_id, Funding funding) {
        this.username = user.getUsername();
        this.type = type;
        this.type_id = type_id;
        this.viewCount = funding.getViewCount();
        this.title = funding.getTitle();
    }

    public GetAllRes(User user, String type, Long type_id, StudentRequest studentRequest) {
        this.username = user.getUsername();
        this.type = type;
        this.type_id = type_id;
        this.viewCount = studentRequest.getViewCount();
        this.title = studentRequest.getTitle();
    }

}