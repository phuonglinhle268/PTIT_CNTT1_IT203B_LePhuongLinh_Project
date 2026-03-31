package dao;

import model.Coupon;
import java.util.List;

public interface CouponDAO {
    boolean createCoupon(Coupon coupon);
    List<Coupon> getAllCoupons();
    Coupon getCouponByCode(String couponCode);
    boolean deleteCoupon(int couponId);
    boolean useCoupon(String couponCode);          // tăng used_count
    boolean isCouponValid(String couponCode);      // còn dùng được không
    boolean toggleActive(int couponId, boolean active);
    void deactivateExpired(); // Tat coupon het han hoac het luot
}