package com.example.final_project.src.student_request;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.jwt.JwtUtil;
import com.example.final_project.common.response.BaseResponseStatus;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.funding.entity.FundingComment;
import com.example.final_project.src.funding.model.FundingRes;
import com.example.final_project.src.funding.repository.FundingCategoryRepository;
import com.example.final_project.src.post_like.PostLikeRepository;
import com.example.final_project.src.post_like.entity.PostLike;
import com.example.final_project.src.student_request.dto.*;
import com.example.final_project.src.student_request.entity.StudentComment;
import com.example.final_project.src.student_request.entity.StudentRequest;
import com.example.final_project.src.user.UserRepository;
import com.example.final_project.src.user.entity.User;
import com.example.final_project.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.final_project.common.Constant.StudentRequestState.DELETE;
import static com.example.final_project.common.response.BaseResponseStatus.*;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;
    @Autowired
    private FundingCategoryRepository fundingCategoryRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private JwtUtil jwtUtil;


    @Transactional
    public StudentcreateResDto createStudentRequest(HttpServletRequest request, StudentReqDto studentReqDto) throws SQLException {
        //0. 헤더에서 jwt토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        // 토큰 없으면 미로그인으로 간주
        if(jwtToken == null){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        // 1. user 객체 추출
        String username = jwtUtil.extractUsername(jwtToken);
        // 유저 id로 유저 객체 조회
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        // 2. 펀딩 카테고리 객체 추출
        Long categotyId = studentReqDto.getCategory_id();
        FundingCategory fundingCategory = fundingCategoryRepository.findFundingCategoriesById(categotyId).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST_CATEGORY));

        //3. updateUser
        StudentRequest saveStudentRequest = studentRepository.save(studentReqDto.toEntity());
        //3-1 유저 객체 업데이트 완료
        saveStudentRequest.updateWriter(user);
        //4. updateCategory
        saveStudentRequest.updateCategory(fundingCategory);

        return new StudentcreateResDto(saveStudentRequest);
    }


    //게시글 리스트 조회
    @Transactional(readOnly = true)
    public List<StudentRequest> getAllStudentRequest(){
        List<StudentRequest> studentRequests = studentRepository.findAllBy();
        return studentRequests;
    }

    //페이지네이션
    @Transactional(readOnly = true)
    public List<StudentRequest> getStudentRequestBypage(List<StudentRequest> studentRequests, int pageNum, int postsPerPage) {
        // pageNum 예외 처리
        if(pageNum <=0){
            pageNum = 1;
        }

        int page = pageNum - 1;
        int totalPosts = studentRequests.size();
        int startIndex = page * postsPerPage;
        int endIndex = Math.min(startIndex + postsPerPage, totalPosts);

        return studentRequests.subList(startIndex, endIndex);
    }

    @Transactional(readOnly = true)
    public List<StudentRequest> getStudentRequestCategory(Long categoryId) {
        FundingCategory fundingCategory = fundingCategoryRepository.findFundingCategoriesById(categoryId).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST_CATEGORY));
        return studentRepository.findStudentRequestByFundingCategory(fundingCategory);
    }

    @Transactional(readOnly = true)
    public List<FundingCategory> getStudentRquestCategoryAllList() {
        return fundingCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StudentRequest> getStudentRequestByUser(Long uesrId) {
        User user = userRepository.findUserById(uesrId).orElseThrow(() ->
                new BaseException(NOT_FIND_USER));

        List<StudentRequest> studentRequests = studentRepository.findStudentRequestByWriter(user);

        return studentRequests;
    }

    @Transactional(readOnly = true)
    public FundingCategory getStudentRequestCategoryById(Long categoryId){
        return fundingCategoryRepository.findFundingCategoriesById(categoryId).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST_CATEGORY));
    }

    @Transactional
    public StudentRequest getStudentRequestByStudentRequest(Long studentrequestId) {
        StudentRequest studentRequest = studentRepository.findStudentRequestById(studentrequestId).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST));
        studentRequest.addViewCount(1);
        return studentRequest;
    }

    //좋아요 누른 게시글 표시
    @Transactional(readOnly = true)
    public List<Boolean> getLikeStudentRequestList(HttpServletRequest request, List<StudentRequest> studentRequestList) {
        CookieUtil.getCookie(request, "jwtToken");
        Optional<String> jwtCookie = CookieUtil.getCookie(request, "jwtToken");

        String jwtToken = null;
        if (jwtCookie.isPresent()) {
            jwtToken = jwtCookie.get();
        } else {
            List<Boolean> booleanList = new ArrayList<>();
            for (StudentRequest studentRequest : studentRequestList) {
                booleanList.add(false);
            }
            return booleanList;
        }

        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username).orElseThrow(() ->
                new BaseException(NOT_FIND_USER));

        List<Boolean> booleanList = new ArrayList<>();

        for (StudentRequest studentRequest : studentRequestList) {
            Long student_request_id = studentRequest.getId();
            Optional<PostLike> postLike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.STUDENT_REQUEST, student_request_id);
            if (postLike.isPresent()) {
                booleanList.add(true);
            } else {
                booleanList.add(false);
            }
        }
        return booleanList;
    }

    // 게시글 업데이트 처리
    @Transactional
    public String updateStudentRequestPost(HttpServletRequest request, StudentUpdateReqDto studentUpdateReqDto){
        // 헤더에서 jwt 토큰 추출 (요청 유효성 확인용)
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        // 현재 로그인한 유저 정보 가져오기
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        Long studentId = studentUpdateReqDto.getStudentId();
        String new_title = studentUpdateReqDto.getTitle();
        Long new_category_Id = studentUpdateReqDto.getCategory_id();
        String new_content = studentUpdateReqDto.getContent();

        // fundingcategoryId를 이용하여 funding 카테고리 객체 가져오기
        FundingCategory fundingCategory = fundingCategoryRepository.findFundingCategoriesById(new_category_Id)
                .orElseThrow(() -> new BaseException(NOT_FIND_FUNDING_CATEGORY));

        // studentrequestId를 이용하여 student 객체 가져오기
        StudentRequest studentRequest = studentRepository.findStudentRequestById(studentId).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST));

        // 로그인 유저와 게시글 작성자 불일치 하면 수정 권한 없음, 예외처리
        if (!username.equals(studentRequest.getWriter().getUsername())) {
            throw new BaseException(ACCESS_DENY_USER);
        }
        // studentRequest에 정보 업데이트 시작
        // title, category, content 업데이트
        studentRequest.updateStudentRequest(studentUpdateReqDto, fundingCategory);
        return "게시글 수정 완료되었습니다.";
    }

    @Transactional
    public String deleteStudentRequestPost(HttpServletRequest request, Long student_request_id){
        // 헤더에서 jwt 토큰 추출 (요청 유효성 확인용)
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if (jwtToken == null || jwtToken.isEmpty()){
            // 브라우저에 토큰값이 없으면, 로그아웃 상태 예외 처리
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        // 현재 로그인한 유저 정보 가져오기
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        StudentRequest studentRequest = studentRepository.findStudentRequestById(student_request_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_STUDENTREQUEST));

        studentRepository.delete(studentRequest);
        return "게시글이 삭제되었습니다.";
    }


    // 게시글 조회
    @Transactional
    public StudentDetailResDto selectStudentRequestPost(HttpServletRequest request, Long student_request_id){
        // 게시글 id로 펀딩 요청 게시글 객체 찾아 반환
        StudentRequest studentRequest = studentRepository.findStudentRequestById(student_request_id).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST));

        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        // 로그인 정보 불러오기
        User user;
        if (jwtToken == null || jwtToken.isEmpty()){
            // 토큰값이 없으면, 좋아요 확인 못하게 null 처리
            user = null;
            // 해당 게시글 조회수 1 증가
            log.info("1 증가");
            studentRequest.addViewCount(1);
            // 좋아요 상태가 false 인 응답 객체 반환
            return new StudentDetailResDto(studentRequest, studentRequest.getFundingCategory(), studentRequest.getWriter(), false, false);
        }

        // 로그인 상태 확인되면 user 정보 불러오기
        else {
            // 유저네임을 이용하여 유저 객체 찾기
            String username = jwtUtil.extractUsername(jwtToken);
            // userId를 이용하여 User 찾기
            user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));

            // 불러올 게시글에 내가 좋아요 눌렀는지 확인
            boolean liked = false;
            boolean isLoginUser = false;
            Optional<PostLike> postLike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.STUDENT_REQUEST, student_request_id);
            // 좋아요 내역이 존재하면, liked true로 변환
            if (postLike.isPresent()) {
                liked = true;
            }
            // 현재 로그인한 유저 id와 게시글 유저 id가 같은지 판단.
            if (user.getUsername().equals(studentRequest.getWriter().getUsername())){
                isLoginUser = true;
            }
            // 해당 게시글 조회수 1 증가
            log.info("1 증가");
            studentRequest.addViewCount(1);
            return new StudentDetailResDto(studentRequest, studentRequest.getFundingCategory(), studentRequest.getWriter(), liked, isLoginUser);
        }
    }


    @Transactional
    public String addStudentRequestPostLike(HttpServletRequest request, Long studentRequestId){
        CookieUtil.getCookie(request, "jwtToken");
        Optional<String> jwtCookie = CookieUtil.getCookie(request, "jwtToken");

        String jwtToken = null;
        if(jwtCookie.isPresent()){
            jwtToken = jwtCookie.get();
        }else {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username).orElseThrow(()
                    -> new BaseException(NOT_FIND_USER));

        Optional<PostLike> checkLike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.STUDENT_REQUEST, studentRequestId);
        //fill하트 -> 빈하트
        if (checkLike.isPresent()) {
            postLikeRepository.delete(checkLike.get());
            return "좋아요 목록에서 삭제했습니다.";
        }
        // 빈하트 -> fill하트
        else {
            PostLike newPostLike = new PostLike(user, Constant.TargetType.STUDENT_REQUEST, studentRequestId);
            postLikeRepository.save(newPostLike);
            return "좋아요 목록에 추가되었습니다.";
        }

    }

    @Transactional(readOnly = true)
    public List<StudentRequest> OrderByKeyword(List<StudentRequest> studentRequestList, String condition){
        //최신
        if (condition == null || condition.equals("newPosts")){
            studentRequestList.sort(Comparator.comparing(StudentRequest::getCreatedAt).reversed());
        }
        //좋아요
        else if (condition.equals("mostLike")) {
            studentRequestList.sort(Comparator.comparing(StudentRequest::getLikeCount).reversed());
        }
        //조회수
        else if (condition.equals("mostView")) {
            studentRequestList.sort(Comparator.comparing(StudentRequest::getViewCount).reversed());
        }else {
            throw new BaseException(INVALID_STATE);
        }

        return studentRequestList;
    }


    // 학생 강의 요청 게시글 state에 따라 가져오기
    @Transactional(readOnly = true)
    public List<StudentRequest> SortByState(List<StudentRequest> studentRequestList, String state){
        List<StudentRequest> resList = new ArrayList<>();
        // String state가 상수값으로 등록된 상태값인지 확인
        try {
            Constant.StudentRequestState studentRequestState = Constant.StudentRequestState.valueOf(state.toUpperCase());

            switch (studentRequestState) {
                case ACTIVE, INACTIVE, DELETE ->  {
                    // state 에 따른 모든 학생 게시글 리스트 가져오기
                    for(StudentRequest studentRequest: studentRequestList) {
                        // 매개 변수의 학생 강의 리스트의 state가 매개변수 state 와 같으면 결과 리스트에 추가
                        if(studentRequest.getState().equals(studentRequestState)) {
                            resList.add(studentRequest);
                        }
                    }
                    return resList;
                }
                default -> {
                    throw new BaseException(INVALID_STATE);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_STATE);
        }
    }

    @Transactional
    public String updatePostLike (HttpServletRequest request, Long student_request_id) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

        if(jwtToken == null || jwtToken.isEmpty()){
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }


        String username = jwtUtil.extractUsername(jwtToken);
        // 유저 id로 유저 객체 조회
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 게시글 id로 펀딩 객체 조회
        StudentRequest studentRequest = studentRepository.findStudentRequestById(student_request_id).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST));


        // user와 funding 객체를 이용하여 PostLike 찾기
        Optional<PostLike> checkPostLike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.STUDENT_REQUEST, student_request_id);

        // 이미 좋아요를 누른 경우, 좋아요 삭제
        if (checkPostLike.isPresent()) {
            // 좋아요 테이블에 해당 객체 삭제
            postLikeRepository.delete(checkPostLike.get());
            // 좋아요 카운트 -1
            studentRequest.minusLikeCount(1);
            return "좋아요 취소가 완료하였습니다.";
        }
        // 좋아요가 없는 경우, 좋아요 등록
        else {
            // 좋아요 테이블에 해당 객체 등록
            PostLike postLike = new PostLike(user, Constant.TargetType.STUDENT_REQUEST, student_request_id);
            postLikeRepository.save(postLike);
            // 좋아요 카운트 +1
            studentRequest.addLikeCount(1);
            return "좋아요가 등록되었습니다.";
        }
    }



    @Transactional(readOnly = true)
    public List<StudentRequest> getStudentListByKeyword(String keyword) {
        // 결과를 저장할 리스트 생성
        List<StudentRequest> combinedFundings = new ArrayList<>();

        // 1. 제목에서 키워드와 부분 일치하는 게시글 목록 가져오기
        List<StudentRequest> fundings1 = studentRepository.findStudentRequestByTitleContaining(keyword);
        combinedFundings.addAll(fundings1);

       /* // 2. 작성자 username에서 키워드와 부분 일치하는 게시글 목록 가져오기
        // 키워드와 부분일치하는 유저 리스트
        List<User> userList = userRepository.findUserByUsernameContaining(keyword);
        if (!userList.isEmpty()) {
            for (User user : userList){
                // 해당 이름에 속한 게시글 목록 가져오기
                List<StudentRequest> fundings2 = studentRepository.findStudentRequestsByWriterContaining(user);
                combinedFundings.addAll(fundings2);
            }
        }*/

        // 3. 카테고리명에서 키워드와 부분 일치하는 카테고리 객체 목록 가져오기
        List<FundingCategory> categories = fundingCategoryRepository.findFundingCategoriesByCategoryNameContaining(keyword);

        log.info("categories = {}", categories.toString());
        // 키워드를 지닌 카테고리명 검색 결과가 empty가 아닐 경우,
        if (!categories.isEmpty()) {
            for (FundingCategory category : categories){
                // 해당 카테고리에 속한 게시글 목록 가져오기
                List<StudentRequest> fundings3 = studentRepository.findStudentRequestsByFundingCategory(category);
                combinedFundings.addAll(fundings3);
            }
        }

        // 중복 제거
        Set<StudentRequest> uniqueFundings = new HashSet<>(combinedFundings);
        combinedFundings.clear();
        combinedFundings.addAll(uniqueFundings);

        // 게시글 리스트 반환
        return combinedFundings;
    }


    // 펀딩 리스트 중 좋아요 누른 게시글 표시 기능
    @Transactional(readOnly = true)
    public List<Boolean> getLikedFundingList(HttpServletRequest request, List<StudentRequest> fundingList) {
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);

        // 로그아웃 상태로 인해 좋아요 기능 처리 불가
        if (jwtToken == null || jwtToken.isEmpty()){
            // 반환할 리스트 확인 List<Boolean> boolList
            List<Boolean> booleanList = new ArrayList<>();//fundingList 길이만큼 생성
            // 매개변수로 받은 전체 펀딩 게시글에서
            for (StudentRequest studentRequest : fundingList) {
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
        for (StudentRequest studentRequest : fundingList) {
            // 게시글 id
            Long student_id = studentRequest.getId();
            // 유저 id, 게시글 id에 해당하는 좋아요 누른 펀딩 게시글 확인
            Optional<PostLike> postLike = postLikeRepository.findPostLikesByUserAndTargetTypeAndAndTargetId(user, Constant.TargetType.STUDENT_REQUEST, student_id);

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


    @Transactional
    public List<StudentResDto> StudentRequestTransferToRes (HttpServletRequest request, List<StudentRequest> fundingList, int totalItems) {
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

        List<StudentResDto> responseList = IntStream.range(0, fundingList.size())
                .mapToObj(i -> {
                    StudentRequest studentRequest = fundingList.get(i);
                    String writer = studentRequest.getWriter().getUsername();
                    Boolean liked = booleanList.get(i);
                    FundingCategory fundingCategory = fundingList.get(i).getFundingCategory();
                    String categoryname = fundingCategory.getCategoryName();
                    return new StudentResDto(studentRequest, fundingCategory, studentRequest.getWriter(), liked, totalItems);
                })
                .collect(Collectors.toList());

        return responseList;
    }


    @Transactional(readOnly = true)
    public List<StudentRequest> getStudentsBypage(List<StudentRequest> fundings, int pageNum, int itemsPerPage) {
        // 페이지 번호가 0부터 시작하므로, 사용자 입력값인 pageNum을 1 빼줍니다.
        int page = pageNum - 1;

        // 전체 게시물 수
        int totalItems = fundings.size();

        // 시작 인덱스 계산
        int startIndex = page * itemsPerPage;

        // 끝 인덱스 계산
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        // 페이지네이션을 적용하여 해당 범위의 게시물 추출
        List<StudentRequest> paginatedFundings = fundings.subList(startIndex, endIndex);

        return paginatedFundings;
    }

    @Transactional
    public String updateStudentRequestStateOnly(Long student_request_id, String forward_state) {
        // 학생 강의 게시글 찾기
        StudentRequest studentRequest = studentRepository.findStudentRequestById(student_request_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_STUDENTREQUEST));

        /*Student_Request 테이블의 상태(state):
        ACTIVE: 활성 상태
        INACTIVE: 비활성 상태
        DELETE: 삭제 상태*/


        try {
            Constant.StudentRequestState studentRequestState = Constant.StudentRequestState.valueOf(forward_state.toUpperCase());

            switch (studentRequestState) {
                case ACTIVE, INACTIVE, DELETE -> {
                    // 게시글 삭제 요청이 들어오면 해당 게시글의 댓글 삭제 먼저 처리
                    if (studentRequestState.equals(DELETE)) {
                        List<StudentComment> studentCommentList = commentRepository.findStudentCommentByStudentRequest(studentRequest);

                        for (StudentComment studentComment : studentCommentList) {
                            studentComment.updateState(Constant.StudentRequestCommentState.DELETE);
                        }
                    } // 게시글 이외 상태 변경 요청이면 댓글 자동 ACTIVE 변경
                    else {
                        List<StudentComment> studentCommentList = commentRepository.findStudentCommentByStudentRequest(studentRequest);

                        for (StudentComment studentComment : studentCommentList) {
                            studentComment.updateState(Constant.StudentRequestCommentState.ACTIVE);
                        }
                    }

                    // 상수값에 등록된 state면 업데이트 실행
                    studentRequest.updateState(studentRequestState);
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
}
