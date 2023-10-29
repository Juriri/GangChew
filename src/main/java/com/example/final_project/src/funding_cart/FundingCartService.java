package com.example.final_project.src.funding_cart;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.jwt.JwtUtil;
import com.example.final_project.common.response.BaseResponseStatus;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.funding.repository.FundingParticipantsRepository;
import com.example.final_project.src.funding.repository.FundingRepository;
import com.example.final_project.src.funding_cart.entity.FundingCart;
import com.example.final_project.src.funding_cart.entity.FundingCartItem;
import com.example.final_project.src.funding_cart.model.FundingCartRes;
import com.example.final_project.src.funding_cart.repository.FundingCartItemRepository;
import com.example.final_project.src.funding_cart.repository.FundingCartRepository;
import com.example.final_project.src.user.UserRepository;
import com.example.final_project.src.user.entity.User;
import com.example.final_project.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
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
@Slf4j
public class FundingCartService {
    @Autowired
    private FundingCartRepository fundingCartRepository;
    @Autowired
    private FundingCartItemRepository fundingCartItemRepository;
    @Autowired
    private FundingRepository fundingRepository;
    @Autowired
    private FundingParticipantsRepository fundingParticipantsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public String addItemToUserCart(HttpServletRequest request, Long fundingId) {

        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if(jwtToken == null || jwtToken.isEmpty()){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 유저네임을 이용하여 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);

        // userId를 이용하여 User 찾기
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // fundingId를 이용하여 Funding 게시글 찾기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // User 객체를 이용하여 유저의 장바구니 객체 있는지 확인
        Optional<FundingCart> checkfundingCart = fundingCartRepository.findFundingCartByUser(user);
        FundingCart fundingCart = null;

        // 유저의 장바구니가 없다면 생성하기
        if(checkfundingCart.isEmpty()){
            // user의 장바구니 객체 생성 및 DB 저장
            fundingCart = new FundingCart(user);
            fundingCartRepository.save(fundingCart);
            fundingCart = fundingCartRepository.findFundingCartByUser(user).get();
        }
        // 유저의 장바구니가 있다면 장바구니 가져오기
        else {
            fundingCart = checkfundingCart.get();
        }

        // 기존 등록된 참여자인지 여부 확인
        Optional<FundingParticipants> optionalFundingParticipants = fundingParticipantsRepository.findFundingParticipantsByParticipantAndFunding(user, funding);

        // 참여자 중복으로 확인되면,
        if (optionalFundingParticipants.isPresent()) {
            FundingParticipants fundingParticipants = optionalFundingParticipants.get();
            Constant.FundingParticipantsState fundingParticipantsState = fundingParticipants.getState();

            // 참여한 펀딩 상태가 진행중
            if (fundingParticipantsState.equals(Constant.FundingParticipantsState.ACTIVE)) {
                throw new BaseException(PARTICIPANTS_USER);
            }
            // 이미 참여하여 펀딩 성공이 된 상태
            else if (fundingParticipantsState.equals(Constant.FundingParticipantsState.COMPLETE)) {
                throw new BaseException(PARTICIPANTS_COMPLETE_USER);
            }

            // 이미 참여하고 그 펀딩이 실패된 상태
            else if (fundingParticipantsState.equals(Constant.FundingParticipantsState.REFUND_NEEDED)) {
                throw new BaseException(PARTICIPANTS_REFUND_REQUEST_USER);
            }
            // 환불까지 완료한 상태
            else if (fundingParticipantsState.equals(Constant.FundingParticipantsState.REFUND_COMPLETE)) {
                throw new BaseException(PARTICIPANTS_REFUND_COMPLETE_USER);
            }
        }
        else {
            // fundingCart 객체를 이용하여 기존 장바구니 추가된 아이템인지 확인
            Optional<FundingCartItem> checkfundingItem = fundingCartItemRepository.findFundingCartItemByFundingAndFundingCart(funding, fundingCart);
            FundingCartItem fundingCartItem = null;

            // 이미 장바구니에 담겨진 아이템이면 예외처리
            if(checkfundingItem.isPresent()) {
                throw new BaseException(EXIST_CART_ITEM);
            }

            // 장바구니에 없는 아이템이라면, FundingCartItem 생성
            else {
                // fundingCartItem 생성
                fundingCartItem = new FundingCartItem(fundingCart, funding);
                // fundingCartItem  저장
                fundingCartItemRepository.save(fundingCartItem);
                // 생성한 객체 가져오기
                fundingCartItem = fundingCartItemRepository.findFundingCartItemByFundingAndFundingCart(funding, fundingCart).get();
            }
            // 아이템 수량 하나 추가
            fundingCartItem.addQuantity(1);

            return "장바구니 추가가 완료되었습니다.";
        }

        return null;
    }



    @Transactional(readOnly = true)
    public List<FundingCartRes> getItemsInUserCart(HttpServletRequest request) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 유저네임을 이용하여 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);
        // userId를 이용하여 User 찾기
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // User 객체를 이용하여 유저의 장바구니 목록 가져오기, 장바구니가 비어있으면 장바구니 생성
        Optional<FundingCart> checkfundingCart = fundingCartRepository.findFundingCartByUser(user);

        // 유저 장비구니가 없는 경우, 장바구니 생성
        if (checkfundingCart.isEmpty()){
            FundingCart fundingCart = new FundingCart(user);
            return null;
        } else {
            // 장바구니가 있으면 장바구니 아이템 가져오기
            FundingCart fundingCart = checkfundingCart.get();
            // 장바구니 아이템 리스트화
            List<FundingCartItem> fundingCartItemList = fundingCartItemRepository.findFundingCartItemByFundingCart(fundingCart);
            // 장바구니 아이템 비어있으면, null 리턴
            if (fundingCartItemList.isEmpty()) {
                return null;
            } else {
                // 장바구니 응답 모델 리스트 생성
                List<FundingCartRes> fundingCartRes = new ArrayList<>();
                for (FundingCartItem fundingCartItem : fundingCartItemList) {
                    // 응답 모델 리스트에 결과 하나씩 저장
                    fundingCartRes.add(new FundingCartRes(fundingCartItem));
                }

                return fundingCartRes;
            }
        }
    }


    // 장바구니 아이템 수량 변경
    @Transactional
    public String updateCartItemQuantity(HttpServletRequest request, String action, Long fundingId) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 유저네임을 이용하여 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);
        log.info("service username={}", username);
        // userId를 이용하여 User 찾기
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // fundingId를 이용하여 funding 객체 찾기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // user를 이용하여 fundingcart 객체 찾기
        FundingCart fundingCart = fundingCartRepository.findFundingCartByUser(user)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CART));

        // 장바구니의 해당 펀딩 아이템 객체 찾기
        FundingCartItem fundingCartItem = fundingCartItemRepository.findFundingCartItemByFundingAndFundingCart(funding, fundingCart)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CART_ITEM));
        // action 유효성 판단
        try {
            switch (action) {
                case "plus", "minus" -> {
                    // 수량 더하기
                    if (action.equals("plus")) {
                        fundingCartItem.addQuantity(1);
                        return "수량 +1 완료하였습니다.";
                    }
                    // 수량 빼기
                    else {
                        fundingCartItem.minusQuantity(1);
                        return "수량 -1 완료하였습니다.";
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


    // 모든 장바구니 아이템 삭제
    @Transactional
    public String deleteAllItemsInUserCart(HttpServletRequest request) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 유저네임을 이용하여 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);
        log.info("service username={}", username);
        // userId를 이용하여 User 찾기
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // user를 이용하여 fundingcart 객체 찾기
        FundingCart fundingCart = fundingCartRepository.findFundingCartByUser(user)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CART));

        // 장바구니의 펀딩 아이템 리스트 저장
        List<FundingCartItem> fundingCartItemList = fundingCartItemRepository.findFundingCartItemByFundingCart(fundingCart);
        if (fundingCartItemList.isEmpty()) {
            return "비어있는 장바구니입니다.";
        }

        // 펀딩 아이템 리스트에서 유저의 장바구니를 지닌 객체를 찾으면 아이템 삭제
        for (FundingCartItem fundingCartItem : fundingCartItemList) {
            if (fundingCartItem.getFundingCart().equals(fundingCart)) {
                fundingCartItemRepository.delete(fundingCartItem);
            }

        }
        return "모든 아이템이 삭제되었습니다.";
    }

    // 특정 장바구니 아이템 삭제
    @Transactional
    public String deleteItemInUserCart(HttpServletRequest request, Long fundingId) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 유저네임을 이용하여 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);
        log.info("service username={}", username);
        // userId를 이용하여 User 찾기
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 펀딩 id로 펀딩 객체 조회
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // user를 이용하여 fundingcart 객체 찾기
        FundingCart fundingCart = fundingCartRepository.findFundingCartByUser(user)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CART));

        // 장바구니의 해당 펀딩 아이템 객체 찾기
        FundingCartItem fundingCartItem = fundingCartItemRepository.findFundingCartItemByFundingAndFundingCart(funding, fundingCart)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CART_ITEM));

        // 해당 아이템만 장바구니 삭제 실행
        fundingCartItemRepository.delete(fundingCartItem);
        return "아이템 삭제가 완료되었습니다.";
    }


    // 특정 장바구니 아이템 삭제
    @Transactional
    public void removeFundingItemFromCartIfExist(User user, Funding funding) {
        // user를 이용하여 fundingcart 객체 찾기
        FundingCart fundingCart = fundingCartRepository.findFundingCartByUser(user)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CART));

        // 장바구니의 해당 펀딩 아이템 객체 찾기
        FundingCartItem fundingCartItem = fundingCartItemRepository.findFundingCartItemByFundingAndFundingCart(funding, fundingCart)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CART_ITEM));

        // 해당 아이템만 장바구니 삭제 실행
        fundingCartItemRepository.delete(fundingCartItem);
    }
}
