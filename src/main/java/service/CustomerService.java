package service;

import dao.CategoryDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import dao.UserProfileDAO;
import impl.CategoryDAOImpl;
import impl.OrderDAOImpl;
import impl.ProductDAOImpl;
import impl.UserProfileDAOImpl;
import model.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerService {
    private final ProductDAO     productDAO;
    private final CategoryDAO    categoryDAO;
    private final OrderDAO       orderDAO;
    private final UserProfileDAO profileDAO;
    private final FlashSaleService flashSaleService;

    public CustomerService() {
        this.productDAO = new ProductDAOImpl();
        this.categoryDAO = new CategoryDAOImpl();
        this.orderDAO = new OrderDAOImpl();
        this.profileDAO = new UserProfileDAOImpl();
        this.flashSaleService = new FlashSaleService();
    }

    // sản phẩm

    public List<Product> getProductsInStock() {
        return productDAO.getProductsInStock();
    }

    public List<Product> filterByCategory(int categoryId) {
        return productDAO.filterByCategory(categoryId);
    }

    public List<Product> filterByBrand(String brand) {
        return productDAO.filterByBrand(brand);
    }

    public List<Product> filterByCategoryAndBrand(int categoryId, String brand) {
        return productDAO.filterByCategoryAndBrand(categoryId, brand);
    }

    public List<Product> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productDAO.filterByPriceRange(minPrice, maxPrice);
    }

    public Product getProductById(int productId) {
        return productDAO.getProductById(productId);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public List<String> getAvailableBrands() {
        return getProductsInStock().stream()
                .map(Product::getBrand)
                .distinct()
                .sorted()
                .toList();
    }

    // giỏ hàng

    //Them san pham vao gio hang.

    public void addToCart(List<CartItem> cart, int productId,
                          int quantity, FlashSale flashSale) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        Product product = productDAO.getProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID = " + productId);
        }
        if (product.getStock() <= 0) {
            throw new IllegalArgumentException(
                    "Sản phẩm \"" + product.getProductName() + "\" đã hết hàng");
        }

        int alreadyInCart = cart.stream()
                .filter(item -> item.getProduct().getProductId() == productId)
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (alreadyInCart + quantity > product.getStock()) {
            throw new IllegalArgumentException(
                    "Không đủ hàng! Tồn kho: " + product.getStock() +
                            " | Đã có trong giỏ: " + alreadyInCart + ".");
        }

        // Tinh gia thuc te tai luc them vao gio
        BigDecimal unitPrice = (flashSale != null)
                ? flashSaleService.calculateFlashPrice(product, flashSale)
                : product.getPrice();

        // Neu da co trong gio → cong them so luong
        for (CartItem item : cart) {
            if (item.getProduct().getProductId() == productId) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        // Chua co → them moi voi unitPrice vua tinh
        cart.add(new CartItem(product, quantity, unitPrice));
    }

    /** Overload khong co flash sale. */
    public void addToCart(List<CartItem> cart, int productId, int quantity) {
        addToCart(cart, productId, quantity, null);
    }

    public void removeFromCart(List<CartItem> cart, int index) {
        if (index < 0 || index >= cart.size()) {
            throw new IllegalArgumentException("Vị trí không hợp lệ");
        }
        cart.remove(index);
    }

    public BigDecimal calculateCartTotal(List<CartItem> cart) {
        return cart.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //đặt hàng

    public Order checkout(List<CartItem> cart, int userId,
                          String shippingAddress) throws SQLException {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ giao hàng không được để trống");
        }

        BigDecimal total = calculateCartTotal(cart);
        Order order = new Order(userId, total, "PENDING");

        List<OrderDetail> details = new ArrayList<>();
        for (CartItem item : cart) {
            OrderDetail detail = new OrderDetail();
            detail.setProductId(item.getProduct().getProductId());
            detail.setQuantity(item.getQuantity());
            detail.setPriceAtPurchase(item.getUnitPrice());
            details.add(detail);
        }

        boolean success = orderDAO.createOrder(order, details);
        if (!success) throw new SQLException("Dat hang that bai.");

        cart.clear();
        return order;
    }

    //đơn

    public List<Order> getOrderHistory(int userId) throws SQLException {
        return orderDAO.findByUserId(userId);
    }

    public List<OrderDetail> getOrderDetails(int orderId) throws SQLException {
        return orderDAO.findDetailsByOrderId(orderId);
    }

    //profile
    public Optional<User> getProfile(int userId) throws SQLException {
        return profileDAO.findById(userId);
    }

    public User updateProfile(int userId, String fullName,
                              String email, String phone,
                              String address) throws SQLException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }

        User user = getProfile(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setPhone(phone != null ? phone.trim() : "");
        user.setAddress(address != null ? address.trim() : "");

        profileDAO.updateProfile(user);
        return user;
    }

    // admin

    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.findAll();
    }

    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        return orderDAO.updateStatus(orderId, status);
    }

    public List<ProductSalesReport> getTop5BestSellingProductsThisMonth() throws SQLException {
        return orderDAO.getTop5BestSellingProductsThisMonth();
    }
}