package com.boot.demo.service;

import com.boot.demo.entity.RefreshToken;
import com.boot.demo.entity.User;
import com.boot.demo.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken){
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 토큰"));
    }

    public RefreshToken findByUser(User user){
        return refreshTokenRepository.findByUser(user).orElse(null);
    }

    public void saveRefreshToken(RefreshToken refreshToken){
        refreshTokenRepository.save(refreshToken);
    }

    public void removeToken(@Valid String refreshToken){
        RefreshToken findToken = refreshTokenRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(EntityNotFoundException::new);
        refreshTokenRepository.delete(findToken);
    }


}
