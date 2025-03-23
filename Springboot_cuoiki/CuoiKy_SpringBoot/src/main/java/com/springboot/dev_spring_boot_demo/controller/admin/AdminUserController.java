package com.springboot.dev_spring_boot_demo.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.dev_spring_boot_demo.entity.User;
import com.springboot.dev_spring_boot_demo.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentPage", "users");
        model.addAttribute("pageTitle", "Users");
        return "admin/users/list";
    }
    
    @GetMapping("/new")
    public String showNewUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("currentPage", "users");
        model.addAttribute("pageTitle", "New User");
        return "admin/users/form";
    }
    
    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User user, 
                          @RequestParam(value = "changePassword", required = false) Boolean changePassword,
                          RedirectAttributes redirectAttributes) {
        
        // If it's an existing user and password change is not requested, get the existing password
        if (user.getId() != null && (changePassword == null || !changePassword)) {
            Optional<User> existingUser = userService.getUserById(user.getId());
            if (existingUser.isPresent()) {
                user.setPassword(existingUser.get().getPassword());
            }
        } else {
            // Encode the password for new users or password changes
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("message", "User saved successfully!");
        return "redirect:/admin/users";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            model.addAttribute("currentPage", "users");
            model.addAttribute("pageTitle", "Edit User");
            return "admin/users/form";
        } else {
            return "redirect:/admin/users";
        }
    }
    
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("message", "User deleted successfully!");
        return "redirect:/admin/users";
    }
} 