package presentation;

import model.User;
import service.UserService;

import java.util.List;
import java.util.Scanner;

public class AdminUserMenu {

    private final UserService userService = new UserService();
    private final Scanner scanner = new Scanner(System.in);

    public void show() {
        while (true) {
            viewAllUsers();

            System.out.println("\n──────────────────────────────");
            System.out.println("1. Xem chi tiết người dùng");
            System.out.println("2. Cập nhật thông tin người dùng");
            System.out.println("0. Quay lại Admin Menu");
            System.out.print("Chọn: ");

            int choice = readInt(0, 2);

            switch (choice) {
                case 1:
                    viewUserDetail();
                    break;
                case 2:
                    updateUserProfile();
                    break;
                case 0:
                    System.out.println("Quay lại Admin Menu...");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("Chưa có khách hàng nào trong hệ thống.");
            return;
        }

        System.out.println("\n========== DANH SÁCH KHÁCH HÀNG ==========");
        System.out.printf("%-5s | %-15s | %-25s | %-25s | %-10s%n",
                "ID", "Username", "Họ tên", "Email", "Vai trò");
        System.out.println("-".repeat(85));

        for (User u : users) {
            System.out.printf("%-5d | %-15s | %-25s | %-25s | %-10s%n",
                    u.getUserId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getRole());
        }
        System.out.println("-".repeat(85));
        System.out.println(" => Tổng số khách hàng: " + users.size());
    }

    private void viewUserDetail() {
        System.out.print("Nhập ID người dùng cần xem: ");
        int userId = readInt(1, Integer.MAX_VALUE);

        User user = userService.getUserById(userId);
        if (user == null) {
            System.out.println("Không tìm thấy người dùng với ID " + userId);
            return;
        }

        System.out.println("\n========== CHI TIẾT NGƯỜI DÙNG ==========");
        System.out.println("ID            : " + user.getUserId());
        System.out.println("Username      : " + user.getUsername());
        System.out.println("Họ tên        : " + user.getFullName());
        System.out.println("Email         : " + user.getEmail());
        System.out.println("Số điện thoại : " + user.getPhone());
        System.out.println("Địa chỉ       : " + (user.getAddress() != null ? user.getAddress() : "Chưa cập nhật"));
        System.out.println("Vai trò       : " + user.getRole());
        System.out.println("Ngày tạo      : " + (user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate() : "N/A"));
    }

    private void updateUserProfile() {
        System.out.print("Nhập ID người dùng cần cập nhật: ");
        int userId = readInt(1, Integer.MAX_VALUE);

        User user = userService.getUserById(userId);
        if (user == null) {
            System.out.println("Không tìm thấy người dùng!");
            return;
        }
        System.out.println("\nĐang cập nhật cho: " + user.getFullName());

        System.out.print("Họ tên mới (Enter = giữ nguyên): ");
        String fullName = scanner.nextLine().trim();
        if (!fullName.isEmpty()) {
            user.setFullName(fullName);
        }
        System.out.print("Email mới: ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) {
            user.setEmail(email);
        }
        System.out.print("Số điện thoại mới: ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) {
            user.setPhone(phone);
        }
        System.out.print("Địa chỉ mới: ");
        String address = scanner.nextLine().trim();
        if (!address.isEmpty()) {
            user.setAddress(address);
        }
        try {
            boolean success = userService.updateUserProfile(user);
            if (success) {
                System.out.println("Cập nhật thông tin thành công!");
            } else {
                System.out.println("Cập nhật thất bại!");
            }
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
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