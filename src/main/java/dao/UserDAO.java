package dao;

import model.User;

import java.util.List;

public interface UserDAO {
    boolean register(User user);
    User login(String username, String plainPassword);
    void createDefaultAdmin();
    List<User> getAllUsers();
    User getUserById(int userId);
    boolean updateProfile(User user);
}
