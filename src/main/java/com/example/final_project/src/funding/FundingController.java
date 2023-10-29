package com.example.final_project.src.funding;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.oauth.OAuthService;
import com.example.final_project.common.response.BaseResponse;
import com.example.final_project.src.funding.entity.*;
import com.example.final_project.src.funding.model.*;
import com.example.final_project.src.post_like.model.GetAllRes;
import com.example.final_project.src.student_request.dto.CommentResDto;
import com.example.final_project.src.student_request.dto.CommentUpdateReqDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.final_project.common.response.BaseResponseStatus.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class FundingController {
    @Autowired
    private FundingService fundingService;
    @Autowired
    private OAuthService oAuthService;

    /**
     * 로그인한 유저가 작성한 펀딩 게시글 목록 조회 API
     * [GET] /funding/mywriter
     * @param request
     * @return BaseResponse<List<Funding>>
     */
    @ResponseBody
    @Operation(summary = "jwt 인증 필요", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/funding/mywriter")
    public BaseResponse<List<Funding>> readAllMyPosts(HttpServletRequest request) {
        return new BaseResponse<>(fundingService.OrderByWriter(request));
    }


    /**
     * 로그인한 유저가 좋아요 누른 목록 조회 API
     * [GET] /mylike
     * @param request
     * @return BaseResponse<List<>>
     */
    @ResponseBody
    @Operation(summary = "jwt 인증 필요", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/mylike")
    public BaseResponse<List<GetAllRes>> readAllMyLikePosts(HttpServletRequest request) {
        return new BaseResponse<>(fundingService.getAllLikePost(request));
    }

    /**
     * 메인 페이지에 필요한 Top N 리스트 반환 펀딩 API
     * [GET] /funding/rank
     *
     * @param condition (optional)  분류 타입
     * @param state     (optional)  펀딩 state 분류 기준
     * @param hoursAgo  (optional)  N 시간 이내
     * @param top_num   (optional)  랭크 수
     * @return BaseResponse<List<Funding>>
     */
    @ResponseBody
    @GetMapping("/funding/rank")
    public BaseResponse<List<FundingMainRes>> readTopFundingPosts(HttpServletRequest request,
                                                                    @RequestParam(name = "orderby", required = false) String condition,
                                                                    @RequestParam(name = "state", required = false) String state,
                                                                    @RequestParam(name = "hours", required = false) Integer hoursAgo,
                                                                    @RequestParam(name = "top", required = false) Integer top_num) {

        log.info(" condition={}, hours = {}", condition, hoursAgo);

        /*메인페이지 실시간 랭킹 펀딩 리스트 호출

        1. 24시간 이내, 일주일 이내 펀딩 참여 유저 수 (내림차순)
        2. 24시간 이내 좋아요 수  (내림차순)
        3. 24시간 이내 펀딩 조회수 (내림차순)

        4. 현재 기준 최근 오픈한 펀딩 일자 순(내림차순)
        5. 현재 기준 마감이 임박한 시간 순서에 따른 내림차순 정렬*/

        // String state가 상수값으로 등록된 상태값인지 확인
        try {
            switch (condition) {
                // 펀딩 참여 유저수 , 좋아요수, 조회수, 최근 오픈 펀딩, 마감 임박순
                case "mostParticipants", "mostLike", "mostView", "newest", "deadline" -> {
                    // 펀딩 참여 유저수 반환
                    if (condition.equals("mostParticipants")) {
                        if (hoursAgo == null || top_num == null || state == null) {
                            // 매개변수 누락 예외 처리
                            throw new BaseException(ALL_EMPTY_CONDITION);
                        } else {
                            return new BaseResponse<>(fundingService.findParticipantsInLastHours(request, hoursAgo, top_num, state));
                        }
                    } else if(condition.equals("mostLike")) { // 좋아요수 반환
                        if (hoursAgo == null || top_num == null) {
                            // 매개변수 누락 예외 처리
                            throw new BaseException(ALL_EMPTY_CONDITION);
                        } else {
                            return new BaseResponse<>(fundingService.findPostLikeInLastHours(request, hoursAgo, top_num));
                        }

                    } else if(condition.equals("mostView")) { // 조회수 반환
                        if (top_num == null || state == null) {
                            // 매개변수 누락 예외 처리
                            throw new BaseException(ALL_EMPTY_CONDITION);
                        } else {
                            return new BaseResponse<>(fundingService.findFundingOrderByViewCount(request, top_num, state));
                        }

                    } else if(condition.equals("newest")) { // 최신순 반환
                        if (top_num == null || state == null) {
                            // 매개변수 누락 예외 처리
                            throw new BaseException(ALL_EMPTY_CONDITION);
                        } else {
                            return new BaseResponse<>(fundingService.findFundingOrderByCreateAt(request, top_num, state));
                        }
                    } else { // 마감 임박순 반환
                        if (top_num == null || state == null) {
                            // 매개변수 누락 예외 처리
                            throw new BaseException(ALL_EMPTY_CONDITION);
                        } else {
                            return new BaseResponse<>(fundingService.findFundingOrderByDeadline(request, top_num, state));
                        }
                    }
                }
                default -> {
                    throw new BaseException(INVALID_STATE);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_STATE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 필터링 및 정렬을 적용한 펀딩 게시글 목록 조회 API
     * [GET] /funding/all
     *
     * @param category_id (optional) 대분류: 카테고리
     * @param condition   (optional)   중분류: 최신순 (newest) --> default / 마감임박 순 (deadline) / 좋아요 순 (인기순, mostLike)
     * @param page_num    소분류: 현재 페이지 숫자 ---> default 모든 페이지 출력
     * @param itemsPerPage 한 페이지에 출력할 아이템 수
     * @return BaseResponse<List < FundingRes>>
     */
    @ResponseBody
    @GetMapping("/funding/all")
    public BaseResponse<List<FundingRes>> readAllFundingPosts(HttpServletRequest request,
                                                              @RequestParam(name = "category", required = false) Long category_id,
                                                              @RequestParam(name = "orderby", required = false) String condition,
                                                              @RequestParam(name = "state", required = false) String state,
                                                              @RequestParam(name = "currentpage", required = false) Integer page_num,
                                                              @RequestParam(name = "itemsPerPage", required = false) Integer itemsPerPage) {

        // 1. 카테고리 분류 결과
        List<Funding> fundingList1 = null;
        if (category_id == null || category_id < 1) {
            fundingList1 = fundingService.getAllFundings();
        } else {
            fundingList1 = fundingService.getFundingsByCategory(category_id);
        }

        // 2.
        //  펀딩 게시글 리스트 order by 1. 최신순 (newest) --> default
        //	펀딩 게시글 리스트 order by 2. 마감임박 순 (deadline)
        //	펀딩 게시글 리스트 order by 3. 좋아요 순 (인기순, mostLike)

        List<Funding> fundingList2 = fundingService.OrderByKeyword(fundingList1, condition); //

        // 3. state 분류 결과
        List<Funding> fundingList3 = fundingService.OrderByState(fundingList2, state);

        int totalItems = fundingList3.size();

        // 5. 페이지네이션
        List<Funding> fundingList4 = null;

        // 페이지 숫자 null이면 모든 키워드 리스트 반환
        if ((page_num == null && itemsPerPage == null)) {
            fundingList4 = fundingList3;
        }
        else if (page_num == null || itemsPerPage == null) {
            throw new BaseException(EMPTY_PAGE_NUM);
        }
        // page_num 값이 정상이면 한 페이지 아이템 리스트로 반환
        else{
            fundingList4 = fundingService.getFundingsBypage(fundingList3, page_num, itemsPerPage);
        }

        // 5. 현재 로그인한 유저의 게시글 좋아요 여부 및 펀딩 게시글 달성률 추가 --> FundingRes 모델
        List<FundingRes> fundingResList = fundingService.FundingTransferToFundingRes(request, fundingList4, totalItems);

        return new BaseResponse<>(fundingResList);
    }

    /**
     * 특정 펀딩 게시글 목록 조회 API
     * [GET] /funding/detail
     * @param funding_id 펀딩 게시글 고유 ID
     * @return BaseResponse<FundingDetailRes>
     */
    @ResponseBody
    @GetMapping("/funding/detail")
    public BaseResponse<FundingDetailRes> readFundingPost(HttpServletRequest request, @RequestParam(name = "funding") Long funding_id) {
        return new BaseResponse<>(fundingService.FundingTransferToFundingDetailRes(request, funding_id));
    }



    /**
     * 특정 유저 혹은 특정 조건의 펀딩 게시글 목록 조회 API
     * [GET] /fundingList
     * @param keyword (optional) 검색할 키워드
     * @param page_num    소분류: 현재 페이지 숫자 ---> default 모든 페이지 출력
     * @param itemsPerPage 한 페이지에 출력할 아이템 수
     * @return BaseResponse<List< FundingRes>>
     */
    @ResponseBody
    @GetMapping("/fundingList")
    public BaseResponse<List<FundingRes>> readFundingPosts(
            HttpServletRequest request,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "currentpage", required = false) Integer page_num,
            @RequestParam(name = "itemsPerPage", required = false) Integer itemsPerPage) {

        if (keyword == null || keyword.isEmpty()){
            throw new BaseException(ALL_EMPTY_CONDITION);
        }

        // 헤더바 검색창으로 검색하여 찾는 경우 -> keyword가 있을 경우 입력한 키워드가 title, subtitle, category에서 부분 일치하는 게시글 조회
        else {
            // 페이지 숫자 유효성 실패로 모든 키워드 리스트 반환
            if ((page_num == null && itemsPerPage == null)) {
                List<Funding> fundingList = fundingService.getFundingsByKeyword(keyword);
                return new BaseResponse<>(fundingService.FundingTransferToFundingRes(request,fundingList, fundingList.size()));
            }
            else if (page_num == null || itemsPerPage == null) {
                throw new BaseException(EMPTY_PAGE_NUM);
            }
            else{
                // 페이지네이션 키워드 리스트 반환
                List<Funding> fundingList5 = fundingService.getFundingsByKeyword(keyword);
                List<Funding> fundingList = fundingService.getFundingsBypage(fundingList5, page_num, itemsPerPage);
                return new BaseResponse<>(fundingService.FundingTransferToFundingRes(request,fundingList, fundingList5.size()));
            }
        }
    }

    /**
     * 좋아요 버튼 클릭 반응으로 PostLike 테이블에 저장할 API
     * [GET] /funding/toggle-like
     *
     * @param request    HTTP 요청 객체
     * @param funding_id 게시글 ID
     * @return BaseResponse<String>
     */
    @ResponseBody
    @Operation(summary = "jwt 인증 필요", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/funding/toggle-like")
    public BaseResponse<String> addLikeUser(HttpServletRequest request, @RequestParam(name = "id") Long funding_id) {
        return new BaseResponse<>(fundingService.updatePostLike(request, funding_id));
    }

    /**
     * 특정 펀딩 게시글의 조회수, 좋아요수, 댓글수 조회 API
     * [GET] /funding/total
     *
     * @param funding_id 게시글 ID
     * @return BaseResponse<Map < String, Long>>
     */
    @ResponseBody
    @GetMapping("/funding/total")
    public BaseResponse<Map<String, Long>> ViewLikeComment(@RequestParam(name = "id") Long funding_id) {
        // funding id를 이용하여 해당 펀딩의 조회수 가져오기
        Long viewCount = fundingService.countFundingView(funding_id);

        // funding id를 이용하여 해당 펀딩의 좋아요 수 가져오기
        Long likeCount = fundingService.countFundingLike(funding_id);

        // funding id를 이용하여 해당 펀딩의 댓글수 가져오기
        Long commentCount = fundingService.countFundingComment(funding_id);

        Map<String, Long> totalCount = new HashMap<>();
        totalCount.put("view", viewCount);
        totalCount.put("like", likeCount);
        totalCount.put("comment", commentCount);
        return new BaseResponse<>(totalCount);
    }


    /**
     * 모든 펀딩 게시글을 조건대로 분류하여 리스트 반환 API
     * [GET] /funding/sort
     *
     * @param status  (optional) 펀딩 상태 선택
     * @param geo_num (optional) 지역 선택
     * @return BaseResponse<List < Funding>>
     */
/*
    @ResponseBody
    @GetMapping("/funding/sort")
    public BaseResponse<List<Funding>> SortedByKeyword(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "geo", required = false) Long geo_num) {
        // 펀딩 상태에 따른 분류 혹은 지역 넘버에 따른 분류 실행
        if (status != null || geo_num != null) {
            */
/*public enum FundingState {
                ACTIVE, IN_PROGRESS, DELETE, COMPLETE, FAIL;
            }*//*

            List<Funding> sortedFundingList = fundingService.SortedByKeyword(status, geo_num);
            return new BaseResponse<>(sortedFundingList);
        }

        // 모든 분류 조건이 null 이면 모든 게시물 조회 (default) 동작 수행
        else {
            return readFundingPosts(null, null, null, null, null);
        }
    }
*/


    /*    *//**
     * 모든 펀딩 게시글을 조건대로 정렬하여 리스트 반환 API
     * [GET] /funding/order
     * @param condition (optional) 최신순(default) / 마감임박순 / 좋아요순 선택
     * @return BaseResponse<List < Funding>>
     *//*
    @ResponseBody
    @GetMapping("/funding/order")
    public BaseResponse<List<Funding>> OrderByKeyword(
            @RequestParam(name = "orderby", required = false) String condition,
            @RequestParam(name = "currentpage", required = false) Integer page_num) {

        int itemsPerPage = 12; // 한 페이지당 표시할 아이템 수
        List<Funding> fundingList;

        if (page_num == null) {
            fundingList = fundingService.OrderByKeyword(condition); // 조건에 맞는 펀딩글 리스트
        } else {
            fundingList = fundingService.getFundingsBypage(fundingService.OrderByKeyword(condition), page_num, itemsPerPage); // 페이지네이션 적용
        }

        return new BaseResponse<>(fundingList);
    }*/

    /**
     * 펀딩 게시글 id를 받아 해당 펀딩의 달성률 반환 API, 소수점없이 일의 자리 int형으로 반환
     * [GET] /funding/achievementrate
     *
     * @param funding_id 펀딩 고유 id
     * @return BaseResponse<int>
     */
    @ResponseBody
    @GetMapping("/funding/achievementrate")
    public BaseResponse<Integer> getFundingByFunding(@RequestParam(name = "funding") Long funding_id) {
        return new BaseResponse<>(fundingService.calculateAchievementRate(funding_id));
    }


    /**
     * 펀딩 게시글 생성 API
     * [POST] /funding/create
     *
     * @param request
     * @param fundingInsertReq 게시글 생성 모델
     * @return BaseResponse<FundingInsertRes>
     */
    @ResponseBody
    @Operation(summary = "jwt 인증 필요", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/funding/create")
    public BaseResponse<FundingInsertRes> createPost(HttpServletRequest request, @RequestBody FundingInsertReq fundingInsertReq) throws SQLException {
       /* log.info("게시글 작성 컨트롤러 진입, getTitle= {}", fundingInsertReq.getTitle());
        log.info("게시글 작성 컨트롤러 진입, getSubtitle= {}", fundingInsertReq.getSubtitle());
        log.info("게시글 작성 컨트롤러 진입, getCategoryId= {}", fundingInsertReq.getCategory_id());
        log.info("게시글 작성 컨트롤러 진입, getWriter= {}", fundingInsertReq.getWriter());
        log.info("게시글 작성 컨트롤러 진입, getLocation= {}", fundingInsertReq.getLocation());
        log.info("게시글 작성 컨트롤러 진입, getDeadline= {}", fundingInsertReq.getDeadline());*/
        log.info("게시글 작성 컨트롤러 진입, getDeadline= {}", fundingInsertReq.toString());
        if (validatePostFormReq(fundingInsertReq) != null) {
            return validatePostFormReq(fundingInsertReq);
        } else {
            return new BaseResponse<>(fundingService.createFunding(request, fundingInsertReq));
        }
    }

    // 게시글 정보 데이터 유효성 확인
    private BaseResponse<FundingInsertRes> validatePostFormReq(FundingInsertReq fundingInsertReq) {
        if (fundingInsertReq.getTitle() == null || fundingInsertReq.getSubtitle() == null
                || fundingInsertReq.getDeadline() == null) {
            // 제목, 부제목., 마감일 공란일 때 예외 처리
            throw new BaseException(FUNDING_EMPTY_TITLE);
        }
        if (fundingInsertReq.getGoal() == null || fundingInsertReq.getMin_participants() == null
                || fundingInsertReq.getMax_participants() == null) {
            // 목표인원, 최소인원, 최대인원 공란일 때 예외 처리
            throw new BaseException(FUNDING_EMPTY_PARTICIPANT);
        }

        if (fundingInsertReq.getAmount() == null) {
            // 펀딩 금액 공란일 때 예외 처리
            throw new BaseException(FUNDING_EMPTY_AMOUNT);
        }

        if (fundingInsertReq.getThumbnail() == null || fundingInsertReq.getContent() == null) {
            // 썸네일, 콘텐츠가 공란일 때 예외처리
            throw new BaseException(FUNDING_EMPTY_BLOB);
        }

        return null; // 유효성 검사 통과
    }


    /**
     * 펀딩 게시글 업데이트 API
     * [POST] /funding/update
     * @param request
     * @param funding_id 펀딩 고유 ID
     * @return BaseResponse<FundingInsertRes>
     */
    @ResponseBody
    @Operation(summary = "jwt 인증 필요", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/funding/update")
    public BaseResponse<String> updatePost(HttpServletRequest request, @RequestParam(name = "id") Long funding_id, @RequestBody FundingUpdateReq fundingUpdateReq) throws SQLException {
        log.info("게시글 업데이트 컨트롤러 진입, funding_id= {}", funding_id);
        log.info("게시글 업데이트 컨트롤러 진입, fundingUpdateReq= {}", fundingUpdateReq.toString());

        if (validateUpdateFormReq(fundingUpdateReq) != null) {
            return validateUpdateFormReq(fundingUpdateReq);
        } else {
            return new BaseResponse<>(fundingService.updateFunding(request, funding_id,fundingUpdateReq));
        }
    }

    // 게시글 정보 데이터 유효성 확인
    private BaseResponse<String> validateUpdateFormReq(FundingUpdateReq fundingUpdateReq) {
        if (fundingUpdateReq.getTitle() == null || fundingUpdateReq.getSubtitle() == null
                || fundingUpdateReq.getDeadline() == null) {
            // 제목, 부제목., 마감일 공란일 때 예외 처리
            throw new BaseException(FUNDING_EMPTY_TITLE);
        }
        if (fundingUpdateReq.getGoal() == null || fundingUpdateReq.getMin_participants() == null
                || fundingUpdateReq.getMax_participants() == null) {
            // 목표인원, 최소인원, 최대인원 공란일 때 예외 처리
            throw new BaseException(FUNDING_EMPTY_PARTICIPANT);
        }

        if (fundingUpdateReq.getAmount() == null) {
            // 펀딩 금액 공란일 때 예외 처리
            throw new BaseException(FUNDING_EMPTY_AMOUNT);
        }

        if (fundingUpdateReq.getThumbnail() == null || fundingUpdateReq.getContent() == null) {
            // 썸네일, 콘텐츠가 공란일 때 예외처리
            throw new BaseException(FUNDING_EMPTY_BLOB);
        }

        return null; // 유효성 검사 통과
    }


    /**
     * 펀딩 게시글 삭제 API
     * [DELETE] /funding/delete
     * @param request
     * @param funding_id 펀딩 고유 ID
     * @return BaseResponse<FundingInsertRes>
     */
    @ResponseBody
    @DeleteMapping("/funding/delete")
    public BaseResponse<String> deletePost(HttpServletRequest request, @RequestParam(name = "id") Long funding_id) throws SQLException {
        return new BaseResponse<>(fundingService.deleteFunding(request, funding_id));

    }

    /**
     * 펀딩 게시글 상태값 업데이트 API
     * [GET] /funding/update/state
     * @param funding_id 펀딩 고유 ID
     * @param forward_state 변환하고 싶은 state 값
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/funding/update/state")
    public BaseResponse<String> updatePostState(@RequestParam(name = "id") Long funding_id, @RequestParam(name = "state") String forward_state) throws SQLException {
        log.info("게시글 업데이트 컨트롤러 진입, funding_id= {}, state={}", funding_id, forward_state);
        return new BaseResponse<>(fundingService.updateFundingStateOnly(funding_id, forward_state));
    }

    /**
     * 모든 카테고리 리스트 반환 API
     * [GET] /category/all
     *
     * @return BaseResponse<List < FundingCategory>>
     */
    @ResponseBody
    @GetMapping("/category/all")
    public BaseResponse<List<FundingCategory>> readAllFundingCategories() {
        return new BaseResponse<>(fundingService.getCategoryAllList());
    }


    /**
     * 특정 카테고리 id 리스트 반환 API
     * [GET] /category
     *
     * @param category_id 카테고리 id
     * @return BaseResponse<List < FundingCategory>>
     */
    @ResponseBody
    @GetMapping("/category")
    public BaseResponse<FundingCategory> readFundingCategories(@RequestParam(name = "id") Long category_id) {
        return new BaseResponse<>(fundingService.getCategoryById(category_id));
    }


    /**
     * 특정 유저의 댓글 리스트 반환 API
     * [GET] /fundingcomment/user
     *
     * @param request HttpServletRequest
     * @return BaseResponse<List < FundingComment>>
     */
    @ResponseBody
    @Operation(summary = "jwt 인증 필요", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/fundingcomment/user")
    public BaseResponse<List<CommentResDto>> readUserComments(HttpServletRequest request) {
        return new BaseResponse<>(fundingService.getCommentByUser(request));
    }


    /**
     * 특정 게시글의 모든 댓글 리스트 반환 API
     * [GET] /fundingcomment
     * @param request
     * @param funding_id 게시글 고유 id
     * @return BaseResponse<List < FundingComment>>
     */
    @ResponseBody
    @GetMapping("/fundingcomment")
    public BaseResponse<List<CommentResDto>> readFundingComments(HttpServletRequest request, @RequestParam(name = "funding") Long funding_id) {
        return new BaseResponse<>(fundingService.getCommentByFunding(request, funding_id));
    }


    /**
     * 게시글 댓글 작성 API
     * [POST] /fundingcomment/create
     * @param request          HttpServletRequest
     * @param commentInsertReq 댓글 생성 모델
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/fundingcomment/create")
    public BaseResponse<String> createFundingComments(HttpServletRequest request, @RequestBody CommentInsertReq commentInsertReq) {
        log.info("펀딩 댓글 생성 모델={}, ", commentInsertReq.toString());
        return new BaseResponse<>(fundingService.insertCommentByUser(request, commentInsertReq));
    }

    /**
     * 게시글 댓글 수정 API
     * [POST] /fundingcomment/update
     * @param request          HttpServletRequest
     * @param updateReqDto     댓글 수정 모델
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/fundingcomment/update")
    public BaseResponse<String> updateFundingComment(HttpServletRequest request, @RequestBody CommentUpdateReqDto updateReqDto) {
        return new BaseResponse<>(fundingService.updateFundingComment(request, updateReqDto));
    }

    /**
     * 게시글 댓글 삭제 API
     * [DELETE] /fundingcomment/delete
     * @param request     HttpServletRequest
     * @param comment_id  댓글 고유 id
     * @return BaseResponse<String>
     */
    @ResponseBody
    @DeleteMapping("/fundingcomment/delete")
    public BaseResponse<String> deleteFundingComment(HttpServletRequest request, @RequestParam(name = "id") Long comment_id) {
        return new BaseResponse<>(fundingService.deleteFundingComment(request, comment_id));
    }

    /**
     * 로그인한 유저가 참여중인 펀딩 리스트 반환 API
     * [GET] /participants/currentuser
     *
     * @param request    HttpServletRequest
     * @param state      (Optional) 참여자 상태
     * @param funding_id (Optional) 게시글 고유 id
     * @return BaseResponse<List < FundingParticipants>>
     */
    @ResponseBody
    @GetMapping("/participants/currentuser")
    public BaseResponse<List<FundingParticipants>> findParitipantsByLoginUser(HttpServletRequest request,
                                                                              @RequestParam(name = "state", required = false) String state,
                                                                              @RequestParam(name = "funding", required = false) Long funding_id) {
        /*<펀딩 참여자 테이블 상태값 목록>
        Funding_Participants 테이블의 상태(state):
        ACTIVE: 참여자가 펀딩 결제를 완료 상태
        COMPLETE: 신청한 펀딩 성공 상태
        REFUND_NEEDED: 신청한 펀딩이 실패하여 환불 필요 상태
        REFUND_COMPLETE: 환불 완료 상태*/

        return new BaseResponse<>(fundingService.getParticipantsByLoginUserAndStateAndFunding(request, state, funding_id));
    }


    /**
     * 한 유저가 참여중인 펀딩 리스트 반환 API
     * [GET] /participants/user
     *
     * @param user_id    (Optional) 유저 고유 id
     * @param state      (Optional) 펀딩 참여자 상태
     * @param funding_id (Optional) 게시글 고유 id
     * @return BaseResponse<List < FundingParticipants>>
     */
    @ResponseBody
    @GetMapping("/participants/user")
    public BaseResponse<List<FundingParticipants>> findParitipantsByCondition(@RequestParam(name = "user", required = false) Long user_id,
                                                                              @RequestParam(name = "state", required = false) String state,
                                                                              @RequestParam(name = "funding", required = false) Long funding_id) {
        /*<펀딩 참여자 테이블 상태값 목록>
        Funding_Participants 테이블의 상태(state):
        ACTIVE: 참여자가 펀딩 결제를 완료 상태
        COMPLETE: 신청한 펀딩 성공 상태
        REFUND_NEEDED: 신청한 펀딩이 실패하여 환불 필요 상태
        REFUND_COMPLETE: 환불 완료 상태*/

        if (user_id == null && state == null && funding_id == null) {
            throw new BaseException(ALL_EMPTY_CONDITION);
        } else {
            return new BaseResponse<>(fundingService.getParticipantsByUserAndStateAndFunding(user_id, state, funding_id));
        }
    }


    /**
     * 한 유저의 참여 펀딩 리스트 state 변경 API
     * [GET] /participants/{forwardState}
     * @param forwardState  동작어 (펀딩 참여, 펀딩 참여 취소 등)
     * @param request       HttpServletRequest
     * @param user_id       (Optional) 유저 고유 id
     * @param funding_id     게시글 고유 id
     * @return BaseResponse<List < FundingParticipants>>
     */
    @ResponseBody
    @GetMapping("/participants/{forwardState}")
    public BaseResponse<String> updatePartipantsState(HttpServletRequest request, @PathVariable(name = "forwardState") String forwardState,
                                                      @RequestParam(name = "id", required = false) Long user_id,
                                                      @RequestParam(name = "funding") Long funding_id) {
        switch (forwardState) {
            // 펀딩 참여 리스트에 추가할 유저 id와 펀딩 id
            case "join" -> {
                return new BaseResponse<>(fundingService.JoinParticipants(request, user_id, funding_id));
            }
            // 펀딩 참여 리스트에 삭제할 유저 id와 펀딩 id
            case "delete" -> {
                return new BaseResponse<>(fundingService.DeleteParticipants(request, user_id, funding_id));
            }
            // 특정 펀딩 모집 성공 시 해당 펀딩 id를 가진 참여자 state를 COMPLETE으로 변경
            case "complete" -> {
                return new BaseResponse<>(fundingService.updateParticipantsState(request, user_id, funding_id, "COMPLETE"));
            }
            // 특정 펀딩 모집 실패 시 해당 펀딩 id를 가진 참여자 state를 REFUND_NEEDED으로 변경
            case "fail" -> {
                return new BaseResponse<>(fundingService.updateParticipantsState(request, user_id, funding_id, "REFUND_NEEDED"));
            }
            // 펀딩 환불이 완료되면 참여자 state를 REFUND_COMPLETE으로 변경
            case "refund" -> {
                return new BaseResponse<>(fundingService.updateParticipantsState(request, user_id, funding_id, "REFUND_COMPLETE"));
            }
            default -> {
                throw new BaseException(INVALID_STATE);
            }
        }
    }


    /**
     * 로그인한 유저의 결제 정보 출력 API
     * [GET] /payment/find
     *
     * @param request HttpServletRequest
     * @param funding_id 게시글 고유 id
     * @return BaseResponse<FundingPayment>
     */
    @ResponseBody
    @GetMapping("/payment/find")
    public BaseResponse<FundingPayment> findPaymentInfoByUser(HttpServletRequest request, @RequestParam(name = "id") Long funding_id) {

        return new BaseResponse<>(fundingService.getPaymentInfoByLoginUser(request, funding_id));
    }


    /**
     * 유저의 결제 정보 생성 API
     * [POST] /payment/create
     * @param request          HttpServletRequest
     * @param paymentInsertReq 유저 결제 정보 입력 모델
     * @return BaseResponse<FundingPayment>
     */
    @ResponseBody
    @PostMapping("/payment/create")
    public BaseResponse<FundingPayment> createPaymentInfo(HttpServletRequest request, @RequestBody PaymentInsertReq paymentInsertReq) throws SQLException {

        if (validatePaymentFormReq(paymentInsertReq) != null) {
            return validatePaymentFormReq(paymentInsertReq);
        } else {
            return new BaseResponse<>(fundingService.InsertPaymentInfoByLoginUser(request, paymentInsertReq));
        }

    }

    // 유저 결제 정보 데이터 유효성 확인
    private BaseResponse<FundingPayment> validatePaymentFormReq(PaymentInsertReq paymentInsertReq) {
        if (paymentInsertReq.getBank_name() == null) {
            // 은행 이름 공란일때
            throw new BaseException(EMPTY_PAYMENT_BANK);
        }
        if (paymentInsertReq.getBank_account() == null) {
            // 은행 이름 공란일때
            throw new BaseException(EMPTY_PAYMENT_BANK_ACCOUNT);
        }
        return null; // 유효성 검사 통과
    }


}