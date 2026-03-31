package dao;

import model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductDAO {

    //admin
    List<Product> getAllProducts();
    boolean addProduct(Product product);
    boolean updateProduct(Product product);
    boolean deleteProduct(int productId);
    List<Product> searchByName(String keyword);
    List<Product> getProductsSortedByPrice(boolean ascending);

    // Phân trang
    List<Product> getProductsByPage(int page, int size);
    int countProducts();

    //customer
    List<Product> getProductsInStock();
    List<Product> filterByCategory(int categoryId);
    List<Product> filterByBrand(String brand);
    List<Product> filterByCategoryAndBrand(int categoryId, String brand);
    List<Product> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    //chung
    Product getProductById(int productId);
    boolean decreaseStock(int productId, int quantity);
}