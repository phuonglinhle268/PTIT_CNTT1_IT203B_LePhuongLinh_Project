package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/project_java_advanced";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456789";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Nếu kết nối bị đóng thì tạo lại
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đóng");
        }
    }

//    public static void testConnection() {
//        DatabaseManager db = getInstance();
//        Connection conn = db.getConnection();
//        if (conn != null) {
//            System.out.println("Test kết nối thành công!");
//        } else {
//            System.out.println("Test kết nối thất bại!");
//        }
//    }

}