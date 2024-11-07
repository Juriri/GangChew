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
public class StudentResDto {

    private Long id;
    private String title;
    private FundingCategory fundingCategory;
    private String writer;
    private Long likeCount;
    private Long viewCount;
    @JsonProperty("like")
    private Boolean like;
    private int totalItems;
    private int comments;

    public StudentResDto(StudentRequest studentRequest, FundingCategory fundingCategory, User writer, Boolean like, int totalItems, int comments) {
        this.id = studentRequest.getId();
        this.title = studentRequest.getTitle();
        this.fundingCategory = fundingCategory;
        this.writer = writer.getUsername();
        this.likeCount = studentRequest.getLikeCount();
        this.viewCount = studentRequest.getViewCount();
        this.like = like;
        this.totalItems = totalItems;
        this.comments = comments;
    }

    public StudentResDto(StudentRequest studentRequest, FundingCategory fundingCategory, User writer, Boolean like, int totalItems) {
        this.id = studentRequest.getId();
        this.title = studentRequest.getTitle();
        this.fundingCategory = fundingCategory;
        this.writer = writer.getUsername();
        this.likeCount = studentRequest.getLikeCount();
        this.viewCount = studentRequest.getViewCount();
        this.like = like;
        this.totalItems = totalItems;
    }
}
