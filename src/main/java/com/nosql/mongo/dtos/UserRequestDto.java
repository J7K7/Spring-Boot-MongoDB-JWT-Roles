package com.nosql.mongo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.NONE)
public class UserRequestDto {
    @NotBlank(message = "Username must be required")
    private String username;

    @NotBlank(message = "Email must be required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password must be required")
    private String password;
}
