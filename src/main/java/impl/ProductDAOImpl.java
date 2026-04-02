package impl;

import dao.ProductDAO;
import model.Product;
import util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {
    private static final String BASE_SELECT = "select p.*, c.category_name " +
                    "from Products p " + "join Categories c on p.category_id = c.category_id ";

    //admin
    @Override
    public List<Product> getAllProducts() {
        String sql = BASE_SELECT + "ORDER BY p.product_id";
        return queryList(sql);
    }

    @Override
    public boolean addProduct(Product p) {
        String sql = "insert into Products " + "(category_id, product_name, brand, storage, color, price, stock, description) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, p.getCategoryId());
            pstmt.setString(2, p.getProductName());
            pstmt.setString(3, p.getBrand());
            pstmt.setString(4, p.getStorage());
            pstmt.setString(5, p.getColor());
            pstmt.setBigDecimal(6, p.getPrice());
            pstmt.setInt(7, p.getStock());
            pstmt.setString(8, p.getDescription());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi thêm sản phẩm: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateProduct(Product p) {
        String sql = "update Products set " +
                "category_id=?, product_name=?, brand=?, storage=?, color=?, " + "price=?, stock=?, description=? " +
                "where product_id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, p.getCategoryId());
            pstmt.setString(2, p.getProductName());
            pstmt.setString(3, p.getBrand());
            pstmt.setString(4, p.getStorage());
            pstmt.setString(5, p.getColor());
            pstmt.setBigDecimal(6, p.getPrice());
            pstmt.setInt(7, p.getStock());
            pstmt.setString(8, p.getDescription());
            pstmt.setInt(9, p.getProductId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật sản phẩm: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteProduct(int productId) {
        String sql = "delete from Products where product_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi xóa sản phẩm: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Product> searchByName(String keyword) {
        String sql = BASE_SELECT + "where p.product_name like ? order by p.product_id";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            return mapList(pstmt.executeQuery());

        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Product> getProductsSortedByPrice(boolean ascending) {
        String sql = BASE_SELECT + "order by p.price " + (ascending ? "ASC" : "DESC");
        return queryList(sql);
    }

    //lấy dữ liệu theo trang
    @Override
    public List<Product> getProductsByPage(int page, int size) {
        String sql = BASE_SELECT + "order by p.product_id limit ? offset ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, size);
            pstmt.setInt(2, (page - 1) * size);

            return mapList(pstmt.executeQuery());

        } catch (SQLException e) {
            System.err.println("Lỗi phân trang: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public int countProducts() {
        String sql = "select count(*) from Products";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Lỗi đếm sản phẩm: " + e.getMessage());
        }
        return 0;
    }

    // customer
    @Override
    public List<Product> getProductsInStock() {
        String sql = BASE_SELECT + "where p.stock > 0 " + "order by p.product_id ASC";
        return queryList(sql);
    }

    @Override
    public List<Product> filterByCategory(int categoryId) {
        String sql = BASE_SELECT + "where p.category_id = ? and p.stock > 0 " + "order by p.product_name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            return mapList(pstmt.executeQuery());

        } catch (SQLException e) {
            System.err.println("Lỗi lọc theo danh mục: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Product> filterByBrand(String brand) {
        String sql = BASE_SELECT + "where lower(c.category_name) = lower(?) and p.stock > 0 " + "order by p.product_name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brand);
            return mapList(pstmt.executeQuery());

        } catch (SQLException e) {
            System.err.println("Lỗi lọc theo hãng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Product> filterByCategoryAndBrand(int categoryId, String brand) {
        String sql = BASE_SELECT +
                "where p.category_id = ? and lower(c.category_name) = lower(?) and p.stock > 0 " + "order by p.product_name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            pstmt.setString(2, brand);
            return mapList(pstmt.executeQuery());

        } catch (SQLException e) {
            System.err.println("Lỗi lọc: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //chung
    @Override
    public Product getProductById(int productId) {
        String sql = BASE_SELECT + "where p.product_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("Lỗi tìm sản phẩm theo ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean decreaseStock(int productId, int quantity) {
        // update khi stock >= quantity
        String sql = "update Products set stock = stock - ? " + "where product_id = ? and stock >= ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi giảm tồn kho: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Product> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        String sql = BASE_SELECT + "where p.stock > 0 and p.price between ? and ? " + "order by p.price asc";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, minPrice);
            pstmt.setBigDecimal(2, maxPrice);

            return mapList(pstmt.executeQuery());

        } catch (SQLException e) {
            System.err.println("Lỗi lọc theo khoảng giá: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Thực thi query không tham số, trả về danh sách
    private List<Product> queryList(String sql) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            return mapList(rs);

        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Product> mapList(ResultSet rs) throws SQLException {
        List<Product> list = new ArrayList<>();
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    /** Ánh xạ một dòng ResultSet → Product. */
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name")); // từ JOIN
        p.setProductName(rs.getString("product_name"));
        p.setBrand(rs.getString("brand"));
        p.setStorage(rs.getString("storage"));
        p.setColor(rs.getString("color"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            p.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            p.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return p;
    }
}