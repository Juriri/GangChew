package com.example.final_project.src.funding.repository;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.funding.entity.FundingPayment;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundingPaymentRepository extends JpaRepository<FundingPayment, Long> {
    // 결제 정보와 유저 정보 1:1 매칭
    Optional<FundingPayment> findFundingPaymentByParticipant(User user);

    Optional<FundingPayment> findFundingPaymentByParticipantAndFunding(User user, Funding funding);
}
