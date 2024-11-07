package com.educationplatform.gangchew.repository.funding;

import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding.FundingParticipants;
import com.educationplatform.gangchew.entity.funding.FundingPayment;
import com.educationplatform.gangchew.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundingPaymentRepository extends JpaRepository<FundingPayment, Long> {
    // 결제 정보와 유저 정보 1:1 매칭
    Optional<FundingPayment> findFundingPaymentByParticipant(User user);

    Optional<FundingPayment> findFundingPaymentByParticipantAndFunding(User user, Funding funding);
}
