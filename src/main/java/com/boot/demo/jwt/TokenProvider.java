package com.boot.demo.jwt;

import com.boot.demo.entity.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";

    private final JwtProperties jwtProperties;

    //토큰생성
    public String createAccessToken(User user, Duration expiredAt){
        Date now = new Date(); //현재
        Date expiry = new Date(now.getTime() + expiredAt.toMillis());
        String authorities = user.getRole().getKey();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(user.getId())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    public String createRefreshToken(Duration expiredAt){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiredAt.toMillis());
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    //토큰검증
    public boolean validateToken(String token) {
        try{
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token);
            return true;
        }catch (Exception e){
            log.info("유효하지 않는 토큰");
            return false;
        }
    }

    //토큰으로 유저 정보 받아오기
    public Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token){
        Claims claims = getClaims(token);
        if(claims.get(AUTHORITIES_KEY) == null){
            throw new RuntimeException("권한 없는 토큰");
        }
        String userId = claims.getSubject();
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        //유저정보를 담은 토큰
        return new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(
                        userId, "", authorities
                ),
                token,
                authorities
        );
    }

}
