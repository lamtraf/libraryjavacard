package com.kma.librarycard;

import static com.kma.librarycard.MainPage.password;
import static com.kma.librarycard.MainPage.url;
import static com.kma.librarycard.MainPage.user;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class BorrowReturnPanel extends JPanel {

    private final JPanel contentPanel;
    private JTable mainTableBorrow;
    private final ProfilePanel profilePanel;

    public BorrowReturnPanel(JPanel contentPanel, ProfilePanel profilePanel) {
        this.contentPanel = contentPanel;
        this.profilePanel = profilePanel;
    }

    public static Object[][] fetchData(boolean isBorrowTable, ProfilePanel profilePanel) {
        String cardID = (profilePanel != null) ? profilePanel.card_id : null;
        String selectSQL = isBorrowTable
                ? "SELECT name, ngay_muon, ngay_het_han, status, price FROM SACH_MUON_TRA WHERE ngay_tra IS NULL AND card_ID = ?"
                : "SELECT id_sach, name FROM SACH";

        ArrayList<Object[]> dataList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, user, password); PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            if (isBorrowTable && cardID != null) {
                preparedStatement.setString(1, cardID);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (isBorrowTable) {
                        String name = resultSet.getString("name");
                        Date ngayMuon = resultSet.getDate("ngay_muon");
                        Date ngayTra = resultSet.getDate("ngay_het_han");
                        String status = resultSet.getString("status");
                        Double price = resultSet.getDouble("price");
                        dataList.add(new Object[]{name, price, ngayMuon != null ? ngayMuon.toString() : "",
                            ngayTra != null ? ngayTra.toString() : "", status});

                    } else {
                        int idSach = resultSet.getInt("id_sach");
                        String name = resultSet.getString("name");
                        dataList.add(new Object[]{idSach, name});
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();

        }
        return dataList.toArray(Object[][]::new);
    }

    public static Object[][] fetchDataBorrowed(boolean isBorrowTable, ProfilePanel profilePanel) {
        String cardID = (profilePanel != null) ? profilePanel.card_id : null;
        String selectSQL = isBorrowTable && cardID != null
                ? "SELECT b.name, b.price FROM book b LEFT JOIN sach_muon_tra s ON b.name = s.name WHERE s.name IS NULL OR s.card_ID != ?"
                : "SELECT name, price FROM book WHERE status = 1";
        ArrayList<Object[]> dataList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, user, password); PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            if (isBorrowTable && cardID != null) {
                preparedStatement.setString(1, cardID);
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (isBorrowTable && cardID != null) {
                        String name = resultSet.getString("name");
                        Double price = resultSet.getDouble("price");
                        dataList.add(new Object[]{name, price});
                    } else {
                        String name = resultSet.getString("name");
                        Double price = resultSet.getDouble("price");
                        dataList.add(new Object[]{name, price});
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();

        }
        return dataList.toArray(Object[][]::new);
    }

    @Override
    public void show() {
        JPanel titlePanelBorrow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanelBorrow.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));
        JLabel tableTitleBorrow = new JLabel("Danh sách đang mượn");
        tableTitleBorrow.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanelBorrow.add(tableTitleBorrow);

        String[] columnNamesBorrow = {"Tên sách", "Giá", "Ngày mượn", "Ngày hết hạn", "Trạng thái"};
        Object[][] dataBorrow = fetchData(true, profilePanel);

        DefaultTableCellRenderer centerRendererBorrow = new DefaultTableCellRenderer();
        centerRendererBorrow.setHorizontalAlignment(SwingConstants.CENTER);
        mainTableBorrow = new JTable(dataBorrow, columnNamesBorrow);
        JTableHeader headerBorrow = mainTableBorrow.getTableHeader();
        headerBorrow.setPreferredSize(new Dimension(0, 36));
        headerBorrow.setFont(new Font("Arial", Font.BOLD, 14));
        headerBorrow.setDefaultRenderer(new CustomHeaderRenderer());

        for (int i = 0; i < mainTableBorrow.getColumnCount(); i++) {
            mainTableBorrow.getColumnModel().getColumn(i).setCellRenderer(centerRendererBorrow);
        }
        mainTableBorrow.setFont(new Font("Arial", Font.PLAIN, 14));
        mainTableBorrow.setRowHeight(36);

        JScrollPane scrollPaneBorrow = new JScrollPane(mainTableBorrow);
        scrollPaneBorrow.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel buttonPanelBorrow = new JPanel();
        buttonPanelBorrow.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton buttonEdit = new JButton("Mượn sách");
        JButton buttonExtend = new JButton("Trả sách");

        buttonEdit.setPreferredSize(new Dimension(150, 40));
        buttonEdit.setBackground(new Color(0, 28, 68));
        buttonExtend.setPreferredSize(new Dimension(150, 40));
        buttonExtend.setBackground(new Color(0, 28, 68));
        buttonEdit.setForeground(Color.WHITE);
        buttonExtend.setForeground(Color.WHITE);

        buttonEdit.addActionListener(e -> showBookSelectionDialog(true));
        buttonExtend.addActionListener(e -> showReturnBookSelectionDialog());
        buttonPanelBorrow.add(buttonEdit);
        buttonPanelBorrow.add(buttonExtend);

        contentPanel.removeAll();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(titlePanelBorrow);
        contentPanel.add(scrollPaneBorrow);
        contentPanel.add(buttonPanelBorrow);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showBookSelectionDialog(boolean isBorrow) {
        JDialog bookDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                isBorrow ? "Chọn Sách Mượn" : "Chọn Sách Trả", true);
        bookDialog.setLayout(new BorderLayout());

        String[] columnNames = isBorrow ? new String[]{"Chọn", "Tên Sách", "Giá"} : new String[]{"Chọn", "Tên Sách", "Giá"};
        Object[][] data = fetchDataBorrowed(true, profilePanel);

        Object[][] tableData = new Object[data.length][3];
        for (int i = 0; i < data.length; i++) {
            tableData[i][0] = Boolean.FALSE;
            tableData[i][1] = data[i][0];
            tableData[i][2] = data[i][1];

        }

        DefaultTableModel model = new DefaultTableModel(tableData, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        JTable bookTable = new JTable(model);
        JTableHeader headerBook = bookTable.getTableHeader();
        headerBook.setPreferredSize(new Dimension(0, 36));
        headerBook.setFont(new Font("Arial", Font.BOLD, 14));
        headerBook.setDefaultRenderer(new CustomHeaderRenderer());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < bookTable.getColumnCount(); i++) {
            bookTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        bookTable.setFont(new Font("Arial", Font.PLAIN, 14));
        bookTable.setRowHeight(36);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(bookTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton selectButton = new JButton("Chọn");
        JButton cancelButton = new JButton("Hủy");
        selectButton.setPreferredSize(new Dimension(100, 35));
        selectButton.setBackground(new Color(0, 28, 68));
        selectButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBackground(new Color(0, 28, 68));
        cancelButton.setForeground(Color.WHITE);

        selectButton.addActionListener(e -> {
            List<String> selectedBooks = new ArrayList<>();
            for (int i = 0; i < bookTable.getRowCount(); i++) {
                Boolean isChecked = (Boolean) bookTable.getValueAt(i, 0);
                if (isChecked) {
                    String selectedBook = bookTable.getValueAt(i, 1).toString();
                    selectedBooks.add(selectedBook);
                }
            }
            if (selectedBooks.isEmpty()) {
                JOptionPane.showMessageDialog(bookDialog, "Vui lòng chọn ít nhất một sách.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                if (isBorrow) {
                    borrowBooks(selectedBooks);
                }
                bookDialog.dispose();
            }
        });

        cancelButton.addActionListener(e -> bookDialog.dispose());
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);

        bookDialog.add(scrollPane, BorderLayout.CENTER);
        bookDialog.add(buttonPanel, BorderLayout.SOUTH);
        bookDialog.setSize(600, 400);
        bookDialog.setLocationRelativeTo(this);
        bookDialog.setVisible(true);
    }

    private void showReturnBookSelectionDialog() {
        JDialog bookDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Chọn Sách Trả", true);
        bookDialog.setLayout(new BorderLayout());

        String[] columnNames = new String[]{"Chọn", "Tên Sách", "Giá"};
        Object[][] data = fetchData(true, profilePanel);

        Object[][] tableData = new Object[data.length][3];
        for (int i = 0; i < data.length; i++) {
            tableData[i][0] = Boolean.FALSE;
            tableData[i][1] = data[i][0];
            tableData[i][2] = data[i][1];
        }

        DefaultTableModel model = new DefaultTableModel(tableData, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        JTable bookTable = new JTable(model);
        JTableHeader headerBook = bookTable.getTableHeader();
        headerBook.setPreferredSize(new Dimension(0, 36));
        headerBook.setFont(new Font("Arial", Font.BOLD, 14));
        headerBook.setDefaultRenderer(new CustomHeaderRenderer());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < bookTable.getColumnCount(); i++) {
            bookTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        bookTable.setFont(new Font("Arial", Font.PLAIN, 14));
        bookTable.setRowHeight(36);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(bookTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton selectButton = new JButton("Chọn");
        JButton cancelButton = new JButton("Hủy");
        selectButton.setPreferredSize(new Dimension(100, 35));
        selectButton.setBackground(new Color(0, 28, 68));
        selectButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBackground(new Color(0, 28, 68));
        cancelButton.setForeground(Color.WHITE);

        selectButton.addActionListener(e -> {
            List<String> selectedBooks = new ArrayList<>();
            for (int i = 0; i < bookTable.getRowCount(); i++) {
                Boolean isChecked = (Boolean) bookTable.getValueAt(i, 0);
                if (isChecked) {
                    String selectedBook = bookTable.getValueAt(i, 1).toString();
                    selectedBooks.add(selectedBook);
                }
            }
            if (selectedBooks.isEmpty()) {
                JOptionPane.showMessageDialog(bookDialog, "Vui lòng chọn ít nhất một sách.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                returnBooks(selectedBooks);
                bookDialog.dispose();
            }
        });

        cancelButton.addActionListener(e -> bookDialog.dispose());
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);

        bookDialog.add(scrollPane, BorderLayout.CENTER);
        bookDialog.add(buttonPanel, BorderLayout.SOUTH);
        bookDialog.setSize(600, 400);
        bookDialog.setLocationRelativeTo(this);
        bookDialog.setVisible(true);
    }

    private void borrowBooks(List<String> selectedBooks) {
        String insertSQL = "INSERT INTO SACH_MUON_TRA (name, price, ngay_muon, ngay_het_han, status, card_ID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, user, password); PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (String selectedBook : selectedBooks) {

                Double price = 0.0;
                String getPriceSQL = "SELECT price FROM book WHERE name = ?";
                try (PreparedStatement priceStatement = connection.prepareStatement(getPriceSQL)) {
                    priceStatement.setString(1, selectedBook);
                    ResultSet priceResult = priceStatement.executeQuery();
                    if (priceResult.next()) {
                        price = priceResult.getDouble("price");
                    }
                }

                preparedStatement.setString(1, selectedBook);
                preparedStatement.setDouble(2, price);
                preparedStatement.setDate(3, new java.sql.Date(new Date().getTime()));
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.add(java.util.Calendar.MONTH, 1);
                preparedStatement.setDate(4, new java.sql.Date(calendar.getTimeInMillis()));
                preparedStatement.setString(5, "Chưa thanh toán");
                preparedStatement.setString(6, profilePanel.card_id);
                preparedStatement.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Mượn sách thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            updateMainTable();
        } catch (SQLException ex) {
            System.err.println("Lỗi khi mượn sách: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi mượn sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBooks(List<String> selectedBooks) {
        String updateSQL = "UPDATE SACH_MUON_TRA SET ngay_tra = ?, status = ? WHERE name = ? AND ngay_tra IS NULL AND card_ID = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password); PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            for (String selectedBook : selectedBooks) {
                String status = "Đã trả";
                // Trim the string, limit to max of 50 character
                String statusToSet = status.trim().substring(0, Math.min(status.length(), 50));

                preparedStatement.setDate(1, new java.sql.Date(new Date().getTime()));
                preparedStatement.setString(2, statusToSet);
                preparedStatement.setString(3, selectedBook);
                preparedStatement.setString(4, profilePanel.card_id);
                preparedStatement.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Trả sách thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            updateMainTable();
        } catch (SQLException ex) {
            System.err.println("Lỗi khi trả sách: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi trả sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMainTable() {
        Object[][] newData = fetchData(true, profilePanel);
        DefaultTableModel model = new DefaultTableModel(newData, new String[]{"Tên sách", "Giá", "Ngày mượn", "Ngày hết hạn", "Trạng thái"});
        mainTableBorrow.setModel(model);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < mainTableBorrow.getColumnCount(); i++) {
            mainTableBorrow.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        mainTableBorrow.setFont(new Font("Arial", Font.PLAIN, 14));
        mainTableBorrow.setRowHeight(36);
        mainTableBorrow.revalidate();
        mainTableBorrow.repaint();

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
