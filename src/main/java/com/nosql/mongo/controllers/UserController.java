package com.nosql.mongo.controllers;

import com.nosql.mongo.dtos.*;
import com.nosql.mongo.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAllUsers(){
        List<UserResponseDto> users = userService.getAllUsers();
        ApiResponseDto<List<UserResponseDto>> responseDto = new ApiResponseDto<>(true, "All users retrieved successfully", users);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/all-users")
    @PreAuthorize("hasRole('Admin')")
    ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAllUsersForAdmin(){
        List<UserResponseDto> users = userService.getAllUsersWithDeleted();
        ApiResponseDto<List<UserResponseDto>> responseDto = new ApiResponseDto<>(true, "All users retrieved successfully", users);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("")
    ResponseEntity<ApiResponseDto<UserResponseDto>> signupUser(@Valid @RequestBody UserRequestDto userRequestDto){
        UserResponseDto userResponseDto = userService.saveUser(userRequestDto);
        ApiResponseDto<UserResponseDto> responseDto = new ApiResponseDto<UserResponseDto>(true, "User created successfully. Please verify your email. An email has been sent to you.", userResponseDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/admin")
    ResponseEntity<ApiResponseDto<UserResponseDto>> signupAdmin(@Valid @RequestBody UserRequestDto userRequestDto){
        UserResponseDto userResponseDto = userService.saveAdmin(userRequestDto);
        ApiResponseDto<UserResponseDto> responseDto = new ApiResponseDto<UserResponseDto>(true, "Admin created successfully. Please verify your email. An email has been sent to you.", userResponseDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/send-email")
    ResponseEntity<ApiResponseDto<String>> sendEmail(@RequestParam(name = "email", required = true) String email){

        if (isValidEmail(email)) {
            ApiResponseDto<String> responseDto = new ApiResponseDto<>(false, "Invalid email format", null);
            return ResponseEntity.badRequest().body(responseDto); // 400 Bad Request
        }

        userService.checkAndSendMail(email);
        ApiResponseDto<String> responseDto = new ApiResponseDto<String>(true, "Email sent successfully", null);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/verify-email")
    ResponseEntity<ApiResponseDto<String>> verifyEmail(@RequestParam(name = "token", required = true) String token){
        userService.checkMailAndToken(token);
        ApiResponseDto<String> responseDto = new ApiResponseDto<String>(true, "Email verify successfully", null);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/forget-password-email")
    ResponseEntity<ApiResponseDto<String>> resetEmail(@RequestParam(name = "email", required = true) String email){

        if (isValidEmail(email)) {
            ApiResponseDto<String> responseDto = new ApiResponseDto<>(false, "Invalid email format", null);
            return ResponseEntity.badRequest().body(responseDto); // 400 Bad Request
        }

        userService.checkAndSendPasswordResetMail(email);
        ApiResponseDto<String> responseDto = new ApiResponseDto<String>(true, "Password reset mail sent successfully", null);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/verify-forget-password-token")
    ResponseEntity<ApiResponseDto<String>> checkResetPasswordToken(@RequestParam(name = "token", required = true) String token){
        if(userService.checkPasswordResetToken(token)){
            ApiResponseDto<String> responseDto = new ApiResponseDto<String>(true, "Password reset token verified", null);
            return ResponseEntity.ok(responseDto);
        }

        ApiResponseDto<String> responseDto = new ApiResponseDto<String>(false, "Token is invalid or expire", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    @PostMapping("/forget-password")
    ResponseEntity<ApiResponseDto<String>> resetPassword(@RequestParam(name = "token", required = true) String token, @RequestParam(name = "newPassword", required = true) String newPassword){
        if(userService.checkTokenAndResetPassword(token, newPassword)){
            ApiResponseDto<String> responseDto = new ApiResponseDto<String>(true, "Password reset successfully", null);
            return ResponseEntity.ok(responseDto);
        }

        ApiResponseDto<String> responseDto = new ApiResponseDto<String>(false, "Token is invalid or expire", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    @PostMapping("/changePassword")
    ResponseEntity<ApiResponseDto<String>> changePassword(@RequestParam(name = "currentPassword", required = true) String currentPassword, @RequestParam(name = "newPassword", required = true) String newPassword){
        if(userService.changePassword(currentPassword, newPassword)){
            ApiResponseDto<String> responseDto = new ApiResponseDto<String>(true, "Password change successfully", null);
            return ResponseEntity.ok(responseDto);
        }
        ApiResponseDto<String> responseDto = new ApiResponseDto<String>(false, "Password not change", null);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/login")
    ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto){
        LoginResponseDto loginResponseDto = userService.loginUser(loginRequestDto);
        ApiResponseDto<LoginResponseDto> responseDto = new ApiResponseDto<LoginResponseDto>(true, "Login Successfully", loginResponseDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/getUserByName")
    ResponseEntity<ApiResponseDto<UserResponseDto>> getUserByName(@RequestParam(name = "name", required = true) String username){
        UserResponseDto userResponseDto = userService.getUserByUsername(username);
        ApiResponseDto<UserResponseDto> responseDto = new ApiResponseDto<UserResponseDto>(true, "User get successfully", userResponseDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/addRoleToUser")
    @PreAuthorize("hasRole('Admin')")
    ResponseEntity<ApiResponseDto<UserResponseDto>> addRoleToUser(@RequestParam(name = "username", required = true) String username, @RequestParam(name = "role", required = true) String role){
        UserResponseDto userResponseDto = userService.addRoleToUser(username, role);
        ApiResponseDto<UserResponseDto> responseDto = new ApiResponseDto<UserResponseDto>(true, "Role added to user.", userResponseDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/update-profile")
    ResponseEntity<ApiResponseDto<UserResponseDto>> updateProfile(@RequestParam(name = "profile", required = true)MultipartFile profile) throws IOException {
        UserResponseDto userResponseDto = userService.updateUserProfile(profile);
        ApiResponseDto<UserResponseDto> responseDto = new ApiResponseDto<UserResponseDto>(true, "Profile updated.", userResponseDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/delete-user")
    @PreAuthorize("hasRole('Admin')")
    ResponseEntity<ApiResponseDto<UserResponseDto>> updateProfile(@RequestParam(name = "id", required = true)String id) {
        UserResponseDto userResponseDto = userService.deleteUser(id);
        ApiResponseDto<UserResponseDto> responseDto = new ApiResponseDto<UserResponseDto>(true, "User has been removed.", userResponseDto);
        return ResponseEntity.ok(responseDto);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email == null || !email.matches(emailRegex);
    }

}
