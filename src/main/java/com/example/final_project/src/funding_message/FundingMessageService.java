package com.example.final_project.src.funding_message;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.jwt.JwtUtil;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.funding.repository.FundingParticipantsRepository;
import com.example.final_project.src.funding.repository.FundingRepository;
import com.example.final_project.src.funding_message.entity.FundingMessage;
import com.example.final_project.src.funding_message.model.FundingMessageReq;
import com.example.final_project.src.funding_message.model.FundingMessageRes;
import com.example.final_project.src.user.UserRepository;
import com.example.final_project.src.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.final_project.common.response.BaseResponseStatus.*;

@Transactional
@RequiredArgsConstructor
@Service
@EnableScheduling
@Slf4j
public class FundingMessageService {

    @Autowired
    private FundingMessageRepository fundingMessageRepository;

    @Autowired
    private FundingParticipantsRepository fundingParticipantsRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;


    @Transactional
    public void sendRefundMessage (HttpServletRequest request,Long user_id, Long funding_id, Long participant_id) {

        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        //유저네임을 이용하여 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);

        //userId를 이용하여 user 찾기
        User user = userRepository.findUserByUsername(username).orElseThrow(() ->
                new BaseException(NOT_FIND_USER));

        //fundingId를 이용하여 funding 게시글 찾기
        Funding funding = fundingRepository.findFundingById(funding_id).orElseThrow(() ->
                new BaseException(NOT_FIND_FUNDING));

        //userId와 fundingId를 이용하여 참여자 찾기
        FundingParticipants fundingParticipants = fundingParticipantsRepository.findFundingParticipantsByParticipantAndFunding(user, funding).orElseThrow(() ->
                new BaseException(NOT_FIND_FUNDING_PARTICIPANTS));

        if (fundingParticipants.getState().equals(Constant.FundingParticipantsState.REFUND_NEEDED)) {
            
        } else if (fundingParticipants.getState().equals(Constant.FundingParticipantsState.REFUND_COMPLETE)) {
            
        }
    }

    //쪽지 읽으면 state ACTIVE -> CHECKED로 변경
    @Transactional
    public void CheckedFundingMessage(HttpServletRequest request, FundingParticipants fundingParticipants) {
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        FundingMessage fundingMessage= fundingMessageRepository.findFundingMessagesByParticipant(fundingParticipants)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_PARTICIPANTS));

        // 펀딩 메시지를 확인한 상태로 업데이트하고 저장
        fundingMessage.updateState(Constant.FundingMessageState.CHECKED);
    }

    // 특정 state 를 가진 취소 사유 모델 리스트 가져오기
    @Transactional
    public List<FundingMessageRes> getMessageAll (HttpServletRequest request, String participants_state, String current_message_state) {
        /*메세지 찾는방법 1. user 객체 가져오기 -> 2. fundingParticipants객체 가져오기 (user 객체 이용) -> 3.fundingMessage 객체 가져오기 (fundingParitipants 객체 사용)*/

        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 유저 객체 가져오기
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));


        // 펀딩 취소 응답 모델 반환 리스트 선언
        List<FundingMessageRes> fundingMessageResList = new ArrayList<>();

        // String state가 상수값으로 등록된 상태값인지 확인
        try {
             Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(participants_state.toUpperCase());

            switch (fundingParticipantsState) {
                case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                    // 로그인한 유저가 참여 중인 펀딩 리스트에서 state "refund needed" 를 가지는 펀딩 리스트 반환
                    List<FundingParticipants> fundingParticipantsList = fundingParticipantsRepository.findFundingParticipantsByParticipantAndState(user, fundingParticipantsState);

                    // 1. String current_message_state 가 null 이면 환불진행 중인 읽음 혹은 읽지않은 쪽지 리스트 모두 반환
                    if (current_message_state == null || current_message_state.isEmpty() ) {
                        // 로그인한 유저의 모든 펀딩 환불 리스트 가져오기
                        for (FundingParticipants fundingParticipants : fundingParticipantsList) {
                            Optional<FundingMessage> checkmsg = fundingMessageRepository.findFundingMessagesByParticipant(fundingParticipants);
                            if (checkmsg.isPresent()) {
                                fundingMessageResList.add(new FundingMessageRes(checkmsg.get()));
                            } else {
                                return null;
                            }
                        }
                    } else { // 2. 로그인한 유저가 참여 중인 펀딩 리스트에서 state 를 가지는 펀딩 리스트 반환
                        try {
                            // String state가 상수값으로 등록된 상태값인지 확인
                            Constant.FundingMessageState fundingMessageState = Constant.FundingMessageState.valueOf(current_message_state.toUpperCase());

                            switch (fundingMessageState) {
                                case ACTIVE, SEND, CHECKED -> {
                                    // 로그인한 유저가 확인하지않은 펀딩 환불 리스트 가져오기
                                    for (FundingParticipants fundingParticipants : fundingParticipantsList) {
                                        Optional<FundingMessage> fundingMessage = fundingMessageRepository.findFundingMessagesByParticipantAndState(fundingParticipants, fundingMessageState);

                                        if (fundingMessage.isPresent()) {
                                            // 응답 모델 리스트에 취소 사유 담은 객체로 반환
                                            fundingMessageResList.add(new FundingMessageRes(fundingMessage.get()));
                                        }


                                    }

                                }
                                default -> {
                                    throw new BaseException(INVALID_STATE);
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            throw new BaseException(INVALID_STATE);
                        }
                    }

                }
                default -> {
                    throw new BaseException(INVALID_STATE);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_STATE);
        }

        return fundingMessageResList;
    }

    // 특정 state 를 가진 취소 사유 모델 리스트 갯수 가져오기
    @Transactional
    public int getCountMessage (HttpServletRequest request, String participants_state, String message_state) {
        /*메세지 찾는방법 1. user 객체 가져오기 -> 2. fundingParticipants객체 가져오기 (user 객체 이용) -> 3.fundingMessage 객체 가져오기 (fundingParitipants 객체 사용)*/

        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 유저 객체 가져오기
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));


        // 펀딩 취소 응답 모델 반환 변수 선언
        int fundingMessageResCount = 0;

        // String state가 상수값으로 등록된 상태값인지 확인
        try {
            Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(participants_state.toUpperCase());

            switch (fundingParticipantsState) {
                case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                    // 로그인한 유저가 참여 중인 펀딩 리스트에서 state "refund needed" 를 가지는 펀딩 리스트 반환
                    List<FundingParticipants> fundingParticipantsList = fundingParticipantsRepository.findFundingParticipantsByParticipantAndState(user, fundingParticipantsState);

                    // String state가 상수값으로 등록된 상태값인지 확인
                    try {
                        Constant.FundingMessageState fundingMessageState = Constant.FundingMessageState.valueOf(message_state.toUpperCase());

                        switch (fundingMessageState) {
                            case ACTIVE, SEND, CHECKED -> {

                                // 로그인한 유저가 확인하지않은 펀딩 환불 리스트 가져오기
                                for (FundingParticipants fundingParticipants : fundingParticipantsList) {
                                    Optional<FundingMessage> fundingMessage = fundingMessageRepository.findFundingMessagesByParticipantAndState(fundingParticipants, Constant.FundingMessageState.SEND);
                                    // 조건으로 찾은 펀딩 메세지 객체가 존재하면, 카운트 1 증가
                                    if(fundingMessage.isPresent()) {
                                        // 응답 모델 리스트에 취소 사유 담은 객체로 반환
                                        fundingMessageResCount += 1;
                                    }

                                }


                            }
                            default -> {
                                throw new BaseException(INVALID_STATE);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        throw new BaseException(INVALID_STATE);
                    }
                }
                default -> {
                    throw new BaseException(INVALID_STATE);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_STATE);
        }

        return fundingMessageResCount;
    }


    // 쪽지 테이블 state 변경
    @Transactional
    public String updateMessageState (HttpServletRequest request, Long fundingId, String forward_state) {
        /*메세지 찾는방법 1. user 객체 가져오기 -> 2. fundingParticipants객체 가져오기 (user 객체 이용) -> 3.fundingMessage 객체 가져오기 (fundingParitipants 객체 사용)*/

        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 유저 객체 가져오기
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));


        // fundingId를 이용하여 funding 객체 찾기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));


        // 참여자 객체 찾기
        FundingParticipants fundingParticipants = fundingParticipantsRepository.findFundingParticipantsByParticipantAndFunding(user, funding)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_PARTICIPANTS));

        // String forward_state가 쪽지 state 상수값으로 등록된 상태값인지 확인
        try {
            Constant.FundingMessageState fundingMessageState = Constant.FundingMessageState.valueOf(forward_state.toUpperCase());

            switch (fundingMessageState) {
                case ACTIVE, SEND, CHECKED -> {
                    // 유저의 참여 정보를 가지고 있는 쪽지 객체 가져오기
                    FundingMessage fundingMessage = fundingMessageRepository.findFundingMessagesByParticipant(fundingParticipants)
                            .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_PARTICIPANTS));

                    fundingMessage.updateState(fundingMessageState);
                    return "변경이 완료되었습니다.";
                }
                default -> {
                    throw new BaseException(INVALID_STATE);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_STATE);
        }
    }
}
