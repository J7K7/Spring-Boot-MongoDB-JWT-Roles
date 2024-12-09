package com.nosql.mongo.exception;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.nosql.mongo.dtos.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            UsernameNotFoundException.class,
            IllegalArgumentException.class,
            AccessDeniedException.class,
            TokenExpiredException.class,
            SignatureVerificationException.class,
            AlgorithmMismatchException.class,
            JWTVerificationException.class,
            RuntimeException.class,
            MethodArgumentNotValidException.class
    })
    ResponseEntity<ApiResponseDto<?>> handleUserExceptions(Exception ex){
        HttpStatus status;
        ApiResponseDto<String> responseDto = new ApiResponseDto<String>();
        responseDto.setSuccess(false);
        responseDto.setData(null);
        switch (ex) {
            case MethodArgumentNotValidException methodArgumentNotValidException -> {
                responseDto.setMessage("Validation Exception");
                ApiResponseDto<List<String>> responseListDto = new ApiResponseDto<List<String>>();
                List<String> errorList = new ArrayList<>();
                for(FieldError error : (((MethodArgumentNotValidException) ex)).getBindingResult().getFieldErrors()){
                    errorList.add(error.getField() + ": " + error.getDefaultMessage());
                }
                responseListDto.setData(errorList);
                status = HttpStatus.NOT_FOUND;
                return ResponseEntity.status(status).body(responseListDto);
            }
            case UsernameNotFoundException usernameNotFoundException -> {
                responseDto.setMessage(ex.getMessage());
                status = HttpStatus.NOT_FOUND;
            }
            case TokenExpiredException tokenExpiredException -> {
                responseDto.setMessage("Token has expired. Please log in again!");
                status = HttpStatus.UNAUTHORIZED;
            }
            case SignatureVerificationException signatureVerificationException -> {
                responseDto.setMessage("Invalid Token Signature. Please log in again!");
                status = HttpStatus.UNAUTHORIZED;
            }
            case AlgorithmMismatchException algorithmMismatchException -> {
                responseDto.setMessage("Algorithm mismatch. Please log in again!");
                status = HttpStatus.UNAUTHORIZED;
            }
            case JWTVerificationException jwtVerificationException -> {
                responseDto.setMessage("Invalid Token. Please log in again!");
                status = HttpStatus.UNAUTHORIZED;
            }
            case IllegalArgumentException illegalArgumentException -> {
                responseDto.setMessage(ex.getMessage());
                status = HttpStatus.BAD_REQUEST;
            }
            case AccessDeniedException accessDeniedException -> {
                responseDto.setMessage(ex.getMessage());
                status = HttpStatus.UNAUTHORIZED;
            }
            case null, default -> {
                responseDto.setMessage(ex.getMessage());
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return ResponseEntity.status(status).body(responseDto);
    }
}
