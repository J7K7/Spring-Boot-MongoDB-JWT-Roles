package com.nosql.mongo.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {
    private static final String JWT_KEY = "MeHoonNa";
    private static final String JWT_EMAIL_VEIFY_KEY = "LetsDoIt";
    private static final String JWT_PASS_RESET_KEY = "CompleteIt";
    private static final String JWT_ISSUER = "MyCompany";
    private Algorithm algorithm = Algorithm.HMAC256(JWT_KEY);
    private Algorithm algorithmForValidateEmail = Algorithm.HMAC256(JWT_EMAIL_VEIFY_KEY);
    private Algorithm algorithmForResetPass = Algorithm.HMAC256(JWT_PASS_RESET_KEY);

    public String createToken(String userId){
        return JWT.create()
                .withIssuer(JWT_ISSUER)
                .withSubject(userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 60 * 1000))
                .sign(algorithm);
    }

    public String createTokenForValidateEmail(String email){
        return JWT.create()
                .withIssuer(JWT_ISSUER)
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .sign(algorithmForValidateEmail);
    }

    public String createTokenForResetPassword(String email){
        return JWT.create()
                .withIssuer(JWT_ISSUER)
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .sign(algorithmForResetPass);
    }

    public boolean isValid(String token){
        try{
            JWT.require(algorithm).withIssuer(JWT_ISSUER).build().verify(token);
            return true;
        } catch (JWTVerificationException ex){
            return false;
        }
    }

    public boolean isValidEmailToken(String token){
        try {
            JWT.require(algorithmForValidateEmail).withIssuer(JWT_ISSUER).build().verify(token);
            return true;
        } catch (JWTVerificationException ex){
            return false;
        }
    }

    public boolean isValidResetPassToken(String token){
        try {
            JWT.require(algorithmForResetPass).withIssuer(JWT_ISSUER).build().verify(token);
            return true;
        } catch (JWTVerificationException ex){
            return false;
        }
    }

    public String retriveUserId(String token){
        var decodeJWT = JWT.decode(token);
        return String.valueOf(decodeJWT.getSubject());
    }

    public String retrieveEmail(String token){
        var decodeJWT = JWT.decode(token);
        return String.valueOf(decodeJWT.getSubject());
    }
}
