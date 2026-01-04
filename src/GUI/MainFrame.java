package src.GUI;

import src.DAO.AccountDAO;
import src.Entities.*;
import src.Exceptions.NotFoundBankAccountException;
import src.Utils.Exporter;
import src.Utils.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private AccountDAO accountDAO;

    public MainFrame() {
        accountDAO = new AccountDAO();
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Hệ Thống Quản Lý Ngân Hàng - Final Project");
        setSize(1150, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. TIÊU ĐỀ ---
        JLabel lblTitle = new JLabel("QUẢN LÝ TÀI KHOẢN NGÂN HÀNG", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(0, 102, 204));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        String[] columns = {"ID", "Mã TK", "Chủ TK", "CCCD", "Loại TK", "Ngày Tạo", "Chi Tiết 1", "Chi Tiết 2", "Kỳ hạn"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        table.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Mã TK
        table.getColumnModel().getColumn(2).setPreferredWidth(140); // Tên
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // CCCD (Mới)
        table.getColumnModel().getColumn(4).setPreferredWidth(90);  // Loại
        table.getColumnModel().getColumn(7).setPreferredWidth(60);  // Kỳ hạn
        table.getColumnModel().getColumn(8).setPreferredWidth(60);  // Kỳ hạn

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 3. THANH CÔNG CỤ ---
        JPanel panelBot = new JPanel(new BorderLayout());
        panelBot.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 3a. Tìm kiếm
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Tìm kiếm");
        panelSearch.add(new JLabel("Từ khóa: "));
        panelSearch.add(txtSearch);
        panelSearch.add(btnSearch);

        // 3b. Chức năng
        JPanel panelAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Thêm Mới");
        JButton btnEdit = new JButton("Cập Nhật");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnExport = new JButton("Xuất Excel");
        JButton btnLogs = new JButton("Xem Nhật ký");
        JButton btnLogout = new JButton("Đăng xuất");

        panelAction.add(btnAdd);
        panelAction.add(btnEdit);
        panelAction.add(btnDelete);
        panelAction.add(btnRefresh);
        panelAction.add(Box.createHorizontalStrut(15));
        panelAction.add(btnExport);
        panelAction.add(btnLogs);
        panelAction.add(Box.createHorizontalStrut(5));
        panelAction.add(btnLogout);

        panelBot.add(panelSearch, BorderLayout.WEST);
        panelBot.add(panelAction, BorderLayout.EAST);
        add(panelBot, BorderLayout.SOUTH);

        // --- 4. XỬ LÝ SỰ KIỆN ---

        btnLogs.addActionListener(e -> {
            new LogListDialog(this).setVisible(true);
        });

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa!");
                return;
            }
            List<BankAccount> results = accountDAO.searchAccounts(keyword);
            updateTable(results);
        });

        btnAdd.addActionListener(e -> {
            AddAccountDialog dialog = new AddAccountDialog(this, accountDAO);
            dialog.setVisible(true);
            if (dialog.isSuccess()) loadData();
        });

        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa!");
                return;
            }
            String code = (String) tableModel.getValueAt(selectedRow, 1);
            List<BankAccount> list = accountDAO.searchAccounts(code);
            if (!list.isEmpty()) {
                BankAccount target = list.get(0);
                AddAccountDialog dialog = new AddAccountDialog(this, accountDAO, target);
                dialog.setVisible(true);
                if (dialog.isSuccess()) loadData();
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!");
                return;
            }
            String code = (String) tableModel.getValueAt(selectedRow, 1);
            String name = (String) tableModel.getValueAt(selectedRow, 2);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn chắc chắn muốn xóa: " + name + "?", "Cảnh báo", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    accountDAO.deleteAccount(code);
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadData();
                } catch (NotFoundBankAccountException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        btnExport.addActionListener(e -> exportToCSV());

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất?", "Đăng xuất", JOptionPane.YES_NO_OPTION);
            UserSession.clear();
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                this.dispose();
            }
        });
    }

    private void loadData() {
        List<BankAccount> list = accountDAO.getAllAccounts();
        updateTable(list);
    }

    private void updateTable(List<BankAccount> list) {
        tableModel.setRowCount(0);
        for (BankAccount acc : list) {
            addRowToTable(acc);
        }
    }

    private void addRowToTable(BankAccount acc) {
        Object[] row;
        if (acc instanceof SavingsAccount) {
            SavingsAccount sa = (SavingsAccount) acc;
            row = new Object[]{
                    acc.getId(),
                    acc.getAccountCode(),
                    acc.getOwnerName(),
                    acc.getCitizenId(),
                    "TIẾT KIỆM",
                    acc.getCreationDate(),
                    "Gửi: " + String.format("%,.0f", sa.getDepositAmount()),
                    "Lãi: " + sa.getInterestRate() + "%",
                    sa.getTerm() + " tháng"
            };
        } else {
            PaymentAccount pa = (PaymentAccount) acc;
            row = new Object[]{
                    acc.getId(),
                    acc.getAccountCode(),
                    acc.getOwnerName(),
                    acc.getCitizenId(),
                    "THANH TOÁN",
                    acc.getCreationDate(),
                    "Thẻ: " + pa.getCardNumber(),
                    "Số dư: " + String.format("%,.0f", pa.getBalance()),
                    "" // Thanh toán không có kỳ hạn nên để trống
            };
        }
        tableModel.addRow(row);
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file báo cáo");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }
            try {
                Exporter.exportTable(table, fileToSave);
                JOptionPane.showMessageDialog(this, "Xuất file thành công!");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage());
            }
        }
    }
}