package com.educationplatform.gangchew.model.funding;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding.FundingComment;
import com.educationplatform.gangchew.entity.student_request.StudentRequest;
import com.educationplatform.gangchew.entity.user.User;

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
