package impl;

import dao.OrderDAO;

import dao.OrderDAO;
import model.Order;
import model.OrderDetail;
import model.ProductSalesReport;
import util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {
    @Override
    public boolean createOrder(Order order, List<OrderDetail> details) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();

        boolean prevAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            int orderId = insertOrder(conn, order);
            order.setOrderId(orderId);

            for (OrderDetail detail : details) {
                detail.setOrderId(orderId);

                //giảm stock — rollback nếu không đủ hàng
                boolean stockOk = decreaseStock(conn, detail.getProductId(), detail.getQuantity());
                if (!stockOk) {
                    conn.rollback();
                    throw new SQLException(
                            "Sản phẩm ID " + detail.getProductId() + " không đủ hàng. Đơn hàng đã bị hủy");
                }
                insertOrderDetail(conn, detail);
            }
            conn.commit();
            return true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(prevAutoCommit);
        }
    }

    @Override
    public List<Order> findByUserId(int userId) throws SQLException {
        String sql = "select * from Orders where user_id = ? order by order_date asc";
        List<Order> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrder(rs));
            }
        }
        return list;
    }

    @Override
    public List<OrderDetail> findDetailsByOrderId(int orderId) throws SQLException {
        String sql = "select od.*, p.product_name, p.storage, p.color " +
                        "from OrderDetails od " +
                        "join Products p on od.product_id = p.product_id " + "where od.order_id = ?";

        List<OrderDetail> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapDetail(rs));
            }
        }
        return list;
    }

    @Override
    public List<Order> findAll() throws SQLException {
        String sql = "select o.*, u.full_name " + "from Orders o " +
                "join Users u on o.user_id = u.user_id " + "order by o.order_date asc";

        List<Order> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order o = mapOrder(rs);
                o.setCustomerName(rs.getString("full_name"));
                list.add(o);
            }
        }
        return list;
    }

    @Override
    public boolean updateStatus(int orderId, String status) throws SQLException {
        String sql = "update Orders set status = ? where order_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }


    // Insert vào bảng Orders, trả về order_id
    private int insertOrder(Connection conn, Order order) throws SQLException {
        String sql =
                "insert into Orders (user_id, total_amount, status, coupon_code) " + "values (?, ?, 'PENDING', ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setBigDecimal(2, order.getTotalAmount());
            ps.setString(3, order.getCouponCode()); // DB cho phép null
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Không lấy được order_id sau khi insert.");
            }
        }
    }

    // Insert một dòng vào bảng OrderDetails
    private void insertOrderDetail(Connection conn, OrderDetail detail) throws SQLException {
        String sql = "insert into OrderDetails (order_id, product_id, quantity, price_at_purchase) " + "values (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detail.getOrderId());
            ps.setInt(2, detail.getProductId());
            ps.setInt(3, detail.getQuantity());
            ps.setBigDecimal(4, detail.getPriceAtPurchase());
            ps.executeUpdate();
        }
    }

//      Giảm stock trong cùng connection của transaction.
//      Chỉ update nếu stock >= quantity
//      Trả về false nếu không đủ hàng.
    private boolean decreaseStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "update Products set stock = stock - ? " + "where product_id = ? and stock >= ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<ProductSalesReport> getTop5BestSellingProductsThisMonth() throws SQLException {
        String sql = """
        select 
            p.product_id,
            p.product_name,
            p.brand,
            sum(od.quantity) as total_quantity,
            sum(od.quantity * od.price_at_purchase) as total_revenue
        from OrderDetails od
        join Products p on od.product_id = p.product_id
        join Orders o on od.order_id = o.order_id
        where month(o.order_date) = month(current_date) and year(o.order_date) = year(current_date)
        group by p.product_id, p.product_name, p.brand
        order by total_revenue desc limit 5
        """;

        List<ProductSalesReport> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProductSalesReport report = new ProductSalesReport();
                report.setProductId(rs.getInt("product_id"));
                report.setProductName(rs.getString("product_name"));
                report.setBrand(rs.getString("brand"));
                report.setTotalQuantity(rs.getInt("total_quantity"));
                report.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                list.add(report);
            }
        }
        return list;
    }

    // Ánh xạ ResultSet → Order
    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setStatus(rs.getString("status"));
        o.setCouponCode(rs.getString("coupon_code"));
        Timestamp ts = rs.getTimestamp("order_date");
        if (ts != null) o.setOrderDate(ts.toLocalDateTime());
        return o;
    }

    //Ánh xạ ResultSet → OrderDetail
    private OrderDetail mapDetail(ResultSet rs) throws SQLException {
        OrderDetail d = new OrderDetail();
        d.setOrderDetailId(rs.getInt("order_detail_id"));
        d.setOrderId(rs.getInt("order_id"));
        d.setProductId(rs.getInt("product_id"));
        d.setProductName(rs.getString("product_name"));
        d.setStorage(rs.getString("storage"));
        d.setColor(rs.getString("color"));
        d.setQuantity(rs.getInt("quantity"));
        d.setPriceAtPurchase(rs.getBigDecimal("price_at_purchase"));
        return d;
    }
}
