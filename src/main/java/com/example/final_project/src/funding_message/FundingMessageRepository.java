package com.example.final_project.src.funding_message;

import com.example.final_project.common.Constant;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.funding_message.entity.FundingMessage;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundingMessageRepository extends JpaRepository<FundingMessage, Long> {


    Optional<FundingMessage> findFundingMessagesByParticipant(FundingParticipants fundingParticipants);
    Optional<FundingMessage> findFundingMessagesByParticipantAndState(FundingParticipants fundingParticipants, Constant.FundingMessageState state);

}
