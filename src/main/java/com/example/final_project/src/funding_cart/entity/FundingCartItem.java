package com.example.final_project.src.funding_cart.entity;

import com.example.final_project.common.Constant;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.user.entity.User;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
@Table(name = "Funding_Cart_Item")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)
public class FundingCartItem {

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", referencedColumnName = "id", nullable = false)
    private FundingCart fundingCart;

    @ManyToOne
    @JoinColumn(name = "funding_id", referencedColumnName = "id")
    private Funding funding;

    @Column(name = "quantity", nullable = false)
    private int quantity = 0;


    // 펀딩 카트 아이템 첫 생성
    @Builder
    public FundingCartItem(FundingCart fundingCart, Funding funding) {
            this.fundingCart = fundingCart;
            this.funding = funding;
            this.quantity = 0;
    }

    public void addQuantity(int plus_num) { this.quantity += plus_num; }
    public void minusQuantity(int minus_num) {
        this.quantity -= minus_num;
        if(this.quantity < 0) {
            this.quantity = 0;
        }
    }
}
