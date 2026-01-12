package src.GUI;

import src.DAO.AccountDAO;
import src.Entities.*;
import src.Validate.Validator;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddAccountDialog extends JDialog {
    private JTextField txtCode, txtName;
    private JTextField txtCitizenId;
    private JComboBox<String> cbType;
    private JFormattedTextField txtDepAmount;
    private JFormattedTextField txtBalance;
    private JFormattedTextField txtDepDate;
    private JTextField txtRate, txtTerm, txtCardNum;
    private JPanel panelCenter;
    private AccountDAO accountDAO;
    private boolean isSuccess = false;
    private boolean isEditMode = false;
    private BankAccount currentAccount = null;

    public AddAccountDialog(Frame parent, AccountDAO dao) {
        super(parent, "Thêm Tài Khoản Mới", true);
        this.accountDAO = dao;
        this.isEditMode = false;
        initUI();
    }

    public AddAccountDialog(Frame parent, AccountDAO dao, BankAccount existingAcc) {
        super(parent, "Cập Nhật Tài Khoản", true);
        this.accountDAO = dao;
        this.currentAccount = existingAcc;
        this.isEditMode = true;
        initUI();
        fillData(existingAcc);
    }

    private void initUI() {
        setSize(450, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel panelCommon = new JPanel(new GridLayout(4, 2, 10, 10));
        panelCommon.setBorder(BorderFactory.createTitledBorder("Thông tin chung"));

        txtCode = new JTextField();
        txtName = new JTextField();
        txtCitizenId = new JTextField();
        cbType = new JComboBox<>(new String[]{"Savings Account", "Payment Account"});

        panelCommon.add(new JLabel("Mã Tài Khoản:"));
        panelCommon.add(txtCode);
        panelCommon.add(new JLabel("Số CCCD:"));
        panelCommon.add(txtCitizenId);
        panelCommon.add(new JLabel("Tên Chủ TK:"));
        panelCommon.add(txtName);
        panelCommon.add(new JLabel("Loại TK:"));
        panelCommon.add(cbType);

        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(true);

        NumberFormatter currencyFormatter = new NumberFormatter(format);
        currencyFormatter.setValueClass(Long.class);
        currencyFormatter.setAllowsInvalid(false);
        currencyFormatter.setMinimum(0L);

        panelCenter = new JPanel(new CardLayout());
        JPanel panelSavings = new JPanel(new GridLayout(4, 2, 10, 10));
        panelSavings.setBorder(BorderFactory.createTitledBorder("Chi tiết Tiết Kiệm"));

        // Áp dụng formatter cho ô Tiền gửi
        txtDepAmount = new JFormattedTextField(currencyFormatter);
        txtDepAmount.setValue(0L);

        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            txtDepDate = new JFormattedTextField(dateMask);
        } catch (ParseException e) {
            txtDepDate = new JFormattedTextField();
        }

        txtRate = new JTextField();
        txtTerm = new JTextField();

        panelSavings.add(new JLabel("Số tiền gửi (VNĐ):"));
        panelSavings.add(txtDepAmount);
        panelSavings.add(new JLabel("Ngày gửi (dd/mm/yyyy):"));
        panelSavings.add(txtDepDate);
        panelSavings.add(new JLabel("Lãi suất (%):"));
        panelSavings.add(txtRate);
        panelSavings.add(new JLabel("Kỳ hạn (tháng):"));
        panelSavings.add(txtTerm);

        JPanel panelPayment = new JPanel(new GridLayout(2, 2, 10, 10));
        panelPayment.setBorder(BorderFactory.createTitledBorder("Chi tiết Thanh Toán"));
        txtCardNum = new JTextField();
        txtBalance = new JFormattedTextField(currencyFormatter);
        txtBalance.setValue(0L);

        panelPayment.add(new JLabel("Số thẻ:"));
        panelPayment.add(txtCardNum);
        panelPayment.add(new JLabel("Số dư (VNĐ):"));
        panelPayment.add(txtBalance);

        panelCenter.add(panelSavings, "Savings Account");
        panelCenter.add(panelPayment, "Payment Account");

        cbType.addActionListener(e -> {
            CardLayout cl = (CardLayout) panelCenter.getLayout();
            cl.show(panelCenter, (String) cbType.getSelectedItem());
        });

        JPanel panelBtn = new JPanel();
        JButton btnSave = new JButton(isEditMode ? "Cập Nhật" : "Lưu Mới");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(this::onSave);
        btnCancel.addActionListener(e -> dispose());

        panelBtn.add(btnSave);
        panelBtn.add(btnCancel);

        add(panelCommon, BorderLayout.NORTH);
        add(panelCenter, BorderLayout.CENTER);
        add(panelBtn, BorderLayout.SOUTH);
    }

    private void fillData(BankAccount acc) {
        txtCode.setText(acc.getAccountCode());
        txtCode.setEditable(false);
        txtName.setText(acc.getOwnerName());
        txtCitizenId.setText(acc.getCitizenId());

        if (acc instanceof SavingsAccount) {
            SavingsAccount sa = (SavingsAccount) acc;
            cbType.setSelectedItem("Savings Account");
            cbType.setEnabled(false);

            txtDepAmount.setValue((long) sa.getDepositAmount());
            txtDepDate.setText(convertDateToGUI(sa.getDepositDate()));
            txtRate.setText(String.valueOf(sa.getInterestRate()));
            txtTerm.setText(String.valueOf(sa.getTerm()));
        } else {
            PaymentAccount pa = (PaymentAccount) acc;
            cbType.setSelectedItem("Payment Account");
            cbType.setEnabled(false);

            txtCardNum.setText(pa.getCardNumber());
            txtBalance.setValue((long) pa.getBalance());
        }
    }

    private void onSave(ActionEvent e) {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        String cid = txtCitizenId.getText().trim();
        String type = (String) cbType.getSelectedItem();

        if (!Validator.isValidAccountCode(code)) {
            JOptionPane.showMessageDialog(this, "Mã tài khoản phải đủ 9 chữ số!");
            return;
        }
        if (Validator.isEmpty(name)) {
            JOptionPane.showMessageDialog(this, "Tên không được để trống!");
            return;
        }
        if (Validator.isEmpty(cid)) { // Validate CCCD
            JOptionPane.showMessageDialog(this, "Số CCCD không được để trống!");
            return;
        }
        if (!isEditMode && accountDAO.isCodeExist(code)) {
            JOptionPane.showMessageDialog(this, "Mã tài khoản đã tồn tại!");
            return;
        }

        try {
            int idToSave = 0;
            int custIdToSave = 0;

            if (isEditMode && currentAccount != null) {
                idToSave = currentAccount.getId();
                custIdToSave = currentAccount.getCustomerId();
            }
            BankAccount acc = null;

            if ("Savings Account".equals(type)) {
                double amount = 0;
                if (txtDepAmount.getValue() != null) {
                    amount = ((Number) txtDepAmount.getValue()).doubleValue();
                }

                String dateStr = txtDepDate.getText().trim();
                String rateStr = txtRate.getText().trim();
                String termStr = txtTerm.getText().trim();

                if (!Validator.isValidDate(dateStr)) {
                    JOptionPane.showMessageDialog(this, "Ngày tháng không hợp lệ (dd/MM/yyyy)!");
                    return;
                }
                if (Validator.isFutureDate(dateStr)) {
                    JOptionPane.showMessageDialog(this, "Ngày gửi không được lớn hơn ngày hiện tại!");
                    return;
                }
                if (!Validator.isPositiveNumber(rateStr) || !Validator.isPositiveNumber(termStr)) {
                    JOptionPane.showMessageDialog(this, "Lãi suất/Kỳ hạn phải dương!");
                    return;
                }

                String sqlDate = convertDateToSQL(dateStr);
                acc = new SavingsAccount(
                        idToSave,
                        code,
                        custIdToSave,
                        name,
                        cid,
                        sqlDate,
                        amount, sqlDate, Double.parseDouble(rateStr), Integer.parseInt(termStr)
                );
            } else {
                String cardNum = txtCardNum.getText().trim();
                double balance = 0;
                if (txtBalance.getValue() != null) {
                    balance = ((Number) txtBalance.getValue()).doubleValue();
                }

                if (Validator.isEmpty(cardNum)) {
                    JOptionPane.showMessageDialog(this, "Số thẻ không được để trống!");
                    return;
                }

                String today = java.time.LocalDate.now().toString();
                String cDate = isEditMode ? currentAccount.getCreationDate() : today;

                acc = new PaymentAccount(
                        idToSave,
                        code,
                        custIdToSave,
                        name,
                        cid,
                        cDate,
                        cardNum, balance
                );
            }

            boolean res = isEditMode ? accountDAO.updateAccount(acc) : accountDAO.addAccount(acc);

            if (res) {
                JOptionPane.showMessageDialog(this, isEditMode ? "Cập nhật thành công!" : "Thêm mới thành công!");
                isSuccess = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi Database! Không thể lưu dữ liệu.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi dữ liệu: " + ex.getMessage());
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    private String convertDateToSQL(String uiDate) {
        try {
            SimpleDateFormat uiFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = uiFormat.parse(uiDate);
            return sqlFormat.format(date);
        } catch (ParseException e) {
            return uiDate;
        }
    }

    private String convertDateToGUI(String sqlDate) {
        if (sqlDate == null) return "";
        try {
            SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat uiFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = sqlFormat.parse(sqlDate);
            return uiFormat.format(date);
        } catch (ParseException e) {
            return sqlDate;
        }
    }
}