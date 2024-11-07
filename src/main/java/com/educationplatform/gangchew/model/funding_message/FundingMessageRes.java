package com.educationplatform.gangchew.model.funding_message;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding.FundingParticipants;
import com.educationplatform.gangchew.entity.funding_message.FundingMessage;

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
