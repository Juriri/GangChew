package com.example.final_project.src.funding_message.entity;

import com.example.final_project.common.Constant;
import com.example.final_project.common.entity.BaseEntity;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.user.entity.User;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
@Table(name = "Funding_Message")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)
public class FundingMessage extends BaseEntity {
    // FundingMessage State를 사용하도록 정의
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private Constant.FundingMessageState state = Constant.FundingMessageState.SEND;

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participant_id", referencedColumnName = "id", nullable = false)
    private FundingParticipants participant;

    @Column(name = "reason", length = 1000, nullable = true)
    private String reason;

/*    @ManyToOne
    @JoinColumn(name = "funding_id", referencedColumnName = "id", nullable = false)
    private Funding funding;*/


    @Builder
    public FundingMessage(FundingParticipants participant, String reason) {
        this.participant = participant;
        this.reason = reason;
    }

    public void updateState(Constant.FundingMessageState state) {this.state = state; }

    public void updateReason(String reason) {
        this.reason = reason;
    }
}
