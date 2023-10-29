package com.example.final_project.src.user;

import com.example.final_project.common.Constant;
import com.example.final_project.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByEmailAndOauthProvider(String email, Constant.SocialLoginType oauthProvider);
    Optional<User> findUserById(Long Id);

    // 유저 주소 정보 업데이트
    @Modifying
    @Query("UPDATE User u SET u.address = :address WHERE u.id = :id")
    void updateUserAddressById(@Param("id") Long id, @Param("address") String address);


    // 유저 정보 업데이트
    @Modifying
    @Query("UPDATE User u SET u.fullname = :fullname, u.nickname = :nickname, u.email = :email, u.address = :address WHERE u.id = :id")
    void updateUserDetailsById(@Param("id") Long id, @Param("fullname") String fullname, @Param("nickname") String nickname, @Param("email") String email, @Param("address") String address);

    // 이름과 부분일치하는 유저 리스트 반환
    List<User> findUserByUsernameContaining(String username);
}
