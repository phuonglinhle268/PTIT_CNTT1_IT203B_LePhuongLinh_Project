package presentation;

import model.Coupon;
import service.CouponService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class AdminCouponMenu {

    private final CouponService service = new CouponService();
    private final Scanner       scanner;

    public AdminCouponMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show() {
        while (true) {
            System.out.println("\n========== QUẢN LÝ MÃ GIẢM GIÁ ==========");
            System.out.println("  [1] Xem tất cả mã giảm giá");
            System.out.println("  [2] Tạo mã giảm giá mới");
            System.out.println("  [3] Bật / Tắt mã giảm giá");
            System.out.println("  [4] Xóa mã giảm giá");
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
        List<Coupon> list = service.getAllCoupons();
        if (list.isEmpty()) {
            System.out.println("  Chưa có mã giảm giá nào.");
            return;
        }
        printTable(list);
    }

    private void create() {
        System.out.println("\n  ── TẠO MÃ GIẢM GIÁ MỚI ────────────────");

        System.out.print("  Mã giảm giá (VD: SALE20): ");
        String code = scanner.nextLine().trim().toUpperCase();

        System.out.print("  Phần trăm giảm (1-100%): ");
        int percent = readInt(1, 100);

        System.out.print("  Số lần sử dụng tối đa: ");
        int maxUses = readInt(1, 10000);

        LocalDate startDate = readDate("  Ngày bắt đầu (yyyy-MM-dd): ");
        if (startDate == null) return;

        LocalDate endDate = readDate("  Ngày kết thúc (yyyy-MM-dd): ");
        if (endDate == null) return;

        try {
            Coupon coupon = new Coupon(code, percent, maxUses, startDate, endDate);
            if (service.createCoupon(coupon)) {
                System.out.println("  ✔ Tạo mã giảm giá thành công!");
                System.out.printf("    ID: %d | Mã: %s | Giảm %d%% | %s → %s%n",
                        coupon.getCouponId(), code, percent, startDate, endDate);
            } else {
                System.out.println("  ✘ Tạo thất bại (mã đã tồn tại?).");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  ✘ " + e.getMessage());
        }
    }

    private void toggleActive() {
        List<Coupon> list = service.getAllCoupons();
        if (list.isEmpty()) { System.out.println("  Chưa có mã nào."); return; }
        printTable(list);

        System.out.print("  Nhập ID mã giảm giá cần bật/tắt (0 = hủy): ");
        int id = readInt(0, Integer.MAX_VALUE);
        if (id == 0) return;

        Coupon target = list.stream()
                .filter(c -> c.getCouponId() == id)
                .findFirst().orElse(null);
        if (target == null) { System.out.println("  ✘ Không tìm thấy ID " + id); return; }

        boolean newState = !target.isActive();
        if (service.toggleActive(id, newState)) {
            System.out.printf("  ✔ Đã %s mã \"%s\" (#%d).%n",
                    newState ? "bật" : "tắt", target.getCouponCode(), id);
        } else {
            System.out.println("  ✘ Thao tác thất bại.");
        }
    }

    private void delete() {
        List<Coupon> list = service.getAllCoupons();
        if (list.isEmpty()) { System.out.println("  Chưa có mã nào."); return; }
        printTable(list);

        System.out.print("  Nhập ID mã giảm giá muốn xóa (0 = hủy): ");
        int id = readInt(0, Integer.MAX_VALUE);
        if (id == 0) return;

        System.out.print("  Xác nhận xóa mã #" + id + "? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return;

        if (service.deleteCoupon(id)) {
            System.out.println("  ✔ Đã xóa mã #" + id);
        } else {
            System.out.println("  ✘ Xóa thất bại hoặc không tìm thấy ID.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void printTable(List<Coupon> list) {
        String line = "  " + "-".repeat(82);
        System.out.println(line);
        System.out.printf("  | %4s | %-12s | %6s | %6s | %6s | %-10s | %-10s | %-14s |%n",
                "ID", "Mã", "% Giảm", "Max", "Đã dùng", "Bắt đầu", "Kết thúc", "Trạng thái");
        System.out.println(line);
        for (Coupon c : list) {
            System.out.printf("  | %4d | %-12s | %5d%% | %6d | %7d | %-10s | %-10s | %-14s |%n",
                    c.getCouponId(),
                    c.getCouponCode(),
                    c.getDiscountPercent(),
                    c.getMaxUses(),
                    c.getUsedCount(),
                    c.getStartDate(),
                    c.getEndDate(),
                    c.getStatusLabel());
        }
        System.out.println(line);
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals("0")) return null;
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("  ⚠ Định dạng sai, nhập lại (yyyy-MM-dd) hoặc 0 để hủy:");
            }
        }
    }

    private int readInt(int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(scanner.nextLine().trim());
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) {}
            System.out.print("  ⚠ Vui lòng nhập số hợp lệ: ");
        }
    }
}