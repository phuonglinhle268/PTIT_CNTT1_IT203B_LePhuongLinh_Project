package service;

import dao.ProductDAO;
import impl.ProductDAOImpl;
import model.Product;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAOImpl();
    }

    public boolean addProduct(Product newProduct) {
        if (newProduct.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Giá phải lớn hơn 0!");
            return false;
        }
        if (newProduct.getStock() < 0) {
            System.err.println("Số lượng tồn kho không được âm!");
            return false;
        }

        // Kiểm tra xem đã có sản phẩm trùng hoàn toàn chưa
        Product existing = findExistingProduct(newProduct);

        if (existing != null) {
            // Trùng → cộng dồn kho
            int newStock = existing.getStock() + newProduct.getStock();
            existing.setStock(newStock);

            boolean success = productDAO.updateProduct(existing);
            if (success) {
                System.out.println("Sản phẩm đã tồn tại. Đã cộng dồn số lượng tồn kho");
            }
            return success;
        } else {
            // Không trùng → thêm mới
            return productDAO.addProduct(newProduct);
        }
    }

    public boolean updateProduct(Product updatedProduct) {
        // Tìm sản phẩm cũ trước khi cập nhật
        Product oldProduct = productDAO.getProductById(updatedProduct.getProductId());
        if (oldProduct == null) {
            System.err.println("Không tìm thấy sản phẩm để cập nhật!");
            return false;
        }
        boolean success = productDAO.updateProduct(updatedProduct);
        if (!success) return false;

        // Sau khi cập nhật, kiểm tra xem có trùng với sản phẩm khác không
        Product duplicate = findExistingProduct(updatedProduct);
        if (duplicate != null && duplicate.getProductId() != updatedProduct.getProductId()) {
            // Trùng với sản phẩm khác → cộng dồn kho và xóa sản phẩm hiện tại
            int totalStock = duplicate.getStock() + updatedProduct.getStock();

            duplicate.setStock(totalStock);
            productDAO.updateProduct(duplicate);

            productDAO.deleteProduct(updatedProduct.getProductId());

            System.out.println("Sản phẩm sau khi sửa trùng với sản phẩm khác. Đã cộng dồn kho");
        }
        return true;
    }

    private Product findExistingProduct(Product p) {
        List<Product> allProducts = productDAO.getAllProducts();

        for (Product existing : allProducts) {
            if (existing.getCategoryId() == p.getCategoryId() &&
                    existing.getProductName().equalsIgnoreCase(p.getProductName()) &&
                    existing.getStorage().equalsIgnoreCase(p.getStorage()) &&
                    existing.getColor().equalsIgnoreCase(p.getColor()) &&
                    existing.getPrice().compareTo(p.getPrice()) == 0) {

                return existing;
            }
        }
        return null;
    }

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public boolean deleteProduct(int id) {
        return productDAO.deleteProduct(id);
    }

    public List<Product> searchByName(String keyword) {
        return productDAO.searchByName(keyword);
    }

    public List<Product> getProductsSortedByPrice(boolean ascending) {
        return productDAO.getProductsSortedByPrice(ascending);
    }

    public Product findById(int id) {
        return productDAO.getProductById(id);
    }

    public List<Product> getProductsByPage(int page, int size) {
        return productDAO.getProductsByPage(page, size);
    }

    public int countProducts() {
        return productDAO.countProducts();
    }
}