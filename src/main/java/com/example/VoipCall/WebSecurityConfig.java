package com.example.VoipCall;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/signal").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/calls/start").permitAll()
                        .requestMatchers("/api/users/role-view").permitAll()
                        .requestMatchers("/api/calls/save").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/admin/call-records").permitAll()
                        .requestMatchers("/api/admin/transcribe/**").permitAll()
                        .anyRequest().authenticated())
                .csrf().disable()
                .httpBasic();

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Allow all origins
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // configuration.setAllowCredentials(true); // Must be disabled when allowing
        // all origins

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}