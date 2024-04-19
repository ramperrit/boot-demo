package com.boot.demo.controller;

import com.boot.demo.dto.LoginDto;
import com.boot.demo.dto.TokenRequest;
import com.boot.demo.dto.UserFormDto;
import com.boot.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid UserFormDto dto){
        try{
            userService.signup(dto);
            return new ResponseEntity<>("회원가입 완료", HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //login api 구현
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid LoginDto dto){
        try {
            return new ResponseEntity<>(userService.login(dto), HttpStatus.OK); //서비스 login 구현해야함
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //refreshToken
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid TokenRequest request){
        try {
            return new ResponseEntity<>(userService.tokenRefresh(request), HttpStatus.OK); //tokenRefresh 메소드 생성해야함
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
        }
    }
}
