import presentation.MainMenu;
import service.UserService;
import util.DatabaseManager;

public class Main {
    public static void main(String[] args) {

        System.out.println("==================================================");
        System.out.println("   HỆ THỐNG QUẢN LÝ SHOP BÁN ĐIỆN THOẠI ONLINE     ");
        System.out.println("             SmartPhone Store Management          ");
        System.out.println("==================================================\n");

        UserService userService = new UserService();
        userService.createDefaultAdmin();

        MainMenu mainMenu = new MainMenu();
        mainMenu.showMainMenu();

        // Đóng kết nối khi thoát chương trình
        DatabaseManager.getInstance().closeConnection();
    }
}
