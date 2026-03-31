package presentation;

import model.Category;
import model.FlashSale;
import model.Product;
import service.CategoryService;
import service.FlashSaleService;
import service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ProductMenu {

    private final ProductService   productService;
    private final CategoryService  categoryService;
    private final FlashSaleService flashSaleService;
    private final Scanner          scanner;

    // Số sản phẩm mỗi trang
    private static final int PAGE_SIZE = 10;

    public ProductMenu(Scanner scanner) {
        this.productService   = new ProductService();
        this.categoryService  = new CategoryService();
        this.flashSaleService = new FlashSaleService();
        this.scanner          = scanner;
    }

    // =========================================================================
    // MENU CHÍNH
    // =========================================================================

    public void showMenu() {
        while (true) {
            System.out.println("\n========== QUẢN LÝ SẢN PHẨM ==========");
            System.out.println("  [1] Hiển thị tất cả sản phẩm");
            System.out.println("  [2] Thêm sản phẩm mới");
            System.out.println("  [3] Sửa thông tin sản phẩm");
            System.out.println("  [4] Xóa sản phẩm");
            System.out.println("  [5] Tìm kiếm theo tên");
            System.out.println("  [6] Sắp xếp theo giá tăng dần");
            System.out.println("  [7] Sắp xếp theo giá giảm dần");
            System.out.println("  [0] Quay lại Admin Menu");
            System.out.print("  Chọn chức năng: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Vui lòng nhập số!");
                continue;
            }

            switch (choice) {
                case 1 -> displayAllProducts();
                case 2 -> addProduct();
                case 3 -> updateProduct();
                case 4 -> deleteProduct();
                case 5 -> searchProduct();
                case 6 -> sortByPrice(true);
                case 7 -> sortByPrice(false);
                case 0 -> { System.out.println("  ← Quay lại Admin Menu..."); return; }
                default -> System.out.println("  ⚠ Lựa chọn không hợp lệ!");
            }
        }
    }

    // =========================================================================
    // [1] HIỂN THỊ DANH SÁCH — CÓ PHÂN TRANG
    // =========================================================================

    private void displayAllProducts() {
        int page       = 1;
        int total      = productService.countProducts();
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        while (true) {
            List<Product> list = productService.getProductsByPage(page, PAGE_SIZE);

            System.out.printf("%n===== DANH SÁCH SẢN PHẨM (Trang %d/%d | Tổng: %d) =====%n",
                    page, totalPages, total);
            printProductTable(list);

            System.out.println("\n  [1] Trang trước  [2] Trang sau  [0] Quay lại");
            System.out.print("  Chọn: ");

            int choice = readIntSafe(-1);
            if (choice == 1) {
                if (page > 1) page--;
                else System.out.println("  ⚠ Đang ở trang đầu!");
            } else if (choice == 2) {
                if (page < totalPages) page++;
                else System.out.println("  ⚠ Đang ở trang cuối!");
            } else if (choice == 0) {
                return;
            }
        }
    }

    // =========================================================================
    // IN BẢNG SẢN PHẨM (dùng cho Admin — không có flash sale)
    // =========================================================================

    public void printProductTable(List<Product> list) {
        if (list.isEmpty()) {
            System.out.println("  Chưa có sản phẩm nào.");
            return;
        }
        String line = "=".repeat(130);
        System.out.println("\n" + line);
        System.out.printf("%-5s | %-35s | %-18s | %-10s | %-12s | %15s | %8s%n",
                "ID", "TÊN SẢN PHẨM", "DANH MỤC", "DUNG LƯỢNG", "MÀU SẮC",
                "GIÁ BÁN", "TỒN KHO");
        System.out.println("-".repeat(130));

        for (Product p : list) {
            System.out.printf("%-5d | %-35s | %-18s | %-10s | %-12s | %,14.0fđ | %8d%n",
                    p.getProductId(),
                    truncate(p.getProductName(), 35),
                    p.getCategoryName() != null ? p.getCategoryName() : "N/A",
                    p.getStorage(),
                    p.getColor(),
                    p.getPrice(),
                    p.getStock());
        }
        System.out.println(line);
    }

    // =========================================================================
    // IN BẢNG SẢN PHẨM CHO CUSTOMER — tính giá flash động theo từng sản phẩm
    // =========================================================================

    public void printProductTableWithFlash(List<Product> list, FlashSale currentFlash) {
        if (list.isEmpty()) {
            System.out.println("  Chưa có sản phẩm nào.");
            return;
        }

        String line = "=".repeat(145);
        System.out.println("\n" + line);
        System.out.printf("%-5s | %-35s | %-18s | %-10s | %-12s | %-22s | %8s%n",
                "ID", "TÊN SẢN PHẨM", "DANH MỤC", "DUNG LƯỢNG", "MÀU SẮC",
                currentFlash != null ? "GIÁ (⚡ FLASH SALE)" : "GIÁ BÁN",
                "TỒN KHO");
        System.out.println("-".repeat(145));

        for (Product p : list) {
            String priceDisplay;

            if (currentFlash != null) {
                // Tính giá flash động từng sản phẩm = price * (1 - discountPercent/100)
                BigDecimal flashPrice = flashSaleService.calculateFlashPrice(p, currentFlash);
                priceDisplay = String.format("%,13.0fđ (-%d%%)",
                        flashPrice, currentFlash.getDiscountPercent());
            } else {
                priceDisplay = String.format("%,13.0fđ", p.getPrice());
            }

            System.out.printf("%-5d | %-35s | %-18s | %-10s | %-12s | %-22s | %8d%n",
                    p.getProductId(),
                    truncate(p.getProductName(), 35),
                    p.getCategoryName() != null ? p.getCategoryName() : "N/A",
                    p.getStorage(),
                    p.getColor(),
                    priceDisplay,
                    p.getStock());
        }
        System.out.println(line);

        if (currentFlash != null) {
            System.out.printf("  ⚡ Flash Sale đang giảm %d%% toàn bộ sản phẩm%n",
                    currentFlash.getDiscountPercent());
        }
    }

    // =========================================================================
    // [2] THÊM SẢN PHẨM
    // =========================================================================

    private void addProduct() {
        System.out.println("\n=== THÊM SẢN PHẨM MỚI ===");

        List<Category> categories = categoryService.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("  ⚠ Chưa có danh mục nào! Hãy vào Quản lý Danh mục để thêm trước.");
            return;
        }

        // Hiển thị danh mục để chọn
        System.out.println("  Danh sách danh mục:");
        for (Category c : categories) {
            System.out.printf("  [%d] %s%n", c.getCategoryId(), c.getCategoryName());
        }

        System.out.print("  Chọn ID danh mục: ");
        int categoryId = readIntSafe(-1);
        if (categoryId == -1) return;

        boolean validCat = categories.stream().anyMatch(c -> c.getCategoryId() == categoryId);
        if (!validCat) {
            System.out.println("  ✘ Danh mục ID " + categoryId + " không tồn tại!");
            return;
        }

        System.out.print("  Tên sản phẩm: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  ✘ Tên không được để trống!"); return; }

        System.out.print("  Dung lượng (ví dụ: 128GB): ");
        String storage = scanner.nextLine().trim();
        if (storage.isEmpty()) { System.out.println("  ✘ Dung lượng không được để trống!"); return; }

        System.out.print("  Màu sắc: ");
        String color = scanner.nextLine().trim();
        if (color.isEmpty()) { System.out.println("  ✘ Màu sắc không được để trống!"); return; }

        System.out.print("  Giá bán (VND): ");
        BigDecimal price;
        try {
            price = new BigDecimal(scanner.nextLine().trim().replace(",", ""));
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("  ✘ Giá phải lớn hơn 0!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("  ✘ Giá không hợp lệ!");
            return;
        }

        System.out.print("  Số lượng tồn kho: ");
        int stock;
        try {
            stock = Integer.parseInt(scanner.nextLine().trim());
            if (stock < 0) { System.out.println("  ✘ Tồn kho không được âm!"); return; }
        } catch (NumberFormatException e) {
            System.out.println("  ✘ Số lượng không hợp lệ!");
            return;
        }

        System.out.print("  Mô tả ngắn: ");
        String description = scanner.nextLine().trim();

        // brand lấy từ category_name — hoặc để admin nhập thêm
        String brand = categories.stream()
                .filter(c -> c.getCategoryId() == categoryId)
                .map(Category::getCategoryName)
                .findFirst().orElse("");

        Product p = new Product(categoryId, name, brand, storage, color, price, stock, description);

        if (productService.addProduct(p)) {
            System.out.println("  ✔ Thêm sản phẩm thành công!");
        } else {
            System.out.println("  ✘ Thêm sản phẩm thất bại!");
        }
    }

    // =========================================================================
    // [3] SỬA SẢN PHẨM
    // =========================================================================

    private void updateProduct() {
        System.out.println("\n=== CẬP NHẬT SẢN PHẨM ===");
        displayAllProducts();

        System.out.print("  Nhập ID sản phẩm cần sửa (0 = hủy): ");
        int id = readIntSafe(-1);
        if (id <= 0) return;

        Product old = productService.findById(id);
        if (old == null) {
            System.out.println("  ✘ Không tìm thấy sản phẩm ID = " + id);
            return;
        }

        System.out.println("  (Nhấn Enter để giữ nguyên giá trị cũ)");

        System.out.printf("  Tên sản phẩm [%s]: ", old.getProductName());
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) old.setProductName(name);

        System.out.printf("  Dung lượng [%s]: ", old.getStorage());
        String storage = scanner.nextLine().trim();
        if (!storage.isEmpty()) old.setStorage(storage);

        System.out.printf("  Màu sắc [%s]: ", old.getColor());
        String color = scanner.nextLine().trim();
        if (!color.isEmpty()) old.setColor(color);

        System.out.printf("  Giá bán [%,.0fđ]: ", old.getPrice());
        String priceStr = scanner.nextLine().trim();
        if (!priceStr.isEmpty()) {
            try {
                BigDecimal newPrice = new BigDecimal(priceStr.replace(",", ""));
                if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("  ⚠ Giá phải lớn hơn 0, giữ nguyên giá cũ.");
                } else {
                    old.setPrice(newPrice);
                }
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Giá không hợp lệ, giữ nguyên giá cũ.");
            }
        }

        System.out.printf("  Tồn kho [%d]: ", old.getStock());
        String stockStr = scanner.nextLine().trim();
        if (!stockStr.isEmpty()) {
            try {
                int newStock = Integer.parseInt(stockStr);
                if (newStock < 0) {
                    System.out.println("  ⚠ Tồn kho không được âm, giữ nguyên.");
                } else {
                    old.setStock(newStock);
                }
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Số lượng không hợp lệ, giữ nguyên.");
            }
        }

        System.out.printf("  Mô tả [%s]: ", old.getDescription());
        String desc = scanner.nextLine().trim();
        if (!desc.isEmpty()) old.setDescription(desc);

        // Đổi danh mục
        System.out.print("  Đổi danh mục? (Y/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            List<Category> categories = categoryService.getAllCategories();
            System.out.println("  Danh sách danh mục:");
            for (Category c : categories) {
                System.out.printf("  [%d] %s%n", c.getCategoryId(), c.getCategoryName());
            }
            System.out.print("  Chọn ID danh mục mới: ");
            int newCatId = readIntSafe(-1);
            if (newCatId > 0) {
                boolean valid = categories.stream().anyMatch(c -> c.getCategoryId() == newCatId);
                if (valid) {
                    old.setCategoryId(newCatId);
                } else {
                    System.out.println("  ⚠ ID không hợp lệ, giữ nguyên danh mục cũ.");
                }
            }
        }

        if (productService.updateProduct(old)) {
            System.out.println("  ✔ Cập nhật sản phẩm thành công!");
        } else {
            System.out.println("  ✘ Cập nhật thất bại!");
        }
    }

    // =========================================================================
    // [4] XÓA SẢN PHẨM
    // =========================================================================

    private void deleteProduct() {
        System.out.println("\n=== XÓA SẢN PHẨM ===");
        displayAllProducts();

        System.out.print("  Nhập ID sản phẩm muốn xóa (0 = hủy): ");
        int id = readIntSafe(-1);
        if (id <= 0) return;

        Product p = productService.findById(id);
        if (p == null) {
            System.out.println("  ✘ Không tìm thấy sản phẩm ID = " + id);
            return;
        }

        System.out.printf("  Sản phẩm: %s | %s | %,.0fđ%n",
                p.getProductName(), p.getStorage(), p.getPrice());
        System.out.print("  Xác nhận xóa? (Y/N): ");

        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            if (productService.deleteProduct(id)) {
                System.out.println("  ✔ Xóa sản phẩm thành công!");
            } else {
                System.out.println("  ✘ Xóa thất bại! (Sản phẩm có thể đang có trong đơn hàng)");
            }
        } else {
            System.out.println("  Đã hủy.");
        }
    }

    // =========================================================================
    // [5] TÌM KIẾM
    // =========================================================================

    private void searchProduct() {
        System.out.print("  Nhập từ khóa tìm kiếm: ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) { System.out.println("  ⚠ Từ khóa không được để trống."); return; }

        List<Product> list = productService.searchByName(keyword);
        System.out.printf("%n  Kết quả tìm kiếm cho \"%s\": %d sản phẩm%n", keyword, list.size());
        printProductTable(list);
    }

    // =========================================================================
    // [6] [7] SẮP XẾP THEO GIÁ
    // =========================================================================

    private void sortByPrice(boolean ascending) {
        List<Product> list = productService.getProductsSortedByPrice(ascending);
        System.out.printf("%n  Sắp xếp theo giá %s:%n", ascending ? "tăng dần ↑" : "giảm dần ↓");
        printProductTable(list);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    /** Đọc số nguyên an toàn, trả về defaultValue nếu lỗi. */
    private int readIntSafe(int defaultValue) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  ⚠ Vui lòng nhập số nguyên hợp lệ.");
            return defaultValue;
        }
    }

    private String truncate(String s, int max) {
        if (s == null || s.isEmpty()) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}