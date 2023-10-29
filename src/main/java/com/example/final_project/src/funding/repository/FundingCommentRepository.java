package com.example.final_project.src.funding.repository;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.funding.entity.FundingComment;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundingCommentRepository extends JpaRepository<FundingComment, Long> {
    // 특정 펀딩 객체(펀딩 id) 를 가진 댓글 카운트
    Long countFundingCommentByFunding(Funding funding);

    // 특정 펀딩 객체(유저 id) 를 가진 댓글 모두 출력
    List<FundingComment> findFundingCommentsByWriter(User writer);

    // 특정 펀딩 객체(게시글 id) 를 가진 댓글 모두 출력
    List<FundingComment> findFundingCommentsByFunding(Funding funding);

    Optional<FundingComment> findFundingCommentById(Long commentId);
}


