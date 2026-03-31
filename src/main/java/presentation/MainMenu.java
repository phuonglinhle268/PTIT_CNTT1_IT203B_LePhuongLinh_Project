package presentation;

import model.Product;
import model.Category;
import model.User;
import service.ProductService;
import service.CategoryService;

import java.util.List;
import java.util.Scanner;

public class MainMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final CategoryService categoryService = new CategoryService();
    private final ProductService productService = new ProductService();
    private final AuthMenu authMenu = new AuthMenu();

    private User currentUser = null;

    public void showMainMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("   SMARTPHONE STORE - CỬA HÀNG ĐIỆN THOẠI ONLINE   ");
            System.out.println("==================================================");
            System.out.println("1. Xem danh mục sản phẩm");
            System.out.println("2. Xem tất cả sản phẩm");
            System.out.println("3. Tìm kiếm sản phẩm");
            System.out.println("4. Đăng nhập / Đăng ký");
            System.out.println("0. Thoát chương trình");
            System.out.println("==================================================");
            System.out.print("Chọn chức năng: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Không hợp lệ");
                continue;
            }

            switch (choice) {
                case 1:
                    viewCategories();
                    break;
                case 2:
                    viewAllProducts();
                    break;
                case 3:
                    searchProducts();
                    break;
                case 4:
                    handleAuth();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private void viewCategories() {
        List<Category> categories = categoryService.getAllCategories();
        System.out.println("\n========== DANH MỤC SẢN PHẨM ==========");
        if (categories.isEmpty()) {
            System.out.println("Chưa có danh mục nào.");
            return;
        }
        for (Category c : categories) {
            System.out.printf("%d. %s%n", c.getCategoryId(), c.getCategoryName());
        }
    }

    private void viewAllProducts() {
        List<Product> products = productService.getAllProducts();
        System.out.println("\n========== DANH SÁCH SẢN PHẨM ==========");
        new ProductMenu(scanner).printProductTable(products);
    }

    private void searchProducts() {
        System.out.print("Nhập từ khóa tìm kiếm: ");
        String keyword = scanner.nextLine().trim();
        List<Product> list = productService.searchByName(keyword);
        System.out.println("\nKết quả tìm kiếm cho: \"" + keyword + "\"");
        new ProductMenu(scanner).printProductTable(list);
    }

    private void handleAuth() {
        authMenu.showLoginRegisterMenu(this);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}