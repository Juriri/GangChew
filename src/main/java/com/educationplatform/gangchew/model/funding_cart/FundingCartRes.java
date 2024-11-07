package com.educationplatform.gangchew.model.funding_cart;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding.FundingCategory;
import com.educationplatform.gangchew.entity.funding_cart.FundingCart;
import com.educationplatform.gangchew.entity.funding_cart.FundingCartItem;
import com.educationplatform.gangchew.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
@NoArgsConstructor
/*@AllArgsConstructor*/
public class FundingCartRes {
    private String username;
    private Funding funding;
    private int quantity;
    private Long fundingcart_id;

    public FundingCartRes(FundingCartItem fundingCartItem) {
        FundingCart fundingCart = fundingCartItem.getFundingCart();
        // 펀딩 카트 주인 가져오기
        this.username = fundingCart.getUser().getUsername();
        // 펀딩 카트에 담긴 펀딩 아이템 가져오기
        this.funding = fundingCartItem.getFunding();
        // 해당 펀딩 수량 가져오기
        this.quantity = fundingCartItem.getQuantity();
        // 펀딩 카트 고유 id 저장
        this.fundingcart_id = fundingCart.getId();
    }


}

