package com.educationplatform.gangchew.repository.student_request;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.dto.student_request.StudentResDto;
import com.educationplatform.gangchew.entity.funding.FundingCategory;
import com.educationplatform.gangchew.entity.student_request.StudentRequest;
import com.educationplatform.gangchew.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentRequest, Long> {

    //전체 게시글 리스트
    List<StudentRequest> findAllBy();

    //특정 카테고리 Id를 가진 게시글 리스트
    List<StudentRequest> findStudentRequestByFundingCategory(FundingCategory fundingCategory);

    //좋아요 순으로 게시글 내림차순 리스트
    List<StudentRequest> findByOrderByLikeCountDesc();

    //조회수 순으로 게시글 내림차순 리스트
    List<StudentRequest> findByOrderByViewCountDesc();

    //유저Id 게시글 리스트
    List<StudentRequest> findStudentRequestByWriter(User user);

    Optional<StudentRequest> findStudentRequestById(Long id);

    //State에 따라 학생 강의 요청 게시글 가져오기
    List<StudentRequest> findStudentRequestsByState(Constant.StudentRequestState state);

    // 키워드가 제목에 속하는 학생 게시글 가져오기
    List<StudentRequest> findStudentRequestByTitleContaining(String keyword);

    List<StudentRequest> findStudentRequestsByWriterContaining(User user);

    List<StudentRequest> findStudentRequestsByFundingCategory(FundingCategory category);
}
