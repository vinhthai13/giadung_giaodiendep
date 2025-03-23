package com.springboot.dev_spring_boot_demo.service;

import java.util.List;
import java.util.Optional;

import com.springboot.dev_spring_boot_demo.entity.User;

public interface UserService {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User save(User user);
    boolean changePassword(User user, String currentPassword, String newPassword);
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
    User registerNewUser(User user);
    List<User> findAll();
    
    // Additional methods for admin functionality
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User saveUser(User user);
    void deleteUser(Long id);
}