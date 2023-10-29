package com.example.final_project.src.funding_cart.model;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.funding_cart.entity.FundingCart;
import com.example.final_project.src.funding_cart.entity.FundingCartItem;
import com.example.final_project.src.user.entity.User;
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

