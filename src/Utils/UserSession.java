package src.Utils;

public class UserSession {
    private static String currentUser; // Biến tĩnh để lưu tên người dùng

    // Lưu người dùng khi đăng nhập
    public static void setCurrentUser(String user) {
        currentUser = user;
    }

    // Lấy người dùng hiện tại để ghi log
    public static String getCurrentUser() {
        // Nếu chưa đăng nhập mà gọi hàm này thì trả về mặc định để không lỗi
        return (currentUser == null) ? "unknown" : currentUser;
    }

    // Xóa session khi đăng xuất
    public static void clear() {
        currentUser = null;
    }
}