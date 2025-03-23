package com.springboot.dev_spring_boot_demo.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.entity.OrderItem;
import com.springboot.dev_spring_boot_demo.entity.Product;
import com.springboot.dev_spring_boot_demo.service.OrderService;
import com.springboot.dev_spring_boot_demo.service.ProductService;
import com.springboot.dev_spring_boot_demo.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String dashboard(Model model) {
        try {
            // Tổng số sản phẩm
            long productCount = productService.getAllProducts().size();
            model.addAttribute("productCount", productCount);
            
            // Tổng số đơn hàng
            List<Order> allOrders = orderService.getAllOrders();
            model.addAttribute("orderCount", allOrders.size());
            
            // Thống kê đơn hàng theo trạng thái
            long pendingOrderCount = allOrders.stream()
                    .filter(o -> "PENDING".equals(o.getOrderStatus()))
                    .count();
            
            long completedOrderCount = allOrders.stream()
                    .filter(o -> "COMPLETED".equals(o.getOrderStatus()))
                    .count();
            
            long cancelledOrderCount = allOrders.stream()
                    .filter(o -> "CANCELLED".equals(o.getOrderStatus()))
                    .count();
            
            model.addAttribute("pendingOrderCount", pendingOrderCount);
            model.addAttribute("completedOrderCount", completedOrderCount);
            model.addAttribute("cancelledOrderCount", cancelledOrderCount);
            
            // Tổng số người dùng
            model.addAttribute("userCount", userService.findAll().size());
            
            // Tổng doanh thu
            BigDecimal totalRevenue = allOrders.stream()
                    .filter(o -> "COMPLETED".equals(o.getOrderStatus()))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            model.addAttribute("totalRevenue", totalRevenue);
            
            // Thống kê doanh thu theo tháng
            BigDecimal[] monthlyRevenue = new BigDecimal[12];
            for (int i = 0; i < 12; i++) {
                monthlyRevenue[i] = BigDecimal.ZERO;
            }
            
            int currentYear = LocalDateTime.now().getYear();
            
            for (Order order : allOrders) {
                if ("COMPLETED".equals(order.getOrderStatus()) && 
                    order.getOrderDate() != null && 
                    order.getOrderDate().getYear() == currentYear) {
                    
                    int month = order.getOrderDate().getMonthValue() - 1; // 0-based index
                    monthlyRevenue[month] = monthlyRevenue[month].add(order.getTotalAmount());
                }
            }
            
            // Chuyển đổi BigDecimal sang double array cho đồ thị
            List<Double> monthlyRevenueList = new ArrayList<>();
            for (BigDecimal amount : monthlyRevenue) {
                monthlyRevenueList.add(amount.doubleValue());
            }
            
            model.addAttribute("monthlyRevenue", monthlyRevenueList);
            
            // Đơn hàng gần đây (5 đơn gần nhất)
            List<Order> recentOrders = allOrders.stream()
                    .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                    .limit(5)
                    .collect(Collectors.toList());
            
            model.addAttribute("recentOrders", recentOrders);
            
            // Thống kê sản phẩm bán chạy
            Map<Long, Map<String, Object>> productStats = new HashMap<>();
            
            for (Order order : allOrders) {
                if ("COMPLETED".equals(order.getOrderStatus())) {
                    for (OrderItem item : order.getOrderItems()) {
                        Product product = item.getProduct();
                        Long productId = product.getId();
                        
                        if (!productStats.containsKey(productId)) {
                            Map<String, Object> stat = new HashMap<>();
                            stat.put("id", productId);
                            stat.put("name", product.getName());
                            stat.put("category", product.getCategory());
                            stat.put("soldCount", 0);
                            stat.put("revenue", BigDecimal.ZERO);
                            productStats.put(productId, stat);
                        }
                        
                        Map<String, Object> stat = productStats.get(productId);
                        Integer currentCount = (Integer) stat.get("soldCount");
                        stat.put("soldCount", currentCount + item.getQuantity());
                        
                        // Doanh thu = số lượng * giá bán
                        BigDecimal currentRevenue = (BigDecimal) stat.get("revenue");
                        BigDecimal itemRevenue = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        stat.put("revenue", currentRevenue.add(itemRevenue));
                    }
                }
            }
            
            // Top 5 sản phẩm bán chạy nhất
            List<Map<String, Object>> topProducts = productStats.values().stream()
                    .sorted((p1, p2) -> ((Integer) p2.get("soldCount")).compareTo((Integer) p1.get("soldCount")))
                    .limit(5)
                    .collect(Collectors.toList());
            
            model.addAttribute("topProducts", topProducts);
            
            // Thống kê khách hàng
            Map<String, Map<String, Object>> customerStats = new HashMap<>();
            
            for (Order order : allOrders) {
                if ("COMPLETED".equals(order.getOrderStatus())) {
                    String email = order.getCustomerEmail();
                    
                    if (!customerStats.containsKey(email)) {
                        Map<String, Object> customer = new HashMap<>();
                        customer.put("id", order.getId());
                        customer.put("customerName", order.getCustomerName());
                        customer.put("customerEmail", email);
                        customer.put("customerPhone", order.getCustomerPhone());
                        customer.put("orderCount", 0);
                        customer.put("totalSpending", BigDecimal.ZERO);
                        customerStats.put(email, customer);
                    }
                    
                    Map<String, Object> customer = customerStats.get(email);
                    Integer currentCount = (Integer) customer.get("orderCount");
                    customer.put("orderCount", currentCount + 1);
                    
                    // Tính tổng chi tiêu
                    BigDecimal currentSpending = (BigDecimal) customer.get("totalSpending");
                    customer.put("totalSpending", currentSpending.add(order.getTotalAmount()));
                }
            }
            
            // Top 5 khách hàng chi tiêu nhiều nhất
            List<Map<String, Object>> topCustomers = customerStats.values().stream()
                    .sorted((c1, c2) -> ((BigDecimal) c2.get("totalSpending")).compareTo((BigDecimal) c1.get("totalSpending")))
                    .limit(5)
                    .collect(Collectors.toList());
            
            model.addAttribute("topCustomers", topCustomers);
            
            return "admin/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            return "admin/dashboard";
        }
    }
} 