package com.springboot.dev_spring_boot_demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmail(String email);
    
    List<Order> findByOrderStatus(String status);
    
    List<Order> findByPaymentStatus(String status);

    List<Order> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    // Added for admin dashboard
    List<Order> findTop5ByOrderByOrderDateDesc();
} 