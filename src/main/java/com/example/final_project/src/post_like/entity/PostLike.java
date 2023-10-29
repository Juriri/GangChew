package com.example.final_project.src.post_like.entity;

import com.example.final_project.common.Constant;
import com.example.final_project.common.entity.BaseEntity;
import com.example.final_project.src.user.entity.User;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
@Table(name = "PostLike")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)
public class PostLike extends BaseEntity  {

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private Constant.TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Builder
    public PostLike(User user, Constant.TargetType targetType, Long targetId) {
        this.user = user;
        this.targetType = targetType;
        this.targetId = targetId;
    }


    //좋아요를 누른 게시글이 펀딩 게시글인지, 학생 강의 요청 게시글인지 판별
    public Optional<Object> findTargetObject(Constant.TargetType targetType, Long targetId) {
        switch (targetType) {
            case FUNDING:
                // targetType이 FUNDING 경우, 펀딩 게시글을 찾아서 반환
                // 예: return Optional.ofNullable(postRepository.findById(targetId).orElse(null));
            case STUDENT_REQUEST:
                // targetType이 STUDENT_REQUEST 경우, 학생 강의 요청 게시글을 찾아서 반환
                // 예: return Optional.ofNullable(commentRepository.findById(targetId).orElse(null));
            default:
                return Optional.empty();
        }
    }



}
