package com.example.final_project.src.student_request.controller;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.response.BaseResponse;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.funding.model.FundingDetailRes;
import com.example.final_project.src.funding.model.FundingRes;
import com.example.final_project.src.student_request.CommentService;
import com.example.final_project.src.student_request.StudentService;
import com.example.final_project.src.student_request.dto.*;
import com.example.final_project.src.student_request.entity.StudentComment;
import com.example.final_project.src.student_request.entity.StudentRequest;
import com.example.final_project.src.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.final_project.common.response.BaseResponseStatus.*;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class StudentRequestController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private CommentService commentService;

    //전체 게시글 리스트 조회(카테고리, 조건별로 정렬)
    @ResponseBody
    @GetMapping("/studentrequest/all")
    public BaseResponse<List<StudentResDto>> allStudentRequestPosts(HttpServletRequest request,
                                                                       @RequestParam (name = "category", required = false) Long category_id,
                                                                       @RequestParam (name = "orderby", required = false) String condition,
                                                                       @RequestParam (name = "state" , required = false) String state,
                                                                       @RequestParam (name = "currentpage", required = false) Integer page_num,
                                                                       @RequestParam (name = "postsPerPage", required = false) Integer postsPerPage){

        // 페이지네이션 매개변수 누락 예외처리
        if (postsPerPage == null && page_num != null ) {
            throw new BaseException(EMPTY_PAGE_NUM);
        }

        // 1. 카테고리별 리스트 가져오기
        List<StudentRequest> studentRequestList1 = null;
        if (category_id == null || category_id <1) {
            studentRequestList1 = studentService.getAllStudentRequest();
        } else {
            studentRequestList1 = studentService.getStudentRequestCategory(category_id);
        }

        // 2. 최신순 or 좋아요순 or 조회수 순으로 리스트 정렬하기
        List<StudentRequest> studentRequestList2 = studentService.OrderByKeyword(studentRequestList1, condition);
        // 3. 강의요청 게시글 state 별로 가져오기 (ACTIVE, INACTIVE, DELETE)
        List<StudentRequest> studentRequestList3 = null;
        if (state == null) {
            studentRequestList3 = studentRequestList2;
        }
        else {
            studentRequestList3 = studentService.SortByState(studentRequestList2, state);
        }

        int totalItems = studentRequestList3.size();
        // 4. 페이지 네이션 적용
        List<StudentRequest> studentRequestList4 = null;
        if ((page_num == null && postsPerPage == null)) {
            studentRequestList4 = studentRequestList3;
        } else if (page_num != null || postsPerPage != null) {
            studentRequestList4 = studentService.getStudentRequestBypage(studentRequestList3, page_num, postsPerPage);
        } else {
            throw new BaseException(EMPTY_PAGE_NUM);
        }

        // 내가 좋아요 누른 게시글인지 확인
        List<Boolean> booleanList = studentService.getLikeStudentRequestList(request, studentRequestList4);
        List<StudentRequest> allStudentRequestList = studentRequestList4;

        // 게시글 응답 모델 생성
        List<StudentResDto> responseList = IntStream.range(0, studentRequestList4.size())
                .mapToObj(i -> {
                    StudentRequest studentRequest = allStudentRequestList.get(i);
                    User writer = studentRequest.getWriter();
                    boolean liked = booleanList.get(i);

                    // 댓글 리스트 가져오기
                    List<CommentResDto> commentResDtos = commentService.getStudentCommentByStudentRequest(request, studentRequest.getId());
                    // 댓글 수 반환
                    int comments = commentResDtos.size();
                    return new StudentResDto(studentRequest, studentRequest.getFundingCategory(), writer, liked, totalItems, comments);
                })
                .collect(Collectors.toList());


        return new BaseResponse<>(responseList);
    }

    @ResponseBody
    @GetMapping("/studentrequestList")
    public BaseResponse<List<StudentRequest>> readStudentRequestPosts(@RequestParam (name = "currentpage", required = false) Integer pageNum,
                                                                      @RequestParam (name = "postsPerPage", required = false) Integer postsPerPage,
                                                                      @RequestParam (name = "user", required = false) Long user_id,
                                                                      @RequestParam (name = "student_request", required = false) Long student_request_id) {

        if (student_request_id == null || student_request_id < 1) {
            if (user_id == null || user_id < 1) {
                throw new BaseException(ALL_EMPTY_CONDITION);
            } else {
                return new BaseResponse<>(studentService.getStudentRequestByUser(user_id));
            }
        } else {
            StudentRequest studentRequest = studentService.getStudentRequestByStudentRequest(student_request_id);
            if (studentRequest != null) {
                return new BaseResponse<>(Collections.singletonList(studentRequest));
            } else {
                throw new BaseException(NOT_FIND_STUDENTREQUEST);
            }
        }
    }

    //좋아요 클릭 - 테이블 저장
    @ResponseBody
    @GetMapping("/studentrequest/likeclick")
    public BaseResponse<String> addLikeClick(HttpServletRequest request, @RequestParam(name = "id") Long student_request_id) {
        return new BaseResponse<>(studentService.addStudentRequestPostLike(request, student_request_id));
    }

/*    //게시글 조회수, 좋아요수 조회
    @ResponseBody
    @GetMapping("/studentrequest/count")
    public BaseResponse<Map<String, Long>> ViewLikeCount(@RequestParam(name = "id") Long student_request_id) {
        Long viewCount = studentService.addViewCount(student_request_id);
        Long likeCount = studentService.addLikeCount(student_request_id);

        Map<String, Long> count = new HashMap<>();
        count.put("view", viewCount);
        count.put("like", likeCount);
        return new BaseResponse<>(count);
    }*/

    //카테고리 리스트
    @ResponseBody
    @GetMapping("/postcategory/all")
    public BaseResponse<List<FundingCategory>> getAllStudentRequestCategory() {
        return new BaseResponse<>(studentService.getStudentRquestCategoryAllList());
    }
    @ResponseBody
    @GetMapping("/postcategory")
    public BaseResponse<FundingCategory> getStudentRequestCategory(@RequestParam(name = "id") Long category_id) {
        return new BaseResponse<>(studentService.getStudentRequestCategoryById(category_id));
    }

    //게시글 작성
    @ResponseBody
    @PostMapping("/studentrequest/save")
    public BaseResponse<StudentcreateResDto> savePosts(HttpServletRequest request, @RequestBody StudentReqDto studentReqDto) throws SQLException {
        /*log.info("게시글 작성, getTitle={}", studentReqDto.getTitle());
        log.info("게시글 작성, getCategoryId={}", studentReqDto.getCategory_id());
        log.info("게시글 작성, getContent={}", studentReqDto.getContent());*/
        log.info("게시글 작성, getContent={}", studentReqDto.toString());
        if (validStudentRequestPosts(studentReqDto)!= null){
            return validStudentRequestPosts(studentReqDto);
        } else {
            return new BaseResponse<>(studentService.createStudentRequest(request, studentReqDto));

        }

    }

    //유효성 검사
    private BaseResponse<StudentcreateResDto> validStudentRequestPosts(StudentReqDto studentReqDto) {
        if (studentReqDto.getTitle() == null) {
            //제목 공란일 때 예회 처리
            throw new BaseException(STUDENTREQUEST_EMPTY_TITLE);
        }
        if (studentReqDto.getCategory_id() == null) {
            //카테고리 공란일 때 예외 처리
            throw new BaseException(STUDENTREQUEST_EMPTY_CATEGORY);
        }
        if(studentReqDto.getContent() == null) {
            //콘텐츠가 공란일 때 에외처리
            throw new BaseException(STUDENTREQUEST_EMPTY_BLOB);
        }

        return null;
    }

    @ResponseBody
    @PostMapping("/studentrequest/update")
    public BaseResponse<String> updatePost(HttpServletRequest request, @RequestBody StudentUpdateReqDto studentUpdateReqDto) {
        log.info("컨트롤러 수정모델, ={}", studentUpdateReqDto.toString());
        if (validStudentUpdateRequestPosts(studentUpdateReqDto) != null) {
            return validStudentUpdateRequestPosts(studentUpdateReqDto);
        } else {
            return new BaseResponse<>(studentService.updateStudentRequestPost(request, studentUpdateReqDto));
        }

    }

    //유효성 검사
    private BaseResponse<String> validStudentUpdateRequestPosts(StudentUpdateReqDto studentUpdateReqDto) {
        if (studentUpdateReqDto.getTitle() == null) {
            //제목 공란일 때 예회 처리
            throw new BaseException(STUDENTREQUEST_EMPTY_TITLE);
        }
        if (studentUpdateReqDto.getCategory_id() == null) {
            //카테고리 공란일 때 예외 처리
            throw new BaseException(STUDENTREQUEST_EMPTY_CATEGORY);
        }
        if(studentUpdateReqDto.getContent() == null) {
            //콘텐츠가 공란일 때 에외처리
            throw new BaseException(STUDENTREQUEST_EMPTY_BLOB);
        }

        return null;
    }

    /*
        String updatePost = studentService.update(user_id, student_request_id, studentReqDto);
        //글 수정을 완료하면 상세보기페이지로 이동
        String ru = "/studentrequestList" + updatePost;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", ru);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
         */

    @ResponseBody
    @DeleteMapping("/studentrequest/delete")
    public BaseResponse<String> deletePost(HttpServletRequest request, @RequestParam(name = "id") Long student_request_id) {
        return new BaseResponse<>(studentService.deleteStudentRequestPost(request, student_request_id));

    }

    /**
     * 학생강의 게시글 디테일 조회 API
     * [GET] /studentrequest/read?id={post_id}
     * @param post_id 게시글 고유 ID
     * @return BaseResponse<StudentRequest>
     */
    @ResponseBody
    @GetMapping("/studentrequest/read")
    public BaseResponse<StudentDetailResDto> readStudentPost(HttpServletRequest request, @RequestParam(name = "id") Long post_id) {
        return new BaseResponse<>(studentService.selectStudentRequestPost(request, post_id));
    }

    /*
    @ResponseBody
    @GetMapping("/studentrequest/{studentrequestId}")
    public BaseResponse<StudentReqDto> Postdetail(@PathVariable Long studentrequestId) {
        //studentrequestId를 사용하여 게시글 정보를 데이터베이스에서 조회
        StudentRequest studentRequest = studentService.getStudentRequestByStudentRequest(studentrequestId);
        //게시글을 찾았을 경우 해당 게시글의 정보를 StudentReqDto로 변환하여 클라이언트에 반환
        if (studentRequest != null) {
            StudentReqDto studentReqDto = new StudentReqDto(studentRequest.getTitle(), studentRequest.getId(), studentRequest.getWriter(), studentRequest.getContent(), studentRequest.getLikeCount(), studentRequest.getViewCount());
            return new BaseResponse<>(studentReqDto);
        } else {
            //게시글을 찾지 못했을 경우 예외 처리
            throw new BaseException(NOT_FIND_STUDENTREQUEST);
        }
    }
     */

    /**
     * 특정 조건의 학생 게시글 키워드 조회 API
     * [GET] /studentList
     * @param keyword (optional) 검색할 키워드
     * @param page_num    소분류: 현재 페이지 숫자 ---> default 모든 페이지 출력
     * @param itemsPerPage 한 페이지에 출력할 아이템 수
     * @return BaseResponse<List< FundingRes>>
     */
    @ResponseBody
    @GetMapping("/studentList")
    public BaseResponse<List<StudentResDto>> readStudentPostsByKeyword(
            HttpServletRequest request,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "currentpage", required = false) Integer page_num,
            @RequestParam(name = "itemsPerPage", required = false) Integer itemsPerPage) {

        if (keyword == null || keyword.isEmpty()){
            throw new BaseException(ALL_EMPTY_CONDITION);
        }

        // 헤더바 검색창으로 검색하여 찾는 경우 -> keyword가 있을 경우 입력한 키워드가 제목, 작성자 username, 카테고리 이름 에서 부분 일치하는 게시글 조회
        else {
            // 페이지 숫자 유효성 실패로 모든 키워드 리스트 반환
            if ((page_num == null && itemsPerPage == null)) {
                List<StudentRequest> studentList = studentService.getStudentListByKeyword(keyword);
                return new BaseResponse<>(studentService.StudentRequestTransferToRes(request, studentList, studentList.size()));
            }
            else if(page_num != null && itemsPerPage != null) {
                int totalItems = 0;
                // 페이지네이션 키워드 리스트 반환
                List<StudentRequest> fundingList5 = studentService.getStudentListByKeyword(keyword);
                totalItems = fundingList5.size();
                // 페이지네이션 실행
                List<StudentRequest> fundingList = studentService.getStudentsBypage(fundingList5, page_num, itemsPerPage);
                return new BaseResponse<>(studentService.StudentRequestTransferToRes(request, fundingList, totalItems));
            }
            else {
                throw new BaseException(EMPTY_PAGE_NUM);
            }
        }
    }

    /**
     * 좋아요 버튼 클릭 반응으로 PostLike 테이블에 저장할 API
     * [GET] /studentrequest/toggle-like
     *
     * @param request    HTTP 요청 객체
     * @param studentrequest_id 게시글 ID
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/studentrequest/toggle-like")
    public BaseResponse<String> toggleLikeUser(HttpServletRequest request, @RequestParam(name = "id") Long studentrequest_id) {
        return new BaseResponse<>(studentService.updatePostLike(request, studentrequest_id));
    }



    /**
     * 학생 게시글 상태값 업데이트 API
     * [GET] /studentrequest/update/state
     * @param student_id 학생 게시글 고유 ID
     * @param forward_state 변환하고 싶은 state 값
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/studentrequest/update/state")
    public BaseResponse<String> updatePostState(@RequestParam(name = "id") Long student_id, @RequestParam(name = "state") String forward_state) throws SQLException {
        log.info("게시글 업데이트 컨트롤러 진입, funding_id= {}, state={}", student_id, forward_state);
        return new BaseResponse<>(studentService.updateStudentRequestStateOnly(student_id, forward_state));
    }
}
