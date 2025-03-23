package com.springboot.dev_spring_boot_demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.dev_spring_boot_demo.entity.Product;
import com.springboot.dev_spring_boot_demo.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priceRange,
            @RequestParam(defaultValue = "name_asc") String sort,
            Model model) {
        
        // Create pageable with the requested page size
        Pageable pageable = PageRequest.of(page, pageSize, getSort(sort));
        
        // Initialize the product page
        Page<Product> productPage;
        
        // Apply price range filters if needed
        Integer minPrice = null;
        Integer maxPrice = null;
        
        if (priceRange != null && !priceRange.equals("all")) {
            switch (priceRange) {
                case "under500":
                    maxPrice = 500000;
                    break;
                case "500to1000":
                    minPrice = 500000;
                    maxPrice = 1000000;
                    break;
                case "1000to2000":
                    minPrice = 1000000;
                    maxPrice = 2000000;
                    break;
                case "above2000":
                    minPrice = 2000000;
                    break;
            }
        }
        
        // Apply category and price filters
        if (category != null && !category.isEmpty() && !category.equals("all")) {
            if (minPrice != null || maxPrice != null) {
                // Both category and price filters
                productPage = productService.getAvailableProductsByPriceRangeAndCategory(
                    minPrice != null ? minPrice : 0, 
                    maxPrice != null ? maxPrice : Integer.MAX_VALUE, 
                    category, 
                    pageable);
            } else {
                // Just category filter
                productPage = productService.getAvailableProductsByCategory(category, pageable);
            }
        } else if (minPrice != null || maxPrice != null) {
            // Just price filter
            productPage = productService.getAvailableProductsByPriceRange(
                minPrice != null ? minPrice : 0, 
                maxPrice != null ? maxPrice : Integer.MAX_VALUE, 
                pageable);
        } else {
            // No filters
            productPage = productService.getAvailableProductsPaged(pageable);
        }
        
        // Add all necessary attributes to the model
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("categories", productService.getAllCategories());
        
        // Add sort and filter parameters to be used in pagination links
        model.addAttribute("sort", sort);
        model.addAttribute("category", category);
        model.addAttribute("priceRange", priceRange);
        
        return "user/products";
    }

    // Phương thức để chuyển đổi tham số sắp xếp thành Sort
    private Sort getSort(String sort) {
        switch (sort) {
            case "name_desc":
                return Sort.by(Sort.Direction.DESC, "name");
            case "price_asc":
                return Sort.by(Sort.Direction.ASC, "price");
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "price");
            case "name_asc":
            default:
                return Sort.by(Sort.Direction.ASC, "name");
        }
    }

    @GetMapping("/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product != null) {
            model.addAttribute("product", product);
        }
        return "user/product-detail";
    }

    @GetMapping("/category/{category}")
    public String listProductsByCategory(@PathVariable String category, Model model) {
        List<Product> products = productService.getProductsByCategory(category);
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        model.addAttribute("categories", productService.getAllCategories());
        return "user/products";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam String keyword, Model model) {
        List<Product> products = productService.searchProducts(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", productService.getAllCategories());
        return "user/products";
    }

    // Admin endpoints
    @GetMapping("/admin/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/admin/save")
    public String saveProduct(@ModelAttribute Product product) {
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/admin/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product != null) {
            model.addAttribute("product", product);
            model.addAttribute("categories", productService.getAllCategories());
        }
        return "admin/product-form";
    }

    @GetMapping("/admin/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}