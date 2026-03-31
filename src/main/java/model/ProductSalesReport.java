package model;

import java.math.BigDecimal;

public class ProductSalesReport {
    int productId;
    String productName;
    String brand;
    int totalQuantity;
    BigDecimal totalRevenue;

    public ProductSalesReport() {}

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    @Override
    public String toString() {
        return String.format("%-5d | %-35s | %-12s | %8d | %,15.0fđ",
                productId, productName, brand, totalQuantity, totalRevenue);
    }
}
