package com.educationplatform.gangchew.model.funding_message;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding.FundingParticipants;
import com.educationplatform.gangchew.entity.funding_message.FundingMessage;
import com.educationplatform.gangchew.entity.user.User;

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
