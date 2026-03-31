package impl;

import dao.UserProfileDAO;
import model.User;
import util.DatabaseManager;

import java.sql.*;
import java.util.Optional;

public class UserProfileDAOImpl implements UserProfileDAO {
    @Override
    public Optional<User> findById(int userId) throws SQLException {
        String sql = "select * from Users where user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean updateProfile(User user) throws SQLException {
        String sql = "update Users set full_name = ?, email = ?, phone = ?, address = ? where user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getAddress() != null ? user.getAddress() : "");
            ps.setInt(5, user.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean emailExistsExclude(String email, int excludeUserId) throws SQLException {
        String sql = "select count(*) from Users where email = ? and user_id != ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setRole(rs.getString("role"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            u.setCreatedAt(ts.toLocalDateTime());
        }
        return u;
    }
}