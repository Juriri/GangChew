package com.example.final_project.src.student_request.controller;

import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.common.response.BaseResponse;
import com.example.final_project.common.response.BaseResponseStatus;
import com.example.final_project.src.student_request.CommentService;
import com.example.final_project.src.student_request.dto.CommentReqDto;
import com.example.final_project.src.student_request.dto.CommentResDto;
import com.example.final_project.src.student_request.dto.CommentUpdateReqDto;
import com.example.final_project.src.student_request.entity.StudentComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

import static com.example.final_project.common.response.BaseResponseStatus.STUDENTREQUESTCOMMENT_EMPTY_CONTENT;
import static com.example.final_project.common.response.BaseResponseStatus.valueOf;

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
