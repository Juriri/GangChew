package com.example.final_project.src.funding_message.model;

import com.example.final_project.common.Constant;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.funding_message.entity.FundingMessage;
import com.example.final_project.src.user.entity.User;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FundingMessageReq {
    private Funding funding;
    private String reason;

    public FundingMessageReq(FundingParticipants fundingParticipants, String reason) {
        this.funding = fundingParticipants.getFunding();
        this.reason = reason;
    }

    public FundingMessage toEntity(FundingParticipants fundingParticipants, String reason) {
        return FundingMessage.builder()
                .participant(fundingParticipants)
                .reason(reason)
                .build();
    }

}
