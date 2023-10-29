package com.example.final_project.common.jwt;

import com.example.final_project.common.exceptions.BaseException;
import com.example.final_project.src.user.UserRepository;
import com.example.final_project.src.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.example.final_project.common.response.BaseResponseStatus.*;

@Service
@NoArgsConstructor
public class JwtUtil {
    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret-key}")
    private String JWT_SECRET_KEY;


    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

/*    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }*/

    /*
    JWT에서 유효기간 가져오기
    @return Authentication
    @throws BaseException
     */

    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        Long Id = Long.parseLong(extractClaim(token, Claims::getSubject));
        User user = userRepository.findById(Id)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        return user.getUsername();
    }

    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    public boolean isRefreshTokenExpired(String jwt) {
        String username = extractUsername(jwt);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        String refreshToken = user.getRefreshToken();
        return getExpirationDateFromToken(refreshToken).before(new Date());
    }


    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60))) // 1시간
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY)
                .compact();
    }

    public String createRefreshToken() {
        return Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365))) // 1 년
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY)
                .compact();
    }

/*    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        User user = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        return createToken(claims, user.getId().toString());
    }*/

    public String generateTokenByUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        return createToken(claims, user.getId().toString());
    }



    public String extractJwtTokenFromHeader(HttpServletRequest request) {
        String TOKEN_PREFIX = "Bearer ";
        String HEADER_STRING = "Authorization";

        String bearerToken = request.getHeader(HEADER_STRING);

        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}
