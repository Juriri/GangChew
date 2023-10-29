package com.example.final_project.src.funding.model;

import com.example.final_project.common.Constant;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingComment;
import com.example.final_project.src.student_request.entity.StudentRequest;
import com.example.final_project.src.user.entity.User;
import lombok.*;

import java.sql.SQLException;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentInsertReq {
    private Long funding_id;
    private String content;
    private User user;
    private Funding funding;

/*
    public CommentInsertReq(String content) {
        this.content = content;
    }
*/

    public FundingComment toEntity() {
        return FundingComment.builder()
                .funding(funding)
                .writer(user)
                .content(this.content)
                .build();
    }


}
