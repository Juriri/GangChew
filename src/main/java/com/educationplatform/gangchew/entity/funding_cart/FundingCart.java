package com.educationplatform.gangchew.entity.funding_cart;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.user.User;

import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
@Table(name = "Funding_Cart")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)
public class FundingCart {

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    //장바구니에 담을 여러개의 펀딩 게시글
    @ManyToMany
    @JoinColumn(name = "funding_id", referencedColumnName = "id", nullable = false)
    private List<FundingCartItem> fundingItems;


    // 장바구니 첫 생성
    @Builder
    public FundingCart(User user) {
        this.user = user;
        fundingItems = new ArrayList<>();
    }


}
