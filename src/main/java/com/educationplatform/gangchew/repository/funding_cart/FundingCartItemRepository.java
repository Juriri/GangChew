package com.educationplatform.gangchew.repository.funding_cart;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding_cart.FundingCart;
import com.educationplatform.gangchew.entity.funding_cart.FundingCartItem;
import com.educationplatform.gangchew.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundingCartItemRepository extends JpaRepository<FundingCartItem, Long> {
    // 특정 유저의 장바구니의 특정 아이템 확인
    Optional<FundingCartItem> findFundingCartItemByFundingAndFundingCart(Funding funding, FundingCart fundingCart);

    // 특정 유저 장바구니의 장바구니 아이템 리스트 가져오기
    List<FundingCartItem> findFundingCartItemByFundingCart(FundingCart fundingCart);
}
