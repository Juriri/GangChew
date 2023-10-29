package com.example.final_project.src.funding.repository;

import static com.example.final_project.common.Constant.*;

import com.example.final_project.common.Constant;
import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.funding.entity.FundingParticipants;
import com.example.final_project.src.post_like.entity.PostLike;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    // 게시글 고유 id를 이용한 펀딩 게시글 조회
    Optional<Funding> findFundingById(Long Id);

    // 모든 펀딩 게시글 List
    List<Funding> findAllBy();

    // 유저 Id 조건 펀딩 게시글 List
    List<Funding> findFundingsByWriter(User user);

    // 유저 Id 조건 펀딩 게시글 List ( 특정 state 조건 포함)
    List<Funding> findFundingsByWriterAndState(User user, FundingCommentState fundingCommentState);

    // 제목 조건 펀딩 게시글 List
    List<Funding> findFundingsByTitleContaining(String keyword);

    // 부제목 조건 펀딩 게시글 List
    List<Funding> findFundingsBySubtitleContaining(String keyword);

    // 특정 카테고리 Id를 가진 펀딩 게시글 List
    List<Funding> findFundingsByFundingCategory(FundingCategory fundingCategory);


    // 특정 상태를 지닌 게시글 상태 리스트 조회
    List<Funding> findFundingsByState(FundingState state);

    // 지역 이름을 받아 해당 지역을 지닌 펀딩 게시글 조회
    List<Funding> findFundingsByLocation(String city);

    // 좋아요가 많은 순서대로 펀딩 게시글 가져오기 (내림차순 정렬)
    List<Funding> findByOrderByLikeCountDesc();

    // 조회수가 많은 순서대로 펀딩 게시글 가져오기 (내림차순 정렬)
    List<Funding> findFundingsByStateOrderByViewCountDesc(Constant.FundingState state);


    // stete IN_PROGRESS DFunding 의 마감일이 해당 시간을 지나지않고 가장 근접한 순서대로 펀딩 게시글 가져오기
    List<Funding> findFundingByStateOrderByDeadlineAsc(Constant.FundingState state);





}
