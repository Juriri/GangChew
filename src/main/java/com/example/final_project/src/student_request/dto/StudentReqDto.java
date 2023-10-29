package com.example.final_project.src.student_request.dto;

import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.student_request.entity.StudentRequest;
import com.example.final_project.src.user.entity.User;
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
