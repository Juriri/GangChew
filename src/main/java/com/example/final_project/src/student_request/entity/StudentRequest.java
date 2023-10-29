package com.example.final_project.src.student_request.entity;

import com.example.final_project.common.Constant;
import com.example.final_project.common.entity.BaseEntity;
import com.example.final_project.src.funding.entity.FundingCategory;
import com.example.final_project.src.student_request.dto.StudentUpdateReqDto;
import com.example.final_project.src.user.entity.User;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
@Table(name = "Student_Request")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)
public class StudentRequest extends BaseEntity {
    // StudentRequestState를 사용하도록 정의
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    protected Constant.StudentRequestState state = Constant.StudentRequestState.ACTIVE;

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = true)
    private FundingCategory fundingCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", referencedColumnName = "id", nullable = true)
    private User writer;

    @Lob // CLOB 형식 컬럼
    @Column(name = "content", nullable = false)
    private String content;


    @Column(name = "like_count", nullable = false)
    private Long likeCount =  0L;   //좋아요

    @Column(name = "view_count", nullable = false)
    private Long viewCount =  0L;   //조회수

    /*
    @OneToMany(mappedBy = "studentRequest", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @OrderBy("id asc") //댓글 정렬
    private List<StudentComment> studentComments;

     */

    //게시판 제목 글자수 제한
    public void setTitle(String title){
        if(title.length() < 5 || title.length() > 100){
            throw new IllegalArgumentException("제목은 최소 5글자, 최대 100글자여야 합니다.");
        }
        this.title = title;
    }

    @Builder
    public StudentRequest(String title, FundingCategory fundingCategory, User writer, String content, Long likeCount, Long viewCount, String delYn, List<StudentComment> studentComments) {
        this.title = title;
        this.fundingCategory = fundingCategory;
        this.writer = writer;
        this.content = content;
        //this.studentComments = studentComments;
        this.likeCount = 0L;
        this.viewCount = 0L;
    }

    //좋아요 증가, 감소
    public void addLikeCount(int plus_num) {
        this.likeCount += plus_num;
    }
    public void minusLikeCount(int minus_num) {
        this.likeCount -= minus_num;
        if (this.likeCount < 0 ){
            this.likeCount = 0L;
        }
    }
    public void addViewCount(int plus_num){
        this.viewCount += plus_num;
    }
    public void updateCategory(FundingCategory fundingCategory){this.fundingCategory = fundingCategory;}
    public void updateWriter(User writer){this.writer = writer;}
    public void updateStudentRequest(StudentUpdateReqDto studentUpdateReqDto, FundingCategory fundingCategory) {
        this.title = studentUpdateReqDto.getTitle();
        this.fundingCategory = fundingCategory;
        this.content = studentUpdateReqDto.getContent();
    }

    public void updateState(Constant.StudentRequestState forwardState) {
        this.state = forwardState;
    }
}
