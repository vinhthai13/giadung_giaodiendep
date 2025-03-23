package com.springboot.dev_spring_boot_demo.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private SimpleUrlAuthenticationSuccessHandler adminSuccessHandler = 
            new SimpleUrlAuthenticationSuccessHandler("/admin");
            
    private SimpleUrlAuthenticationSuccessHandler userSuccessHandler = 
            new SimpleUrlAuthenticationSuccessHandler("/");
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        boolean isAdmin = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        // Nếu là admin, chuyển hướng đến trang admin dashboard
        if (isAdmin) {
            adminSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        } else {
            // Kiểm tra xem có tham số checkout hay không
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("checkout=true")) {
                // Chuyển hướng đến trang thanh toán
                response.sendRedirect("/orders/checkout");
            } else {
                // Chuyển hướng đến trang chủ
                userSuccessHandler.onAuthenticationSuccess(request, response, authentication);
            }
        }
    }
} 