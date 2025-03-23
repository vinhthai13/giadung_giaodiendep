package com.springboot.dev_spring_boot_demo.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;
    
    @GetMapping("")
    public String listOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        
        // Set default values for orderStatus and paymentStatus if they are null
        for (Order order : orders) {
            if (order.getOrderStatus() == null || order.getOrderStatus().isEmpty()) {
                order.setOrderStatus("PENDING");
            }
            if (order.getPaymentStatus() == null || order.getPaymentStatus().isEmpty()) {
                order.setPaymentStatus("UNPAID");
            }
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", "orders");
        model.addAttribute("pageTitle", "Orders");
        return "admin/orders/list";
    }
    
    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order != null) {
                System.out.println("Order ID: " + order.getId());
                System.out.println("Order Status: " + order.getOrderStatus());
                System.out.println("Payment Status: " + order.getPaymentStatus());
                System.out.println("Items: " + (order.getOrderItems() != null ? order.getOrderItems().size() : 0));
                
                // Set default values if needed
                if (order.getOrderStatus() == null || order.getOrderStatus().isEmpty()) {
                    order.setOrderStatus("PENDING");
                }
                if (order.getPaymentStatus() == null || order.getPaymentStatus().isEmpty()) {
                    order.setPaymentStatus("UNPAID");
                }
                
                model.addAttribute("order", order);
                return "admin/orders/view";
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng với ID: " + id);
                return "redirect:/admin/orders";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải đơn hàng: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }
    
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
    
    @PostMapping("/update-payment/{id}")
    public String updatePaymentStatus(
            @PathVariable Long id, 
            @RequestParam("paymentStatus") String paymentStatus,
            RedirectAttributes redirectAttributes) {
        
        try {
            orderService.updatePaymentStatus(id, paymentStatus);
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái thanh toán thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái thanh toán: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
    
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("message", "Đơn hàng đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa đơn hàng: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
} 