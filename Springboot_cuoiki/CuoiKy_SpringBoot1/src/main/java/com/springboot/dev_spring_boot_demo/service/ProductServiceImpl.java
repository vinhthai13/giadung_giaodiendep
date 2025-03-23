package com.springboot.dev_spring_boot_demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.dev_spring_boot_demo.entity.Product;
import com.springboot.dev_spring_boot_demo.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameOrDescriptionContainingIgnoreCase(keyword);
    }

    @Override
    public List<Product> findByActive(boolean active) {
        return productRepository.findByActive(active);
    }

    @Override
    public List<Product> findByStockGreaterThan(int minStock) {
        return productRepository.findByStockGreaterThan(minStock);
    }

    @Override
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Override
    public Page<Product> getAvailableProductsPaged(Pageable pageable) {
        return productRepository.findAvailableProducts(pageable);
    }

    @Override
    public Page<Product> getAvailableProductsByPriceRange(double minPrice, double maxPrice, Pageable pageable) {
        return productRepository.findAvailableProductsByPriceRange(minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Product> getAvailableProductsByCategory(String category, Pageable pageable) {
        return productRepository.findAvailableProductsByCategory(category, pageable);
    }

    @Override
    public Page<Product> getAvailableProductsByPriceRangeAndCategory(double minPrice, double maxPrice, String category, Pageable pageable) {
        return productRepository.findAvailableProductsByPriceRangeAndCategory(minPrice, maxPrice, category, pageable);
    }
}