package com.example.final_project.src.student_request.entity;

import com.example.final_project.common.Constant;
import com.example.final_project.common.entity.BaseEntity;
import com.example.final_project.src.user.entity.User;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)

@EqualsAndHashCode(callSuper = false)
@Getter
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
@Table(name = "Student_Comment")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)
public class StudentComment extends BaseEntity {
    // StudentRequestCommentState를 사용하도록 정의
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    protected Constant.StudentRequestCommentState state = Constant.StudentRequestCommentState.ACTIVE;

    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_request_id", referencedColumnName = "id", nullable = false)
    private StudentRequest studentRequest;

    @ManyToOne
    @JoinColumn(name = "writer_id", referencedColumnName = "id", nullable = false)
    private User writer;


    @Column(name = "content", nullable = false)
    private String content;

    public void update(String content){
        this.content = content;
    }

    @Builder
    private StudentComment(StudentRequest studentRequest, User writer, String content){
        this.studentRequest = studentRequest;
        this.writer = writer;
        this.content = content;
    }

    public void updateStudentRequest(StudentRequest studentRequest) {
        this.studentRequest = studentRequest;
    }

    public void updateWriter(User writer) {
        this.writer = writer;
    }

    public void updateStudentComment(String content){
        this.content = content;
    }

    public void updateState(Constant.StudentRequestCommentState forward_state) {
        this.state = forward_state;
    }
}
