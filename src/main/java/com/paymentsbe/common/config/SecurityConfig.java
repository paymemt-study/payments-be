package com.paymentsbe.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 임시 비활성화 (폼 로그인 안 쓸 거면)
                .csrf(AbstractHttpConfigurer::disable)
                // 모든 요청 허용
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        // 세션/폼로그인 등 다른 기본 설정도 전부 끔
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
