package com.example.final_project.src.student_request.dto;

import com.example.final_project.src.student_request.entity.StudentComment;
import com.example.final_project.src.student_request.entity.StudentRequest;
import com.example.final_project.src.user.entity.User;
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
