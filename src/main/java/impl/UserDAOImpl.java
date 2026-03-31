package impl;

import dao.UserDAO;
import model.User;
import util.BCryptUtil;
import util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public boolean register(User user) {
        String sql = "insert into Users (username, password, full_name, email, phone, address, role) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCryptUtil.hashPassword(user.getPassword());

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getAddress() != null ? user.getAddress() : "");
            pstmt.setString(7, user.getRole());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.err.println("Username hoặc Email đã tồn tại!");
            } else {
                System.err.println("Lỗi đăng ký: " + e.getMessage());
            }
            return false;
        }
    }

    @Override
    public User login(String username, String plainPassword) {
        String sql = "select * from Users where username = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");

                    if (!BCryptUtil.checkPassword(plainPassword, hashedPassword)) {
                        return null;
                    }

                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setRole(rs.getString("role"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) user.setCreatedAt(ts.toLocalDateTime());

                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đăng nhập: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createDefaultAdmin() {
        String checkSql = "select count(*) from Users where username = 'admin'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "insert into Users (username, password, full_name, email, phone, address, role) " +"values (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, BCryptUtil.hashPassword("123456"));
                    pstmt.setString(3, "Administrator");
                    pstmt.setString(4, "admin@phonestore.com");
                    pstmt.setString(5, "0123456789");
                    pstmt.setString(6, "Hà Nội");
                    pstmt.setString(7, "ADMIN");
                    pstmt.executeUpdate();
                    System.out.println("Đã tạo tài khoản Admin mặc định");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo admin mặc định: " + e.getMessage());
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "select * from Users where role = 'CUSTOMER' order by user_id asc";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách khách hàng: " + e.getMessage());
        }
        return users;
    }

    @Override
    public User getUserById(int userId) {
        String sql = "select * from Users where user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy thông tin user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateProfile(User user) {
        String sql = "update Users set full_name = ?, email = ?, phone = ?, address = ? where user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getAddress() != null ? user.getAddress() : "");
            pstmt.setInt(5, user.getUserId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật profile: " + e.getMessage());
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setRole(rs.getString("role"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());

        return u;
    }
}