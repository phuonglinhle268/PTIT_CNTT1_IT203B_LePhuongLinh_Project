package presentation;

import model.Category;
import service.CategoryService;

import java.util.List;
import java.util.Scanner;

public class CategoryMenu {
    private final CategoryService categoryService = new CategoryService();
    Scanner scanner = new Scanner(System.in);

    public void showMenu() {
        while (true) {
            System.out.println("\n");
            System.out.println("=========== QUẢN LÝ DANH MỤC ===========");
            System.out.println("1. Hiển thị danh sách danh mục");
            System.out.println("2. Thêm danh mục mới");
            System.out.println("3. Sửa danh mục");
            System.out.println("4. Xóa danh mục");
            System.out.println("0. Quay lại menu chính");
            System.out.print("Chọn chức năng: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    displayAllCategories();
                    break;
                case 2:
                    addNewCategory();
                    break;
                case 3:
                    updateCategory();
                    break;
                case 4:
                    deleteCategory();
                    break;
                case 0:
                    System.out.println("Quay lại menu chính");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private void displayAllCategories() {
        List<Category> list = categoryService.getAllCategories();

        if (list.isEmpty()) {
            System.out.println("Chưa có danh mục nào trong hệ thống.");
            return;
        }
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("%-5s | %-35s | %-18s%n", "ID", "TÊN DANH MỤC", "NGÀY TẠO");
        System.out.println("-".repeat(60));

        for (Category c : list) {
            String createdDate = c.getCreatedAt() != null
                    ? c.getCreatedAt().toLocalDate().toString() : "N/A";

            System.out.printf("%-5d | %-35s | %-18s%n", c.getCategoryId(), c.getCategoryName(), createdDate);
        }

        System.out.println("=".repeat(60));
        System.out.println(" => Tổng số danh mục: " + list.size());
    }

    private void addNewCategory() {
        System.out.print("Nhập tên danh mục mới: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Tên danh mục không được để trống!");
            return;
        }

        if (categoryService.addCategory(name)) {
            System.out.println("Thêm danh mục thành công!");
            displayAllCategories();
        }
    }

    private void updateCategory() {
        displayAllCategories();
        System.out.print("Nhập ID danh mục muốn sửa: ");

        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID phải là số!");
            return;
        }

        System.out.print("Nhập tên mới: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            System.out.println("Tên danh mục không được để trống!");
            return;
        }

        if (categoryService.updateCategory(id, newName)) {
            System.out.println("Sửa danh mục thành công!");
            displayAllCategories();
        }
    }

    private void deleteCategory() {
        displayAllCategories();
        System.out.print("Nhập ID danh mục muốn xóa: ");

        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID phải là số");
            return;
        }

        System.out.print("Bạn có chắc chắn muốn xóa danh mục này? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y")) {
            if (categoryService.deleteCategory(id)) {
                System.out.println("Xóa danh mục thành công!");
                displayAllCategories();
            }
        } else {
            System.out.println("Đã hủy xóa.");
        }
    }
}
