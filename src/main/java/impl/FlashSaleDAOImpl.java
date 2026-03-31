package impl;

import dao.FlashSaleDAO;
import model.FlashSale;
import util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlashSaleDAOImpl implements FlashSaleDAO {

    @Override
    public boolean createFlashSale(FlashSale fs) {
        String sql = "insert into FlashSales (discount_percent, start_time, end_time, is_active) " + "values (?, ?, ?, true)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, fs.getDiscountPercent());
            ps.setTimestamp(2, Timestamp.valueOf(fs.getStartTime()));
            ps.setTimestamp(3, Timestamp.valueOf(fs.getEndTime()));
            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) fs.setFlashSaleId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo Flash Sale: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<FlashSale> getAllFlashSales() {
        // Sắp xếp: đang diễn ra trước, rồi sắp tới, rồi đã xong
        String sql = "select * from FlashSales order by start_time asc";
        List<FlashSale> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Lỗi lấy Flash Sales: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<FlashSale> getActiveFlashSales() {
        String sql = "select * from FlashSales where is_active = true order by start_time asc";
        List<FlashSale> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Lỗi lấy Flash Sales đang active: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void deactivateExpired() {
        // Tu dong tat flash sale da het gio (end_time < NOW())
        String sql = "update FlashSales set is_active = false " + "where is_active = true and end_time < now()";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[System] Da tu dong tat " + rows + " Flash Sale het han.");
            }
        } catch (SQLException e) {
            System.err.println("Loi tat Flash Sale het han: " + e.getMessage());
        }
    }

    @Override
    public FlashSale getCurrentActiveFlashSale() {
        // is_active = true VÀ đang trong khoảng thời gian
        String sql = "select * from FlashSales " +
                "where is_active = true and start_time <= now() and end_time >= now() " +
                "order by start_time desc limit 1";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("Lỗi lấy Flash Sale hiện tại: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean deleteFlashSale(int flashSaleId) {
        String sql = "delete from FlashSales where flash_sale_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, flashSaleId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi xóa Flash Sale: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean toggleActive(int flashSaleId, boolean active) {
        String sql = "update FlashSales set is_active = ? where flash_sale_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, flashSaleId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi toggle Flash Sale: " + e.getMessage());
            return false;
        }
    }

    private FlashSale mapRow(ResultSet rs) throws SQLException {
        FlashSale fs = new FlashSale();
        fs.setFlashSaleId(rs.getInt("flash_sale_id"));
        fs.setDiscountPercent(rs.getInt("discount_percent"));
        fs.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        fs.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        fs.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) fs.setCreatedAt(ts.toLocalDateTime());
        return fs;
    }
}