package com.kma.librarycard;

import static com.kma.librarycard.MainPage.password;
import static com.kma.librarycard.MainPage.url;
import static com.kma.librarycard.MainPage.user;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.util.Locale;

public class PaymentPanel extends JPanel {

    private final JPanel contentPanel;
    private JTable tablePayment;
    private DefaultTableModel modelPayment;
    private final ProfilePanel profilePanel;
    private final double balance;

    public PaymentPanel(JPanel contentPanel, ProfilePanel profilePanel, double balance) {
        this.contentPanel = contentPanel;
        this.profilePanel = profilePanel;
        this.balance = balance;
    }

    public static List<Object[]> fetchDataList(String cardID) {
        String selectSQL = "SELECT * FROM sach_muon_tra WHERE ngay_het_han < CURRENT_DATE AND status = ? AND card_id = ?";
        List<Object[]> dataList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, user, password); PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            preparedStatement.setString(1, "Chưa thanh toán");
            preparedStatement.setString(2, cardID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String bookName = resultSet.getString("name");
                    String price = resultSet.getString("price");
                    Date ngayHetHan = resultSet.getDate("ngay_het_han");
                    double priceAmount = Double.parseDouble(price);
                    String formattedPrice = formatCurrency(priceAmount);

                    dataList.add(new Object[]{
                        id,
                        false,
                        bookName != null ? bookName : "",
                        formattedPrice,
                        ngayHetHan != null ? ngayHetHan.toString() : "",});
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
        return dataList;
    }

    public static Object[][] fetchData(String cardID) {
        List<Object[]> dataList = fetchDataList(cardID);
        return dataList.toArray(Object[][]::new);
    }

    public static String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
        return currencyFormat.format(amount) + " VND";
    }

    @Override
    public void show() {
        String cardID = "";
        if (profilePanel != null && profilePanel.card_id != null) {
            cardID = profilePanel.card_id;
        }
        // Title Panel for Payment Table
        JPanel titlePanelPayment = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel tableTitlePayment = new JLabel("Thông tin sách quá hạn");
        tableTitlePayment.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));
        tableTitlePayment.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanelPayment.add(tableTitlePayment);

        // Table for Payment Information
        String[] columnNamesPayment = {"ID", "Chọn", "Tên sách", "Số tiền cần thanh toán", "Ngày hết hạn thanh toán"};
        Object[][] dataPayment = fetchData(cardID);

        modelPayment = new DefaultTableModel(dataPayment, columnNamesPayment) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        DefaultTableCellRenderer centerRendererPayment = new DefaultTableCellRenderer();
        centerRendererPayment.setHorizontalAlignment(SwingConstants.CENTER);

        tablePayment = new JTable(modelPayment);
        JTableHeader headerPayment = tablePayment.getTableHeader();
        headerPayment.setPreferredSize(new Dimension(0, 36));
        headerPayment.setFont(new Font("Arial", Font.BOLD, 14));
        headerPayment.setDefaultRenderer(new CustomHeaderRenderer());

        // Hide the ID column
        TableColumn idColumn = tablePayment.getColumnModel().getColumn(0);
        idColumn.setMinWidth(0);
        idColumn.setMaxWidth(0);
        idColumn.setPreferredWidth(0);

        // Center align the cells (except checkbox column)
        for (int i = 2; i < tablePayment.getColumnCount(); i++) {
            tablePayment.getColumnModel().getColumn(i).setCellRenderer(centerRendererPayment);
        }

        tablePayment.setFont(new Font("Arial", Font.PLAIN, 14));
        tablePayment.setRowHeight(36);

        // Scroll Pane for Table
        JScrollPane scrollPanePayment = new JScrollPane(tablePayment);
        scrollPanePayment.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Button Panel for Actions
        JPanel buttonPanelPayment = new JPanel();
        buttonPanelPayment.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Create Buttons
        JButton buttonPayNow = new JButton("Thanh toán");
        JButton buttonRecharge = new JButton("Nạp tiền");

        // Set size and style for buttons
        buttonPayNow.setPreferredSize(new Dimension(150, 40));
        buttonPayNow.setBackground(new Color(0, 123, 255));
        buttonPayNow.setForeground(Color.WHITE);

        buttonRecharge.setPreferredSize(new Dimension(150, 40));
        buttonRecharge.setBackground(new Color(46, 139, 87));
        buttonRecharge.setForeground(Color.WHITE);

        buttonPayNow.addActionListener((ActionEvent e) -> {
            List<Integer> selectedIds = new ArrayList<>();
            double totalPayment = 0;
            for (int i = 0; i < modelPayment.getRowCount(); i++) {
                boolean isChecked = (Boolean) modelPayment.getValueAt(i, 1);
                if (isChecked) {
                    int id = (int) modelPayment.getValueAt(i, 0);
                    selectedIds.add(id);
                    String priceText = (String) modelPayment.getValueAt(i, 3);
                    double priceAmount = Double.parseDouble(priceText.replace(",", "").replace(" VND", ""));
                    totalPayment += priceAmount;
                }
            }
            if (selectedIds.isEmpty()) {
                JOptionPane.showMessageDialog(PaymentPanel.this, "Vui lòng chọn sách để thanh toán", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (totalPayment > balance) {
                HistoryPanel.insertRecord("Thanh toán", "Thất bại", profilePanel.card_id);

                JOptionPane.showMessageDialog(PaymentPanel.this, "Số dư không đủ để thanh toán", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (profilePanel != null && profilePanel.card_id != null) {
                String cardID1 = profilePanel.card_id;
                updateStatus(selectedIds, cardID1);
                HistoryPanel.insertRecord("Thanh toán", "Thành công", profilePanel.card_id);
                JOptionPane.showMessageDialog(PaymentPanel.this, "Thanh toán thành công ", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                //Tải lại dữ liệu
                updateTable(cardID1);
            }
        });

        buttonRecharge.addActionListener((ActionEvent e) -> {
            showRechargeDialog();
        });

        // Add buttons to the button panel
        buttonPanelPayment.add(buttonPayNow);
        buttonPanelPayment.add(buttonRecharge);
        // Update Content Panel
        contentPanel.removeAll();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(titlePanelPayment);
        contentPanel.add(scrollPanePayment);
        contentPanel.add(buttonPanelPayment);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void updateTable(String cardID) {
        Object[][] dataPayment = fetchData(cardID);
        modelPayment.setDataVector(dataPayment, new String[]{"ID", "Chọn", "Tên sách", "Số tiền cần thanh toán", "Ngày hết hạn thanh toán"});
    }

    private void updateStatus(List<Integer> selectedIds, String cardID) {
        String updateSQL = "UPDATE sach_muon_tra SET status = ? WHERE id = ? AND card_id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password); PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            for (int id : selectedIds) {
                preparedStatement.setString(1, "Đã thanh toán");
                preparedStatement.setInt(2, id);
                preparedStatement.setString(3, cardID);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật trạng thái sách: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái sách", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRechargeDialog() {
        JDialog rechargeDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Nạp tiền", true);
        rechargeDialog.setLayout(new GridBagLayout());
        rechargeDialog.setSize(350, 220);
        rechargeDialog.setResizable(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel amountLabel = new JLabel("Số tiền nạp:");
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField amountField = new JTextField(15);
        amountField.setPreferredSize(new Dimension(150, 32));
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        amountField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        gbc.gridx = 0;
        gbc.gridy = 0;
        rechargeDialog.add(amountLabel, gbc);
        gbc.gridx = 1;
        rechargeDialog.add(amountField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setPreferredSize(new Dimension(120, 40));
        confirmButton.setBackground(new Color(0, 123, 255));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createEmptyBorder());

        JButton cancelButton = new JButton("Hủy bỏ");
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.setBackground(Color.GRAY);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder());

        confirmButton.addActionListener((ActionEvent e) -> {
            String amountText = amountField.getText();
            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(rechargeDialog, "Vui lòng nhập số tiền", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(rechargeDialog, "Số tiền phải lớn hơn 0", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    HistoryPanel.insertRecord("Nạp tiền", "Thất bại", profilePanel.card_id);
                    return;
                }
                if (profilePanel != null && profilePanel.card_id != null) {
                    String cardID = profilePanel.card_id;
                    updateBalance(cardID, amount);
                    String formattedAmount = formatCurrency(amount);
                    JOptionPane.showMessageDialog(rechargeDialog, "Nạp tiền thành công với số tiền " + formattedAmount, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    HistoryPanel.insertRecord("Nạp tiền", "Thành công", profilePanel.card_id);

                    rechargeDialog.dispose();
                } else {
                    HistoryPanel.insertRecord("Nạp tiền", "Thất bại", profilePanel.card_id);

                    JOptionPane.showMessageDialog(rechargeDialog, "Không tìm thấy thông tin thẻ", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                HistoryPanel.insertRecord("Nạp tiền", "Thất bại", profilePanel.card_id);
                JOptionPane.showMessageDialog(rechargeDialog, "Số tiền nhập không đúng định dạng", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelButton.addActionListener((ActionEvent e) -> {
            rechargeDialog.dispose();
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        rechargeDialog.add(buttonPanel, gbc);

        rechargeDialog.setLocationRelativeTo(this);
        rechargeDialog.setVisible(true);
    }

    private void updateBalance(String cardID, double amount) {
        String updateSQL = "UPDATE card_info SET balance = balance + ? WHERE card_id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password); PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            preparedStatement.setDouble(1, amount);
            preparedStatement.setString(2, cardID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật số dư: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật số dư", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class CustomHeaderRenderer extends DefaultTableCellRenderer {

        public CustomHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(new Font("Arial", Font.BOLD, 14));
            c.setForeground(Color.BLACK);
            c.setBackground(new Color(240, 240, 240));
            ((JLabel) c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            return c;
        }
    }
}
