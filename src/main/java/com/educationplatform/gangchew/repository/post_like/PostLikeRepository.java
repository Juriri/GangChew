package com.educationplatform.gangchew.repository.post_like;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.common.Constant.*;
import com.educationplatform.gangchew.entity.funding_cart.FundingCart;
import com.educationplatform.gangchew.entity.post_like.PostLike;
import com.educationplatform.gangchew.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findPostLikesByUserAndTargetTypeAndAndTargetId(User user, Constant.TargetType targetType, Long tartgetId);
    // LocalDateTime 현재시간과 마감시간 받아 해당 시간동안의 좋아요 리스트 반환
    int countPostLikeByCreatedAtBetweenAndTargetTypeAndTargetId(LocalDateTime currenttime, LocalDateTime deadline, Constant.TargetType targetType, Long targetId);
    List<PostLike> findPostLikeByCreatedAtBetweenAndTargetTypeAndTargetId(LocalDateTime currenttime, LocalDateTime deadline, Constant.TargetType targetType, Long targetId);

    // 내가 좋아요 누른 리스트 반환
    List<PostLike> findPostLikesByUser(User user);
}
