package src.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;"
            + "databaseName=BankDB;"
            + "integratedSecurity=true;"
            + "trustServerCertificate=true;";

    public static Connection getConnection() {
        try {
            // Kiểm tra Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            Connection conn = DriverManager.getConnection(URL);

            // In ra console để biết đã kết nối thành công (Chỉ dùng lúc debug)
            System.out.println("Kết nối SQL Server (Windows Auth) thành công!");
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Driver!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối!");
            e.printStackTrace();
            return null;
        }
    }
}