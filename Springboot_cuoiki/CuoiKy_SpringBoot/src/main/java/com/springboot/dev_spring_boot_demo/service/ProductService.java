package com.springboot.dev_spring_boot_demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.springboot.dev_spring_boot_demo.entity.Product;

public interface ProductService {
    // Basic CRUD operations
    List<Product> findAll();
    Optional<Product> findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
    
    // Custom query methods
    List<Product> findByCategory(String category);
    List<Product> searchProducts(String keyword);
    List<Product> findByActive(boolean active);
    List<Product> findByStockGreaterThan(int minStock);
    List<String> getAllCategories();
    
    // Alias methods for existing implementations
    default List<Product> getAllProducts() {
        return findAll();
    }
    
    default Product getProductById(Long id) {
        return findById(id).orElse(null);
    }
    
    default List<Product> getProductsByCategory(String category) {
        return findByCategory(category);
    }
    
    default List<Product> getProductsByCategories(List<String> categories) {
        return findAll().stream()
                .filter(product -> categories.contains(product.getCategory()))
                .collect(Collectors.toList());
    }
    
    default Product saveProduct(Product product) {
        return save(product);
    }
    
    default void deleteProduct(Long id) {
        deleteById(id);
    }
    
    default List<Product> getAvailableProducts() {
        return findByStockGreaterThan(0);
    }

    // Phân trang cho sản phẩm có sẵn
    Page<Product> getAvailableProductsPaged(Pageable pageable);
    
    // Phân trang cho sản phẩm theo khoảng giá
    Page<Product> getAvailableProductsByPriceRange(double minPrice, double maxPrice, Pageable pageable);

    // Phân trang cho sản phẩm theo danh mục
    Page<Product> getAvailableProductsByCategory(String category, Pageable pageable);

    // Phân trang cho sản phẩm theo khoảng giá và danh mục
    Page<Product> getAvailableProductsByPriceRangeAndCategory(double minPrice, double maxPrice, String category, Pageable pageable);
    
    default boolean updateStock(Long productId, int quantity) {
        Optional<Product> productOpt = findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int newStock = product.getStockQuantity() - quantity;
            if (newStock >= 0) {
                product.setStockQuantity(newStock);
                save(product);
                return true;
            }
        }
        return false;
    }
} 