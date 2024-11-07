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
public class CommentReqDto {
    private Long student_request_id;
    private String content;
    private User user;
    private StudentRequest studentRequest;

    public StudentComment toEntity() {
        return StudentComment.builder()
                .studentRequest(studentRequest)
                .writer(user)
                .content(content)
                .build();
    }

    public void updateCommentReq(User user, StudentRequest studentRequest){
        this.user = user;
        this.studentRequest = studentRequest;
    }
}
