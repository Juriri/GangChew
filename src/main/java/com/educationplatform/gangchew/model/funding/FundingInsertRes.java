package com.educationplatform.gangchew.model.funding;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.user.User;

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
