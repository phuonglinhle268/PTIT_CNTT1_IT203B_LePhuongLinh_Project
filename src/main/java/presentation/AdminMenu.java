package presentation;

import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final CategoryMenu categoryMenu = new CategoryMenu();
    private final ProductMenu productMenu = new ProductMenu(scanner);
    private final AdminOrderMenu adminOrderMenu = new AdminOrderMenu();
    private final AdminUserMenu adminUserMenu = new AdminUserMenu();
    private final AdminReportMenu adminReportMenu = new AdminReportMenu();
    private final AdminFlashSaleMenu adminFlashSaleMenu = new AdminFlashSaleMenu(scanner);
    private final AdminCouponMenu adminCouponMenu = new AdminCouponMenu(scanner);

    public void showMenu() {
        while (true) {
            System.out.println("\n");
            System.out.println("=================== ADMIN MENU ===================");
            System.out.println("1. Quản lý Danh mục");
            System.out.println("2. Quản lý Sản phẩm");
            System.out.println("3. Quản lý Đơn hàng");
            System.out.println("4. Quản lý Khách hàng");
            System.out.println("5. Quản lý Flash Sale");
            System.out.println("6. Thống kê Doanh thu");
            System.out.println("7. Quản lý Mã giảm giá");
            System.out.println("0. Đăng xuất");
            System.out.println("==================================================");
            System.out.print("Chọn chức năng: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Không hợp lệ");
                continue;
            }

            switch (choice) {
                case 1:
                    categoryMenu.showMenu();
                    break;
                case 2:
                    productMenu.showMenu();
                    break;
                case 3:
                    adminOrderMenu.show();
                    break;
                case 4:
                    adminUserMenu.show();
                    break;
                case 5:
                    adminFlashSaleMenu.show();
                    break;
                case 6:
                    adminReportMenu.show();
                    break;
                case 7:
                    adminCouponMenu.show();
                    break;
                case 0:
                    System.out.println("Đăng xuất thành công!");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ");
            }
        }
    }
}
