package com.springboot.dev_spring_boot_demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.dev_spring_boot_demo.entity.Cart;
import com.springboot.dev_spring_boot_demo.entity.CartItem;
import com.springboot.dev_spring_boot_demo.entity.Product;
import com.springboot.dev_spring_boot_demo.repository.CartRepository;
import com.springboot.dev_spring_boot_demo.repository.ProductRepository;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public Cart getCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    return cartRepository.save(newCart);
                });
    }

    public Cart addToCart(Long cartId, Long productId, Integer quantity) {
        Cart cart = getCart(cartId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            // Kiểm tra số lượng tồn kho
            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " chỉ còn " + product.getStockQuantity() + " sản phẩm");
            }
            
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + quantity;
                
                // Kiểm tra tổng số lượng sau khi thêm
                if (product.getStockQuantity() < newQuantity) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng yêu cầu");
                }
                
                item.setQuantity(newQuantity);
            } else {
                cart.addItem(new CartItem(product, quantity));
            }

            return cartRepository.save(cart);
        }

        return cart;
    }

    public Cart updateCartItemQuantity(Long cartId, Long productId, Integer quantity) {
        Cart cart = getCart(cartId);
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        return cartRepository.save(cart);
    }

    public Cart removeFromCart(Long cartId, Long productId) {
        Cart cart = getCart(cartId);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        return cartRepository.save(cart);
    }

    public void clearCart(Long cartId) {
        Cart cart = getCart(cartId);
        cart.clear();
        cartRepository.save(cart);
    }

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }
}