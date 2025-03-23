package com.springboot.dev_spring_boot_demo.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.dev_spring_boot_demo.entity.Order;
import com.springboot.dev_spring_boot_demo.entity.User;
import com.springboot.dev_spring_boot_demo.service.OrderService;
import com.springboot.dev_spring_boot_demo.service.UserService;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            
            // Lấy danh sách đơn hàng gần đây
            List<Order> recentOrders = orderService.getRecentOrdersByUser(user.getId());
            
            model.addAttribute("user", user);
            model.addAttribute("recentOrders", recentOrders);
            return "user/profile/profile";
        } catch (Exception e) {
            // Log lỗi
            System.err.println("Lỗi khi tải trang profile: " + e.getMessage());
            e.printStackTrace();
            
            // Thêm thông báo lỗi vào model
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải thông tin người dùng: " + e.getMessage());
            return "user/access-denied";
        }
    }

    @GetMapping("/orders")
    public String viewOrders(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            System.out.println("\n=== KIỂM TRA DANH SÁCH ĐƠN HÀNG ===");
            System.out.println("Username: " + username);
            
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            System.out.println("Tìm thấy người dùng: ID=" + user.getId() + ", Email=" + user.getEmail());
            
            List<Order> orders = orderService.getAllOrdersByUser(user.getId());
            System.out.println("Số lượng đơn hàng tìm thấy: " + orders.size());
            
            if (orders.isEmpty()) {
                System.out.println("Không tìm thấy đơn hàng nào cho người dùng ID: " + user.getId());
            } else {
                System.out.println("Danh sách ID đơn hàng:");
                for (Order order : orders) {
                    System.out.println("- Order ID: " + order.getId() + ", User ID: " + (order.getUser() != null ? order.getUser().getId() : "null"));
                }
            }
            
            model.addAttribute("user", user);
            model.addAttribute("orders", orders);
            return "user/profile/orders";
        } catch (Exception e) {
            System.err.println("Lỗi khi tải trang đơn hàng: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải thông tin đơn hàng: " + e.getMessage());
            return "user/access-denied";
        }
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            
            Order order = orderService.getOrderById(orderId);
            
            // Kiểm tra nếu đơn hàng không thuộc về người dùng hiện tại
            if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
                model.addAttribute("errorMessage", "Bạn không có quyền xem đơn hàng này.");
                return "user/access-denied";
            }
            
            model.addAttribute("user", user);
            model.addAttribute("order", order);
            return "user/profile/order-details";
        } catch (Exception e) {
            System.err.println("Lỗi khi tải chi tiết đơn hàng: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải chi tiết đơn hàng: " + e.getMessage());
            return "user/access-denied";
        }
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser, 
                              Principal principal,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User currentUser = userOptional.get();
            
            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setPhone(updatedUser.getPhone());
            currentUser.setAddress(updatedUser.getAddress());
            
            userService.save(currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
            return "redirect:/profile";
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật thông tin người dùng: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật thông tin: " + e.getMessage());
            return "user/access-denied";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            model.addAttribute("user", user);
            model.addAttribute("passwordForm", new PasswordChangeForm());
            return "user/profile/change-password";
        } catch (Exception e) {
            System.err.println("Lỗi khi tải trang đổi mật khẩu: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải trang đổi mật khẩu: " + e.getMessage());
            return "user/access-denied";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute PasswordChangeForm passwordForm,
                               Principal principal,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            
            if (userService.changePassword(user, 
                                         passwordForm.getCurrentPassword(), 
                                         passwordForm.getNewPassword())) {
                redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công");
                return "redirect:/profile";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng");
                return "redirect:/profile/change-password";
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi đổi mật khẩu: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi đổi mật khẩu: " + e.getMessage());
            return "user/access-denied";
        }
    }

    @GetMapping("/error")
    public String handleProfileError(Model model) {
        model.addAttribute("errorMessage", "Có lỗi xảy ra khi truy cập trang cá nhân. Vui lòng đăng nhập lại.");
        return "user/access-denied";
    }

    @GetMapping("/addresses")
    public String viewAddresses(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            model.addAttribute("user", user);
            return "user/profile/addresses";
        } catch (Exception e) {
            System.err.println("Lỗi khi tải trang địa chỉ: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải trang địa chỉ: " + e.getMessage());
            return "user/access-denied";
        }
    }

    @PostMapping("/addresses/add")
    public String addAddress(@RequestParam String fullName,
                           @RequestParam String phone,
                           @RequestParam String address,
                           Principal principal,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            
            // Cập nhật thông tin địa chỉ
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setAddress(address);
            
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ mới thành công!");
            return "redirect:/profile/addresses";
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm địa chỉ: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi thêm địa chỉ: " + e.getMessage());
            redirectAttributes.addAttribute("showAddModal", "true");
            return "redirect:/profile/addresses";
        }
    }

    @PostMapping("/addresses/update")
    public String updateAddress(@RequestParam String fullName,
                              @RequestParam String phone,
                              @RequestParam String address,
                              Principal principal,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            
            // Cập nhật thông tin địa chỉ
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setAddress(address);
            
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ thành công!");
            return "redirect:/profile/addresses";
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật địa chỉ: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật địa chỉ: " + e.getMessage());
            redirectAttributes.addAttribute("showEditModal", "true");
            return "redirect:/profile/addresses";
        }
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId, 
                            Principal principal,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                System.err.println("Không tìm thấy người dùng với username: " + username);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
                return "user/access-denied";
            }
            
            User user = userOptional.get();
            
            Order order = orderService.getOrderById(orderId);
            
            // Kiểm tra nếu đơn hàng không thuộc về người dùng hiện tại
            if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
                model.addAttribute("errorMessage", "Bạn không có quyền hủy đơn hàng này.");
                return "user/access-denied";
            }
            
            // Kiểm tra nếu đơn hàng không ở trạng thái PENDING
            if (!"PENDING".equals(order.getOrderStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể hủy đơn hàng ở trạng thái chờ xác nhận.");
                return "redirect:/profile/orders/" + orderId;
            }
            
            // Cập nhật trạng thái đơn hàng thành CANCELLED
            orderService.updateOrderStatus(orderId, "CANCELLED");
            
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được hủy thành công.");
            return "redirect:/profile/orders/" + orderId;
        } catch (Exception e) {
            System.err.println("Lỗi khi hủy đơn hàng: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi hủy đơn hàng: " + e.getMessage());
            return "user/access-denied";
        }
    }
}

class PasswordChangeForm {
    private String currentPassword;
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}