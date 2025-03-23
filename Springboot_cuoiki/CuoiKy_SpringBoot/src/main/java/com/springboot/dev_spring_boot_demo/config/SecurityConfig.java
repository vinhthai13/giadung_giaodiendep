package com.springboot.dev_spring_boot_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/products/**", "/about", "/contact", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/cart/**").permitAll() // Cho phép truy cập giỏ hàng mà không cần đăng nhập
                .requestMatchers("/orders/checkout").authenticated() // Yêu cầu đăng nhập cho trang thanh toán
                .requestMatchers("/profile/**").authenticated()  // Yêu cầu đăng nhập cho tất cả đường dẫn /profile/**
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler()) // Sử dụng custom success handler
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );
        
        return http.build();
    }
    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setUseReferer(true); // Sử dụng referer để chuyển hướng
        
        // Nếu có tham số checkout=true, chuyển hướng đến trang thanh toán
        handler.setDefaultTargetUrl("/orders/checkout");
        
        return handler;
    }
} 