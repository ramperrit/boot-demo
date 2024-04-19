package com.boot.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFormDto {

    //@NotNull -> null check
    //@NotEmpty -> null check * ""
    //@NotBlank


    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String password;
}
