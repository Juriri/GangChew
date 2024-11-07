package com.educationplatform.gangchew.dto.student_request;

import com.educationplatform.gangchew.entity.funding.FundingCategory;
import com.educationplatform.gangchew.entity.student_request.StudentRequest;
import com.educationplatform.gangchew.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailResDto {

    private Long id;
    private String title;
    private FundingCategory fundingCategory;
    private String writer;
    private String content;
    private Long likeCount;
    private Long viewCount;
    @JsonProperty("like")
    private boolean like;
    private boolean isLoginUser;


    public StudentDetailResDto(StudentRequest studentRequest, FundingCategory fundingCategory, User writer, boolean like, boolean isLoginUser) {
        this.id = studentRequest.getId();
        this.title = studentRequest.getTitle();
        this.fundingCategory = fundingCategory;
        this.writer = writer.getUsername();
        this.content = studentRequest.getContent();
        this.likeCount = studentRequest.getLikeCount();
        this.viewCount = studentRequest.getViewCount();
        this.like = like;
        this.isLoginUser = isLoginUser;
    }
}
