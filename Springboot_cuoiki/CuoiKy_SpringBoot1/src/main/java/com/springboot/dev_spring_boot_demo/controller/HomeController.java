package com.springboot.dev_spring_boot_demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.dev_spring_boot_demo.entity.Product;
import com.springboot.dev_spring_boot_demo.service.ProductService;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(required = false) String category,
                      @RequestParam(required = false) String priceRange,
                      Model model) {
        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage;
        
        double minPrice = 0;
        double maxPrice = Double.MAX_VALUE;

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
        
        if (category != null && !category.isEmpty()) {
            if (priceRange != null && !priceRange.equals("all")) {
                productPage = productService.getAvailableProductsByPriceRangeAndCategory(minPrice, maxPrice, category, pageable);
            } else {
                productPage = productService.getAvailableProductsByCategory(category, pageable);
            }
        } else if (priceRange != null && !priceRange.equals("all")) {
            productPage = productService.getAvailableProductsByPriceRange(minPrice, maxPrice, pageable);
        } else {
            productPage = productService.getAvailableProductsPaged(pageable);
        }
        
        model.addAttribute("products", productPage);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("category", category);

        return "user/index";
    }

    @GetMapping("/about")
    public String about() {
        return "user/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "user/contact";
    }
} 