package com.springboot.dev_spring_boot_demo.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class Security {
    
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        
        // Cấu hình truy vấn để lấy user và kiểm tra enabled
        users.setUsersByUsernameQuery(
            "select username, password, true as enabled from users where username = ? and role is not null");
        
        // Cấu hình truy vấn để lấy role từ bảng users
        users.setAuthoritiesByUsernameQuery(
            "select username, role from users where username = ?");
        
        return users;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/products/**", "/about", "/contact",
                               "/register", "/login", "/images/**", "/css/**", "/js/**",
                               "/cart/**", "/orders/**", "/checkout/**", "/static/**",
                               "/fragments/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/authenticateTheUser")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(config -> 
                config.accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}
