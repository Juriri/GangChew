package com.example.final_project.src.funding_message.model;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.funding_message.entity.FundingMessage;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FundingMessageRes {
    private Funding funding;
    private String username;
    private String reason;

    public FundingMessageRes(FundingMessage fundingMessage) {
        this.funding = fundingMessage.getParticipant().getFunding();
        this.username = fundingMessage.getParticipant().getParticipant().getUsername();
        this.reason = fundingMessage.getReason();
    }

}
