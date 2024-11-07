package com.educationplatform.gangchew.dto.student_request;

import com.educationplatform.gangchew.entity.student_request.StudentRequest;

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