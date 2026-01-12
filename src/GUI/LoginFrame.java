package src.GUI;

import src.DAO.UserDAO;
import src.Utils.UserSession;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        setTitle("Đăng Nhập Hệ Thống");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Tiêu đề
        JLabel lblTitle = new JLabel("BANK MANAGEMENT LOGIN", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Form nhập
        JPanel panelCenter = new JPanel(new GridLayout(2, 2, 10, 10));
        panelCenter.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        txtUser = new JTextField();
        txtPass = new JPasswordField();

        panelCenter.add(new JLabel("Username:"));
        panelCenter.add(txtUser);
        panelCenter.add(new JLabel("Password:"));
        panelCenter.add(txtPass);
        add(panelCenter, BorderLayout.CENTER);

        // Nút bấm
        JButton btnLogin = new JButton("Đăng Nhập");
        JPanel panelBot = new JPanel();
        panelBot.add(btnLogin);
        add(panelBot, BorderLayout.SOUTH);

        // SỰ KIỆN NÚT LOGIN
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword()); // Lấy mật khẩu từ JPasswordField

            if (userDAO.checkLogin(user, pass)) {
                UserSession.setCurrentUser(user);
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");

                // --- CHUYỂN CẢNH ---
                new MainFrame().setVisible(true); // 1. Mở màn hình chính
                this.dispose(); // 2. Đóng màn hình đăng nhập lại

            } else {
                JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}