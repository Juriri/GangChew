package com.example.final_project.src.funding.repository;

import com.example.final_project.common.Constant;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingComment;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FundingParticipantsRepository extends JpaRepository<FundingParticipants, Long> {
    // USER 객체, 펀딩 객체 참조하여 해당 게시글에 참여중인 참여자 찾기
    Optional<FundingParticipants> findFundingParticipantsByParticipantAndFunding(User user, Funding funding);
    // 펀딩 객체 참조하여 해당 게시글에 참여중인 모든 참여자 찾기
    List<FundingParticipants> findFundingParticipantsByFunding(Funding funding);

    // 유저 객체 참조하여 해당 게시글에 참여중인 모든 참여자 찾기
    List<FundingParticipants> findFundingParticipantsByParticipant(User user);


    // 상태값 참조하여 해당 게시글에 참여중인 모든 참여자 찾기
    List<FundingParticipants> findFundingParticipantsByState(Constant.FundingParticipantsState fundingParticipantsState);

    // 펀딩객체와 상태값 참조하여 참여 리스트 반환
    List<FundingParticipants> findFundingParticipantsByFundingAndState(Funding funding, Constant.FundingParticipantsState fundingParticipantsState);

    // USER 객체, 펀딩 참여 상태 참조하여 해당 게시글에 참여중인 모든 참여자 찾기
    List<FundingParticipants> findFundingParticipantsByParticipantAndState(User user, Constant.FundingParticipantsState fundingParticipantsState);

    // USER 객체, 펀딩 참여 상태, 펀딩 객체 참조하여 해당 게시글에 참여중인 모든 참여자 찾기
    Optional<FundingParticipants> findFundingParticipantsByParticipantAndStateAndFunding(User user, Constant.FundingParticipantsState fundingParticipantsState, Funding funding);

    // 특정 펀딩에 참여중인 유저수 반환
    int countFundingParticipantsByFunding(Funding funding);

    // LocalDateTime 현재시간과 마감시간 받아 해당 시간동안의 createAt을 가지고 있는 참여 리스트 수 반환
    int countFundingParticipantsByCreatedAtBetweenAndFundingAndState(LocalDateTime currenttime, LocalDateTime deadline, Funding funding, Constant.FundingParticipantsState state);


    List<FundingParticipants> findFundingParticipantsByStateAndFunding(Constant.FundingParticipantsState fundingParticipantsState, Funding funding);
}


