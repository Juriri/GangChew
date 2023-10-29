package com.example.final_project.src.funding.model;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingPayment;
import com.example.final_project.src.user.entity.User;
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
