package com.springboot.dev_spring_boot_demo.service;

import java.util.List;
import java.util.Optional;

import com.springboot.dev_spring_boot_demo.entity.Category;

public interface CategoryService {
    
    List<Category> getAllCategories();
    
    List<Category> getActiveCategories();
    
    Optional<Category> getCategoryById(Long id);
    
    Category saveCategory(Category category);
    
    void deleteCategory(Long id);
    
    Category getCategoryByName(String name);
} 