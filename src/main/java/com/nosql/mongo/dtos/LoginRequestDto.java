package com.nosql.mongo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.NONE)
public class LoginRequestDto {

    @NotBlank(message = "Email must be required")
    @Email(message = "Email must be valid")
    String email;

    @NotBlank(message = "Password must be required")
    String password;
}
