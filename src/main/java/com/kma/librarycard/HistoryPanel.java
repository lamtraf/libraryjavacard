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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class HistoryPanel extends JPanel {

    private final JPanel contentPanel;

    public HistoryPanel(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    /**
     * Fetches historical data from the database.
     *
     * @return a two-dimensional Object array containing the history data.
     */
    public static Object[][] fetchData() {
        String selectSQL = "SELECT date, action, result FROM lich_su";
        ArrayList<Object[]> dataList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Date date = resultSet.getDate("date");
                String action = resultSet.getString("action");
                String result = resultSet.getString("result");

                dataList.add(new Object[]{
                        date != null ? date.toString() : "",
                        action != null ? action : "",
                        result != null ? result : ""
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }

        return dataList.toArray(Object[][]::new);
    }

    /**
     * Displays the history panel with dynamically loaded data.
     */
    @Override
    public void show() {
        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Lịch sử hoạt động");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));

        // Table for History
        String[] columnNames = {"Ngày", "Hành động", "Kết quả"};
        Object[][] data = fetchData();

        JTable historyTable = new JTable(data, columnNames);
        JTableHeader tableHeader = historyTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 36));
        tableHeader.setFont(new Font("Arial", Font.BOLD, 14));
        tableHeader.setDefaultRenderer(new CustomHeaderRenderer());

        // Center align the table cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        historyTable.setFont(new Font("Arial", Font.PLAIN, 14));
        historyTable.setRowHeight(36);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Main Content Panel
        contentPanel.removeAll();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(titlePanel);
        contentPanel.add(scrollPane);
        contentPanel.add(buttonPanel);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Custom header renderer for table headers.
     */
    static class CustomHeaderRenderer extends DefaultTableCellRenderer {

        public CustomHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(new Font("Arial", Font.BOLD, 14));
            c.setForeground(Color.BLACK);
            c.setBackground(new Color(240, 240, 240));
            ((JLabel) c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            return c;
        }
    }
}
