package com.example.final_project.src.user.entity;

import com.example.final_project.common.Constant;
import com.example.final_project.common.entity.BaseEntity;

import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@EntityScan
/*@Table(name = "MyUser")// Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.)*/
@Table(name = "MyUser", indexes = { // 고속 검색을 위해 username을 인덱스로 추가
        @Index(name = "idx_username", columnList = "username")
})

public class User extends BaseEntity {
    // UserState를 사용하도록 재정의
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    protected Constant.UserState state = Constant.UserState.ACTIVE;

/*    @Id  // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @SequenceGenerator(name = "num_sequence", sequenceName = "NUM_SEQUENCE", allocationSize = 1)
    private Long id;*/

    @Id // PK를 의미하는 어노테이션
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "num_sequence")
    @org.hibernate.annotations.GenericGenerator(
            name = "num_sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(
                            name = "sequence_name",
                            value = "NUM_SEQUENCE"
                    ),
                    @org.hibernate.annotations.Parameter(
                            name = "initial_value",
                            value = "1"
                    ),
                    @org.hibernate.annotations.Parameter(
                            name = "increment_size",
                            value = "1"
                    )
            }
    )
    private Long id;

    @Column(name = "username", nullable = false, updatable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "nickname", length = 20)
    private String nickname;

    @Column(name = "full_name", nullable = false, length = 20)
    private String fullname;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private Constant.SocialLoginType oauthProvider;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;


    @Builder
    public User(String username, String password, String nickname, String fullname, String email, String address, Constant.SocialLoginType oauthProvider) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.fullname = fullname;
        this.email = email;
        this.address = address;
        this.oauthProvider = oauthProvider;
    }

    public void updateName(String fullname) {
        this.fullname = fullname;
    }

    public void updateRefreshToken(String refreshToken)  {
        this.refreshToken = refreshToken;
    }

    public void updateAddress(String address) {this.address = address; }
}