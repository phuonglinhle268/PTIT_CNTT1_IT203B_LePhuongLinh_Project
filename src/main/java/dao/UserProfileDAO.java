package dao;

import model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserProfileDAO {
    Optional<User> findById(int userId) throws SQLException;
    boolean updateProfile(User user) throws SQLException;
    boolean emailExistsExclude(String email, int excludeUserId) throws SQLException;
}
