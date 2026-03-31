package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Category {
    int categoryId;
    String categoryName;
    LocalDateTime createdAt;

    public Category() {}

    public Category(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        String date = (createdAt != null) ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
        return String.format("| %-4d | %-30s | %-12s |", categoryId, categoryName, date);
    }
}
