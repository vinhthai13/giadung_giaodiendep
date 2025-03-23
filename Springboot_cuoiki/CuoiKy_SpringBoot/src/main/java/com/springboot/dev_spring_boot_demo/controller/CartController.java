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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.dev_spring_boot_demo.entity.Cart;
import com.springboot.dev_spring_boot_demo.service.CartService;

import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes("cartId")
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public String viewCart(@SessionAttribute(name = "cartId", required = false) Long cartId, Model model) {
        if (cartId != null) {
            Cart cart = cartService.getCart(cartId);
            model.addAttribute("cart", cart);
            model.addAttribute("cartItems", cart.getItems());
            model.addAttribute("totalAmount", cart.getTotalAmount());
        }
        return "user/cart";
    }

    @PostMapping("/add")
    public String addToCart(@SessionAttribute(name = "cartId", required = false) Long cartId,
                          @RequestParam Long productId,
                          @RequestParam(defaultValue = "1") Integer quantity,
                          Model model,
                          HttpSession session) {
        // Nếu chưa có giỏ hàng, tạo giỏ hàng mới
        if (cartId == null) {
            Cart newCart = new Cart();
            Cart savedCart = cartService.saveCart(newCart);
            cartId = savedCart.getId();
            model.addAttribute("cartId", cartId);
            session.setAttribute("cartId", cartId);
        }

        Cart cart = cartService.addToCart(cartId, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@SessionAttribute("cartId") Long cartId,
                               @RequestParam Long productId,
                               @RequestParam Integer quantity) {
        cartService.updateCartItemQuantity(cartId, productId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@SessionAttribute("cartId") Long cartId,
                               @PathVariable Long productId) {
        cartService.removeFromCart(cartId, productId);
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart(@SessionAttribute("cartId") Long cartId) {
        cartService.clearCart(cartId);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(@SessionAttribute(name = "cartId", required = false) Long cartId,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Lưu URL hiện tại vào session để sau khi đăng nhập chuyển về
            session.setAttribute("redirectUrl", "/cart/checkout");
            return "redirect:/login";
        }

        // Kiểm tra xem giỏ hàng có tồn tại và có sản phẩm không
        if (cartId == null) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }

        Cart cart = cartService.getCart(cartId);
        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        return "user/checkout";
    }
} 