package com.educationplatform.gangchew.entity.funding;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.common.entity.BaseEntity;
import com.educationplatform.gangchew.entity.funding_cart.FundingCart;
import com.educationplatform.gangchew.entity.user.User;
import com.educationplatform.gangchew.service.funding.FundingService;
import com.educationplatform.gangchew.service.funding_cart.FundingCartService;

import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
@Table(name = "Funding_Participants")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)
public class  FundingParticipants extends BaseEntity{

    // FundingParticipantsState를 사용하도록 정의
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    protected Constant.FundingParticipantsState state = Constant.FundingParticipantsState.ACTIVE;

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participant_id", referencedColumnName = "id", nullable = false)
    private User participant;

    @ManyToOne
    @JoinColumn(name = "funding_id", referencedColumnName = "id", nullable = false)
    private Funding funding;

    @Builder
    public FundingParticipants(User participant, Funding funding) {
        this.participant = participant;
        this.funding = funding;
    }


    public void updateFundingParticipantsState (Constant.FundingParticipantsState fundingParticipantsState) {this.state =fundingParticipantsState; }
}