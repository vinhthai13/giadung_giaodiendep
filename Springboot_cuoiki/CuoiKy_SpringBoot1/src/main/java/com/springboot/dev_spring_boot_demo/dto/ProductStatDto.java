package com.springboot.dev_spring_boot_demo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.springboot.dev_spring_boot_demo.entity.Category;

public class ProductStatDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Category category;
    private Integer soldCount;
    private BigDecimal revenue;

    public ProductStatDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    @Override
    public String toString() {
        return "ProductStatDto [id=" + id + ", name=" + name + ", category=" + category + ", soldCount=" + soldCount
                + ", revenue=" + revenue + "]";
    }
} 