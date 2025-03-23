package com.springboot.dev_spring_boot_demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.dev_spring_boot_demo.entity.Cart;
import com.springboot.dev_spring_boot_demo.entity.CartItem;
import com.springboot.dev_spring_boot_demo.entity.Category;
import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.entity.OrderItem;
import com.springboot.dev_spring_boot_demo.entity.Product;
import com.springboot.dev_spring_boot_demo.entity.User;
import com.springboot.dev_spring_boot_demo.repository.CartItemRepository;
import com.springboot.dev_spring_boot_demo.repository.CartRepository;
import com.springboot.dev_spring_boot_demo.repository.CategoryRepository;
import com.springboot.dev_spring_boot_demo.repository.OrderItemRepository;
import com.springboot.dev_spring_boot_demo.repository.OrderRepository;
import com.springboot.dev_spring_boot_demo.repository.ProductRepository;
import com.springboot.dev_spring_boot_demo.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class RestApiController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/order-items")
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {
        List<OrderItem> orderItems = orderItemRepository.findAll();
        return ResponseEntity.ok(orderItems);
    }
    
    @GetMapping("/carts")
    public ResponseEntity<List<Cart>> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        return ResponseEntity.ok(carts);
    }
    
    @GetMapping("/cart-items")
    public ResponseEntity<List<CartItem>> getAllCartItems() {
        List<CartItem> cartItems = cartItemRepository.findAll();
        return ResponseEntity.ok(cartItems);
    }
    
    // Endpoint đơn giản để test
    @GetMapping("/test")
    public String test() {
        return "API đang hoạt động!";
    }
} 