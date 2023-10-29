package com.example.final_project.src.funding.model;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FundingInsertRes {
    private Long id;
    private String username;

    public FundingInsertRes(Funding funding) {
        this.id = funding.getId();
        this.username = funding.getWriter().getUsername();
    }
}
