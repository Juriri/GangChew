package com.educationplatform.gangchew.model.funding;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding.FundingCategory;
import com.educationplatform.gangchew.entity.user.User;
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
    private String deadline;

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
        this.deadline  = transferLocalDateToString(funding.getDeadline());
    }

    public String transferLocalDateToString (LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }

}
