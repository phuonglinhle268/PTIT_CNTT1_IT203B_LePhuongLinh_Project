package impl;

import dao.CouponDAO;
import model.Coupon;
import util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CouponDAOImpl implements CouponDAO {

    @Override
    public boolean createCoupon(Coupon coupon) {
        String sql = "insert into Coupons " +
                "(coupon_code, discount_percent, max_uses, start_date, end_date, is_active) " +
                "values (?, ?, ?, ?, ?, 1)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, coupon.getCouponCode().toUpperCase());
            ps.setInt(2, coupon.getDiscountPercent());
            ps.setInt(3, coupon.getMaxUses());
            ps.setDate(4, Date.valueOf(coupon.getStartDate()));
            ps.setDate(5, Date.valueOf(coupon.getEndDate()));
            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) coupon.setCouponId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.err.println("Mã giảm giá \"" + coupon.getCouponCode() + "\" đã tồn tại!");
            } else {
                System.err.println("Lỗi tạo Coupon: " + e.getMessage());
            }
        }
        return false;
    }

    @Override
    public List<Coupon> getAllCoupons() {
        String sql = "select * from Coupons order by start_date asc, coupon_id asc";
        List<Coupon> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Lỗi lấy Coupons: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Coupon getCouponByCode(String couponCode) {
        String sql = "select * from Coupons where coupon_code = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, couponCode.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi tìm Coupon: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean deleteCoupon(int couponId) {
        String sql = "delete from Coupons where coupon_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, couponId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi xóa Coupon: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean useCoupon(String couponCode) {
        String sql = "update Coupons set used_count = used_count + 1 " +
                "where coupon_code = ? and used_count < max_uses";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, couponCode.toUpperCase());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi sử dụng Coupon: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isCouponValid(String couponCode) {
        Coupon c = getCouponByCode(couponCode);
        return c != null && c.isUsable();
    }

    @Override
    public void deactivateExpired() {
        // Tat coupon het han HOAC het luot su dung
        String sql = "update Coupons set is_active = 0 " +
                "where is_active = 1 " +
                "and (end_date < date(now()) or used_count >= max_uses)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Đã tự động tắt " + rows + " Coupon hết hạn/hết lượt");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tắt Coupon hết hạn: " + e.getMessage());
        }
    }

    @Override
    public boolean toggleActive(int couponId, boolean active) {
        String sql = "update Coupons set is_active = ? where coupon_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, couponId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi toggle Coupon: " + e.getMessage());
            return false;
        }
    }

    private Coupon mapRow(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setCouponId(rs.getInt("coupon_id"));
        c.setCouponCode(rs.getString("coupon_code"));
        c.setDiscountPercent(rs.getInt("discount_percent"));
        c.setMaxUses(rs.getInt("max_uses"));
        c.setUsedCount(rs.getInt("used_count"));
        c.setStartDate(rs.getDate("start_date").toLocalDate());
        c.setEndDate(rs.getDate("end_date").toLocalDate());
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}