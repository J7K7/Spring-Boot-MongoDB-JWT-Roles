package com.nosql.mongo.dtos;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.NONE)
public class LoginResponseDto {
    String username;
    String email;
    String profileImg;
    String token;
}
