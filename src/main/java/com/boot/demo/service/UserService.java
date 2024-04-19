package com.boot.demo.service;

import com.boot.demo.dto.LoginDto;
import com.boot.demo.dto.TokenRequest;
import com.boot.demo.dto.TokenResponse;
import com.boot.demo.dto.UserFormDto;
import com.boot.demo.entity.RefreshToken;
import com.boot.demo.entity.User;
import com.boot.demo.jwt.TokenProvider;
import com.boot.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    //아이디 중복검사시 따로 빼야함(boolean)
    public boolean validateUserId(UserFormDto user) {
        User existUser = userRepository.findById(user.getId()).orElse(null);
        return existUser == null;
    }

    public void signup(UserFormDto user) {
        if (validateUserId(user)) {
            userRepository.save(User.createUser(user, passwordEncoder));
        } else {
            throw new RuntimeException("존재하는 아이디입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findById(username)
                .orElseThrow(()->new UsernameNotFoundException("존재하지 않는 유저"));
    }


    public TokenResponse tokenRefresh(TokenRequest request) throws Exception{
        //refresh 토큰까지 거부
        if (!tokenProvider.validateToken(request.getRefreshToken())){
            throw new IllegalAccessException("Unexpected token");
        }
        //refresh 토큰 정상
        RefreshToken refreshToken = refreshTokenService.findByRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        String accessToken = tokenProvider.createAccessToken(user, Duration.ofHours(2));
        String newRefreshToken = refreshToken.update(tokenProvider.createRefreshToken(Duration.ofDays(1))).getRefreshToken();

        return new TokenResponse(accessToken, newRefreshToken, user.getRole().getKey());

    }


    public TokenResponse login(LoginDto dto){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dto.getId(), dto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        //1. 해당 유저를 조회
        User user = userRepository.findById(authentication.getName()).orElseThrow(EntityNotFoundException::new);
        String newRefreshToken = tokenProvider.createRefreshToken(Duration.ofDays(1));

        //2. 해당 유저와 매핑된 리스레시 토큰 조회
        RefreshToken existRefreshToken = refreshTokenService.findByUser(user);

        //2-1. 기존에 리프레시 토큰이 없다면 토큰을 생성-저장
        if (existRefreshToken == null) {
            refreshTokenService.saveRefreshToken(new RefreshToken(user, newRefreshToken));
        } else {
            //2-2. 이미 존재한다면 새로 발급하여 update
            existRefreshToken.update(newRefreshToken);
        }

        //3. 엑세스 토큰을 발급하고
        String accessToken = tokenProvider.createAccessToken(user, Duration.ofHours(2));

        return new TokenResponse(accessToken, newRefreshToken, user.getRole().getKey());

    }

    public void logout(TokenRequest request){
        refreshTokenService.removeToken(request.getRefreshToken());
    }



















}

