package com.springboot.dev_spring_boot_demo.controller.admin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.dev_spring_boot_demo.dto.CustomerDto;
import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.service.OrderService;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("customerName").ascending());
        
        // Lấy danh sách đơn hàng và nhóm theo thông tin khách hàng
        List<Order> allOrders = orderService.getAllOrders();
        
        // Nhóm đơn hàng theo email khách hàng
        Map<String, List<Order>> ordersByCustomer = allOrders.stream()
                .collect(Collectors.groupingBy(Order::getCustomerEmail));
        
        // Tạo danh sách khách hàng từ đơn hàng
        List<CustomerDto> customers = ordersByCustomer.entrySet().stream()
                .map(entry -> {
                    List<Order> customerOrders = entry.getValue();
                    Order firstOrder = customerOrders.get(0); // Lấy thông tin từ đơn hàng đầu tiên
                    
                    CustomerDto customer = new CustomerDto();
                    customer.setId(firstOrder.getId()); // Sử dụng ID của đơn hàng đầu tiên
                    customer.setCustomerName(firstOrder.getCustomerName());
                    customer.setCustomerEmail(firstOrder.getCustomerEmail());
                    customer.setCustomerPhone(firstOrder.getCustomerPhone());
                    customer.setShippingAddress(firstOrder.getShippingAddress());
                    customer.setOrderCount(customerOrders.size());
                    
                    return customer;
                })
                .collect(Collectors.toList());
        
        // Lọc theo từ khóa tìm kiếm nếu có
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            customers = customers.stream()
                    .filter(c -> 
                        (c.getCustomerName() != null && c.getCustomerName().toLowerCase().contains(searchLower)) ||
                        (c.getCustomerEmail() != null && c.getCustomerEmail().toLowerCase().contains(searchLower)) ||
                        (c.getCustomerPhone() != null && c.getCustomerPhone().toLowerCase().contains(searchLower)) ||
                        (c.getShippingAddress() != null && c.getShippingAddress().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }
        
        // Phân trang thủ công
        int start = Math.min(page * size, customers.size());
        int end = Math.min((page + 1) * size, customers.size());
        List<CustomerDto> pagedCustomers = customers.subList(start, end);
        
        model.addAttribute("customers", pagedCustomers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) customers.size() / size));
        model.addAttribute("totalItems", customers.size());
        
        return "admin/customers/list";
    }

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public String viewCustomer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin đơn hàng
            Order order = orderService.getOrderById(id);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
                return "redirect:/admin/customers";
            }
            
            // Lấy thông tin khách hàng từ đơn hàng
            CustomerDto customer = new CustomerDto();
            customer.setId(order.getId());
            customer.setCustomerName(order.getCustomerName());
            customer.setCustomerEmail(order.getCustomerEmail());
            customer.setCustomerPhone(order.getCustomerPhone());
            customer.setShippingAddress(order.getShippingAddress());
            
            // Lấy danh sách đơn hàng của khách hàng theo email
            List<Order> customerOrders = orderService.getAllOrders().stream()
                    .filter(o -> o.getCustomerEmail().equals(order.getCustomerEmail()))
                    .collect(Collectors.toList());
            
            // Tính toán thống kê
            Map<String, Object> customerStats = new HashMap<>();
            customerStats.put("totalOrders", customerOrders.size());
            
            // Tổng chi tiêu
            BigDecimal totalSpending = customerOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            customerStats.put("totalSpending", totalSpending);
            
            // Đơn hàng theo trạng thái
            long completedOrders = customerOrders.stream()
                    .filter(o -> "COMPLETED".equals(o.getOrderStatus()))
                    .count();
            long pendingOrders = customerOrders.stream()
                    .filter(o -> "PENDING".equals(o.getOrderStatus()))
                    .count();
            long cancelledOrders = customerOrders.stream()
                    .filter(o -> "CANCELLED".equals(o.getOrderStatus()))
                    .count();
            
            customerStats.put("completedOrders", completedOrders);
            customerStats.put("pendingOrders", pendingOrders);
            customerStats.put("cancelledOrders", cancelledOrders);
            
            model.addAttribute("customer", customer);
            model.addAttribute("orders", customerOrders);
            model.addAttribute("customerStats", customerStats);
            
            return "admin/customers/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải thông tin khách hàng: " + e.getMessage());
            return "redirect:/admin/customers";
        }
    }
} 