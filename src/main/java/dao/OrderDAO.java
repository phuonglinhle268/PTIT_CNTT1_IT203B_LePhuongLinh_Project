package dao;

import model.Order;
import model.OrderDetail;
import model.ProductSalesReport;

import java.sql.SQLException;
import java.util.List;

public interface OrderDAO {
    boolean createOrder(Order order, List<OrderDetail> details) throws SQLException;
    List<Order> findByUserId(int userId) throws SQLException;
    List<OrderDetail> findDetailsByOrderId(int orderId) throws SQLException;
    List<Order> findAll() throws SQLException;
    boolean updateStatus(int orderId, String status) throws SQLException;
    List<ProductSalesReport> getTop5BestSellingProductsThisMonth() throws SQLException;
}