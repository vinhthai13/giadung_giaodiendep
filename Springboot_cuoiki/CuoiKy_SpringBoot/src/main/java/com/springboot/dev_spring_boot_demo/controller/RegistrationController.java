package com.springboot.dev_spring_boot_demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.dev_spring_boot_demo.entity.User;
import com.springboot.dev_spring_boot_demo.service.UserService;

import jakarta.validation.Valid;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra username đã tồn tại chưa
        if (userService.isUsernameExists(user.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Tên đăng nhập đã được sử dụng");
        }

        // Kiểm tra email đã tồn tại chưa
        if (userService.isEmailExists(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email đã được sử dụng");
        }

        if (bindingResult.hasErrors()) {
            return "user/register";
        }

        try {
            userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("success", true);
            return "redirect:/register?success";
        } catch (Exception e) {
            bindingResult.rejectValue("username", "error.user", "Có lỗi xảy ra, vui lòng thử lại");
            return "user/register";
        }
    }
}