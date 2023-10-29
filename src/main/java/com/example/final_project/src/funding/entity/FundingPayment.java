package com.example.final_project.src.funding.entity;

import com.example.final_project.src.funding_cart.entity.FundingCart;
import com.example.final_project.src.user.entity.User;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
@Table(name = "Funding_Payment")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)
public class FundingPayment {

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participant_id", referencedColumnName = "id", nullable = false)
    private User participant;

    @Column(name = "bank_name", nullable = false, length = 20)
    private String bankName;

    @Column(name = "bank_account", nullable = false, length = 100)
    private String bankAccount;

    @ManyToOne
    @JoinColumn(name = "funding_id", referencedColumnName = "id", nullable = true)
    private Funding funding;

/*
    @ManyToOne
    @JoinColumn(name = "funding_amount", referencedColumnName = "amount", nullable = false)
    private Funding fundingAmount;
*/
    @Builder
    public FundingPayment(User participant, String bankName, String bankAccount) {
        this.participant = participant;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }


    public void updateFunding(Funding funding) {this.funding = funding; }
    public void updateParticipants(User participant) {this.participant = participant; }

}
