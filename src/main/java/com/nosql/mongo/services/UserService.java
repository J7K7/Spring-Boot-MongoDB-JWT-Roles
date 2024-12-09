package com.nosql.mongo.services;

import com.nosql.mongo.dtos.LoginRequestDto;
import com.nosql.mongo.dtos.LoginResponseDto;
import com.nosql.mongo.dtos.UserRequestDto;
import com.nosql.mongo.dtos.UserResponseDto;
import com.nosql.mongo.entities.UserEntity;
import com.nosql.mongo.repositories.UserRepository;
import com.nosql.mongo.security.JWTService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${spring.path.domain}")
    private String domain;

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final JavaMailSender mailSender;

    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, JWTService jwtService, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
    }

    public List<UserResponseDto> getAllUsers(){
        List<UserEntity> users = userRepository.findAll();
        List<UserResponseDto> userData = new ArrayList<>();
        for(UserEntity user : users){
            if(!user.getIsDeleted()) {
                UserResponseDto tmpUser = modelMapper.map(user, UserResponseDto.class);
                userData.add(tmpUser);
            }
        }
        return userData;
    }

    public List<UserResponseDto> getAllUsersWithDeleted(){
        List<UserEntity> users = userRepository.findAll();
        List<UserResponseDto> userData = new ArrayList<>();
        for(UserEntity user : users){
            UserResponseDto tmpUser = modelMapper.map(user, UserResponseDto.class);
            if(user.getIsDeleted()){
                tmpUser.setDescription("Deleted");
            }
            userData.add(tmpUser);
        }
        return userData;
    }

    @Transactional
    public UserResponseDto saveUser(UserRequestDto userRequestDto){
        try{
            UserEntity checkUser = userRepository.findByEmail(userRequestDto.getEmail().trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if(checkUser.getIsDeleted()){
                throw new IllegalArgumentException("Email is already in use");
            }
        } catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex.getMessage());
        } catch (Exception ex){

        }

        try{
            UserEntity checkUser = userRepository.findByUsername(userRequestDto.getUsername().trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if(checkUser.getIsDeleted()){
                throw new IllegalArgumentException("Username is already exists");
            }
        } catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex.getMessage());
        } catch (Exception ex){

        }

        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        UserEntity user = modelMapper.map(userRequestDto, UserEntity.class);
        List<String> roles = new ArrayList<>();
        roles.add("User");
        user.setRoles(roles);
        user.setCreatedDateTime(LocalDateTime.now());
        UserEntity savedUser = userRepository.save(user);
        UserResponseDto userResponseDto = modelMapper.map(savedUser, UserResponseDto.class);
        sendMailForVerifyEmail(user.getEmail());
        userResponseDto.setDescription("Email sent to your email address. Please verify email address");
        return userResponseDto;
    }

    @Transactional
    public UserResponseDto saveAdmin(UserRequestDto userRequestDto){
        try{
            UserEntity checkUser = userRepository.findByEmail(userRequestDto.getEmail().trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if(checkUser.getIsDeleted()){
                throw new IllegalArgumentException("Email is already in use");
            }
        } catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex.getMessage());
        } catch (Exception ex){

        }

        try{
            UserEntity checkUser = userRepository.findByUsername(userRequestDto.getUsername().trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if(checkUser.getIsDeleted()){
                throw new IllegalArgumentException("Username is already exists");
            }
        } catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex.getMessage());
        } catch (Exception ex){

        }

        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        UserEntity user = modelMapper.map(userRequestDto, UserEntity.class);
        List<String> roles = new ArrayList<>();
        roles.add("Admin");
        user.setRoles(roles);
        user.setCreatedDateTime(LocalDateTime.now());
        UserEntity savedUser = userRepository.save(user);
        UserResponseDto userResponseDto = modelMapper.map(savedUser, UserResponseDto.class);
        sendMailForVerifyEmail(user.getEmail());
        userResponseDto.setDescription("Email sent to your email address. Please verify email address");
        return userResponseDto;
    }

    public void checkAndSendMail(String email){
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        if(user.getIsVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        } else if(user.getIsDeleted()){
            throw new UsernameNotFoundException("User not found");
        }
        sendMailForVerifyEmail(email);
    }

    private void sendMailForVerifyEmail(String email){
        try {
            String token = jwtService.createTokenForValidateEmail(email);
            String verificationUrl = "http://" + domain + "users/verify-email?token=" + token;
            String emailBody = "<html><body>" +
                    "<h3>Hello,</h3>" +
                    "<p>Thank you for registering! Please click the button below to verify your email address.</p>" +
                    "<a href='" + verificationUrl + "' style='background-color: #4CAF50; color: white; padding: 15px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Verify Email</a>" +
                    "<p>If you didn't register with us, please ignore this email.</p>" +
                    "</body></html>";
            if (!sendMail(email, "Verify your email address", emailBody)){
                throw new IllegalArgumentException("Email Not Sent. Please Enter Valid Email Address");
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Email Not Sent. Please Enter Valid Email Address");
        }
    }

    public void checkMailAndToken(String token){
        try {
            if (jwtService.isValidEmailToken(token) && token != null) {
                String email = jwtService.retrieveEmail(token);
                UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Email Not Found"));
                if(user.getIsDeleted()){
                    throw new UsernameNotFoundException("User not found");
                }
                user.setIsVerified(true);
                String emailBody = "<html><body>" +
                        "<h3>Congratulations!</h3>" +
                        "<p>Your email address has been successfully verified.</p>" +
                        "<p>You can now log in to your account using your credentials.</p>" +
                        "<p>If you did not verify this email, please contact our support team.</p>" +
                        "</body></html>";

                sendMail(email, "Email Verified Successfully", emailBody);
                userRepository.save(user);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to send verification success email");
        }
    }

    public void checkAndSendPasswordResetMail(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        if(user.getIsDeleted()){
            throw new UsernameNotFoundException("User not found");
        }
        sendMailForForgotPassword(email);
    }

    public boolean checkPasswordResetToken(String token){
        try {
            if (jwtService.isValidResetPassToken(token) && token != null) {
                return true;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Token is invalid or expire");
        }
        return false;
    }

    public boolean checkTokenAndResetPassword(String token, String password){
        try {
            if (jwtService.isValidResetPassToken(token) && token != null) {
                String email = jwtService.retrieveEmail(token);
                UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Email not found"));
                if(user.getIsDeleted()){
                    throw new UsernameNotFoundException("User not found");
                }
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                return true;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Token is invalid or expire");
        }
        return false;
    }

    private void sendMailForForgotPassword(String email) {
        try {
            String token = jwtService.createTokenForResetPassword(email);

            String resetUrl = "http://" + domain + "users/verify-reset-password-token?token=" + token;

            String emailBody = "<html><body>" +
                    "<h3>Hello,</h3>" +
                    "<p>We received a request to reset your password. Click the button below to reset it.</p>" +
                    "<a href='" + resetUrl + "' style='background-color: #007BFF; color: white; padding: 15px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Reset Password</a>" +
                    "<p>If you didn't request this, please ignore this email.</p>" +
                    "</body></html>";

            sendMail(email, "Reset your password", emailBody);
        } catch (Exception e) {
            throw new IllegalArgumentException("Email Not Sent. Please Enter Valid Email Address");
        }
    }

    private boolean sendMail(String to, String subject, String body) throws MessagingException {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Transactional
    public boolean changePassword(String currentPassword, String newPassword){
        try{
            User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserEntity user = userRepository.findById(principal.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if(user.getIsDeleted()){
                throw new UsernameNotFoundException("User not found");
            }
            if(passwordEncoder.matches(currentPassword, user.getPassword())){
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            } else{
                throw new IllegalArgumentException("Password miss match");
            }
        } catch (Exception e){
            return false;
        }
    }

    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto){
        UserEntity user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        if(user.getIsDeleted()){
            throw new IllegalArgumentException("User not found");
        }
        if(!user.getIsVerified()){
            throw new IllegalArgumentException("Please verify email address");
        }
        if(!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("Email or Password is incorrect");
        }
        LoginResponseDto loginResponseDto = modelMapper.map(user, LoginResponseDto.class);
        loginResponseDto.setToken(jwtService.createToken(user.getId()));
        return loginResponseDto;
    }

    public UserResponseDto getUserByUsername(String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(user.getIsDeleted()){
            throw new UsernameNotFoundException("User not found");
        }
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Transactional
    public UserResponseDto addRoleToUser(String username, String role){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(user.getIsDeleted()){
            throw new UsernameNotFoundException("User not found");
        }
        user.getRoles().add(role);
        userRepository.save(user);
        return modelMapper.map(user, UserResponseDto.class);
    }

    public List<String> getRolesById(String id){
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(user.getIsDeleted()){
            throw new UsernameNotFoundException("User not found");
        }
        return user.getRoles();
    }

    @Transactional
    public UserResponseDto deleteUser(String id){
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setIsDeleted(true);
        userRepository.save(user);
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Transactional
    public UserResponseDto updateUserProfile(MultipartFile file) throws IOException {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = userRepository.findById(principal.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(user.getIsDeleted()){
            throw new UsernameNotFoundException("User not found");
        }
        if(isImage(file)){
            if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
                deleteOldProfileImage(user.getProfileImg());
            }

            String profileUrl = uploadFile(file);
            user.setProfileImg(profileUrl);
            UserEntity updateUser = userRepository.save(user);
            return modelMapper.map(updateUser, UserResponseDto.class);
        }
        throw new IllegalArgumentException("Profile not update");
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename != null && !originalFilename.isEmpty()) {

            Path uploadPath = Paths.get(System.getProperty("user.dir") + "/upload/images");

            try {
                // Ensure the directory exists
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

                // Create the new filename: original name + timestamp + extension
                String filename = originalFilename.substring(0, originalFilename.lastIndexOf("."))
                        + "_" + System.currentTimeMillis()
                        + extension;

                Path filePath = uploadPath.resolve(filename);

                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                return "/images/" + filename;
            } catch (IOException e) {
                throw new IOException("Failed to upload the file: " + file.getOriginalFilename(), e);
            }
        } else{
            throw new IOException("File not available");
        }
    }
    
    private void deleteOldProfileImage(String profileImgPath) throws IOException {
        if (profileImgPath != null && !profileImgPath.isEmpty()) {
            Path oldImagePath = Paths.get(System.getProperty("user.dir") + "/upload" + profileImgPath);

            // Check if the file exists and delete it
            if (Files.exists(oldImagePath)) {
                try {
                    Files.delete(oldImagePath);
                } catch (IOException e) {
                    throw new IOException("Failed to delete the old profile image: " + profileImgPath, e);
                }
            }
        }
    }
}
