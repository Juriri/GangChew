package com.example.final_project.src.funding_cart.repository;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding_cart.entity.FundingCart;
import com.example.final_project.src.funding_cart.entity.FundingCartItem;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundingCartItemRepository extends JpaRepository<FundingCartItem, Long> {
    // 특정 유저의 장바구니의 특정 아이템 확인
    Optional<FundingCartItem> findFundingCartItemByFundingAndFundingCart(Funding funding, FundingCart fundingCart);

    // 특정 유저 장바구니의 장바구니 아이템 리스트 가져오기
    List<FundingCartItem> findFundingCartItemByFundingCart(FundingCart fundingCart);
}
