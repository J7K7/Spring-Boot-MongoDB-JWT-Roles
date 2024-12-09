package com.nosql.mongo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AppSecurityConfig {
    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    public AppSecurityConfig(JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.cors(cors -> cors.configurationSource(corsConfiguration()))
           .csrf(csrf -> csrf.disable())
           .sessionManagement(session -> session
                   .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           .authorizeHttpRequests(authorize -> authorize
                   .requestMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()
                   .requestMatchers(HttpMethod.POST, "/users", "/users/admin").permitAll()
                   .requestMatchers(HttpMethod.POST, "/users", "/users/forget-password").permitAll()
                   .requestMatchers(HttpMethod.POST, "/users", "/users").permitAll()
                   .requestMatchers(HttpMethod.POST, "/users", "/users/forget-password-email").permitAll()
                   .requestMatchers(HttpMethod.POST, "/users", "/users/send-email").permitAll()
                   .requestMatchers(HttpMethod.GET, "/users", "/users/verify-forger-password-token").permitAll()
                   .requestMatchers(HttpMethod.GET, "/users", "/users/verify-email").permitAll()
                   .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                   .anyRequest().authenticated()
           ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfiguration(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:8855"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",corsConfiguration);
        return source;
    }
}
