package com.example.final_project.src.student_request.dto;

import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.student_request.entity.StudentRequest;
import com.example.final_project.src.user.entity.User;
import lombok.*;

import java.sql.SQLException;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StudentUpdateReqDto {
    private Long studentId;
    private String title;
    private Long category_id;
    private String content;

    //dto -> Entity
    public StudentUpdateReqDto(String title, FundingCategory fundingCategory, String content) {
        this.title = title;
        this.category_id = fundingCategory.getId();
        this.content = content;
    }
}
