package com.springboot.dev_spring_boot_demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.dev_spring_boot_demo.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
} 