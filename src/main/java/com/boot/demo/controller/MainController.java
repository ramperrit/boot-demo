package com.boot.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    //@CrossOrigin("http://localhost:3000") //가장 간단한 방법, 설정없음 : 모든 Orgin CORs 허용
    @GetMapping("/main")
    public String getMain(Authentication authentication){
        System.out.println(authentication.getName());
        System.out.println(authentication.getAuthorities());
        return "Hello World";
    }
}
