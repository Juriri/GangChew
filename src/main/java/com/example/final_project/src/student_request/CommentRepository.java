package com.example.final_project.src.student_request;

import com.example.final_project.src.student_request.entity.StudentComment;
import com.example.final_project.src.student_request.entity.StudentRequest;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<StudentComment, Long> {

    Optional<StudentComment> findStudentCommentByWriterAndStudentRequest(User user, StudentRequest studentRequest);

    //특정 게시글 객체(유저 id)를 가진 댓글 모두 출력
    List<StudentComment> findStudentCommentByWriter(User writer);

    //특정 게시글 객체(게시글 id)를 가진 댓글 모두 출력
    List<StudentComment> findStudentCommentByStudentRequest(StudentRequest studentRequest);

    Optional<StudentComment> findStudentCommentById(Long comment_id);

}
