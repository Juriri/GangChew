package com.example.final_project.src.student_request;

import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.jwt.JwtUtil;
import com.example.final_project.common.response.BaseResponseStatus;
import com.example.final_project.src.funding.entity.FundingComment;
import com.example.final_project.src.student_request.dto.CommentReqDto;
import com.example.final_project.src.student_request.dto.CommentResDto;
import com.example.final_project.src.student_request.dto.CommentUpdateReqDto;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.final_project.common.response.BaseResponseStatus.*;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public String createStudentComment(HttpServletRequest request, CommentReqDto commentReqDto){
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if(jwtToken == null || jwtToken.isEmpty()) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        // 로그인 유저 객체 찾기
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // 학생 강의 게시글 찾기
        Long student_request_id = commentReqDto.getStudent_request_id();
        StudentRequest studentRequest = studentRepository.findStudentRequestById(student_request_id).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST));

        // 댓글 모델에 작성자와 학생 게시글 객체 업데이트
        commentReqDto.updateCommentReq(user, studentRequest);
        StudentComment studentComment = commentReqDto.toEntity();

        //학생 게시글 작성 댓글을 DB에 저장
        commentRepository.save(studentComment);

        return "댓글이 추가되었습니다.";
    }

    @Transactional
    public String updateStudentComment(HttpServletRequest request, CommentUpdateReqDto commentUpdateReqDto){
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

        StudentComment studentComment = commentRepository.findStudentCommentById(comment_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_STUDENTREQUEST_COMMENT));

        studentComment.updateStudentComment(commentUpdateReqDto.getContent());

        return "댓글이 수정되었습니다.";
    }

    @Transactional
    public String deleteStudentComment(HttpServletRequest request, Long comment_id){
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        if(jwtToken == null) {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }
        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        StudentComment studentComment = commentRepository.findStudentCommentById(comment_id)
                .orElseThrow(() -> new BaseException(NOT_FIND_STUDENTREQUEST_COMMENT));

        commentRepository.delete(studentComment);

        return "댓글을 삭제했습니다.";
    }

    //댓글 리스트
    @Transactional(readOnly = true)
    public List<StudentComment> getStudentCommentByUser (HttpServletRequest request) {
        Optional<String> jwtCookie = CookieUtil.getCookie(request, "jwtToken");
        String jwtToken = null;
        if(jwtCookie.isPresent()) {
            jwtToken = jwtCookie.get();
        } else {
            throw new BaseException(NOT_FIND_LOGIN_SESSION);
        }

        String username = jwtUtil.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username).orElseThrow(() ->
                new BaseException(NOT_FIND_USER));

        return commentRepository.findStudentCommentByWriter(user);
    }

    @Transactional(readOnly = true)
    public List<CommentResDto> getStudentCommentByStudentRequest(HttpServletRequest request, Long student_request_id){

        // studentId 이용하여 student request 객체 조회
        StudentRequest studentRequest = studentRepository.findStudentRequestById(student_request_id).orElseThrow(() ->
                new BaseException(NOT_FIND_STUDENTREQUEST));

        // 학생 게시글의 댓글 리스트 가져오기
        List<StudentComment> commentList = commentRepository.findStudentCommentByStudentRequest(studentRequest);
        // 학생 게시글 작성자와 현재 로그인 사용자의 일치 여부를 저장할 배열 선언
        List<Boolean> booleanList = new ArrayList<>(Collections.nCopies(commentList.size(), false));
        List<CommentResDto> commentResDtos = new ArrayList<>();
        // 헤더에서 jwt 토큰 추출
        String jwtToken = jwtUtil.extractJwtTokenFromHeader(request);
        User user;
        if (jwtToken == null || jwtToken.isEmpty()){
            user = null;
            for(StudentComment studentComment : commentList) {
                commentResDtos.add(new CommentResDto(studentComment, false));
            }
            return commentResDtos;
        }
        else{
            // 유저네임을 이용하여 유저 객체 찾기
            String username = jwtUtil.extractUsername(jwtToken);
            user = userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));

            // 각 댓글의 리스트에서 작성자가 현재 로그인과 일치하는지 여부 확인
            for(StudentComment studentComment : commentList) {
                // 일치하면 true 값을 포함한 응답 모델 반환
                if(studentComment.getWriter().getUsername().equals(user.getUsername())) {
                    commentResDtos.add(new CommentResDto(studentComment, true));
                }
                else {
                    // 일치하지않으면 false 값을 포함한 응답 모델 반환
                    commentResDtos.add(new CommentResDto(studentComment, false));
                }
            }
            return commentResDtos;
        }
    }

}
