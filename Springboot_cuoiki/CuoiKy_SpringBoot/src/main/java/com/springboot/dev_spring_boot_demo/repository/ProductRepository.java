package com.springboot.dev_spring_boot_demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.dev_spring_boot_demo.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    
    List<Product> findByNameContainingIgnoreCase(String keyword);
    
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findAllCategories();
    
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
    
    List<Product> findByActive(boolean active);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > :minStock")
    List<Product> findByStockGreaterThan(int minStock);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameOrDescriptionContainingIgnoreCase(String keyword);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    Page<Product> findAvailableProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.price >= :minPrice AND p.price <= :maxPrice")
    Page<Product> findAvailableProductsByPriceRange(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.category = :category")
    Page<Product> findAvailableProductsByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.price >= :minPrice AND p.price <= :maxPrice AND p.category = :category")
    Page<Product> findAvailableProductsByPriceRangeAndCategory(
        @Param("minPrice") double minPrice, 
        @Param("maxPrice") double maxPrice, 
        @Param("category") String category, 
        Pageable pageable
    );
} 