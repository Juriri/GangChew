package com.example.final_project.src.funding.entity;

import com.example.final_project.common.Constant;
import com.example.final_project.common.entity.BaseEntity;

import com.example.final_project.src.funding.model.FundingUpdateReq;
import com.example.final_project.src.funding.repository.FundingParticipantsRepository;
import com.example.final_project.src.funding_message.entity.FundingMessage;
import com.example.final_project.src.user.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.security.core.parameters.P;

import javax.persistence.*;
import java.sql.Clob;
import java.time.*;
import java.util.Date;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
/*@Table(name = "Funding")*/
@Table(name = "Funding", indexes = { // 고속 검색을 위해 카테고리와 유저 id, state를 인덱스로 추가
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_writer_id", columnList = "writer_id"),
        @Index(name = "idx_state", columnList = "state")
})
public class Funding extends BaseEntity {

    // FundingState를 사용하도록 정의
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private Constant.FundingState state = Constant.FundingState.ACTIVE;

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "subtitle", nullable = false, length = 500)
    private String subtitle;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private FundingCategory fundingCategory;

    @ManyToOne
    @JoinColumn(name = "writer_id", referencedColumnName = "id", nullable = false)
    private User writer;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "deadline", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime deadline;

    @Column(name = "goal", nullable = false)
    private Long goal;

    @Column(name = "min_participants", nullable = false)
    private Long min_participants;

    @Column(name = "max_participants", nullable = false)
    private Long max_participants;

    @Column(name = "amount", nullable = false)
    private Long amount;


    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Lob // CLOB 형식 컬럼
    @Column(name = "thumbnail", nullable = false)
    private String thumbnail;

    @Lob
    @Column(name = "content", nullable = false) // 본문 이미지와 본문 내용을 base64 인코딩 데이터 그대로 저장
    private String content;

    @Builder
    public Funding(String title, String subtitle, FundingCategory fundingCategory, User writer, String location, LocalDateTime deadline,
                   Long goal, Long min_participants, Long max_participants, Long amount, String thumbnail, String content) {
        this.title = title;
        this.subtitle = subtitle;
        this.fundingCategory = fundingCategory;
        this.writer = writer;
        this.location = location;
        this.deadline = deadline;
        this.goal = goal;
        this.min_participants = min_participants;
        this.max_participants = max_participants;
        this.amount = amount;
        this.likeCount = 0L;   // 기본값 0으로 설정
        this.viewCount = 0L;   // 기본값 0으로 설정
        this.thumbnail = thumbnail;
        this.content = content;
    }


    public void updateThumnail(String thumbnail){ this.thumbnail = thumbnail;}

    public void updateContent(String content) { this.content = content;}

    public void updateCategory(FundingCategory fundingCategory){
        this.fundingCategory = fundingCategory;
    }

    public void updateWriter(User writer){
        this.writer = writer;
    }

    public void addViewCount(int plus_num){this.viewCount += plus_num;}
    public void minusViewCount(int minus_num) {
        this.viewCount -= minus_num;
        if (this.viewCount <= 0) {
            this.viewCount = 0L;
        }
    }
    //좋아요 +1
    public void addLikeCount(int plus_num) {this.likeCount += plus_num;}
    public void minusLikeCount(int minus_num) {
        this.likeCount -= minus_num;
        if (this.likeCount <= 0) {
            this.likeCount = 0L;
        }
    }

    public void updateState(Constant.FundingState state) {
        this.state = state;
    }

    public void updateFundingAndPariticpantsState(Constant.FundingState funding_forward_state, List<FundingParticipants> fundingParticipantsList) {

        // 펀딩 변경 상태가 fail로 들어오면 매개변수의 참여자 리스트 상태값도 환불 필요 상태로 변경
        if (funding_forward_state.equals(Constant.FundingState.FAIL)) {
            for (FundingParticipants fundingParticipants: fundingParticipantsList) {
                fundingParticipants.updateFundingParticipantsState(Constant.FundingParticipantsState.REFUND_NEEDED);

            }
        }
        // 펀딩 변경 상태가 complete으로 들어오면 매개변수의 참여자 리스트 상태값도 펀딩 성공 상태로 변경
        else if (funding_forward_state.equals(Constant.FundingState.COMPLETE)) {
            for (FundingParticipants fundingParticipants: fundingParticipantsList) {
                fundingParticipants.updateFundingParticipantsState(Constant.FundingParticipantsState.COMPLETE);
            }

        }
    }

    public void updateAll(FundingUpdateReq fundingUpdateReq, FundingCategory fundingCategory) {
        this.title = fundingUpdateReq.getTitle();
        this.subtitle =  fundingUpdateReq.getSubtitle();
        this.fundingCategory =  fundingCategory;
        this.location =  fundingUpdateReq.getLocation();
        this.deadline = convertDateToLocalDateTime(fundingUpdateReq.getDeadline());
        this.goal =  fundingUpdateReq.getGoal();
        this.min_participants = fundingUpdateReq.getMin_participants();
        this.max_participants = fundingUpdateReq.getMax_participants();
        this.amount = fundingUpdateReq.getAmount();
        this.thumbnail =  fundingUpdateReq.getThumbnail();
        this.content =  fundingUpdateReq.getContent();
    }

    // Date를 LocalDateTime으로 변환하는 메서드
    private static LocalDateTime convertDateToLocalDateTime(Date date) {
        // Date 객체를 LocalDate와 LocalTime으로 분리
        Instant instant = date.toInstant();
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        LocalDate localDate = zonedDateTime.toLocalDate().plusDays(1); // 다음 날로 설정
        // 다음 날의 자정 (00:00:00)으로 LocalDateTime 생성
        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
    }
}