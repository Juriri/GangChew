package com.example.final_project.security;

import com.example.final_project.common.Constant;
import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.src.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.final_project.common.response.BaseResponseStatus.*;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //입력받은 회원ID를 DB의 조회 결과가 없으면 가입되지않은 회원임을 예외로 던짐.
        com.example.final_project.src.user.entity.User user = userRepository.findUserByUsername(username)
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
