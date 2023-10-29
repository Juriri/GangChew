package com.example.final_project.src.funding.model;

import com.example.final_project.src.funding.entity.Funding;
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
