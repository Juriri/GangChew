package com.educationplatform.gangchew.dto.student_request;

import com.educationplatform.gangchew.entity.funding.FundingCategory;
import com.educationplatform.gangchew.entity.student_request.StudentRequest;
import com.educationplatform.gangchew.entity.user.User;

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
