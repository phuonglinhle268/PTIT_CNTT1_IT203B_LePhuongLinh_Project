package presentation;

import model.Order;
import model.OrderDetail;
import service.CustomerService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminOrderMenu {

    private final CustomerService service = new CustomerService();
    private final Scanner scanner = new Scanner(System.in);

    public void show() {
        while (true) {
            viewAllOrders();

            System.out.println("\n──────────────────────────────");
            System.out.println("1. Xem chi tiết đơn hàng");
            System.out.println("2. Cập nhật trạng thái đơn hàng");
            System.out.println("0. Quay lại Admin Menu");
            System.out.print("Chọn: ");

            int choice = readInt(0, 2);
            if (choice == 0){
                return;
            }
            if (choice == 1) {
                viewOrderDetail();
            }
            if (choice == 2) {
                updateOrderStatus();
            }
        }
    }

    private void viewAllOrders() {
        try {
            List<Order> orders = service.getAllOrders();

            if (orders.isEmpty()) {
                System.out.println("Chưa có đơn hàng nào.");
                return;
            }
            System.out.println("\n========== DANH SÁCH ĐƠN HÀNG ==========");
            System.out.printf("%-8s | %-15s | %-15s | %-15s | %-12s%n",
                    "Mã đơn", "Ngày đặt", "Khách hàng", "Tổng tiền", "Trạng thái");
            System.out.println("-".repeat(75));

            for (Order o : orders) {
                System.out.printf("%-8d | %-15s | %-15s | %,12.0fđ | %-12s%n",
                        o.getOrderId(),
                        o.getOrderDate() != null ? o.getOrderDate().toLocalDate() : "N/A",
                        o.getCustomerName() != null ? o.getCustomerName() : "Khách #" + o.getUserId(),
                        o.getTotalAmount(),
                        o.getStatus());
            }
        } catch (SQLException e) {
            System.out.println("Lỗi tải danh sách đơn hàng: " + e.getMessage());
        }
    }

    private void viewOrderDetail() {
        System.out.print("Nhập mã đơn hàng cần xem chi tiết: ");
        int orderId = readInt(1, Integer.MAX_VALUE);

        try {
            List<OrderDetail> details = service.getOrderDetails(orderId);
            if (details.isEmpty()) {
                System.out.println("Không tìm thấy đơn hàng");
                return;
            }

            System.out.println("\n===== CHI TIẾT ĐƠN HÀNG #" + orderId + " =====");
            for (OrderDetail d : details) {
                System.out.printf("• %s x%d = %, .0fđ%n",
                        d.getProductName() != null ? d.getProductName() : "Sản phẩm #" + d.getProductId(),
                        d.getQuantity(),
                        d.getPriceAtPurchase().multiply(BigDecimal.valueOf(d.getQuantity())));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi tải chi tiết: " + e.getMessage());
        }
    }

    private void updateOrderStatus() {
        System.out.print("Nhập mã đơn hàng cần cập nhật: ");
        int orderId = readInt(1, Integer.MAX_VALUE);

        System.out.println("Chọn trạng thái mới:");
        System.out.println("1. PENDING (Chờ xử lý)");
        System.out.println("2. SHIPPING (Đang giao)");
        System.out.println("3. DELIVERED (Đã giao)");
        System.out.println("4. CANCELLED (Đã hủy)");
        int choice = readInt(1, 4);

        String status = switch (choice) {
            case 1 -> "PENDING";
            case 2 -> "SHIPPING";
            case 3 -> "DELIVERED";
            case 4 -> "CANCELLED";
            default -> "PENDING";
        };

        try {
            boolean success = service.updateOrderStatus(orderId, status);
            if (success) {
                System.out.println("Đã cập nhật trạng thái đơn #" + orderId + " thành: " + status);
            } else {
                System.out.println("Không tìm thấy đơn hàng hoặc cập nhật thất bại.");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi cập nhật: " + e.getMessage());
        }
    }

    private int readInt(int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(scanner.nextLine().trim());
                if (n >= min && n <= max) return n;
            } catch (Exception ignored) {}
            System.out.print("Vui lòng nhập số hợp lệ: ");
        }
    }
}