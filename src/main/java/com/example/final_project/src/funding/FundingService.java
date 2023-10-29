package com.example.final_project.src.funding;

import com.example.final_project.src.student_request.StudentRepository;
import com.example.final_project.src.student_request.dto.CommentResDto;
import com.example.final_project.src.student_request.dto.CommentUpdateReqDto;
import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.jwt.JwtUtil;
import com.example.final_project.common.response.BaseResponse;
import com.example.final_project.src.funding.entity.*;
import com.example.final_project.src.funding.model.*;
import com.example.final_project.src.funding.repository.*;
import com.example.final_project.src.funding_message.FundingMessageRepository;
import com.example.final_project.src.funding_message.entity.FundingMessage;
import com.example.final_project.src.post_like.PostLikeRepository;
import com.example.final_project.src.post_like.entity.PostLike;
import com.example.final_project.src.post_like.model.GetAllRes;
import com.example.final_project.src.student_request.entity.StudentComment;
import com.example.final_project.src.student_request.entity.StudentRequest;
import com.example.final_project.src.user.UserRepository;
import com.example.final_project.src.user.entity.User;
import com.example.final_project.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.final_project.common.Constant.FundingState.*;
import static com.example.final_project.common.response.BaseResponseStatus.*;

import javax.servlet.http.HttpServletRequest;

// Service Create, Update, Delete 의 로직 처리
@Transactional
@RequiredArgsConstructor
@Service
@EnableScheduling
@Slf4j
public class FundingService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Autowired
    private FundingCategoryRepository fundingCategoryRepository;

    @Autowired
    private FundingCommentRepository fundingCommentRepository;

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private FundingParticipantsRepository fundingParticipantsRepository;

    @Autowired
    private FundingPaymentRepository fundingPaymentRepository;

    @Autowired
    private FundingMessageRepository fundingMessageRepository;

    @Autowired
    private JwtUtil jwtUtil;


    // 공통코드:: 토큰으로부터 유저 객체 추출
    private User getCurrentUserFromToken(HttpServletRequest request) {
        // 요청에서 헤더의 토큰 부분 분리
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        // 토큰이 null이면 예외처리
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        // null이 아니면 유저 객체 변환
        String username = jwtUtil.extractUsername(jwtToken);
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
    }

    // state가 DELETE인 펀딩 게시글 제외
    private List<Funding> getActiveFundingList(List<Funding> fundingList) {
        List<Funding> activeFundingList = new ArrayList<>();
        for (Funding funding : fundingList) {
            if (!funding.getState().equals(DELETE)) {
                activeFundingList.add(funding);
            }
        }
        return activeFundingList;
    }


    @Transactional
    public FundingInsertRes createFunding(HttpServletRequest request, FundingInsertReq fundingInsertReq) throws SQLException {
        // funding 객체를 DB에 저장
        Funding saveFunding = fundingRepository.save(fundingInsertReq.toEntity());

        // 1-1. category Id를 변수에 저장
        Long categoryId = fundingInsertReq.getCategory_id();
        // 1-2. catefory Id를 이용하여 카테고리명 객체
        FundingCategory category = fundingCategoryRepository.findFundingCategoriesById(categoryId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CATEGORY));

        // 1-3. 조회한 카테고리 객체를 funding 객체에 저장
        saveFunding.updateCategory(category);

        // 2-1. 토큰으로부터 유저 객체 추출
        User user = getCurrentUserFromToken(request);
        // 2-2. 조회한 유저 객체를 funding 객체에 저장
        saveFunding.updateWriter(user);

        // 게시글 생성 완료 응답 모델 반환
        return new FundingInsertRes(saveFunding);
    }


    @Transactional
    public String updateFunding(HttpServletRequest request, Long fundingId, FundingUpdateReq fundingUpdateReq) throws SQLException {
        // 헤더에서 jwt 토큰 추출하여 유저 객체 조회
        User user = getCurrentUserFromToken(request);

        // fundingId를 이용하여 funding 객체 찾기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // fundingcategoryId를 이용하여 funding 카테고리 객체 가져오기
        FundingCategory fundingCategory = fundingCategoryRepository.findFundingCategoriesById(fundingUpdateReq.getCategory_id())
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CATEGORY));

        // 업데이트 폼 작성 내용 DB 저장
        funding.updateAll(fundingUpdateReq, fundingCategory);

        // 게시글 생성 완료 응답
        return "수정이 완료되었습니다.";
    }



    @Transactional
    public String deleteFunding(HttpServletRequest request, Long fundingId) throws SQLException {
        // 작성자와 현재 로그인 유저 일치 확인
        // 헤더에서 jwt 토큰 추출하여 유저 객체 조회
        User user = getCurrentUserFromToken(request);
        String username = user.getUsername();

        // fundingId를 이용하여 funding 객체 찾기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // 로그인 유저와 작성자 일치
        if (username.equals(funding.getWriter().getUsername())) {

            // 게시글 삭제 실행
            fundingRepository.delete(funding);

            // 게시글 생성 완료 응답 반환
            return "게시글 삭제가 완료 되었습니다.";
        }
        // 로그인 유저와 작성자 불일치
        else {
            throw new BaseException(DENY_USER);
        }
    }


    @Transactional(readOnly = true)
    public List<Funding> getAllFundings() {
        List<Funding> fundings =  fundingRepository.findAllBy();

        // 게시글 리스트 반환
        return fundings;
    }

    @Transactional
    public String updateFundingStateOnly(Long fundingId, String forward_state) {
        // fundingId를 이용하여 funding 객체 찾기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

       /* Funding 테이블의 상태(state):
        ACTIVE: 게시글 등록 상태
        IN_PROGRESS: 펀딩 진행 상태 (수정불가)
        DELETE: 삭제 상태
        COMPLETE: 펀딩 성공 상태
        FAIL: 펀딩 실패 상태
        */

        try {
            Constant.FundingState fundingState = Constant.FundingState.valueOf(forward_state.toUpperCase());

            switch (fundingState) {
                case ACTIVE, IN_PROGRESS, DELETE, COMPLETE, FAIL -> {
                    // 게시글 삭제 요청이 들어오면 해당 게시글의 댓글 삭제 먼저 처리
                    if (fundingState.equals(DELETE)) {
                        List<FundingComment> fundingCommentList = fundingCommentRepository.findFundingCommentsByFunding(funding);

                        for (FundingComment fundingComment : fundingCommentList) {
                            fundingComment.updateState(Constant.FundingCommentState.DELETE);
                        }
                    } // 게시글 이외 상태 변경 요청이면 댓글 자동 ACTIVE 변경
                    else {
                        List<FundingComment> fundingCommentList = fundingCommentRepository.findFundingCommentsByFunding(funding);

                        for (FundingComment fundingComment : fundingCommentList) {
                            fundingComment.updateState(Constant.FundingCommentState.ACTIVE);
                        }
                    }

                    // 상수값에 등록된 state면 업데이트 실행
                    funding.updateState(fundingState);
                    // 삭제 후 쪽지 발송
                    List<FundingParticipants> participants = fundingParticipantsRepository.findFundingParticipantsByFunding(funding);
                    for(FundingParticipants fundingParticipants : participants) {
                        Optional<FundingMessage> fundingMessage = fundingMessageRepository.findFundingMessagesByParticipant(fundingParticipants);
                        if (fundingMessage.isPresent()) {
                            fundingMessage.get().updateState(Constant.FundingMessageState.SEND);
                            fundingMessage.get().updateReason("펀딩 게시글 삭제");
                        } else {
                            // 환불 대상 등록
                            fundingParticipants.updateFundingParticipantsState(Constant.FundingParticipantsState.REFUND_NEEDED);
                            // 환불 메세지 등록
                            FundingMessage new_fundingMessage = new FundingMessage(fundingParticipants, "펀딩 게시글 삭제");
                            fundingMessageRepository.save(new_fundingMessage);
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


        return "처리 완료되었습니다.";
    }

    // 내가 좋아요 누른 펀딩과 학생 좋아요 누른 리스트 반환
    @Transactional(readOnly = true)
    public List<GetAllRes> getAllLikePost(HttpServletRequest request) {
        // 반환 리스트 선언
        List<GetAllRes> resList = new ArrayList<>();

        // 헤더에서 jwt 토큰 추출하여 유저 객체 조회
        User user = getCurrentUserFromToken(request);

        // 내가 좋아요 누른 리스트
        List<PostLike> postLikeList = postLikeRepository.findPostLikesByUser(user);

        for(PostLike postLike : postLikeList) {
            // 펀딩 좋아요이면 펀딩 title 추가
            if (postLike.getTargetType().equals(Constant.TargetType.FUNDING)) {
                Long fundingId = postLike.getTargetId();
                Funding funding = fundingRepository.findFundingById(fundingId)
                        .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

                // 상태가 DELETE가 아니면 응답 배열에 추가
                if (!funding.getState().equals(DELETE)) {
                    // 응답 모델 객체 생성
                    GetAllRes getAllRes = new GetAllRes(user, postLike.getTargetType().toString(), postLike.getTargetId(), funding);
                    // 응답 리스트에 추가
                    resList.add(getAllRes);
                }

            }
            else { // 학생 강의글 좋아요이면 펀딩 title 추가
                Long studentId = postLike.getTargetId();
                // studentId 이용하여 student request 객체 조회
                StudentRequest studentRequest = studentRepository.findStudentRequestById(studentId).orElseThrow(() ->
                        new BaseException(NOT_FIND_STUDENTREQUEST));

                if (!studentRequest.getState().equals(Constant.StudentRequestState.DELETE)) {
                    // 응답 모델 객체 생성
                    GetAllRes getAllRes = new GetAllRes(user, postLike.getTargetType().toString(), postLike.getTargetId(), studentRequest);
                    // 응답 리스트에 추가
                    resList.add(getAllRes);
                }
            }
        }

        // 게시글 리스트 반환
        return resList;
    }

    @Transactional(readOnly = true)
    public List<Funding> getFundingsBypage(List<Funding> fundings, int pageNum, int itemsPerPage) {
        // 페이지 번호가 0부터 시작하므로, 사용자 입력값인 pageNum을 1 빼줍니다.
        int page = pageNum - 1;

        // 전체 게시물 수
        int totalItems = fundings.size();

        // 시작 인덱스 계산
        int startIndex = page * itemsPerPage;

        // 끝 인덱스 계산
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        // 페이지네이션을 적용하여 해당 범위의 게시물 추출
        List<Funding> paginatedFundings = fundings.subList(startIndex, endIndex);

        // 상태가 DELETE가 아닌 배열만 출력
        return getActiveFundingList(paginatedFundings);
        /*return paginatedFundings;*/
    }

    // 펀딩 리스트 중 좋아요 누른 게시글 표시 기능
    @Transactional(readOnly = true)
    public List<Boolean> getLikedFundingList(HttpServletRequest request, List<Funding> fundingList) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        /*log.info("getLikedFundingList jwtToken ={}", jwtToken);*/

        // 로그아웃 상태로 인해 좋아요 기능 처리 불가
        if (jwtToken == null || jwtToken.isEmpty()){
            // 반환할 리스트 확인 List<Boolean> boolList
            List<Boolean> booleanList = new ArrayList<>();//fundingList 길이만큼 생성
            // 매개변수로 받은 전체 펀딩 게시글에서
            for (Funding funding : fundingList) {
                booleanList.add(false);
            }
            // 게시글 리스트 반환
            return booleanList;
        }


        // 유저네임을 이용하여 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 반환할 리스트 확인 List<Boolean> boolList
        List<Boolean> booleanList = new ArrayList<>();//fundingList 길이만큼 생성

        // 매개변수로 받은 전체 펀딩 게시글에서
        for (Funding funding : fundingList) {
            // 게시글 id
            Long funding_id = funding.getId();
            // 유저 id, 게시글 id에 해당하는 좋아요 누른 펀딩 게시글 확인
            Optional<PostLike> postLike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.FUNDING, funding_id);

            if(postLike.isPresent()){
                booleanList.add(true);
            }
            else {
                booleanList.add(false);
            }
        }
        // 게시글 리스트 반환
        return booleanList;
    }



    @Transactional(readOnly = true)
    public List<Funding> getFundingsByUser(Long userId) {
        // userId를 이용하여 user 객체 가져오기
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 해당 user 객체를 가진 펀딩 게시글 리스트 찾기
        List<Funding> fundings =  fundingRepository.findFundingsByWriter(user);

        // 게시글 리스트 반환
        return fundings;
    }

    @Transactional(readOnly = true)
    public List<Funding> getFundingsByCategory(Long categoryId) {

        // fundingId를 이용하여 funding 카테고리 객체 가져오기
        FundingCategory fundingCategory = fundingCategoryRepository.findFundingCategoriesById(categoryId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CATEGORY));

        // 해당 카테고리를 지닌 게시글 리스트 반환
        return fundingRepository.findFundingsByFundingCategory(fundingCategory);
    }



    @Transactional(readOnly = true)
    public Funding getFundingByFunding(Long fundingId) {
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // View Count 1 증가
        funding.addViewCount(1);

        // 게시글 반환
        return funding;
    }

    @Transactional(readOnly = true)
    public List<Funding> getFundingsByKeyword(String keyword) {

        // 결과를 저장할 리스트 생성
        List<Funding> combinedFundings = new ArrayList<>();

        // 1. 제목에서 키워드와 부분 일치하는 게시글 목록 가져오기
        List<Funding> fundings1 = fundingRepository.findFundingsByTitleContaining(keyword);

        combinedFundings.addAll(getActiveFundingList(fundings1));

        // 2. 부제목에서 키워드와 부분 일치하는 게시글 목록 가져오기
        List<Funding> fundings2 = fundingRepository.findFundingsBySubtitleContaining(keyword);
        combinedFundings.addAll(getActiveFundingList(fundings2));

        // 3. 카테고리명에서 키워드와 부분 일치하는 카테고리 객체 목록 가져오기
        List<FundingCategory> categories = fundingCategoryRepository.findFundingCategoriesByCategoryNameContaining(keyword);

        // 키워드를 지닌 카테고리명 검색 결과가 empty가 아닐 경우,
        if (!categories.isEmpty()) {
            for (FundingCategory category : categories){
                // 해당 카테고리에 속한 게시글 목록 가져오기
                List<Funding> fundings3 = fundingRepository.findFundingsByFundingCategory(category);
                combinedFundings.addAll(getActiveFundingList(fundings3));
            }
        }

        // 중복 제거
        Set<Funding> uniqueFundings = new HashSet<>(combinedFundings);
        combinedFundings.clear();
        combinedFundings.addAll(uniqueFundings);

        // 게시글 리스트 반환
        return combinedFundings;
    }


    @Transactional
    public String updatePostLike (HttpServletRequest request, Long fundingId) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

        if(jwtToken == null || jwtToken.isEmpty()){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }


        String username = jwtUtil.extractUsername(jwtToken);
        // 유저 id로 유저 객체 조회
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 펀딩 id로 펀딩 객체 조회
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // user와 funding 객체를 이용하여 PostLike 찾기
        Optional<PostLike> checkPostLike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.FUNDING, fundingId);

        // 이미 좋아요를 누른 경우, 좋아요 삭제
        if (checkPostLike.isPresent()) {
            // 좋아요 테이블에 해당 객체 삭제
            postLikeRepository.delete(checkPostLike.get());
            // 좋아요 카운트 -1
            funding.minusLikeCount(1);
            return "좋아요 취소가 완료하였습니다.";
        }
        // 좋아요가 없는 경우, 좋아요 등록
        else {
            // 좋아요 테이블에 해당 객체 등록
            PostLike postLike = new PostLike(user, Constant.TargetType.FUNDING, fundingId);
            postLikeRepository.save(postLike);
            // 좋아요 카운트 +1
            funding.addLikeCount(1);
            return "좋아요가 등록되었습니다.";
        }
    }

    @Transactional(readOnly = true)
    public Long countFundingView (Long fundingId) {
        // fundingId를 이용하여 user 객체 가져오기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        return funding.getViewCount();
    }

    @Transactional(readOnly = true)
    public Long countFundingLike (Long fundingId) {
        // fundingId를 이용하여 user 객체 가져오기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        return funding.getLikeCount();
    }

    @Transactional(readOnly = true)
    public Long countFundingComment (Long fundingId) {
        // fundingId를 이용하여 user 객체 가져오기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        return fundingCommentRepository.countFundingCommentByFunding(funding);
    }


   /* @Transactional(readOnly = true)
    public List<Funding> SortedByKeyword (String status, Long geo_num) {
        if (status != null){
            Constant.FundingState fundingState = Constant.FundingState.valueOf(status.toUpperCase());
            return fundingRepository.findFundingsByState(fundingState);
        }

        else if (geo_num != null) {
            // 지역 넘버를 이용하여 대한민국 시 가져오기
            City city = cityRepository.findCitiesByParentId(geo_num)
                    .orElseThrow(() -> new BaseException(NOT_FIND_CITY));

            // 지역 네임을 지닌 펀딩 게시글 리스트 반환
            return fundingRepository.findFundingsByLocation(city.getName());
        }
        return null;
    }*/


    @Transactional(readOnly = true)
    public List<Funding> OrderByKeyword (List<Funding> fundingList, String condition) {
        if (condition == null || condition.equals("newest")) {
            // 최신순 정렬
            fundingList.sort(Comparator.comparing(Funding::getCreatedAt).reversed());
        } else if (condition.equals("deadline")) {
            // 마감임박순 정렬
            LocalDateTime currentTime = LocalDateTime.now();
            fundingList.sort(Comparator.comparing(funding -> {
                LocalDateTime deadline = funding.getDeadline();
                return (deadline != null && deadline.isAfter(currentTime)) ? deadline : currentTime;
            }));
        } else if (condition.equals("mostLike")) {
            // 좋아요순 정렬
            fundingList.sort(Comparator.comparing(Funding::getLikeCount).reversed());
        } else {
            throw new BaseException(NOT_FIND_FUNDING);
        }
        return getActiveFundingList(fundingList);

    }


    @Transactional(readOnly = true)
    public List<Funding> OrderByState (List<Funding> fundingList, String state) {
        if (state == null || state.isEmpty()) {
            // 상태가 지정되지 않은 경우, 원래 리스트를 그대로 반환
            return getActiveFundingList(fundingList);
        }

        else {
            // String state가 상수값으로 등록된 상태값인지 확인
            try {
                Constant.FundingState fundingState = Constant.FundingState.valueOf(state.toUpperCase());

                switch (fundingState) {
                    case ACTIVE, IN_PROGRESS, DELETE, COMPLETE, FAIL -> {
                        // 입력된 상태값에 따라 필터링
                        List<Funding> filteredList = new ArrayList<>();
                        for (Funding funding : fundingList) {
                            if (funding.getState().equals(fundingState)) {
                                // 매개변수 state와 funding state가 일치하면 저장
                                filteredList.add(funding);
                            }
                        }
                        return getActiveFundingList(filteredList);
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


    @Transactional(readOnly = true)
    public List<Funding> OrderByWriter (HttpServletRequest request) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if(jwtToken == null) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 해당 유저가 작성한 리스트 가져오기
        return getActiveFundingList(fundingRepository.findFundingsByWriter(user));

    }

    @Transactional
    public int calculateAchievementRate (Long funding_id) {
        // fundingId를 이용하여 user 객체 가져오기
        Funding funding = fundingRepository.findFundingById(funding_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // 펀딩 게시글의 목표 인원
        Long goal = funding.getGoal();
        // 현재 참여 인원
        int currentPeople = fundingParticipantsRepository.countFundingParticipantsByFunding(funding);

        // 달성률 퍼센티지 반환 (반올림)
        return Math.round((float) currentPeople / goal * 100);
    }

    @Transactional
    public List<FundingRes> FundingTransferToFundingRes (HttpServletRequest request, List<Funding> fundingList, int totalItems) {
        // 좋아요 리스트 추출
        List<Boolean> booleanList = getLikedFundingList(request, fundingList);

        User user;
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null){
            user = null;
        } else {
            String username = jwtUtil.extractUsername(jwtToken);
            user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        }

        List<FundingRes> responseList = IntStream.range(0, fundingList.size())
                .mapToObj(i -> {
                    Funding funding = fundingList.get(i);
                    String writer = funding.getWriter().getUsername();
                    Boolean liked = booleanList.get(i);
                    int achievementrate = calculateAchievementRate(funding.getId());
                    String categoryname = fundingList.get(i).getFundingCategory().getCategoryName();
                    return new FundingRes(funding, liked, achievementrate, totalItems, writer, categoryname);
                })
                .collect(Collectors.toList());

        return responseList;
    }


    @Transactional
    public FundingDetailRes FundingTransferToFundingDetailRes (HttpServletRequest request, Long funding_id) {
        // fundingId를 이용하여 funding 객체 가져오기
        Funding funding = fundingRepository.findFundingById(funding_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        User user;
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

        // 좋아요 변수 선언
        boolean liked = false;
        // 펀딩작성자와 로그인 유저 일치 확인 변수 선언
        boolean isLoginUser = false;

        // jwtToken null 값이면, 미로그인 상태로 간주
        if(jwtToken == null || jwtToken.isEmpty()){
            // 유저 null 로 저장
            user = null;
        } else {
            // jwtToken 값이 있으면 user 객체 조회
            String username = jwtUtil.extractUsername(jwtToken);
            user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));

            // 게시글 작성자가 현재 로그인한 유저와 같으면
            if(funding.getWriter().getUsername().equals(user.getUsername())) {
                isLoginUser = true;
            }

            // 좋아요 누른 유저인지 DB 테이블 조회
            Optional<PostLike> checkPostLike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.FUNDING, funding_id);
            // 조회 결과가 있다면 true 반환
            if (checkPostLike.isPresent()) {
                liked = true;
            }
        }

        // 호출한 게시글 viewCount +1
        funding.addViewCount(1);

        // 달성률 계산
        int achievementrate = calculateAchievementRate(funding.getId());

        // 현재 참여 인원
        int participants = fundingParticipantsRepository.countFundingParticipantsByFunding(funding);

        return new FundingDetailRes(funding, funding.getFundingCategory(), funding.getWriter().getUsername(), liked, achievementrate, isLoginUser, participants);
    }

    @Transactional(readOnly = true)
    public List<FundingCategory> getCategoryAllList () {
        return fundingCategoryRepository.findAll();
    }


    @Transactional(readOnly = true)
    public FundingCategory getCategoryById (Long catogoryId) {

        return fundingCategoryRepository.findFundingCategoriesById(catogoryId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CATEGORY));
    }


    @Transactional(readOnly = true)
    public List<CommentResDto> getCommentByUser (HttpServletRequest request) {
        return null;
    }



    @Transactional(readOnly = true)
    public List<CommentResDto> getCommentByFunding (HttpServletRequest request, Long fundingId) {
        // fundingId를 이용하여 funding 객체 가져오기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        List<FundingComment> fundingCommentList = fundingCommentRepository.findFundingCommentsByFunding(funding);
        List<CommentResDto> commentResDtos = new ArrayList<>();

        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        // 로그인 상태가 아니면 댓글 응답 모델에 게시글 작성자와 로그인 유저 일치 여부 false 삽입
        if (jwtToken == null){
            for (FundingComment fundingComment : fundingCommentList) {
                commentResDtos.add(new CommentResDto(fundingComment, false ));
            }
            return commentResDtos;
        }
        else { // 로그인 상태면 게시글 작성자와 로그인 유저 일치 확인 진행

            // 로그인한 유저 객체
            String username = jwtUtil.extractUsername(jwtToken);
            User user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));

            for (FundingComment fundingComment : fundingCommentList) {
                // 댓글 작성자
                String writer = fundingComment.getWriter().getUsername();

                // 댓글 작성가와 로그인 유저가 같으면 댓글 반환 모델에 true 추가
                if (writer.equals(user.getUsername())) {
                    commentResDtos.add(new CommentResDto(fundingComment, true));
                } else { // 같지않으면, false 추가
                    commentResDtos.add(new CommentResDto(fundingComment, false ));
                }
            }
            return commentResDtos;
        }
    }


    @Transactional
    public String insertCommentByUser (HttpServletRequest request, CommentInsertReq commentInsertReq) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

        if(jwtToken == null || jwtToken.isEmpty()){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // jwt 토큰을 이용하여 username 추출
        String username = jwtUtil.extractUsername(jwtToken);
        // username을 이용하여 user 찾기
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 펀딩 고유 id 추출
        Long funding_id = commentInsertReq.getFunding_id();
        // 펀딩 id를 이용하여 funding 객체 찾기
        Funding funding = fundingRepository.findFundingById(funding_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // 댓글 생성 모델에 펀딩 객체 업데이트
        commentInsertReq.setFunding(funding);
        // 댓글 생성 모델에 유저 객체 업데이트
        commentInsertReq.setUser(user);

        // 댓글 모델 entity화 (String content 저장)
        FundingComment fundingComment = commentInsertReq.toEntity();

        // 댓글 모델 DB 저장 완료
        fundingCommentRepository.save(fundingComment);

        return "댓글 쓰기가 완료되었습니다.";

    }

    @Transactional
    public String updateFundingComment(HttpServletRequest request, CommentUpdateReqDto commentUpdateReqDto){
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if(jwtToken == null) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 댓글 고유 id로 StudentComment 객체 찾기
        Long comment_id = commentUpdateReqDto.getComment_id();

        FundingComment fundingComment = fundingCommentRepository.findFundingCommentById(comment_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_STUDENTREQUEST_COMMENT));

        fundingComment.updateContent(commentUpdateReqDto.getContent());

        return "댓글이 수정되었습니다.";
    }


    @Transactional
    public String deleteFundingComment(HttpServletRequest request, Long comment_id){
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if(jwtToken == null) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 댓글 객체 조회
        FundingComment fundingComment = fundingCommentRepository.findFundingCommentById(comment_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_STUDENTREQUEST_COMMENT));

        // 해당 댓글 객체 삭제 실행
        fundingCommentRepository.delete(fundingComment);

        return "댓글을 삭제했습니다.";
    }

    @Transactional(readOnly = true)
    public List<FundingParticipants> getParticipantsByLoginUserAndStateAndFunding (HttpServletRequest request, String state, Long funding_id) {

        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

        if(jwtToken == null || jwtToken.isEmpty()){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // jwt 토큰을 이용하여 username 추출
        String username = jwtUtil.extractUsername(jwtToken);
        // username을 이용하여 user 찾기
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));


        // 펀딩 참여자 상태 및 펀딩 게시글 id가 null이 아닐때, (user_id, funding_id, 참여자 state로 검색)
        if (state != null && funding_id != null) {
            // fundingId를 이용하여 funding 객체 가져오기
            Funding funding = fundingRepository.findFundingById(funding_id)
                    .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

            // String state가 상수값으로 등록된 상태값인지 확인
            try {
                Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(state.toUpperCase());

                switch (fundingParticipantsState) {
                    case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                        // 유저가 참여중인 펀딩의 state 조건으로 참여자 상태 찾기
                        Optional<FundingParticipants> checkfundingParticipant = fundingParticipantsRepository.findFundingParticipantsByParticipantAndStateAndFunding(user, fundingParticipantsState, funding);
                        // 결과가 있으면 리스트화하여 반환, 없으면 null 반환
                        return checkfundingParticipant.map(Collections::singletonList).orElse(null);
                    }
                    default -> {
                        throw new BaseException(INVALID_STATE);
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new BaseException(INVALID_STATE);
            }
        } else {
            // 펀딩 참여자 상태 값이 null이 아닐때, (user_id, 참여자 state로 검색)
            if (state != null) {

                // String state가 상수값으로 등록된 상태값인지 확인
                try {
                    Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(state.toUpperCase());

                    switch (fundingParticipantsState) {
                        case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                            return fundingParticipantsRepository.findFundingParticipantsByParticipantAndState(user, fundingParticipantsState);
                        }
                        default -> {
                            throw new BaseException(INVALID_STATE);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new BaseException(INVALID_STATE);
                }
            }
            // 펀딩 id가 null이 아닐때, (user_id, funding id로 검색)
            else if (funding_id != null) {
                // fundingId를 이용하여 funding 객체 가져오기
                Funding funding = fundingRepository.findFundingById(funding_id)
                        .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

                // funding, user 객체를 활용하여 펀딩 참여자 리스트 가져오기
                Optional<FundingParticipants> optionalFundingParticipants = fundingParticipantsRepository.findFundingParticipantsByParticipantAndFunding(user, funding);

                if (optionalFundingParticipants.isPresent()) {
                    List<FundingParticipants> resultList = new ArrayList<>();
                    resultList.add(optionalFundingParticipants.get());
                    return resultList;
                } else {
                    // Optional에 값이 없는 경우 빈 리스트 반환
                    return Collections.emptyList();
                }

            } else {
                // user_id는 필수이므로, user_id만 넘어온 경우에는 userid로만 처리
                log.info("user= {}", user);
                return fundingParticipantsRepository.findFundingParticipantsByParticipant(user);
            }
        }
    }



    // user_id , state , funding_id 검색 옵션
    @Transactional(readOnly = true)
    public List<FundingParticipants> getParticipantsByUserAndStateAndFunding(Long user_id, String state, Long funding_id) {
        //1. user id 가 null 이 아닌 경우
        // 1-1. state가 null이고 funding_id가 null 인 경우
        // 1-2. state가 null이 아니고 funding_id가 null 인 경우
        // 1-1. state가 null이고 funding_id가 null이 아닌 경우

        //2. state 가 null 이 아닌 경우
        // 2-1. user_id null이고 funding_id가 null 인 경우
        // 2-2. user_id null이 아니고 funding_id가 null 인 경우
        // 2-1. user_id null이고 funding_id가 null이 아닌 경우

        //3. funding_id 가 null 이 아닌 경우
        // 2-1. user_id null이고 state null 인 경우
        // 2-2. user_id null이 아니고 state null 인 경우
        // 2-1. user_id null이고 state null이 아닌 경우


        //1. user id 가 null 이 아닌 경우
        if (user_id != null) {
            // userid를 이용하여 user 객체 찾기
            User user = userRepository.findUserById(user_id)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));

            // user_id만 넘어온 경우에는 userid로만 처리
            if (state == null && funding_id == null) {
                return fundingParticipantsRepository.findFundingParticipantsByParticipant(user);
            }

            // user_id와 state를 이용하여 리스트 반환
            else if (state != null && funding_id == null) {
                try {
                    Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(state.toUpperCase());

                    switch (fundingParticipantsState) {
                        case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                            return fundingParticipantsRepository.findFundingParticipantsByParticipantAndState(user, fundingParticipantsState);
                        }
                        default -> {
                            throw new BaseException(INVALID_STATE);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new BaseException(INVALID_STATE);
                }
            }

            // user_id와 state, funding_id 를 이용하여 리스트 반환
            else {
                try {
                    Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(state.toUpperCase());
                    // fundingId를 이용하여 funding 객체 가져오기
                    Funding funding = fundingRepository.findFundingById(funding_id)
                            .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

                    switch (fundingParticipantsState) {
                        case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                            // 해당 조건으로 참여자 상태가 있는지 확인
                            Optional<FundingParticipants> checkfunding = fundingParticipantsRepository.findFundingParticipantsByParticipantAndStateAndFunding(user, fundingParticipantsState, funding);
                            // 있으면 리스트화하여 반환, 없으면 null 반환
                            return checkfunding.map(Collections::singletonList).orElse(null);
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

        //2. state 가 null 이 아닌 경우
        if (state != null) {
            Constant.FundingParticipantsState fundingParticipantsState = null;
            try {
                switch (Constant.FundingParticipantsState.valueOf(state.toUpperCase())) {
                    case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                        fundingParticipantsState = Constant.FundingParticipantsState.valueOf(state.toUpperCase());
                    }
                    default -> {
                        throw new BaseException(INVALID_STATE);
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new BaseException(INVALID_STATE);
            }

            if (user_id == null && funding_id == null) {
                return fundingParticipantsRepository.findFundingParticipantsByState(fundingParticipantsState);
            } else if (user_id != null && funding_id == null) {
                // userid를 이용하여 user 객체 찾기
                User user = userRepository.findUserById(user_id)
                        .orElseThrow(() -> new BaseException(NOT_FIND_USER));

                return fundingParticipantsRepository.findFundingParticipantsByParticipantAndState(user, fundingParticipantsState);
            } else {
                // fundingId를 이용하여 funding 객체 가져오기
                Funding funding = fundingRepository.findFundingById(funding_id)
                        .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

                return fundingParticipantsRepository.findFundingParticipantsByFundingAndState(funding, fundingParticipantsState);
            }
        }

        //3. funding_id 가 null 이 아닌 경우
        if (funding_id != null) {
            // fundingId를 이용하여 funding 객체 가져오기
            Funding funding = fundingRepository.findFundingById(funding_id)
                    .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

            if (user_id == null && state == null) {
                return fundingParticipantsRepository.findFundingParticipantsByFunding(funding);
            } else if (user_id != null && state == null) {
                // userid를 이용하여 user 객체 찾기
                User user = userRepository.findUserById(user_id)
                        .orElseThrow(() -> new BaseException(NOT_FIND_USER));

                FundingParticipants fundingParticipants = fundingParticipantsRepository.findFundingParticipantsByParticipantAndFunding(user, funding)
                        .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));
                return Collections.singletonList(fundingParticipants);
            } else {

                try {
                    Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(state.toUpperCase());

                    switch (fundingParticipantsState) {
                        case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                            return fundingParticipantsRepository.findFundingParticipantsByFundingAndState(funding, fundingParticipantsState);
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
        return null;
    }





    // 펀딩 참여 리스트에 추가할 유저 id와 펀딩 id
    @Transactional
    public String JoinParticipants (HttpServletRequest request, Long user_id, Long funding_id) {
        User user = null;

        //userid가 null이면 header의 jwt토큰에서 username 추출
        if (user_id == null){
            // 헤더에서 jwt 토큰 추출
            String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

            if(jwtToken == null || jwtToken.isEmpty()){
                throw new BaseException(NOT_FIND_LOGIN_SESSION);
            }

            String username = jwtUtil.extractUsername(jwtToken);
            user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        }

        else {
            // userid가 있으면, 해당 id를 이용하여 user 객체 찾기
            user = userRepository.findUserById(user_id)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        }


        // fundingId를 이용하여 user 객체 가져오기
        Funding funding = fundingRepository.findFundingById(funding_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

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
            // 객체 생성
            FundingParticipants fundingParticipants = new FundingParticipants(user, funding);

            // 참여자 객체 참여자 테이블에 저장
            fundingParticipantsRepository.save(fundingParticipants);

            return "펀딩에 참여되었습니다. ";
        }
        return null;
    }



    // 펀딩 참여 리스트에 삭제할 유저 id와 펀딩 id
    @Transactional
    public String DeleteParticipants (HttpServletRequest request, Long user_id, Long funding_id) {
        User user = null;

        //userid가 null이면 header의 jwt토큰에서 username 추출
        if (user_id == null){
            // 헤더에서 jwt 토큰 추출
            String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
            if (jwtToken == null){
                throw new BaseException(NOT_FIND_LOGIN_SESSION);
            }
            String username = jwtUtil.extractUsername(jwtToken);
            user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        }

        else {
            // userid가 있으면, 해당 id를 이용하여 user 객체 찾기
            user = userRepository.findUserById(user_id)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        }

        // fundingId를 이용하여 user 객체 가져오기
        Funding funding = fundingRepository.findFundingById(funding_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));


        // 참여자 객체 찾기
        FundingParticipants fundingParticipants = fundingParticipantsRepository.findFundingParticipantsByParticipantAndFunding(user, funding)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_PARTICIPANTS));

        // 참여자 객체 참여자 테이블에서 삭제
        fundingParticipantsRepository.delete(fundingParticipants);

        return "참여자 삭제 성공입니다. ";
    }



    // 특정 펀딩 모집 성공 시 해당 펀딩 id를 가진 참여자 state를 ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE으로 변경
    /*<펀딩 참여자 테이블 상태값 목록>
        Funding_Participants 테이블의 상태(state):
        ACTIVE: 참여자가 펀딩 결제를 완료 상태
        COMPLETE: 신청한 펀딩 성공 상태
        REFUND_NEEDED: 신청한 펀딩이 실패하여 환불 필요 상태
        REFUND_COMPLETE: 환불 완료 상태*/

    @Transactional
    public String updateParticipantsState (HttpServletRequest request, Long user_id, Long funding_id, String forwardState) {
        User user = null;

        //userid가 null이면 header의 jwt토큰에서 username 추출
        if (user_id == null){
            // 헤더에서 jwt 토큰 추출
            String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
            if (jwtToken == null){
                throw new BaseException(NOT_FIND_LOGIN_SESSION);
            }
            String username = jwtUtil.extractUsername(jwtToken);
            user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        }

        else {
            // userid가 있으면, 해당 id를 이용하여 user 객체 찾기
            user = userRepository.findUserById(user_id)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        }

        // fundingId를 이용하여 user 객체 가져오기
        Funding funding = fundingRepository.findFundingById(funding_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));


        // 참여자 객체 찾기
        FundingParticipants fundingParticipants = fundingParticipantsRepository.findFundingParticipantsByParticipantAndFunding(user, funding)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_PARTICIPANTS));

        // String state가 상수값으로 등록된 상태값인지 확인
        try {
            Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(forwardState.toUpperCase());

            // 참여자 객체의 state 변경 시도
            switch (fundingParticipantsState) {
                case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                    fundingParticipants.updateFundingParticipantsState(fundingParticipantsState);
                }
                default -> {
                    throw new BaseException(INVALID_STATE);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_STATE);
        }

        return "참여자 "+forwardState +" 으로 변경 성공입니다. ";
    }


    // 펀딩 마감 시간 확인 후 state 변경
    /*@Scheduled(cron = "0 0 0 * * ?") // 매일 24시 (자정)에 실행*/
    /*@Scheduled(cron = "0 0/30 * * * ?") // 매 30분마다 실행*/
    @Scheduled(cron = "0 * * * * ?") // 매 분마다 실행
    public void updateFundingState() {
        // 현재 진행중인 펀딩 게시글 추출
        List<Funding> fundingList = OrderByState(getAllFundings(), "IN_PROGRESS");

        for (Funding funding : fundingList){
            // 펀딩 최소인원
            Long min_participants = funding.getMin_participants();

            // 현재 펀딩 참여 인원
            int currentPeople = fundingParticipantsRepository.countFundingParticipantsByFunding(funding);

            // 현재 시간
            LocalDateTime currentTime = LocalDateTime.now();
            // 마감일 시간
            LocalDateTime deadLine = funding.getDeadline();

            // 마감일 전 최소 인원 달성 했을 경우,
            if (deadLine.isAfter(currentTime) && currentPeople >= min_participants) {
                log.info("마감일 최소 도달-> state COMPLETE 변경");
                // 해당 펀딩의 참여자 리스트 가져오기
                List<FundingParticipants> fundingParticipantsList = fundingParticipantsRepository.findFundingParticipantsByFunding(funding);
                // 펀딩 게시글의 state IN_PROGRESS -> COMPLETE 변경, 펀딩 참여자 리스트 state ACTIVE -> COMPLETE 변경
                funding.updateFundingAndPariticpantsState(Constant.FundingState.COMPLETE, fundingParticipantsList);
            }

            // 마감일이 지났을 경우
            else if (deadLine.isBefore(currentTime)) {
                /*log.info("마감일 지남-> state FAIL 변경");*/
                // 해당 펀딩의 참여자 리스트 가져오기
                List<FundingParticipants> fundingParticipantsList = fundingParticipantsRepository.findFundingParticipantsByFunding(funding);
                // 펀딩 게시글의 state IN_PROGRESS -> FAIL 변경, 펀딩 참여자 리스트 state ACTIVE -> REFUND_NEEDED 변경
                funding.updateFundingAndPariticpantsState(Constant.FundingState.FAIL, fundingParticipantsList);

                // 환불이 필요한 펀딩에 참여중인 필요 대상
                for(FundingParticipants fundingParticipant: fundingParticipantsList) {
                    // 펀딩 환불 대상 유저 객체
                    User user = fundingParticipant.getParticipant();
                    // 쪽지 테이블에 등록할 환불 대상 정보 및 환불 사유
                    FundingParticipants refundNeededUser = fundingParticipantsRepository.findFundingParticipantsByParticipantAndStateAndFunding(user, Constant.FundingParticipantsState.REFUND_NEEDED, funding)
                                                                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_PARTICIPANTS));

                    Optional<FundingMessage> checkfundingMessage = fundingMessageRepository.findFundingMessagesByParticipant(refundNeededUser);
                    // 아직 쪽지 대상에 없으면 쪽지 생성
                    if(checkfundingMessage.isEmpty()) {
                        log.info("마감일 지남-> 새 쪽지 발송 대상 등록 = {}", refundNeededUser.getParticipant().getUsername());
                        //환불 대상이 발견되면 쪽지 테이블에 객체 생성 (모델:: (참여자 정보, 취소사유)
                        FundingMessage fundingMessage = new FundingMessage(refundNeededUser, "펀딩 달성 실패");
                        fundingMessageRepository.save(fundingMessage);
                    }
                }
            }
        }
    }


    /*@Transactional
    public void updateFundingMessage(List<FundingParticipants> fundingRefundNeededList) {
        for (FundingParticipants fundingParticipants : fundingRefundNeededList) {
            if (fundingParticipants.getState().equals(Constant.FundingParticipantsState.REFUND_NEEDED)) {
                FundingMessage fundingMessage = new FundingMessage(fundingParticipants, "펀딩 달성 실패");
                fundingMessageRepository.save(fundingMessage);
            }
        }
    }*/

/*    @Transactional
    public void updateFundingMessage(List<FundingParticipants> fundingRefundNeededList) {
        Set<FundingParticipants> uniqueParticipants = new HashSet<>(fundingRefundNeededList);

        for (FundingParticipants fundingParticipants : uniqueParticipants) {
            if (fundingParticipants.getState().equals(Constant.FundingParticipantsState.REFUND_NEEDED)) {
                FundingMessage fundingMessage = new FundingMessage(fundingParticipants, "펀딩 달성 실패");
                fundingMessageRepository.save(fundingMessage);
            }
        }
    }*/
    @Transactional(readOnly = true)
    public FundingPayment getPaymentInfoByLoginUser (HttpServletRequest request, Long fundingId) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

        if(jwtToken == null || jwtToken.isEmpty()){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // fundingId를 이용하여 funding 객체 찾기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // 유저의 결제 정보 찾기
        Optional<FundingPayment> optionalFundingPayment = fundingPaymentRepository.findFundingPaymentByParticipantAndFunding(user, funding);

        // 유저의 결제 정보가 등록 되어있을 경우,
        if (optionalFundingPayment.isPresent()) {
            return optionalFundingPayment.get();
        }

        // 유저의 결제 정보가 없을 경우
        else {
            throw new BaseException(EMPTY_PAYMENT_USER);
        }
    }



    @Transactional
    public FundingPayment InsertPaymentInfoByLoginUser (HttpServletRequest request, PaymentInsertReq paymentInsertReq) throws SQLException {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

        if(jwtToken == null || jwtToken.isEmpty()){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // fundingId
        Long fundingId = paymentInsertReq.getFunding_id();

        // fundingId를 이용하여 funding 객체 찾기
        Funding funding = fundingRepository.findFundingById(fundingId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING));

        // 유저의 결제 정보 찾기
        Optional<FundingPayment> optionalFundingPayment = fundingPaymentRepository.findFundingPaymentByParticipantAndFunding(user, funding);

        // 유저의 결제 정보가 등록 되어있을 경우, 이미 있는 유저임을 반환
        if (optionalFundingPayment.isPresent()) {
            throw new BaseException(EXISTS_PAYMENT_USER);
        }

        // 유저의 결제 정보가 없을 경우, Payment 테이블에 정보 저장
        else {
            // 은행 이름 및 은행 계좌 저장.
            FundingPayment fundingPayment = paymentInsertReq.toEntity();

            // 유저 정보 저장
            fundingPayment.updateParticipants(user);

            // DB에 fundingPayment 객체 저장
            fundingPaymentRepository.save(fundingPayment);

            return fundingPayment;
        }
    }

    //1. 최근 N 시간 이내 펀딩에 참여한 유저가 많은 순서대로 출력 (내림차순)
    @Transactional(readOnly = true)
    public List<FundingMainRes> findParticipantsInLastHours(HttpServletRequest request, int hoursAgo, int top_num, String state) throws SQLException {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        String username = null;

        if(jwtToken == null || jwtToken.isEmpty()) {
            username = null;
        } else{
            username = jwtUtil.extractUsername(jwtToken);
        }

        // 시작시간
        LocalDateTime startTime = getPastLocalDateTime(hoursAgo);
        // 현재시간
        LocalDateTime currentTime = LocalDateTime.now();

        // 모든 펀딩 리스트 가져오기
        List<Funding> fundingList = getAllFundings();

        // 펀딩과 해당 펀딩에 참여한 유저 수를 저장할 맵
        Map<FundingMainRes, Integer> fundingParticipantsCountMap = new HashMap<>();

        // 각 펀딩에 대해 참여한 유저 수를 계산하여 맵에 저장
        for(Funding funding : fundingList) {
            // 좋아요 유무 확인

            // String state가 상수값으로 등록된 상태값인지 확인
            try {
                Constant.FundingParticipantsState fundingParticipantsState = Constant.FundingParticipantsState.valueOf(state.toUpperCase());

                switch (fundingParticipantsState) {
                    case ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE -> {
                        // 등록된 상수 값이 맞으면, Map에 저장
                        int participantCount = fundingParticipantsRepository.countFundingParticipantsByCreatedAtBetweenAndFundingAndState(startTime, currentTime, funding, fundingParticipantsState);
                        // 펀딩 객체별 달성률 저장
                        int achievementrate = calculateAchievementRate(funding.getId());

                        if (username == null || username.isEmpty()){
                            fundingParticipantsCountMap.put(new FundingMainRes(funding, achievementrate, false), participantCount);
                        } else{
                            User user = userRepository.findUserByUsername(username)
                                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
                            Optional<PostLike> checklike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.FUNDING, funding.getId());
                            if (checklike.isPresent()) {
                                fundingParticipantsCountMap.put(new FundingMainRes(funding, achievementrate, true), participantCount);
                            } else {
                                fundingParticipantsCountMap.put(new FundingMainRes(funding, achievementrate, false), participantCount);
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

        // 맵을 유저 수를 기준으로 내림차순으로 정렬
        List<Map.Entry<FundingMainRes, Integer>> sortedFundingList = fundingParticipantsCountMap.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()))
                .collect(Collectors.toList());

        // 상위 top_num 펀딩을 선택
        List<FundingMainRes> topFundingList = sortedFundingList.stream()
                .limit(top_num)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());


        return topFundingList;
    }

    // 매개변수 hour을 LocalDatetime으로 변환
    public LocalDateTime getPastLocalDateTime (int hoursAgo) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime resultTime = currentTime.minusHours(hoursAgo); // hour 시간 이전의 LocalDateTime 계산
        return resultTime;
    }



    //2. 최근 N 시간 이내 좋아요 수  (내림차순)
    @Transactional(readOnly = true)
    public List<FundingMainRes> findPostLikeInLastHours(HttpServletRequest request, int hoursAgo, int top_num) throws SQLException {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        String username = "";

        if(jwtToken == null || jwtToken.isEmpty()) {
            username = "";
        } else{
            username = jwtUtil.extractUsername(jwtToken);
        }

        // 시작시간
        LocalDateTime startTime = getPastLocalDateTime(hoursAgo);
        // 현재시간
        LocalDateTime currentTime = LocalDateTime.now();

        // 모든 펀딩 리스트 가져오기
        List<Funding> fundingList = getAllFundings();

        // 펀딩과 해당 펀딩에 참여한 유저 수를 저장할 맵
        Map<FundingMainRes, Integer> fundingPostLikeCountMap = new HashMap<>();

        // 각 펀딩에 대한 좋아요 수 계산하여 맵에 저장
        for(Funding funding : fundingList) {
            int postLikeCount = postLikeRepository.countPostLikeByCreatedAtBetweenAndTargetTypeAndTargetId(startTime, currentTime, Constant.TargetType.FUNDING, funding.getId());
            // 펀딩 객체별 달성률 계산
            int achievementrate = calculateAchievementRate(funding.getId());
            if (username == null || username.isEmpty()) {
                fundingPostLikeCountMap.put(new FundingMainRes(funding, achievementrate, false), postLikeCount);
            } else{
                User user = userRepository.findUserByUsername(username)
                        .orElseThrow(() -> new BaseException(NOT_FIND_USER));
                Optional<PostLike> checklike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.FUNDING, funding.getId());
                if (checklike.isPresent()) {
                    fundingPostLikeCountMap.put(new FundingMainRes(funding, achievementrate, true), postLikeCount);
                } else {
                    fundingPostLikeCountMap.put(new FundingMainRes(funding, achievementrate, false), postLikeCount);
                }
            }
        }

        // 맵을 유저 수를 기준으로 내림차순으로 정렬
        List<Map.Entry<FundingMainRes, Integer>> sortedFundingList = fundingPostLikeCountMap.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()))
                .collect(Collectors.toList());

        // 상위 top_num 펀딩을 선택
        List<FundingMainRes> topFundingList = sortedFundingList.stream()
                .limit(top_num)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return topFundingList;
    }



    //3. 펀딩 조회수 (내림차순)
    @Transactional(readOnly = true)
    public List<FundingMainRes> findFundingOrderByViewCount(HttpServletRequest request, int top_num, String state) throws SQLException {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        String username = null;

        if(jwtToken == null || jwtToken.isEmpty()) {
            username = null;
        } else{
            username = jwtUtil.extractUsername(jwtToken);
        }

        // 반환 모델 선언
        List<FundingMainRes> fundingMainResList = new ArrayList<>();

        // String state가 상수값으로 등록된 상태값인지 확인
        try {
            Constant.FundingState fundingState = Constant.FundingState.valueOf(state.toUpperCase());

            switch (fundingState) {
                case ACTIVE, IN_PROGRESS, DELETE, COMPLETE, FAIL -> {
                    // 등록된 상수 값이 맞으면 조회수가 많은 순서대로 펀딩 가져오기
                    List<Funding> fundingList = fundingRepository.findFundingsByStateOrderByViewCountDesc(fundingState);

                    for (Funding funding : fundingList) {
                        // 달성률 계산
                        int achievementrate = calculateAchievementRate(funding.getId());
                        if (username == null || username.isEmpty()){
                            fundingMainResList.add(new FundingMainRes(funding, achievementrate, false));
                        } else {
                            User user = userRepository.findUserByUsername(username)
                                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
                            Optional<PostLike> checklike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.FUNDING, funding.getId());
                            if (checklike.isPresent()) {
                                fundingMainResList.add(new FundingMainRes(funding, achievementrate, true));
                            } else {
                                fundingMainResList.add(new FundingMainRes(funding, achievementrate, false));
                            }
                        }
                    }
                    // top_num만큼 자르기
                    return fundingMainResList.subList(0, Math.min(top_num, fundingList.size()));
                }
                default -> {
                    throw new BaseException(INVALID_STATE);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_STATE);
        }


    }


    //4. 최근 오픈한 펀딩 일자 순(내림차순)
    @Transactional(readOnly = true)
    public List<FundingMainRes> findFundingOrderByCreateAt(HttpServletRequest request, int top_num, String state) throws SQLException {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        String username = null;

        if(jwtToken == null || jwtToken.isEmpty()) {
            username = null;
        } else{
            username = jwtUtil.extractUsername(jwtToken);
        }

        // 반환 모델 리스트 선언
        List<FundingMainRes> fundingMainResList = new ArrayList<>();

        // String state가 상수값으로 등록된 상태값인지 확인
        try {
            Constant.FundingState fundingState = Constant.FundingState.valueOf(state.toUpperCase());

            switch (fundingState) {
                case ACTIVE, IN_PROGRESS, DELETE, COMPLETE, FAIL -> {
                    // 등록된 상수 값이 맞으면 모든 펀딩 가져오기
                    List<Funding> fundingList = fundingRepository.findFundingsByState(fundingState);
                    /*List<Funding> fundingList = getAllFundings();*/
                    // 최근 오픈한 펀딩 순서로 정렬
                    fundingList.sort(Comparator.comparing(Funding::getCreatedAt).reversed());

                    for(Funding funding : fundingList) {
                        // 펀딩 객체별 달성률 계산
                        int achievementrate = calculateAchievementRate(funding.getId());
                        if (username == null || username.isEmpty()){
                            fundingMainResList.add(new FundingMainRes(funding, achievementrate, false));
                        } else {
                            User user = userRepository.findUserByUsername(username)
                                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
                            Optional<PostLike> checklike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.FUNDING, funding.getId());
                            if (checklike.isPresent()) {
                                fundingMainResList.add(new FundingMainRes(funding, achievementrate, true));
                            } else {
                                fundingMainResList.add(new FundingMainRes(funding, achievementrate, false));
                            }
                        }
                    }
                    // top_num만큼 자르기
                    return fundingMainResList.subList(0, Math.min(top_num, fundingList.size()));
                }
                default -> {
                    throw new BaseException(INVALID_STATE);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_STATE);
        }
    }


    //5. 마감이 임박한 시간 순서에 따른 내림차순
    @Transactional(readOnly = true)
    public List<FundingMainRes> findFundingOrderByDeadline(HttpServletRequest request, int top_num, String state) throws SQLException {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        String username = null;

        if(jwtToken == null || jwtToken.isEmpty()) {
            username = null;
        } else{
            username = jwtUtil.extractUsername(jwtToken);
        }

        // 반환 모델 리스트 선언
        List<FundingMainRes> fundingMainResList = new ArrayList<>();

        // String state가 상수값으로 등록된 상태값인지 확인
        try {
            Constant.FundingState fundingState = Constant.FundingState.valueOf(state.toUpperCase());

            switch (fundingState) {
                case ACTIVE, IN_PROGRESS, DELETE, COMPLETE, FAIL -> {
                    // 현재시간과 가장 근접한 펀딩 가져오기
                    List<Funding> fundingList = fundingRepository.findFundingByStateOrderByDeadlineAsc(fundingState);
                    for (Funding funding : fundingList) {
                        // 달성률 계산
                        int achievementrate = calculateAchievementRate(funding.getId());
                        
                        if (username ==null) {
                            fundingMainResList.add(new FundingMainRes(funding, achievementrate, false));
                        } else {
                            User user = userRepository.findUserByUsername(username)
                                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
                            Optional<PostLike> checklike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.FUNDING, funding.getId());
                            if (checklike.isPresent()) {
                                fundingMainResList.add(new FundingMainRes(funding, achievementrate, true));
                            } else {
                                fundingMainResList.add(new FundingMainRes(funding, achievementrate, false));
                            }
                        }
                    }

                    // top_num만큼 자르기
                    return fundingMainResList.subList(0, Math.min(top_num, fundingList.size()));
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
