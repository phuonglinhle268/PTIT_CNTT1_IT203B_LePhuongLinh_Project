package service;

import dao.FlashSaleDAO;
import impl.FlashSaleDAOImpl;
import model.FlashSale;
import model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class FlashSaleService {

    private final FlashSaleDAO flashSaleDAO = new FlashSaleDAOImpl();

    // ── Admin ─────────────────────────────────────────────────────────────

    /**
     * Tạo Flash Sale mới.
     * Validate: % hợp lệ, thời gian hợp lệ, không trùng với flash sale đang chạy.
     */
    public boolean createFlashSale(FlashSale fs) {
        if (fs.getDiscountPercent() < 1 || fs.getDiscountPercent() > 99) {
            throw new IllegalArgumentException("Phần trăm giảm phải từ 1% đến 99%.");
        }
        if (!fs.getStartTime().isBefore(fs.getEndTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc.");
        }
        if (fs.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian bắt đầu không được ở quá khứ.");
        }
        return flashSaleDAO.createFlashSale(fs);
    }

    public List<FlashSale> getAllFlashSales() {
        return flashSaleDAO.getAllFlashSales();
    }

    public boolean deleteFlashSale(int id) {
        return flashSaleDAO.deleteFlashSale(id);
    }

    public boolean toggleActive(int id, boolean active) {
        return flashSaleDAO.toggleActive(id, active);
    }

    // ── Customer / dùng chung ─────────────────────────────────────────────

    /** Lấy flash sale đang diễn ra (nếu có). */
    public FlashSale getCurrentActiveFlashSale() {
        return flashSaleDAO.getCurrentActiveFlashSale();
    }

    /**
     * Tính giá flash sale của một sản phẩm.
     * Trả về giá gốc nếu không có flash sale.
     */
    public BigDecimal calculateFlashPrice(Product product, FlashSale flashSale) {
        if (flashSale == null || product == null) {
            return product != null ? product.getPrice() : BigDecimal.ZERO;
        }
        BigDecimal discount = BigDecimal.valueOf(flashSale.getDiscountPercent())
                .divide(BigDecimal.valueOf(100));
        return product.getPrice()
                .multiply(BigDecimal.ONE.subtract(discount))
                .setScale(0, RoundingMode.HALF_UP);
    }
}