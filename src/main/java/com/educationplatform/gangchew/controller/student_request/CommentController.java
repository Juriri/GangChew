package com.educationplatform.gangchew.controller.student_request;

import com.educationplatform.gangchew.common.exceptions.BaseException;
import com.educationplatform.gangchew.common.response.BaseResponse;
import com.educationplatform.gangchew.common.response.BaseResponseStatus;
import com.educationplatform.gangchew.dto.student_request.CommentReqDto;
import com.educationplatform.gangchew.dto.student_request.CommentResDto;
import com.educationplatform.gangchew.dto.student_request.CommentUpdateReqDto;
import com.educationplatform.gangchew.entity.student_request.StudentComment;
import com.educationplatform.gangchew.service.stundent_request.CommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.educationplatform.gangchew.common.response.BaseResponseStatus.STUDENTREQUESTCOMMENT_EMPTY_CONTENT;
import static com.educationplatform.gangchew.common.response.BaseResponseStatus.valueOf;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class CommentController {

    @Autowired
    private CommentService commentService;


    //댓글 리스트(유저)
    @ResponseBody
    @GetMapping("/studentcomment/writer")
    public BaseResponse<List<StudentComment>> getComments(HttpServletRequest request) {
        return new BaseResponse<>(commentService.getStudentCommentByUser(request));
    }

    //댓글 리스트(게시글)
    @ResponseBody
    @GetMapping("/studentcomment")
    public BaseResponse<List<CommentResDto>> getCommentsAll(HttpServletRequest request, @RequestParam(name = "id") Long student_request_id) {
        return new BaseResponse<>(commentService.getStudentCommentByStudentRequest(request, student_request_id));
    }

    //댓글 등록
    @ResponseBody
    @PostMapping("/studentcomment/save")
    public BaseResponse<String> saveStudentRequestComment(HttpServletRequest request, @RequestBody CommentReqDto commentReqDto) {
        log.info("댓글 생성 컨트롤러 로그={} ", commentReqDto.toString());
        if (validStudentRequestPosts(commentReqDto) != null) {
            return validStudentRequestPosts(commentReqDto);
        } else {
            return new BaseResponse<>(commentService.createStudentComment(request, commentReqDto));
        }

    }

    //댓글 유효성 검사
    private BaseResponse<String> validStudentRequestPosts(CommentReqDto commentReqDto) {
        if (commentReqDto.getContent() == null) {
            throw new BaseException(STUDENTREQUESTCOMMENT_EMPTY_CONTENT);
        }
        return null;
    }

    @ResponseBody
    @PostMapping("/studentcomment/update")
    public BaseResponse<String> updateStudentRequestComment(HttpServletRequest request, @RequestBody CommentUpdateReqDto updateReqDto) {
        return new BaseResponse<>(commentService.updateStudentComment(request, updateReqDto));
    }

    @ResponseBody
    @DeleteMapping("/studentcomment/delete")
    public BaseResponse<String> deleteStudentRequestComment(HttpServletRequest request, @RequestParam(name = "id") Long comment_id) {
        return new BaseResponse<>(commentService.deleteStudentComment(request, comment_id));
    }

}
