package src.Utils;

public class UserSession {
    private static String currentUser;

    public static void setCurrentUser(String user) {
        currentUser = user;
    }

    public static String getCurrentUser() {
        return (currentUser == null) ? "unknown" : currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}