package service;

import dao.CategoryDAO;
import impl.CategoryDAOImpl;
import model.Category;

import java.util.List;

public class CategoryService {
    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAOImpl();   // Inject implementation
    }
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public boolean addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Tên danh mục không được để trống");
            return false;
        }
        if (categoryDAO.existsByName(name)) {
            System.err.println("Danh mục '" + name + "' đã tồn tại");
            return false;
        }
        return categoryDAO.addCategory(name);
    }

    public boolean updateCategory(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            System.err.println("Tên danh mục không được để trống");
            return false;
        }
        if (categoryDAO.findById(id) == null) {
            System.err.println("Không tìm thấy danh mục");
            return false;
        }
        return categoryDAO.updateCategory(id, newName);
    }

    public boolean deleteCategory(int id) {
        if (categoryDAO.findById(id) == null) {
            System.err.println("Không tìm thấy danh mục");
            return false;
        }
        return categoryDAO.deleteCategory(id);
    }
}
