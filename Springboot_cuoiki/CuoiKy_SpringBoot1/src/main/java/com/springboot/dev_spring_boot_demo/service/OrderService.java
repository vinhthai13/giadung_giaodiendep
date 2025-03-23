package com.springboot.dev_spring_boot_demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.springboot.dev_spring_boot_demo.entity.Cart;
import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.entity.User;

@Service
public interface OrderService {
    List<Order> getRecentOrdersByUser(Long userId);
    List<Order> getAllOrdersByUser(Long userId);
    Order getOrderById(Long orderId);
    Order createOrder(Order order);
    Order updateOrder(Order order);
    List<Order> getAllOrders();
    List<Order> getOrdersByCustomerEmail(String email);
    List<Order> getOrdersByStatus(String status);
    Order createOrder(Cart cart, String customerName, String customerEmail, 
                     String customerPhone, String shippingAddress, 
                     String paymentMethod);
    Order updateOrderStatus(Long orderId, String status);
    Order updatePaymentStatus(Long orderId, String status);
    List<Order> findOrdersByUser(User user);
    String generateInvoice(Order order);
    
    // Additional methods for admin functionality
    List<Order> getRecentOrders(int count);
    void deleteOrder(Long id);
} 