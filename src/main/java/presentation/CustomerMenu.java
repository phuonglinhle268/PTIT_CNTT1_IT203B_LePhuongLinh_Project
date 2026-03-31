package presentation;

import model.*;
import service.CouponService;
import service.CustomerService;
import service.FlashSaleService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu {

    private final CustomerService  service = new CustomerService();
    private final FlashSaleService flashSaleService = new FlashSaleService();
    private final CouponService couponService = new CouponService();
    private final Scanner scanner;
    private final User currentUser;
    private final List<CartItem> cart = new ArrayList<>();

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CustomerMenu(Scanner scanner, User currentUser) {
        this.scanner     = scanner;
        this.currentUser = currentUser;
    }

    public void show() {
        while (true) {
            printHeader();
            System.out.println("  [1] Xem danh sách sản phẩm");
            System.out.println("  [2] Thêm vào giỏ hàng");
            System.out.println("  [3] Xem giỏ hàng & Đặt hàng");
            System.out.println("  [4] Lịch sử đơn hàng");
            System.out.println("  [5] Quản lý hồ sơ");
            System.out.println("  [0] Đăng xuất");
            System.out.print("  Chọn: ");

            int choice = readInt(0, 5);
            switch (choice) {
                case 1 -> viewAndFilterProducts();
                case 2 -> addToCart();
                case 3 -> handleCart();
                case 4 -> viewOrderHistory();
                case 5 -> handleProfile();
                case 0 -> {
                    System.out.println("Đã đăng xuất. Hẹn gặp lại, "
                            + currentUser.getFullName() + "!");
                    return;
                }
            }
        }
    }

    private void viewAndFilterProducts() {
        while (true) {
            FlashSale flash = flashSaleService.getCurrentActiveFlashSale();
            List<Product> products = service.getProductsInStock();

            System.out.println("\n=== DANH SÁCH SẢN PHẨM CÒN HÀNG ===");
            if (flash != null) {
                System.out.printf("  ⚡ FLASH SALE ĐANG DIỄN RA: Giảm %d%% | Kết thúc lúc %s%n",
                        flash.getDiscountPercent(),
                        flash.getEndTime().format(DT_FMT));
                System.out.println("  " + "=".repeat(60));
            }

            printProductTable(products, flash);

            System.out.println("\n  [1] Lọc theo hãng");
            System.out.println("  [2] Lọc theo khoảng giá");
            System.out.println("  [0] Quay lại");
            System.out.print("  Chọn: ");

            int choice = readInt(0, 2);
            if (choice == 0) return;

            List<Product> filtered = null;

            if (choice == 1) {
                // Hiển thị danh sách hãng có sẵn
                List<String> brands = service.getAvailableBrands();
                System.out.println("  Các hãng hiện có: " + String.join(", ", brands));
                System.out.print("  Nhập tên hãng: ");
                String brand = scanner.nextLine().trim();
                if (brand.isEmpty()) continue;
                filtered = service.filterByBrand(brand);

            } else if (choice == 2) {
                try {
                    System.out.print("  Giá từ (triệu VND, ví dụ: 5): ");
                    BigDecimal min = new BigDecimal(scanner.nextLine().trim())
                            .multiply(BigDecimal.valueOf(1_000_000));

                    System.out.print("  Giá đến (triệu VND, ví dụ: 20): ");
                    BigDecimal max = new BigDecimal(scanner.nextLine().trim())
                            .multiply(BigDecimal.valueOf(1_000_000));

                    if (min.compareTo(max) > 0) {
                        System.out.println("Giá tối thiểu phải nhỏ hơn giá tối đa!");
                        continue;
                    }
                    filtered = service.filterByPriceRange(min, max);
                } catch (NumberFormatException e) {
                    System.out.println("Vui lòng nhập số hợp lệ.");
                    continue;
                }
            }

            if (filtered == null) continue;

            System.out.println("\n  Kết quả lọc:");
            if (filtered.isEmpty()) {
                System.out.println("  Không tìm thấy sản phẩm phù hợp.");
            } else {
                printProductTable(filtered, flash);
            }

            System.out.print("\n  Nhấn Enter để tiếp tục...");
            scanner.nextLine();
        }
    }

    private void addToCart() {
        FlashSale flash = flashSaleService.getCurrentActiveFlashSale();
        List<Product> products = service.getProductsInStock();

        if (products.isEmpty()) {
            System.out.println("Hiện chưa có sản phẩm nào còn hàng.");
            return;
        }

        System.out.println("\n=== CHỌN SẢN PHẨM ===");
        printProductTable(products, flash);

        System.out.print("\n  Nhập ID sản phẩm muốn thêm (0 = hủy): ");
        int productId = readInt(0, Integer.MAX_VALUE);
        if (productId == 0) return;

        System.out.print("  Số lượng: ");
        int qty = readInt(1, 999);

        try {
            service.addToCart(cart, productId, qty, flash);
            String name = cart.stream()
                    .filter(i -> i.getProduct().getProductId() == productId)
                    .map(i -> i.getProduct().getProductName())
                    .findFirst().orElse("ID " + productId);
            System.out.printf("Đã thêm \"%s\" x%d vào giỏ hàng!%n", name, qty);
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

    private void handleCart() {
        if (cart.isEmpty()) {
            System.out.println("Giỏ hàng đang trống!");
            return;
        }

        while (true) {
            printCartTable();

            System.out.println("\n  [1] Đặt các sản phẩm được chọn");
            System.out.println("  [2] Đặt tất cả trong giỏ");
            System.out.println("  [3] Xóa sản phẩm khỏi giỏ");
            System.out.println("  [0] Quay lại");
            System.out.print("  Chọn: ");

            int choice = readInt(0, 3);
            switch (choice) {
                case 1 -> checkoutSelected();
                case 2 -> performCheckout(new ArrayList<>(cart));
                case 3 -> {
                    removeCartItems();
                    if (cart.isEmpty()) return;
                }
                case 0 -> { return; }
            }
        }
    }

    // Đặt hàng có chọn lọc
    private void checkoutSelected() {
        printCartTable();

        System.out.println("\nNhập STT sản phẩm muốn đặt (cách nhau dấu cách, ví dụ: 1 3):");
        System.out.print("  > ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return;

        List<Integer> stts = new ArrayList<>();
        for (String s : input.split("\\s+")) {
            try {
                int stt = Integer.parseInt(s);
                if (stt >= 1 && stt <= cart.size() && !stts.contains(stt)) {
                    stts.add(stt);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (stts.isEmpty()) {
            System.out.println("Không có sản phẩm nào được chọn hợp lệ.");
            return;
        }

        // Với mỗi sản phẩm được chọn → hỏi số lượng muốn đặt
        // Tạo CartItem mới với số lượng đã điều chỉnh (không thay đổi giỏ gốc)
        List<CartItem> selected = new ArrayList<>();
        for (int stt : stts) {
            CartItem item = cart.get(stt - 1);
            Product p = item.getProduct();

            System.out.printf("\n  [%d] %s (Giỏ: %d | Tồn kho: %d)%n",
                    stt, p.getProductName(), item.getQuantity(), p.getStock());
            System.out.printf("  Số lượng muốn đặt (1-%d, Enter = đặt hết %d): ",
                    item.getQuantity(), item.getQuantity());
            String qtyInput = scanner.nextLine().trim();

            int qtyToOrder;
            if (qtyInput.isEmpty()) {
                qtyToOrder = item.getQuantity(); // đặt hết số lượng trong giỏ
            } else {
                try {
                    qtyToOrder = Integer.parseInt(qtyInput);
                    if (qtyToOrder <= 0 || qtyToOrder > item.getQuantity()) {
                        System.out.printf("Số lượng không hợp lệ, đặt hết %d.%n", item.getQuantity());
                        qtyToOrder = item.getQuantity();
                    }
                } catch (NumberFormatException e) {
                    System.out.printf("Nhập sai, đặt hết %d.%n", item.getQuantity());
                    qtyToOrder = item.getQuantity();
                }
            }

            // Tạo CartItem tạm với số lượng muốn đặt, giữ nguyên unitPrice
            selected.add(new CartItem(p, qtyToOrder, item.getUnitPrice()));
        }

        // Hiển thị tóm tắt
        System.out.println("\n  Sản phẩm sẽ đặt:");
        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < selected.size(); i++) {
            CartItem item = selected.get(i);
            subtotal = subtotal.add(item.getSubtotal());
            System.out.printf("  %d. %-30s x%-3d = %,.0fđ%n",
                    i + 1, truncate(item.getProduct().getProductName(), 30),
                    item.getQuantity(), item.getSubtotal());
        }
        System.out.printf("  Tạm tính: %,.0f VND%n", subtotal);

        performCheckout(selected);
    }

    // Thực hiện đặt hàng
    private void performCheckout(List<CartItem> selectedItems) {
        String shippingAddress;
        String currentAddr = currentUser.getAddress();

        if (currentAddr != null && !currentAddr.isEmpty()) {
            System.out.println("\n  Địa chỉ hiện tại: " + currentAddr);
            System.out.print("  Dùng địa chỉ này? (Y/N): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                shippingAddress = currentAddr;
            } else {
                System.out.print("  Nhập địa chỉ giao hàng mới: ");
                shippingAddress = scanner.nextLine().trim();
            }
        } else {
            System.out.print("\n  Nhập địa chỉ giao hàng: ");
            shippingAddress = scanner.nextLine().trim();
        }

        if (shippingAddress.isEmpty()) {
            System.out.println("Địa chỉ không được để trống.");
            return;
        }

        BigDecimal originalTotal = selectedItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.print("  Nhập mã giảm giá (Enter để bỏ qua): ");
        String couponInput = scanner.nextLine().trim().toUpperCase();
        String couponCode  = couponInput.isEmpty() ? null : couponInput;

        BigDecimal finalTotal = originalTotal;

        if (couponCode != null) {
            try {
                finalTotal = couponService.applyDiscount(originalTotal, couponCode);
                BigDecimal saved = originalTotal.subtract(finalTotal);
                System.out.printf("Mã hợp lệ! Tiết kiệm: %,.0f VND%n", saved);
            } catch (IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage());
                System.out.print("  Tiếp tục không áp dụng mã giảm giá? (Y/N): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return;
                couponCode = null;
                finalTotal = originalTotal;
            }
        }

        System.out.println("\n  ┌─────────────────────────────────────────────┐");
        System.out.println("  │             TÓM TẮT ĐƠN HÀNG               │");
        System.out.println("  ├─────────────────────────────────────────────┤");
        for (CartItem item : selectedItems) {
            System.out.printf("  │  %-28s x%-3d %,9.0fđ  │%n",
                    truncate(item.getProduct().getProductName(), 28),
                    item.getQuantity(), item.getSubtotal());
        }
        System.out.println("  ├─────────────────────────────────────────────┤");
        System.out.printf("  │  Tổng gốc    : %,28.0f VND │%n", originalTotal);
        if (couponCode != null) {
            System.out.printf("  │  Mã giảm giá : %-28s │%n", couponCode);
            System.out.printf("  │  Sau giảm    : %,28.0f VND │%n", finalTotal);
        }
        System.out.printf("  │  Giao đến    : %-28s │%n", truncate(shippingAddress, 28));
        System.out.println("  └─────────────────────────────────────────────┘");

        System.out.print("  Xác nhận đặt hàng? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("  Đã hủy đặt hàng.");
            return;
        }

        List<CartItem> tempCart = new ArrayList<>(selectedItems);

        try {
            Order order = service.checkout(tempCart, currentUser.getUserId(), shippingAddress);

            if (couponCode != null) {
                couponService.useCoupon(couponCode);
            }

            System.out.println("\nĐặt hàng thành công!");
            System.out.println("  ┌──────────────────────────────────────────────┐");
            System.out.printf ("  │  Mã đơn hàng : #%-27d│%n", order.getOrderId());
            System.out.printf ("  │  Thanh toán  : %,27.0f VND│%n", finalTotal);
            System.out.printf ("  │  Mã giảm giá : %-27s│%n",
                    couponCode != null ? couponCode : "-");
            System.out.printf ("  │  Trạng thái  : %-27s│%n", "⏳ Chờ xử lý (PENDING)");
            System.out.printf ("  │  Giao đến    : %-27s│%n", truncate(shippingAddress, 27));
            System.out.println("  └──────────────────────────────────────────────┘");

            // Cap nhat gio hang sau dat hang:
            // Neu dat het → xoa khoi gio
            // Neu dat mot phan → giam so luong trong gio
            for (CartItem orderedItem : selectedItems) {
                int orderedProductId = orderedItem.getProduct().getProductId();
                cart.removeIf(cartItem -> {
                    if (cartItem.getProduct().getProductId() == orderedProductId) {
                        int remaining = cartItem.getQuantity() - orderedItem.getQuantity();
                        if (remaining <= 0) {
                            return true; // xoa khoi gio
                        } else {
                            cartItem.setQuantity(remaining); // giam so luong
                            return false;
                        }
                    }
                    return false;
                });
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Lỗi đặt hàng: " + e.getMessage());
        }
    }

    private void removeCartItems() {
        printCartTable();
        System.out.println("  Nhập STT muốn xóa (cách nhau dấu cách, ví dụ: 1 3):");
        System.out.print("  > ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return;

        List<Integer> indices = new ArrayList<>();
        for (String s : input.split("\\s+")) {
            try {
                int stt = Integer.parseInt(s);
                if (stt >= 1 && stt <= cart.size() && !indices.contains(stt - 1)) {
                    indices.add(stt - 1);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (indices.isEmpty()) {
            System.out.println("STT không hợp lệ.");
            return;
        }

        System.out.print("Xác nhận xóa " + indices.size() + " sản phẩm? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return;

        // Xóa từ cuối để không lệch index
        indices.sort((a, b) -> b - a);
        for (int idx : indices) cart.remove(idx);
        System.out.println("Đã xóa " + indices.size() + " sản phẩm khỏi giỏ hàng.");
    }

    private void viewOrderHistory() {
        try {
            List<Order> orders = service.getOrderHistory(currentUser.getUserId());

            if (orders.isEmpty()) {
                System.out.println("  Bạn chưa có đơn hàng nào.");
                return;
            }

            System.out.println("\n=== LỊCH SỬ ĐƠN HÀNG ===");
            printOrderTable(orders);

            System.out.print("\nNhập mã đơn để xem chi tiết (0 = quay lại): ");
            int orderId = readInt(0, Integer.MAX_VALUE);
            if (orderId == 0) return;

            // Kiểm tra đơn có thuộc customer này không
            boolean belongs = orders.stream().anyMatch(o -> o.getOrderId() == orderId);
            if (!belongs) {
                System.out.println("Không tìm thấy đơn hàng #" + orderId + " trong lịch sử của bạn.");
                return;
            }
            viewOrderDetail(orderId, orders);

        } catch (SQLException e) {
            System.out.println("Lỗi tải lịch sử: " + e.getMessage());
        }
    }

    private void viewOrderDetail(int orderId, List<Order> orders) {
        // Lấy thông tin đơn hàng từ danh sách để hiển thị trạng thái
        Order order = orders.stream()
                .filter(o -> o.getOrderId() == orderId)
                .findFirst().orElse(null);

        try {
            List<OrderDetail> details = service.getOrderDetails(orderId);

            if (details.isEmpty()) {
                System.out.println("Không có chi tiết cho đơn #" + orderId);
                return;
            }

            System.out.println("\n  ── Chi tiết đơn hàng #" + orderId + " ──────────────────────────");
            if (order != null) {
                System.out.printf("  Ngày đặt  : %s%n",
                        order.getOrderDate() != null ? order.getOrderDate().format(DT_FMT) : "N/A");
                System.out.printf("  Trạng thái: %s%n", statusLabel(order.getStatus()));
                if (order.getCouponCode() != null) {
                    System.out.printf("  Mã CK     : %s%n", order.getCouponCode());
                }
            }

            String line = "  " + "-".repeat(80);
            System.out.println(line);
            System.out.printf("  | %-28s | %-8s | %-10s | %4s | %14s |%n", "Sản phẩm", "Lưu trữ", "Màu sắc", "SL", "Thành tiền");
            System.out.println(line);

            BigDecimal total = BigDecimal.ZERO;
            for (OrderDetail d : details) {
                BigDecimal sub = d.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(d.getQuantity()));
                total = total.add(sub);
                System.out.printf("  | %-28s | %-8s | %-10s | %4d | %,14.0f |%n",
                        truncate(d.getProductName(), 28),
                        d.getStorage(),
                        d.getColor(),
                        d.getQuantity(),
                        sub);
            }
            System.out.println(line);
            System.out.printf("  %-62s TỔNG: %,14.0f VND%n", "", total);

        } catch (SQLException e) {
            System.out.println("  ✘ Lỗi tải chi tiết: " + e.getMessage());
        }
    }

    private void handleProfile() {
        while (true) {
            System.out.println("\n=== QUẢN LÝ HỒ SƠ ===");

            try {
                User profile = service.getProfile(currentUser.getUserId())
                        .orElse(currentUser);
                System.out.println("  ┌──────────────────────────────────────────────┐");
                System.out.printf ("  │  Username   : %-30s│%n", profile.getUsername());
                System.out.printf ("  │  Họ tên     : %-30s│%n", profile.getFullName());
                System.out.printf ("  │  Email      : %-30s│%n", profile.getEmail());
                System.out.printf ("  │  Điện thoại : %-30s│%n", profile.getPhone());
                System.out.printf ("  │  Địa chỉ    : %-30s│%n",
                        truncate(profile.getAddress(), 30));
                System.out.println("  └──────────────────────────────────────────────┘");
            } catch (SQLException e) {
                System.out.println("Lỗi tải hồ sơ: " + e.getMessage());
            }

            System.out.println("\n  [1] Cập nhật thông tin");
            System.out.println("  [0] Quay lại");
            System.out.print("  Chọn: ");

            if (readInt(0, 1) == 0) return;

            System.out.printf("  Họ và tên [%s]: ", currentUser.getFullName());
            String fullName = scanner.nextLine().trim();
            if (fullName.isEmpty()) fullName = currentUser.getFullName();

            System.out.printf("  Email [%s]: ", currentUser.getEmail());
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) email = currentUser.getEmail();

            System.out.printf("  Số điện thoại [%s]: ", currentUser.getPhone());
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty()) phone = currentUser.getPhone();

            System.out.printf("  Địa chỉ [%s]: ",
                    currentUser.getAddress() != null ? currentUser.getAddress() : "");
            String address = scanner.nextLine().trim();
            if (address.isEmpty()) address = currentUser.getAddress();

            try {
                User updated = service.updateProfile(
                        currentUser.getUserId(), fullName, email, phone, address);
                // Cập nhật currentUser ngay để phản ánh trên menu
                currentUser.setFullName(updated.getFullName());
                currentUser.setEmail(updated.getEmail());
                currentUser.setPhone(updated.getPhone());
                currentUser.setAddress(updated.getAddress());
                System.out.println("Cập nhật hồ sơ thành công!");
            } catch (IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }
    }

    private void printProductTable(List<Product> list, FlashSale flash) {
        String line = "  " + "-".repeat(108);
        System.out.println(line);
        System.out.printf("  | %4s | %-28s | %-10s | %-7s | %-10s | %14s | %5s |%n",
                "ID", "Tên sản phẩm", "Hãng", "Lưu trữ", "Màu sắc",
                flash != null ? "Giá Flash ⚡" : "Giá (VND)", "Kho");
        System.out.println(line);

        for (Product p : list) {
            BigDecimal displayPrice = (flash != null)
                    ? flashSaleService.calculateFlashPrice(p, flash)
                    : p.getPrice();
            System.out.printf("  | %4d | %-28s | %-10s | %-7s | %-10s | %,14.0f | %5d |%n",
                    p.getProductId(),
                    truncate(p.getProductName(), 28),
                    truncate(p.getBrand(), 10),
                    p.getStorage(),
                    truncate(p.getColor(), 10),
                    displayPrice,
                    p.getStock());
        }

        System.out.println(line);
        System.out.print("  Tổng: " + list.size() + " sản phẩm");
        if (flash != null) {
            System.out.printf(" | Đang giảm %d%% tất cả sản phẩm", flash.getDiscountPercent());
        }
        System.out.println();
    }

    private void printCartTable() {
        String line = "  " + "-".repeat(86);
        System.out.println("\n  --- GIỎ HÀNG ---");
        System.out.println(line);
        System.out.printf("  | %3s | %-28s | %-7s | %-10s | %4s | %14s |%n",
                "STT", "Tên sản phẩm", "Lưu trữ", "Màu sắc", "SL", "Thành tiền");
        System.out.println(line);

        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            Product  p    = item.getProduct();
            System.out.printf("  | %3d | %-28s | %-7s | %-10s | %4d | %,14.0f |%n",
                    i + 1,
                    truncate(p.getProductName(), 28),
                    p.getStorage(),
                    truncate(p.getColor(), 10),
                    item.getQuantity(),
                    item.getSubtotal());
        }

        System.out.println(line);
        System.out.printf("  %-72s TỔNG: %,14.0f VND%n",
                "", service.calculateCartTotal(cart));
    }

    private void printOrderTable(List<Order> orders) {
        String line = "  " + "-".repeat(80);
        System.out.println(line);
        System.out.printf("  | %6s | %-16s | %14s | %-14s | %-10s |%n",
                "Mã ĐH", "Ngày đặt", "Tổng tiền", "Trạng thái", "Mã CK");
        System.out.println(line);
        for (Order o : orders) {
            System.out.printf("  | %6d | %-16s | %,14.0f | %-14s | %-10s |%n",
                    o.getOrderId(),
                    o.getOrderDate() != null ? o.getOrderDate().format(DT_FMT) : "N/A",
                    o.getTotalAmount(),
                    statusLabel(o.getStatus()),
                    o.getCouponCode() != null ? o.getCouponCode() : "-");
        }
        System.out.println(line);
        System.out.println("  Tổng: " + orders.size() + " đơn hàng");
    }

    private void printHeader() {
        System.out.println("\n=================== MENU KHÁCH HÀNG ===================");
        System.out.printf("  Xin chào, %s!%s%n",
                currentUser.getFullName(),
                cart.isEmpty() ? "" : "  |  Giỏ hàng: " + cart.size() + " loại sp");
        System.out.println("=======================================================");
    }

    private String statusLabel(String status) {
        if (status == null) return "N/A";
        return switch (status) {
            case "PENDING"   -> "⏳ Chờ xử lý";
            case "SHIPPING"  -> "🚚 Đang giao";
            case "DELIVERED" -> "✅ Đã giao";
            case "CANCELLED" -> "❌ Đã hủy";
            default          -> status;
        };
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

    private String truncate(String s, int max) {
        if (s == null || s.isEmpty()) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}