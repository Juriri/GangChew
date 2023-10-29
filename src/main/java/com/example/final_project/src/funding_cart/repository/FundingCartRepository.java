package com.example.final_project.src.funding_cart.repository;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding_cart.entity.FundingCart;
import com.example.final_project.src.funding_cart.entity.FundingCartItem;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundingCartRepository extends JpaRepository<FundingCart, Long> {
    // 유저의 장바구니 찾기
    Optional<FundingCart> findFundingCartByUser(User user);
}
