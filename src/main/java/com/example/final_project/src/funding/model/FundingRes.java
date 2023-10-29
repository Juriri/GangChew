package com.example.final_project.src.funding.model;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FundingRes {
    private Long fundingId;
    private String title;
    private String subtitle;
    private String thumbnail;
    private boolean liked;
    private int achievementrate;
    private int totalItems;
    private Long likeCount;
    private Long viewCount;
    private String writer;
    private String categoryname;

    public FundingRes(Funding funding, boolean liked, int achievementrate,  int totalItems) {
        this.fundingId = funding.getId();
        this.title = funding.getTitle();
        this.subtitle = funding.getSubtitle();
        this.thumbnail = funding.getThumbnail();
        this.liked = liked;
        this.achievementrate = achievementrate;
        this.totalItems = totalItems;
        this.likeCount = funding.getLikeCount();
        this.viewCount = funding.getViewCount();
        this.writer = writer;
    }

    public FundingRes(Funding funding, boolean liked, int achievementrate,  int totalItems, String writer, String categoryname) {
        this.fundingId = funding.getId();
        this.title = funding.getTitle();
        this.subtitle = funding.getSubtitle();
        this.thumbnail = funding.getThumbnail();
        this.liked = liked;
        this.achievementrate = achievementrate;
        this.totalItems = totalItems;
        this.likeCount = funding.getLikeCount();
        this.viewCount = funding.getViewCount();
        this.writer = writer;
        this.categoryname = categoryname;
    }


}
