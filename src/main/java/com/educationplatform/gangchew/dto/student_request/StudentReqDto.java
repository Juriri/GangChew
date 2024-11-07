package com.educationplatform.gangchew.dto.student_request;

import com.educationplatform.gangchew.entity.funding.FundingCategory;
import com.educationplatform.gangchew.entity.student_request.StudentRequest;
import com.educationplatform.gangchew.entity.user.User;

import lombok.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.PrimitiveIterator;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StudentReqDto {
    private String title;
    private Long category_id;
    private String content;


    //dto -> Entity
    public StudentRequest toEntity() throws SQLException {
        return StudentRequest.builder()
                .title(this.title)
                .content(this.content)
                .build();
    }
}
