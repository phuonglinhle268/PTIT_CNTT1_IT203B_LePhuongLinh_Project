package dao;

import model.Category;

import java.util.List;

public interface CategoryDAO {
    List<Category> getAllCategories();
    boolean addCategory(String categoryName);
    boolean updateCategory(int categoryId, String newName);
    boolean deleteCategory(int categoryId);
    boolean existsByName(String categoryName);
    Category findById(int categoryId);
}
