package presentation;

import model.User;
import service.UserService;

import java.util.Scanner;

public class AuthMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService = new UserService();

    public void showLoginRegisterMenu(MainMenu mainMenu) {
        while (true) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║      ĐĂNG NHẬP / ĐĂNG KÝ         ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  [1] Đăng nhập                   ║");
            System.out.println("║  [2] Đăng ký tài khoản mới       ║");
            System.out.println("║  [0] Quay lại                    ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Chọn: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Không hợp lệ! Vui lòng nhập số.");
                continue;
            }

            switch (choice) {
                case 1:
                    User loggedInUser = performLogin();
                    if (loggedInUser != null) {
                        mainMenu.setCurrentUser(loggedInUser);
                        System.out.println("\nĐăng nhập thành công! Xin chào, " + loggedInUser.getFullName() + "!");

                        handleAfterLogin(loggedInUser, mainMenu);
                        return;
                    }
                    break;

                case 2:
                    performRegister();
                    break;

                case 0:
                    return;

                default:
                    System.out.println("Lựa chọn không hợp lệ!");
                    break;
            }
        }
    }

    private User performLogin() {
        System.out.println("\n───────────────────────── ĐĂNG NHẬP ─────────────────────────");

        System.out.print("  Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("  Mật khẩu: ");
        String password = scanner.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username và mật khẩu không được để trống.");
            return null;
        }

        User user = userService.login(username, password);

        if (user == null) {
            System.out.println("Đăng nhập thất bại!");
        }
        return user;
    }

    private void performRegister() {
        System.out.println("\n───────────────────────── ĐĂNG KÝ TÀI KHOẢN ─────────────────");

        System.out.print("  Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("  Mật khẩu (ít nhất 6 ký tự): ");
        String password = scanner.nextLine().trim();
        System.out.print("  Họ và tên: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("  Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("  Số điện thoại: ");
        String phone = scanner.nextLine().trim();
        System.out.print("  Địa chỉ: ");
        String address = scanner.nextLine().trim();

        User newUser = new User(username, password, fullName, email, phone, address, "CUSTOMER");

        String errorMsg = userService.register(newUser);

        if (errorMsg == null) {
            System.out.println("Đăng ký thành công! Đăng nhập để bắt đầu");
        } else {
            System.out.println("Đăng ký thất bại: " + errorMsg);
        }
    }

    private void handleAfterLogin(User user, MainMenu mainMenu) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            System.out.println("-> Bạn đang truy cập với quyền ADMIN");
            new AdminMenu().showMenu();
        } else {
            new CustomerMenu(scanner, user).show();
        }
    }
}