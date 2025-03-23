package com.springboot.dev_spring_boot_demo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CustomerDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;
    private Integer orderCount;
    private BigDecimal totalSpending;

    public CustomerDto() {
        this.totalSpending = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(BigDecimal totalSpending) {
        this.totalSpending = totalSpending;
    }

    @Override
    public String toString() {
        return "CustomerDto [id=" + id + ", customerName=" + customerName + ", customerEmail=" + customerEmail
                + ", customerPhone=" + customerPhone + ", shippingAddress=" + shippingAddress + ", orderCount="
                + orderCount + ", totalSpending=" + totalSpending + "]";
    }
} 