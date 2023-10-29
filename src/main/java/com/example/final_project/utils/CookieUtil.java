package com.example.final_project.utils;

import org.springframework.util.SerializationUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

public class CookieUtil {
    public static Optional<String> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.ofNullable(cookie.getValue());
                }
            }
        }

        return Optional.empty();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, Date expirationDate) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 어떤 경로에서든 쿠키에 접근 가능
        cookie.setSecure(true);
        cookie.setHttpOnly(true); // JavaScript에서 쿠키에 접근하지 못하도록 설정
        // 만료 시간을 계산하여 설정
        long maxAgeInSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
        cookie.setMaxAge((int) maxAgeInSeconds);

        //SameSite=Strict:쿠키는 동일한 도메인 내에서만 공유
        //SameSite=Lax: 쿠키는 동일한 도메인 내에서만 공유되지만, 외부 사이트로의 GET 요청 시에는 쿠키가 전송
        //SameSite=None:쿠키는 동일한 도메인과 다른 도메인 모두에서 요청 시에 전송

        response.setHeader("Set-Cookie", name + "=" + value + "; Path=/; Max-Age=" + maxAgeInSeconds + "; SameSite=None");
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    public static String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.getValue())));
    }
}
