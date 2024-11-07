package com.educationplatform.gangchew.model.funding;

import com.educationplatform.gangchew.entity.funding.Funding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FundingMainRes {
    private Funding funding;
    private int achievementrate;
    private boolean liked;
    public FundingMainRes(Funding funding, int achievementrate, boolean liked) {
        this.funding = funding;
        this.achievementrate = achievementrate;
        this.liked = liked;
    }
}
