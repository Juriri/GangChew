package com.educationplatform.gangchew.dto.student_request;

import com.educationplatform.gangchew.entity.student_request.StudentComment;
import com.educationplatform.gangchew.entity.student_request.StudentRequest;
import com.educationplatform.gangchew.entity.user.User;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateReqDto {
    private Long comment_id;
    private String content;
}
