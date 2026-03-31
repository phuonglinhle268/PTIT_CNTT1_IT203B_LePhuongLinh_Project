package service;

import dao.CouponDAO;
import impl.CouponDAOImpl;
import model.Coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class CouponService {
    private final CouponDAO couponDAO = new CouponDAOImpl();

    // admin
    public boolean createCoupon(Coupon coupon) {
        if (coupon.getCouponCode() == null || coupon.getCouponCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không được để trống.");
        }
        if (coupon.getDiscountPercent() < 1 || coupon.getDiscountPercent() > 100) {
            throw new IllegalArgumentException("Phần trăm giảm phải từ 1% đến 100%.");
        }
        if (coupon.getMaxUses() < 1) {
            throw new IllegalArgumentException("Số lần sử dụng tối đa phải ít nhất là 1.");
        }
        if (coupon.getStartDate() == null || coupon.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống.");
        }
        if (!coupon.getStartDate().isBefore(coupon.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc.");
        }
        return couponDAO.createCoupon(coupon);
    }

    public List<Coupon> getAllCoupons() {
        couponDAO.deactivateExpired(); // tat het han/het luot truoc khi hien thi
        return couponDAO.getAllCoupons();
    }

    public boolean deleteCoupon(int id) {
        return couponDAO.deleteCoupon(id);
    }

    public boolean toggleActive(int id, boolean active) {
        return couponDAO.toggleActive(id, active);
    }

    // customer - chung

    //Kiểm tra mã còn hợp lệ không.
    public Coupon validateCoupon(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không được để trống.");
        }

        Coupon coupon = couponDAO.getCouponByCode(couponCode.trim().toUpperCase());

        if (coupon == null) {
            throw new IllegalArgumentException("Mã giảm giá \"" + couponCode + "\" không tồn tại.");
        }
        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Mã giảm giá này đã bị vô hiệu hóa.");
        }
        if (LocalDate.now().isAfter(coupon.getEndDate())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn (hết hạn: " + coupon.getEndDate() + ").");
        }
        if (LocalDate.now().isBefore(coupon.getStartDate())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến ngày sử dụng (từ: " + coupon.getStartDate() + ").");
        }
        if (coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
        }

        return coupon;
    }

    //Tính tổng tiền sau khi áp dụng coupon.
    public BigDecimal applyDiscount(BigDecimal originalAmount, String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return originalAmount;
        }

        Coupon coupon = validateCoupon(couponCode); // ném exception nếu không hợp lệ
        BigDecimal discount = BigDecimal.valueOf(coupon.getDiscountPercent())
                .divide(BigDecimal.valueOf(100));

        return originalAmount.multiply(BigDecimal.ONE.subtract(discount))
                .setScale(0, RoundingMode.HALF_UP);
    }

    /** Tăng used_count sau khi đặt hàng thành công. */
    public void useCoupon(String couponCode) {
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            couponDAO.useCoupon(couponCode.trim().toUpperCase());
        }
    }
}