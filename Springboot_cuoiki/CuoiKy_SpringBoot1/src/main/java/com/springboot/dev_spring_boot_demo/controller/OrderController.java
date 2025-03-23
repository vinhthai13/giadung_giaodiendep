package com.springboot.dev_spring_boot_demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.springboot.dev_spring_boot_demo.entity.Cart;
import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.entity.User;
import com.springboot.dev_spring_boot_demo.service.CartService;
import com.springboot.dev_spring_boot_demo.service.OrderService;
import com.springboot.dev_spring_boot_demo.service.UserService;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping("/checkout")
    public String showCheckoutForm(@SessionAttribute("cartId") Long cartId, Model model) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName().equals("anonymousUser")) {
            // Nếu chưa đăng nhập, chuyển hướng đến trang đăng nhập
            return "redirect:/login?checkout=true";
        }
        
        Cart cart = cartService.getCart(cartId);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        
        // Lấy thông tin người dùng đã đăng nhập
        try {
            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + userEmail));
            
            // Điền sẵn thông tin người dùng vào form
            model.addAttribute("user", user);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thông tin người dùng: " + e.getMessage());
        }
        
        model.addAttribute("cart", cart);
        return "user/checkout";
    }

    @PostMapping("/create")
    public String createOrder(@SessionAttribute("cartId") Long cartId,
                            @RequestParam String customerName,
                            @RequestParam String customerEmail,
                            @RequestParam String customerPhone,
                            @RequestParam String shippingAddress,
                            @RequestParam String paymentMethod) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName().equals("anonymousUser")) {
            // Nếu chưa đăng nhập, chuyển hướng đến trang đăng nhập
            return "redirect:/login?checkout=true";
        }
        
        try {
            System.out.println("\n=== BẮT ĐẦU TẠO ĐƠN HÀNG ===");
            System.out.println("Cart ID: " + cartId);
            System.out.println("Customer: " + customerName + " - " + customerEmail + " - " + customerPhone);
            System.out.println("Address: " + shippingAddress);
            System.out.println("Payment: " + paymentMethod);
            System.out.println("User: " + authentication.getName());
            
            Cart cart = cartService.getCart(cartId);
            System.out.println("Đã lấy giỏ hàng: " + cart.getId());
            System.out.println("Số lượng sản phẩm: " + cart.getItems().size());
            
            Order order = orderService.createOrder(cart, customerName, customerEmail,
                    customerPhone, shippingAddress, paymentMethod);
            
            System.out.println("=== TẠO ĐƠN HÀNG THÀNH CÔNG ===");
            System.out.println("Order ID: " + order.getId());
            System.out.println("User ID: " + (order.getUser() != null ? order.getUser().getId() : "null"));
            System.out.println("Chuyển hướng đến: /orders/" + order.getId() + "/success");
            
            return "redirect:/orders/" + order.getId() + "/success";
        } catch (Exception e) {
            System.err.println("\n=== LỖI TẠO ĐƠN HÀNG ===");
            System.err.println("Loại lỗi: " + e.getClass().getName());
            System.err.println("Thông báo: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Nguyên nhân: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return "redirect:/cart?error=" + e.getMessage();
        }
    }

    @GetMapping("/{id}/success")
    public String showOrderSuccess(@PathVariable Long id, Model model) {
        try {
            System.out.println("=== HIỂN THỊ TRANG SUCCESS ===");
            System.out.println("Order ID: " + id);
            
            Order order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            model.addAttribute("invoice", orderService.generateInvoice(order));
            
            System.out.println("=== HIỂN THỊ THÀNH CÔNG ===");
            return "user/order-success";
        } catch (RuntimeException e) {
            System.err.println("=== LỖI HIỂN THỊ TRANG SUCCESS ===");
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/profile/orders?error=Order-not-found";
        }
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        try {
            Order order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            return "user/order-detail";
        } catch (RuntimeException e) {
            return "redirect:/profile/orders?error=Order-not-found";
        }
    }

    // Admin endpoints
    @GetMapping("/admin")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }

    @PostMapping("/admin/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            orderService.updateOrderStatus(id, status);
            return "redirect:/orders/admin";
        } catch (RuntimeException e) {
            return "redirect:/orders/admin?error=Cannot-update-order-status";
        }
    }

    @PostMapping("/admin/{id}/payment")
    public String updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            orderService.updatePaymentStatus(id, status);
            return "redirect:/orders/admin";
        } catch (RuntimeException e) {
            return "redirect:/orders/admin?error=Cannot-update-payment-status";
        }
    }
} 