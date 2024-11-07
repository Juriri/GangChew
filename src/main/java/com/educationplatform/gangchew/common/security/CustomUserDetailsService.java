package com.educationplatform.gangchew.common.security;

import com.educationplatform.gangchew.common.Constant;
import com.educationplatform.gangchew.common.exceptions.BaseException;
import com.educationplatform.gangchew.repository.user.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.educationplatform.gangchew.common.response.BaseResponseStatus.*;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //입력받은 회원ID를 DB의 조회 결과가 없으면 가입되지않은 회원임을 예외로 던짐.
        com.educationplatform.gangchew.entity.user.User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        // Role_User 권한을 List화
        List<Constant.RoleType> roleList = new ArrayList<>();
        roleList.add(Constant.RoleType.USER);

        /*log.info("UserDetails loadUserByUsername {}, ", username);*/

        // 사용자의 권한(Role)을 문자열 배열로 변환
        String[] roles = roleList.stream()
                .map(Enum::toString)
                .toArray(String[]::new);

        // 사용자 정보를 UserDetails 객체로 반환
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }

}
