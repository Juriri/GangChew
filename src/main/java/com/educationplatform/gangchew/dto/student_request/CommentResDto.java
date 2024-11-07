package com.educationplatform.gangchew.dto.student_request;

import com.educationplatform.gangchew.entity.funding.FundingComment;
import com.educationplatform.gangchew.entity.student_request.StudentComment;
import com.educationplatform.gangchew.entity.user.User;

import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResDto {

    private Long comment_id;
    private String content;
    private String username;
    private boolean isLoginUser;
    private Long student_request_id;
    private Long funding_request_id;
    private LocalDateTime createdAt;
    private String formatdate;

    public CommentResDto(StudentComment studentComment, boolean isLoginUser){
        this.comment_id = studentComment.getId();
        this.content = studentComment.getContent();
        this.username = studentComment.getWriter().getUsername();
        this.student_request_id = studentComment.getStudentRequest().getId();
        this.isLoginUser = isLoginUser;
        this.createdAt = studentComment.getCreatedAt();
        this.formatdate = transferLocalDateToString(this.createdAt);
    }

    public CommentResDto(FundingComment fundingComment, boolean isLoginUser){
        this.comment_id = fundingComment.getId();
        this.content = fundingComment.getContent();
        this.username = fundingComment.getWriter().getUsername();
        this.funding_request_id = fundingComment.getFunding().getId();
        this.isLoginUser = isLoginUser;
        this.createdAt = fundingComment.getCreatedAt();
        this.formatdate = transferLocalDateToString(this.createdAt);
    }

    public String transferLocalDateToString (LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }
}
