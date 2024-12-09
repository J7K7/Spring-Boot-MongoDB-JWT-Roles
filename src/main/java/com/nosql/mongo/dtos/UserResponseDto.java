package com.nosql.mongo.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {
    String username;
    String email;
    String profileImg;
    String description;
}
