package com.educationplatform.gangchew.model.funding;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding.FundingPayment;
import com.educationplatform.gangchew.entity.user.User;

import lombok.*;

import java.sql.SQLException;
import java.time.*;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInsertReq {
    private Long participant_id;
    private String bank_name;
    private String bank_account;
    private Long funding_id;

    public PaymentInsertReq(User participants, String bank_name, String bank_account, Funding funding) {
        this.participant_id = participants.getId();
        this.bank_name = bank_name;
        this.bank_account = bank_account;
        this.funding_id = funding.getId();
    }

    public FundingPayment toEntity() throws SQLException {
        return FundingPayment.builder()
                .bankName(this.bank_name)
                .bankAccount(this.bank_account)
                .build();
    }
}
