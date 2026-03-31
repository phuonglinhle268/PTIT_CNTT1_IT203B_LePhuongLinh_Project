package presentation;

import model.ProductSalesReport;
import service.CustomerService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminReportMenu {
    private final CustomerService service = new CustomerService();
    private final Scanner scanner = new Scanner(System.in);

    public void show() {
        while (true) {
            System.out.println("\n");
            System.out.println("=================== THỐNG KÊ DOANH THU ===================");
            System.out.println("1. Top 5 sản phẩm bán chạy trong tháng");
            System.out.println("0. Quay lại Admin Menu");
            System.out.println("==========================================================");
            System.out.print("Chọn: ");

            int choice = readInt(0, 1);

            if (choice == 0) return;
            if (choice == 1) topSellingProducts();
        }
    }

    private void topSellingProducts() {
        try {
            List<ProductSalesReport> top5 = service.getTop5BestSellingProductsThisMonth();

            if (top5.isEmpty()) {
                System.out.println("Chưa có đơn hàng nào trong tháng này.");
                return;
            }

            System.out.println("\n=== TOP 5 SẢN PHẨM BÁN CHẠY THÁNG NÀY ===");
            System.out.printf("%-5s | %-35s | %-12s | %10s | %15s%n",
                    "ID", "Tên sản phẩm", "Hãng", "Số lượng", "Doanh thu");
            System.out.println("-".repeat(85));

            for (ProductSalesReport r : top5) {
                System.out.println(r);
            }

        } catch (SQLException e) {
            System.out.println("Lỗi thống kê: " + e.getMessage());
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