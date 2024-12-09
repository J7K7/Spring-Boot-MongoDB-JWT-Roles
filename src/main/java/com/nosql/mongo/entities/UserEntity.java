package com.nosql.mongo.entities;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "user")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.NONE)
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    private String id;

    @NotBlank(message = "Username cannot be empty or blank")
    private String username;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be empty or blank")
    private String email;

    @NotBlank(message = "Password cannot be empty or blank")
    private String password;

    private String profileImg;

    @NotEmpty(message = "Roles cannot be empty")
    private List<String> roles;

    @Builder.Default
    private Boolean isVerified = false;

    @Builder.Default
    private Boolean isDeleted = false;

    @Builder.Default
    private Boolean isSubscribe = false;

    @NotEmpty(message = "Create Date is required")
    private LocalDateTime createdDateTime;
}
