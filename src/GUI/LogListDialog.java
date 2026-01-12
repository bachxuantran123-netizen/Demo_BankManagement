package src.GUI;

import src.Database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class LogListDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;

    public LogListDialog(Frame parent) {
        super(parent, "Nhật Ký Hoạt Động Hệ Thống", true);
        setSize(900, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // 1. Tiêu đề
        JLabel lblTitle = new JLabel("NHẬT KÝ HOẠT ĐỘNG (SYSTEM LOGS)", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(180, 0, 0)); // Màu đỏ đậm
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // 2. Bảng dữ liệu
        String[] columns = {"ID", "Người dùng", "Hành động", "Thời gian"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(450);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. Nút đóng & Làm mới
        JPanel panelBot = new JPanel();
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnClose = new JButton("Đóng");

        btnRefresh.addActionListener(e -> loadLogs());
        btnClose.addActionListener(e -> dispose());

        panelBot.add(btnRefresh);
        panelBot.add(btnClose);
        add(panelBot, BorderLayout.SOUTH);

        loadLogs();
    }

    private void loadLogs() {
        String sql = "SELECT * FROM ActivityLogs ORDER BY log_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            tableModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("log_time");
                String timeStr = (ts != null) ? sdf.format(ts) : "";

                tableModel.addRow(new Object[]{
                        rs.getInt("log_id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        timeStr
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải nhật ký: " + e.getMessage());
        }
    }
}