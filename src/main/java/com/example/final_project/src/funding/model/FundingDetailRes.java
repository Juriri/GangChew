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
public class FundingDetailRes {
    private Funding funding;
    private FundingCategory fundingCategory;
    private String writer;
    private String deadline;
    /*@JsonProperty("liked") // JSON 필드 이름을 "liked"로 설정*/
    private boolean liked;
    private int achievementrate;
    private boolean isLoginUser;
    private int participants;

    public FundingDetailRes(Funding funding, FundingCategory fundingCategory, String writer, boolean liked, int achievementrate , boolean isLoginUser, int participants) {
        this.funding = funding;
        this.fundingCategory = fundingCategory;
        this.writer = writer;
        this.deadline = LocalDateTimeToString(funding.getDeadline());
        this.liked = liked;
        this.achievementrate = achievementrate;
        this.isLoginUser = isLoginUser;
        this.participants = participants;
    }


    public String LocalDateTimeToString (LocalDateTime deadline) {
        // Create a DateTimeFormatter with the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일, HH시 mm분");

        // Format the LocalDateTime instance to a string
        String formattedDeadline = deadline.format(formatter);

        // Print the formatted date
        return formattedDeadline;
    }

}
