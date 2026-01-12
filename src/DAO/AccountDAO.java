package src.DAO;

import src.Database.DatabaseConnection;
import src.Entities.*;
import src.Exceptions.NotFoundBankAccountException;
import src.Utils.UserSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private BankAccount mapRowToAccount(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String code = rs.getString("account_code");
        String date = rs.getString("creation_date");
        String type = rs.getString("type");
        int custId = rs.getInt("customer_id");
        String name = rs.getString("full_name");
        String citizenId = rs.getString("citizen_id");

        if ("SAVINGS".equalsIgnoreCase(type)) {
            return new SavingsAccount(id, code, custId, name, citizenId, date,
                    rs.getDouble("deposit_amount"),
                    rs.getString("deposit_date"),
                    rs.getDouble("interest_rate"),
                    rs.getInt("term"));
        } else {
            return new PaymentAccount(id, code, custId, name, citizenId, date,
                    rs.getString("card_number"),
                    rs.getDouble("balance"));
        }
    }

    // 1. LẤY DANH SÁCH
    public List<BankAccount> getAllAccounts() {
        List<BankAccount> list = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name, c.citizen_id " +
                "FROM Accounts a " +
                "JOIN Customers c ON a.customer_id = c.customer_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. TÌM KIẾM
    public List<BankAccount> searchAccounts(String keyword) {
        List<BankAccount> list = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name, c.citizen_id FROM Accounts a " +
                "JOIN Customers c ON a.customer_id = c.customer_id " +
                "WHERE a.account_code LIKE ? OR c.full_name LIKE ? OR c.citizen_id LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String query = "%" + keyword + "%";
            pstmt.setString(1, query);
            pstmt.setString(2, query);
            pstmt.setString(3, query);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. THÊM TÀI KHOẢN
    public boolean addAccount(BankAccount acc) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int customerId = -1;

            String checkCust = "SELECT customer_id FROM Customers WHERE citizen_id = ?";
            try (PreparedStatement pCheck = conn.prepareStatement(checkCust)) {
                pCheck.setString(1, acc.getCitizenId());
                ResultSet rsCheck = pCheck.executeQuery();
                if (rsCheck.next()) {
                    customerId = rsCheck.getInt("customer_id");
                }
            }

            if (customerId == -1) {
                String insertCust = "INSERT INTO Customers (citizen_id, full_name) VALUES (?, ?)";
                try (PreparedStatement pInsCust = conn.prepareStatement(insertCust, Statement.RETURN_GENERATED_KEYS)) {
                    pInsCust.setString(1, acc.getCitizenId());
                    pInsCust.setString(2, acc.getOwnerName());
                    pInsCust.executeUpdate();

                    ResultSet rsKey = pInsCust.getGeneratedKeys();
                    if (rsKey.next()) customerId = rsKey.getInt(1);
                }
            }

            String sqlAcc = "INSERT INTO Accounts (account_code, customer_id, creation_date, type, " +
                    "deposit_amount, deposit_date, interest_rate, term, card_number, balance) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlAcc)) {
                pstmt.setString(1, acc.getAccountCode());
                pstmt.setInt(2, customerId);
                String cDate = (acc.getCreationDate() == null) ? java.time.LocalDate.now().toString() : acc.getCreationDate();
                pstmt.setDate(3, java.sql.Date.valueOf(cDate));

                if (acc instanceof SavingsAccount) {
                    SavingsAccount sa = (SavingsAccount) acc;
                    pstmt.setString(4, "SAVINGS");
                    pstmt.setDouble(5, sa.getDepositAmount());
                    pstmt.setDate(6, java.sql.Date.valueOf(sa.getDepositDate()));
                    pstmt.setDouble(7, sa.getInterestRate());
                    pstmt.setInt(8, sa.getTerm());
                    pstmt.setNull(9, Types.VARCHAR);
                    pstmt.setNull(10, Types.DOUBLE);
                } else {
                    PaymentAccount pa = (PaymentAccount) acc;
                    pstmt.setString(4, "PAYMENT");
                    pstmt.setNull(5, Types.DOUBLE);
                    pstmt.setNull(6, Types.DATE);
                    pstmt.setNull(7, Types.DOUBLE);
                    pstmt.setNull(8, Types.INTEGER);
                    pstmt.setString(9, pa.getCardNumber());
                    pstmt.setDouble(10, pa.getBalance());
                }
                pstmt.executeUpdate();
            }

            String sqlLog = "INSERT INTO ActivityLogs (username, action) VALUES (?, ?)";
            try (PreparedStatement pLog = conn.prepareStatement(sqlLog)) {
                pLog.setString(1, UserSession.getCurrentUser());
                pLog.setString(2, "Thêm tài khoản mới: " + acc.getAccountCode());
                pLog.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                conn.close();
            } catch (Exception e) {}
        }
    }

    public boolean updateAccount(BankAccount acc) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int targetCustomerId = -1;

            String checkSql = "SELECT customer_id FROM Customers WHERE citizen_id = ?";
            try (PreparedStatement pCheck = conn.prepareStatement(checkSql)) {
                pCheck.setString(1, acc.getCitizenId());
                ResultSet rsCheck = pCheck.executeQuery();
                if (rsCheck.next()) {
                    targetCustomerId = rsCheck.getInt("customer_id");
                }
            }

            if (targetCustomerId != -1) {
                String updateNameSql = "UPDATE Customers SET full_name = ? WHERE customer_id = ?";
                try (PreparedStatement pUpdName = conn.prepareStatement(updateNameSql)) {
                    pUpdName.setString(1, acc.getOwnerName());
                    pUpdName.setInt(2, targetCustomerId);
                    pUpdName.executeUpdate();
                }
            } else {
                String insertCust = "INSERT INTO Customers (citizen_id, full_name) VALUES (?, ?)";
                try (PreparedStatement pIns = conn.prepareStatement(insertCust, Statement.RETURN_GENERATED_KEYS)) {
                    pIns.setString(1, acc.getCitizenId());
                    pIns.setString(2, acc.getOwnerName());
                    pIns.executeUpdate();
                    ResultSet rsKey = pIns.getGeneratedKeys();
                    if (rsKey.next()) targetCustomerId = rsKey.getInt(1);
                }
            }

            String sqlAcc = "UPDATE Accounts SET customer_id=?, deposit_amount=?, deposit_date=?, interest_rate=?, term=?, card_number=?, balance=? WHERE account_code=?";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlAcc)) {
                pstmt.setInt(1, targetCustomerId);

                if (acc instanceof SavingsAccount) {
                    SavingsAccount sa = (SavingsAccount) acc;
                    pstmt.setDouble(2, sa.getDepositAmount());
                    pstmt.setDate(3, java.sql.Date.valueOf(sa.getDepositDate()));
                    pstmt.setDouble(4, sa.getInterestRate());
                    pstmt.setInt(5, sa.getTerm());
                    pstmt.setNull(6, Types.VARCHAR);
                    pstmt.setNull(7, Types.DOUBLE);
                } else {
                    PaymentAccount pa = (PaymentAccount) acc;
                    pstmt.setNull(2, Types.DOUBLE);
                    pstmt.setNull(3, Types.DATE);
                    pstmt.setNull(4, Types.DOUBLE);
                    pstmt.setNull(5, Types.INTEGER);
                    pstmt.setString(6, pa.getCardNumber());
                    pstmt.setDouble(7, pa.getBalance());
                }
                pstmt.setString(8, acc.getAccountCode());
                pstmt.executeUpdate();
            }
            try (PreparedStatement pLog = conn.prepareStatement("INSERT INTO ActivityLogs (username, action) VALUES (?, ?)")) {
                pLog.setString(1, UserSession.getCurrentUser());
                pLog.setString(2, "Cập nhật tài khoản: " + acc.getAccountCode());
                pLog.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                conn.close();
            } catch (Exception e) {}
        }
    }

    public void deleteAccount(String code) throws NotFoundBankAccountException {
        String sql = "DELETE FROM Accounts WHERE account_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            if (pstmt.executeUpdate() == 0) throw new NotFoundBankAccountException("Không tìm thấy tài khoản!");

            try (PreparedStatement pLog = conn.prepareStatement("INSERT INTO ActivityLogs (username, action) VALUES (?, ?)")) {
                pLog.setString(1, UserSession.getCurrentUser());
                pLog.setString(2, "Đã xóa tài khoản: " + code);
                pLog.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isCodeExist(String code) {
        String sql = "SELECT COUNT(*) FROM Accounts WHERE account_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}