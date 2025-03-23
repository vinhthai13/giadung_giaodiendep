package com.springboot.dev_spring_boot_demo.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.dev_spring_boot_demo.entity.Category;
import com.springboot.dev_spring_boot_demo.repository.CategoryRepository;
import com.springboot.dev_spring_boot_demo.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category saveCategory(Category category) {
        if (category.getId() == null) {
            category.setCreatedAt(LocalDateTime.now());
        } else {
            category.setUpdatedAt(LocalDateTime.now());
        }
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
} 