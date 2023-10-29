package com.example.final_project.src.student_request.dto;

import com.example.final_project.src.student_request.entity.StudentRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentcreateResDto {

    private Long id;
    private String username;

    public StudentcreateResDto(StudentRequest studentRequest) {

        this.id = studentRequest.getId();
        this.username = studentRequest.getWriter().getUsername();

    }
}