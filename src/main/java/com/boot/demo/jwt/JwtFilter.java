package com.boot.demo.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter { //필터체인에 한번만 구현하는 필터
    private final TokenProvider tokenProvider;

    private final static String HEADER_AUTHORIZATION = "Authentication";

    private final static String TOKEN_PREFIX = "Bearer "; //공백 주의 뒤에 토큰 붙음

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getAccessToken(request.getHeader(HEADER_AUTHORIZATION)); //토큰가져옴

        if (token != null && tokenProvider.validateToken(token)){
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);

    }

    private String getAccessToken(String authoricationHeader){
        if(authoricationHeader != null && authoricationHeader.startsWith(TOKEN_PREFIX)){ //받을때 Bearer 검증
            return authoricationHeader.substring(TOKEN_PREFIX.length());  //받을때 Bearer 땜
        }
        return null;
    }
}
