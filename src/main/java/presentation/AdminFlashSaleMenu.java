package presentation;

import model.FlashSale;
import service.FlashSaleService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class AdminFlashSaleMenu {

    private final FlashSaleService service = new FlashSaleService();
    private final Scanner scanner;

    private static final DateTimeFormatter INPUT_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminFlashSaleMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show() {
        while (true) {
            System.out.println("\n========== QUẢN LÝ FLASH SALE ==========");
            System.out.println("  [1] Xem tất cả Flash Sale");
            System.out.println("  [2] Tạo Flash Sale mới");
            System.out.println("  [3] Bật / Tắt Flash Sale");
            System.out.println("  [4] Xóa Flash Sale");
            System.out.println("  [0] Quay lại");
            System.out.print("  Chọn: ");

            int choice = readInt(0, 4);
            switch (choice) {
                case 1 -> viewAll();
                case 2 -> create();
                case 3 -> toggleActive();
                case 4 -> delete();
                case 0 -> { return; }
            }
        }
    }

    private void viewAll() {
        List<FlashSale> list = service.getAllFlashSales();
        if (list.isEmpty()) {
            System.out.println("  Chưa có Flash Sale nào.");
            return;
        }
        printTable(list);
    }

    private void create() {
        System.out.println("\n  ── TẠO FLASH SALE MỚI ──────────────────");
        System.out.println("  (Flash Sale áp dụng TOÀN BỘ sản phẩm trong cửa hàng)");

        System.out.print("  Phần trăm giảm giá (1-99%): ");
        int percent = readInt(1, 99);

        LocalDateTime startTime = readDateTime("  Thời gian bắt đầu (yyyy-MM-dd HH:mm): ");
        if (startTime == null) return;

        LocalDateTime endTime = readDateTime("  Thời gian kết thúc (yyyy-MM-dd HH:mm): ");
        if (endTime == null) return;

        try {
            FlashSale fs = new FlashSale(percent, startTime, endTime);
            if (service.createFlashSale(fs)) {
                System.out.println("Tạo Flash Sale thành công!");
                System.out.printf("    ID: %d | Giảm %d%% | %s → %s%n",
                        fs.getFlashSaleId(), percent,
                        startTime.format(DISPLAY_FMT), endTime.format(DISPLAY_FMT));
            } else {
                System.out.println("Tạo thất bại.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi tạo: " + e.getMessage());
        }
    }

    private void toggleActive() {
        List<FlashSale> list = service.getAllFlashSales();
        if (list.isEmpty()) { System.out.println("  Chưa có Flash Sale nào."); return; }
        printTable(list);

        System.out.print("  Nhập ID Flash Sale cần bật/tắt (0 = hủy): ");
        int id = readInt(0, Integer.MAX_VALUE);
        if (id == 0) return;

        FlashSale target = list.stream()
                .filter(f -> f.getFlashSaleId() == id)
                .findFirst().orElse(null);
        if (target == null) { System.out.println("Không tìm thấy ID " + id); return; }

        boolean newState = !target.isActive();
        if (service.toggleActive(id, newState)) {
            System.out.printf("Đã %s Flash Sale #%d.%n", newState ? "bật" : "tắt", id);
        } else {
            System.out.println("Thao tác thất bại.");
        }
    }

    private void delete() {
        List<FlashSale> list = service.getAllFlashSales();
        if (list.isEmpty()) { System.out.println("  Chưa có Flash Sale nào."); return; }
        printTable(list);

        System.out.print("  Nhập ID Flash Sale muốn xóa (0 = hủy): ");
        int id = readInt(0, Integer.MAX_VALUE);
        if (id == 0) return;

        System.out.print("  Xác nhận xóa Flash Sale #" + id + "? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return;

        if (service.deleteFlashSale(id)) {
            System.out.println("Đã xóa Flash Sale #" + id);
        } else {
            System.out.println("Xóa thất bại hoặc không tìm thấy ID.");
        }
    }

    private void printTable(List<FlashSale> list) {
        String line = "  " + "-".repeat(72);
        System.out.println(line);
        System.out.printf("  | %4s | %6s | %-18s | %-18s | %-12s |%n",
                "ID", "% Giảm", "Bắt đầu", "Kết thúc", "Trạng thái");
        System.out.println(line);
        for (FlashSale fs : list) {
            System.out.printf("  | %4d | %5d%% | %-18s | %-18s | %-12s |%n",
                    fs.getFlashSaleId(),
                    fs.getDiscountPercent(),
                    fs.getStartTime().format(DISPLAY_FMT),
                    fs.getEndTime().format(DISPLAY_FMT),
                    fs.getStatusLabel());
        }
        System.out.println(line);
    }

    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("0")) return null;
            try {
                return LocalDateTime.parse(input, INPUT_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Định dạng sai, nhập lại (yyyy-MM-dd HH:mm) hoặc 0 để hủy:");
            }
        }
    }

    private int readInt(int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(scanner.nextLine().trim());
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) {}
            System.out.print("Vui lòng nhập số hợp lệ: ");
        }
    }
}