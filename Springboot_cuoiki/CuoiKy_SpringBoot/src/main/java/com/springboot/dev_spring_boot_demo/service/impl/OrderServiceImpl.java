package com.springboot.dev_spring_boot_demo.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.dev_spring_boot_demo.entity.Cart;
import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.entity.OrderItem;
import com.springboot.dev_spring_boot_demo.entity.Product;
import com.springboot.dev_spring_boot_demo.entity.User;
import com.springboot.dev_spring_boot_demo.repository.OrderRepository;
import com.springboot.dev_spring_boot_demo.repository.ProductRepository;
import com.springboot.dev_spring_boot_demo.service.CartService;
import com.springboot.dev_spring_boot_demo.service.OrderService;
import com.springboot.dev_spring_boot_demo.service.UserService;

import jakarta.mail.internet.MimeMessage;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + orderId));
    }

    @Override
    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email);
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByOrderStatus(status);
    }

    @Override
    @Transactional
    public Order createOrder(Cart cart, String customerName, String customerEmail, 
                           String customerPhone, String shippingAddress, 
                           String paymentMethod) {
        // Kiểm tra tồn kho
        for (var cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng");
            }
        }

        // Tạo đơn hàng mới
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        
        // Tìm người dùng từ authentication hoặc sử dụng người dùng mặc định
        User user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        System.out.println("\n=== KIỂM TRA THÔNG TIN NGƯỜI DÙNG ===");
        System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Is anonymousUser: " + (authentication != null && authentication.getName().equals("anonymousUser")));

        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            // Người dùng đã đăng nhập
            String username = authentication.getName();
            System.out.println("Tìm người dùng với username: " + username);
            try {
                // Thử tìm người dùng bằng username trước
                Optional<User> userOptional = userService.findByUsername(username);
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                    System.out.println("Tìm thấy người dùng bằng username: ID=" + user.getId());
                } else {
                    // Nếu không tìm thấy bằng username, thử tìm bằng email
                    userOptional = userService.findByEmail(username);
                    if (userOptional.isPresent()) {
                        user = userOptional.get();
                        System.out.println("Tìm thấy người dùng bằng email: ID=" + user.getId());
                    } else {
                        System.out.println("Không tìm thấy người dùng với username hoặc email: " + username);
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tìm người dùng: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Gán người dùng cho đơn hàng nếu tìm thấy
        if (user != null) {
            order.setUser(user);
            System.out.println("Đã gán người dùng ID: " + user.getId() + " cho đơn hàng");
        } else {
            System.out.println("CẢNH BÁO: Đơn hàng được tạo mà không có người dùng");
        }

        // Thêm các sản phẩm vào đơn hàng
        for (var cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem(product, cartItem.getQuantity());
            order.addItem(orderItem);

            // Cập nhật tồn kho
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Tính phí vận chuyển
        BigDecimal shippingFee = calculateShippingFee(order);
        order.setShippingFee(shippingFee);

        // Tính tổng tiền đơn hàng
        order.calculateTotal();

        // Lưu đơn hàng
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng
        cart.getItems().clear(); // Xóa cart items trước
        cartService.saveCart(cart); // Lưu cart để cập nhật
        cartService.clearCart(cart.getId()); // Xóa cart

        // Gửi email xác nhận
        sendOrderConfirmationEmail(savedOrder);

        return savedOrder;
    }

    private void sendOrderConfirmationEmail(Order order) {
        try {
            System.out.println("\n=== BẮT ĐẦU GỬI EMAIL XÁC NHẬN ===");
            System.out.println("Email người nhận: " + order.getCustomerEmail());
            System.out.println("Mã đơn hàng: " + order.getId());
            
            // Sử dụng MimeMessage thay vì SimpleMailMessage để hỗ trợ UTF-8
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Xác nhận đơn hàng #" + order.getId());
            
            // Tạo nội dung HTML
            String htmlContent = generateHtmlEmail(order);
            
            // Kiểm tra nội dung HTML có hợp lệ không
            System.out.println("Nội dung HTML đã được tạo: " + (htmlContent != null && !htmlContent.isEmpty() ? "OK" : "EMPTY"));
            
            // Thiết lập nội dung HTML
            helper.setText(htmlContent, true); // true = HTML
            
            System.out.println("Đang gửi email đến: " + order.getCustomerEmail());
            
            emailSender.send(mimeMessage);
            
            System.out.println("=== GỬI EMAIL XÁC NHẬN THÀNH CÔNG ===");
            System.out.println("Email người nhận: " + order.getCustomerEmail());
            System.out.println("Mã đơn hàng: " + order.getId() + "\n");
        } catch (Exception e) {
            System.err.println("\n=== LỖI GỬI EMAIL ===");
            System.err.println("Email người nhận: " + order.getCustomerEmail());
            System.err.println("Mã đơn hàng: " + order.getId());
            System.err.println("Loại lỗi: " + e.getClass().getName());
            System.err.println("Thông báo lỗi: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Nguyên nhân: " + e.getCause().getMessage());
            }
            
            // In thông tin về cấu hình email
            try {
                System.err.println("Cấu hình email:");
                System.err.println("Host: " + emailSender.toString());
                System.err.println("From: " + fromEmail);
            } catch (Exception ex) {
                System.err.println("Không thể in thông tin cấu hình: " + ex.getMessage());
            }
            
            e.printStackTrace();
            System.err.println("=== KẾT THÚC LOG LỖI EMAIL ===\n");
        }
    }

    private String generateHtmlEmail(Order order) {
        StringBuilder html = new StringBuilder();
        
        // Bắt đầu HTML
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; }\n");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }\n");
        html.append(".header { text-align: center; margin-bottom: 20px; }\n");
        html.append(".invoice { border: 1px solid #ddd; padding: 20px; }\n");
        html.append(".invoice-header { text-align: center; margin-bottom: 20px; }\n");
        html.append(".invoice-title { font-size: 24px; font-weight: bold; }\n");
        html.append(".shop-info { margin-bottom: 20px; }\n");
        html.append(".customer-info { margin-bottom: 20px; }\n");
        html.append(".divider { border-top: 1px solid #ddd; margin: 15px 0; }\n");
        html.append(".items-table { width: 100%; border-collapse: collapse; }\n");
        html.append(".items-table th, .items-table td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append(".product-image { width: 80px; height: 80px; object-fit: cover; border-radius: 4px; }\n");
        html.append(".product-info { display: inline-block; vertical-align: top; margin-left: 10px; }\n");
        html.append(".total { margin-top: 20px; text-align: right; }\n");
        html.append(".footer { margin-top: 30px; text-align: center; font-style: italic; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"container\">\n");
        
        // Lời chào
        html.append("<div class=\"header\">\n");
        html.append("<h2>Xin chào ").append(order.getCustomerName()).append(",</h2>\n");
        html.append("<p>Cảm ơn bạn đã đặt hàng tại cửa hàng chúng tôi.</p>\n");
        html.append("</div>\n");
        
        // Hóa đơn
        html.append("<div class=\"invoice\">\n");
        html.append("<div class=\"invoice-header\">\n");
        html.append("<div class=\"invoice-title\">HÓA ĐƠN BÁN HÀNG</div>\n");
        html.append("</div>\n");
        
        // Thông tin cửa hàng
        html.append("<div class=\"shop-info\">\n");
        html.append("<p><strong>SHOP GIA DỤNG ONLINE</strong></p>\n");
        html.append("<p>Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM</p>\n");
        html.append("<p>Điện thoại: 0978 537 109</p>\n");
        html.append("<p>Email: levinhthai1322004@gmail.com</p>\n");
        html.append("</div>\n");
        
        html.append("<div class=\"divider\"></div>\n");
        
        // Thông tin đơn hàng
        html.append("<p><strong>Mã đơn hàng:</strong> #").append(order.getId()).append("</p>\n");
        html.append("<p><strong>Ngày:</strong> ").append(order.getOrderDate()).append("</p>\n");
        
        html.append("<div class=\"divider\"></div>\n");
        
        // Thông tin khách hàng
        html.append("<div class=\"customer-info\">\n");
        html.append("<p><strong>Khách hàng:</strong> ").append(order.getCustomerName()).append("</p>\n");
        html.append("<p><strong>Điện thoại:</strong> ").append(order.getCustomerPhone()).append("</p>\n");
        html.append("<p><strong>Địa chỉ:</strong> ").append(order.getShippingAddress()).append("</p>\n");
        html.append("</div>\n");
        
        html.append("<div class=\"divider\"></div>\n");
        
        // Chi tiết sản phẩm
        html.append("<table class=\"items-table\">\n");
        html.append("<tr>\n");
        html.append("<th>Hình ảnh</th>\n");
        html.append("<th>Sản phẩm</th>\n");
        html.append("<th>SL</th>\n");
        html.append("<th>Đơn giá</th>\n");
        html.append("<th>Thành tiền</th>\n");
        html.append("</tr>\n");
        
        for (OrderItem item : order.getOrderItems()) {
            html.append("<tr>\n");
            
            // Cột hình ảnh sản phẩm
            html.append("<td>\n");
            if (item.getProduct() != null && item.getProduct().getImageUrl() != null && !item.getProduct().getImageUrl().isEmpty()) {
                html.append("<img src=\"").append(item.getProduct().getImageUrl()).append("\" class=\"product-image\" alt=\"").append(item.getProduct().getName()).append("\">\n");
            } else {
                html.append("<div style=\"width: 80px; height: 80px; background-color: #f0f0f0; display: flex; align-items: center; justify-content: center; border-radius: 4px;\">Không có ảnh</div>\n");
            }
            html.append("</td>\n");
            
            // Cột thông tin sản phẩm
            html.append("<td>").append(item.getProduct().getName()).append("</td>\n");
            html.append("<td>").append(item.getQuantity()).append("</td>\n");
            html.append("<td>").append(formatCurrency(item.getPrice())).append("</td>\n");
            html.append("<td>").append(formatCurrency(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).append("</td>\n");
            html.append("</tr>\n");
        }
        
        html.append("</table>\n");
        
        // Tổng tiền
        html.append("<div class=\"total\">\n");
        html.append("<p><strong>Tổng tiền hàng:</strong> ").append(formatCurrency(order.getTotalAmount().subtract(order.getShippingFee()))).append("</p>\n");
        html.append("<p><strong>Phí vận chuyển:</strong> ").append(formatCurrency(order.getShippingFee())).append("</p>\n");
        html.append("<p><strong>Tổng cộng:</strong> ").append(formatCurrency(order.getTotalAmount())).append("</p>\n");
        html.append("</div>\n");
        
        html.append("<div class=\"divider\"></div>\n");
        
        // Phương thức thanh toán
        html.append("<p><strong>Phương thức thanh toán:</strong> ");
        html.append(order.getPaymentMethod().equals("COD") ? "Thanh toán khi nhận hàng" : "Chuyển khoản ngân hàng");
        html.append("</p>\n");
        html.append("<p><strong>Trạng thái thanh toán:</strong> ");
        html.append(order.getPaymentStatus().equals("PAID") ? "Đã thanh toán" : "Chưa thanh toán");
        html.append("</p>\n");
        
        // Thông tin chuyển khoản nếu là thanh toán qua ngân hàng
        if ("BANK_TRANSFER".equals(order.getPaymentMethod())) {
            html.append("<div class=\"divider\"></div>\n");
            html.append("<div class=\"bank-info\">\n");
            html.append("<p><strong>THÔNG TIN CHUYỂN KHOẢN:</strong></p>\n");
            html.append("<p>Ngân hàng: BIDV</p>\n");
            html.append("<p>Số tài khoản: 1234567890</p>\n");
            html.append("<p>Chủ tài khoản: SHOP GIA DUNG ONLINE</p>\n");
            html.append("<p>Nội dung CK: DH").append(order.getId()).append("</p>\n");
            html.append("<p>Vui lòng chuyển khoản trong vòng 24h</p>\n");
            html.append("</div>\n");
        }
        
        html.append("</div>\n"); // Đóng invoice
        
        // Footer
        html.append("<div class=\"footer\">\n");
        html.append("<p>Cảm ơn quý khách đã mua hàng!</p>\n");
        html.append("<p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email hoặc điện thoại.</p>\n");
        html.append("</div>\n");
        
        html.append("</div>\n"); // Đóng container
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public Order updatePaymentStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus(status);
        return orderRepository.save(order);
    }

    private BigDecimal calculateShippingFee(Order order) {
        BigDecimal orderTotal = order.getOrderItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Miễn phí vận chuyển cho đơn hàng trên 500,000 VNĐ
        if (orderTotal.compareTo(new BigDecimal("500000")) >= 0) {
            return BigDecimal.ZERO;
        }

        // Phí cơ bản: 30,000 VNĐ
        BigDecimal baseFee = new BigDecimal("30000");
        
        // Tính phí dựa trên địa chỉ (có thể mở rộng logic này)
        String address = order.getShippingAddress().toLowerCase();
        if (address.contains("hà nội") || address.contains("ha noi")) {
            return baseFee;
        } else if (address.contains("hồ chí minh") || address.contains("ho chi minh") || address.contains("sài gòn") || address.contains("sai gon")) {
            return baseFee;
        } else {
            // Các tỉnh khác: phí cơ bản + 10,000 VNĐ
            return baseFee.add(new BigDecimal("10000"));
        }
    }

    @Override
    public List<Order> getRecentOrdersByUser(Long userId) {
        return orderRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Order> getAllOrdersByUser(Long userId) {
        System.out.println("\n=== KIỂM TRA getAllOrdersByUser ===");
        System.out.println("Tìm đơn hàng cho userId: " + userId);
        
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        System.out.println("Số lượng đơn hàng tìm thấy: " + orders.size());
        if (!orders.isEmpty()) {
            System.out.println("Danh sách ID đơn hàng:");
            for (Order order : orders) {
                System.out.println("- Order ID: " + order.getId() + ", User ID: " + (order.getUser() != null ? order.getUser().getId() : "null"));
            }
        }
        
        return orders;
    }

    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<Order> findOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    @Override
    public String generateInvoice(Order order) {
        // Implementation details
        return "Invoice generated";
    }
    
    // Implementation of additional methods for admin functionality
    
    @Override
    public List<Order> getRecentOrders(int count) {
        List<Order> recentOrders = orderRepository.findTop5ByOrderByOrderDateDesc();
        return recentOrders.size() > count ? recentOrders.subList(0, count) : recentOrders;
    }
    
    @Override
    @Transactional
    public void deleteOrder(Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
            
            // Cập nhật lại số lượng sản phẩm trong kho
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getProduct() != null) {
                        Product product = item.getProduct();
                        // Cộng lại số lượng sản phẩm vào kho
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
            }
            
            // Xóa đơn hàng
            orderRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa đơn hàng: " + e.getMessage(), e);
        }
    }
} 