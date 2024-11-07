package com.educationplatform.gangchew.repository.funding_cart;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding_cart.FundingCart;
import com.educationplatform.gangchew.entity.funding_cart.FundingCartItem;
import com.educationplatform.gangchew.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundingCartRepository extends JpaRepository<FundingCart, Long> {
    // 유저의 장바구니 찾기
    Optional<FundingCart> findFundingCartByUser(User user);
}
