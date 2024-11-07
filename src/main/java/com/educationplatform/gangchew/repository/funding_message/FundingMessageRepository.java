package com.educationplatform.gangchew.repository.funding_message;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.entity.funding.Funding;
import com.educationplatform.gangchew.entity.funding.FundingCategory;
import com.educationplatform.gangchew.entity.funding.FundingParticipants;
import com.educationplatform.gangchew.entity.funding_message.FundingMessage;
import com.educationplatform.gangchew.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundingMessageRepository extends JpaRepository<FundingMessage, Long> {


    Optional<FundingMessage> findFundingMessagesByParticipant(FundingParticipants fundingParticipants);
    Optional<FundingMessage> findFundingMessagesByParticipantAndState(FundingParticipants fundingParticipants, Constant.FundingMessageState state);

}
