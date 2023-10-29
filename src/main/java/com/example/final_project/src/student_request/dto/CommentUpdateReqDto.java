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
public class CommentUpdateReqDto {
    private Long comment_id;
    private String content;
}
