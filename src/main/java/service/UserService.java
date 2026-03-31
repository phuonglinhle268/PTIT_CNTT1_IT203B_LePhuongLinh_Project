package service;

import dao.UserDAO;
import impl.UserDAOImpl;
import model.User;

import java.util.List;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    public String register(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "Username không được để trống.";
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự.";
        }
        if (user.getEmail() == null || !user.getEmail().contains("@gmail.com")) {
            return "Email không hợp lệ (phải chứa ký tự @gmail.com).";
        }
        if (user.getPhone() == null || !user.getPhone().matches("^0\\d{9}$")) {
            return "Số điện thoại phải bắt đầu bằng 0 và đủ 10 số.";
        }

        boolean success = userDAO.register(user);
        return success ? null : "Username hoặc Email đã tồn tại.";
    }

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            return null;
        }
        return userDAO.login(username.trim(), password);
    }

    public void createDefaultAdmin() {
        userDAO.createDefaultAdmin();
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public boolean updateUserProfile(User user) {
        if (user == null || user.getUserId() <= 0) {
            return false;
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            System.err.println("Họ tên không được để trống.");
            return false;
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            System.err.println("Email không hợp lệ.");
            return false;
        }

        return userDAO.updateProfile(user);
    }
}