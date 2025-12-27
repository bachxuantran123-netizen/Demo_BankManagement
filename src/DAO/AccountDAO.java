package src.DAO;

import src.Database.DatabaseConnection;
import src.Entities.*;
import src.Exceptions.NotFoundBankAccountException;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private BankAccount mapRowToAccount(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String code = rs.getString("account_code");
        String name = rs.getString("owner_name");
        String date = rs.getString("creation_date");
        String type = rs.getString("type");

        if ("SAVINGS".equalsIgnoreCase(type)) {
            return new SavingsAccount(id, code, name, date,
                    rs.getDouble("deposit_amount"),
                    rs.getString("deposit_date"),
                    rs.getDouble("interest_rate"),
                    rs.getInt("term"));
        } else {
            return new PaymentAccount(id, code, name, date,
                    rs.getString("card_number"),
                    rs.getDouble("balance"));
        }
    }
    private void setDateSafe(PreparedStatement pstmt, int index, String dateStr) throws SQLException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            pstmt.setNull(index, Types.DATE);
            return;
        }
        try {
            // Ưu tiên 1: Thử ép kiểu chuẩn Database (yyyy-MM-dd)
            pstmt.setDate(index, java.sql.Date.valueOf(dateStr));
        } catch (IllegalArgumentException e) {
            // Ưu tiên 2: Nếu thất bại, thử ép kiểu Giao diện (dd/MM/yyyy)
            try {
                java.util.Date utilDate = new SimpleDateFormat("dd/MM/yyyy").parse(dateStr);
                pstmt.setDate(index, new java.sql.Date(utilDate.getTime()));
            } catch (Exception ex) {
                // Đường cùng: Gửi chuỗi thô (Hên xui, nhưng thường 2 bước trên đã bắt được hết rồi)
                pstmt.setString(index, dateStr);
            }
        }
    }
    // 1. LẤY TOÀN BỘ DANH SÁCH (READ)
    public List<BankAccount> getAllAccounts() {
        List<BankAccount> list = new ArrayList<>();
        String sql = "SELECT * FROM Accounts";

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

    // 2. THÊM TÀI KHOẢN (CREATE)
    public boolean addAccount(BankAccount acc) {
        String sql = "INSERT INTO Accounts (account_code, owner_name, creation_date, type, " +
                "deposit_amount, deposit_date, interest_rate, term, card_number, balance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, acc.getAccountCode());
            pstmt.setString(2, acc.getAccountName());

            // Tự động lấy ngày hiện tại nếu không có
            String creationDate = acc.getCreationDate();
            if (creationDate == null || creationDate.isEmpty()) {
                creationDate = LocalDate.now().toString(); // Lấy ngày hôm nay (yyyy-MM-dd)
            }
            setDateSafe(pstmt, 3, creationDate);

            if (acc instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) acc;
                pstmt.setString(4, "SAVINGS");
                pstmt.setDouble(5, sa.getDepositAmount());

                setDateSafe(pstmt, 6, sa.getDepositDate());

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

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. CẬP NHẬT TÀI KHOẢN (UPDATE)
    public boolean updateAccount(BankAccount acc) {
        String sql = "UPDATE Accounts SET owner_name=?, creation_date=?, " +
                "deposit_amount=?, deposit_date=?, interest_rate=?, term=?, " +
                "card_number=?, balance=? " +
                "WHERE account_code=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, acc.getAccountName());
            setDateSafe(pstmt, 2, acc.getCreationDate());

            if (acc instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) acc;
                pstmt.setDouble(3, sa.getDepositAmount());

                setDateSafe(pstmt, 4, sa.getDepositDate());

                pstmt.setDouble(5, sa.getInterestRate());
                pstmt.setInt(6, sa.getTerm());

                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.DOUBLE);
            } else {
                PaymentAccount pa = (PaymentAccount) acc;
                pstmt.setNull(3, Types.DOUBLE);
                pstmt.setNull(4, Types.DATE);
                pstmt.setNull(5, Types.DOUBLE);
                pstmt.setNull(6, Types.INTEGER);

                pstmt.setString(7, pa.getCardNumber());
                pstmt.setDouble(8, pa.getBalance());
            }

            pstmt.setString(9, acc.getAccountCode());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. XÓA TÀI KHOẢN
    public void deleteAccount(String accountCode) throws NotFoundBankAccountException {
        String sql = "DELETE FROM Accounts WHERE account_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountCode);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new NotFoundBankAccountException("Không tìm thấy tài khoản có mã: " + accountCode + " để xóa!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 5. KIỂM TRA TỒN TẠI
    public boolean isCodeExist(String code) {
        String sql = "SELECT COUNT(*) FROM Accounts WHERE account_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 6. TÌM KIẾM
    public List<BankAccount> searchAccounts(String keyword) {
        List<BankAccount> list = new ArrayList<>();
        String sql = "SELECT * FROM Accounts WHERE account_code LIKE ? OR owner_name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String query = "%" + keyword + "%";
            pstmt.setString(1, query);
            pstmt.setString(2, query);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}