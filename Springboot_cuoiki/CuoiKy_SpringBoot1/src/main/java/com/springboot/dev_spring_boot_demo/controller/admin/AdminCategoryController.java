package com.springboot.dev_spring_boot_demo.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.dev_spring_boot_demo.entity.Category;
import com.springboot.dev_spring_boot_demo.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;
    
    @GetMapping("")
    public String categories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories/list";
    }
    
    @GetMapping("/new")
    public String showNewCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("currentPage", "categories");
        model.addAttribute("pageTitle", "New Category");
        return "admin/categories/form";
    }
    
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category, RedirectAttributes redirectAttributes) {
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("message", "Category saved successfully!");
        return "redirect:/admin/categories";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            model.addAttribute("currentPage", "categories");
            model.addAttribute("pageTitle", "Edit Category");
            return "admin/categories/form";
        } else {
            return "redirect:/admin/categories";
        }
    }
    
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("message", "Category deleted successfully!");
        return "redirect:/admin/categories";
    }
} 