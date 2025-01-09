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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class DashboardPanel extends JPanel {

    private final JPanel contentPanel;
    private final double balance;
    public DashboardPanel(JPanel contentPanel,double balance) {
        this.contentPanel = contentPanel;
        this.balance = balance;
    }

    private String formatBalance(double balance) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        return numberFormat.format(balance); // Format with commas
    }
    public static Object[][] fetchBookData() {
        String selectSQL = "SELECT name, ngay_muon, ngay_tra, status FROM SACH_MUON_TRA";
        ArrayList dataList = new ArrayList(); // Không sử dụng generic

        try (Connection connection = DriverManager.getConnection(url, user, password); PreparedStatement preparedStatement = connection.prepareStatement(selectSQL); ResultSet resultSet = preparedStatement.executeQuery()) {

            // Duyệt qua từng bản ghi và thêm vào danh sách
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                Date ngayMuon = resultSet.getDate("ngay_muon");
                Date ngayTra = resultSet.getDate("ngay_tra");
                String status = resultSet.getString("status");

                // Thêm bản ghi vào danh sách
                dataList.add(new Object[]{name, ngayMuon != null ? ngayMuon.toString() : "", ngayTra != null ? ngayTra.toString() : "", status});
            }

        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }

        // Chuyển danh sách thành mảng hai chiều
        return (Object[][]) dataList.toArray(new Object[dataList.size()][]);
    }

    @Override
    public void show() {
        // Create dashboard panel
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 4, 30, 10));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
        Object[][] bookDataList = fetchBookData();
        int borrowedBookCount = bookDataList.length;
        String balanceFormatted = formatBalance(balance); 
        JLabel box1 = new JLabel("<html><div style='text-align: center;'>"
                + "<p style='vertical-align: middle;margin-bottom:4px'>"
                + "<img src='" + getClass().getResource("/image/Books-3d-icon.png") + "' width='50' height='50' /></p>"
                + "<span style='display: inline-block; vertical-align: middle;'>"
                + "Tổng số sách đang mượn<br>= " + borrowedBookCount + "</span></div></html>");
        JLabel box2 = new JLabel("<html><div style='text-align: center;'>"
                + "<p style='vertical-align: middle;margin-bottom:4px'>"
                + "<img src='" + getClass().getResource("/image/wallet-icon.png") + "' width='50' height='50' /></p>"
                + "<span style='display: inline-block; vertical-align: middle;'>"
                + "Số dư phí<br>" + balanceFormatted + " vnđ</span></div></html>");
        JLabel box3 = new JLabel("<html><div style='text-align: center;'>"
                + "<p style='vertical-align: middle;margin-bottom:4px'>"
                + "<img src='" + getClass().getResource("/image/Status-media-playlist-repeat-icon.png") + "' width='50' height='50' /></p>"
                + "<span style='display: inline-block; vertical-align: middle;'>"
                + "Trạng thái thẻ<br></span></div></html>");

        JLabel[] boxes = {box1, box2, box3};
        for (JLabel box : boxes) {
            box.setHorizontalAlignment(SwingConstants.CENTER);
            box.setVerticalAlignment(SwingConstants.CENTER);
            box.setFont(new Font("Arial", Font.BOLD, 20));
            box.setOpaque(true);
            box.setBackground(new Color(240, 240, 240));
            box.setBorder(BorderFactory.createRaisedSoftBevelBorder());
            dashboardPanel.add(box);
            box.setPreferredSize(new Dimension(230, 150));
        }

        // Tiêu đề
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel tableTitle = new JLabel("Danh sách mượn trả gần đây", SwingConstants.LEFT);
        tableTitle.setFont(new Font("Arial", Font.BOLD, 16));
        tableTitle.setOpaque(true);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        titlePanel.add(tableTitle);

        // Cột tiêu đề
        String[] columnNames = {"Tên sách", "Ngày mượn", "Ngày trả", "Trạng thái"};

        // Lấy dữ liệu từ database
        Object[][] data = fetchBookData();

        // Tạo bảng
        JTable table = new JTable(data, columnNames);

        // Thiết lập hiển thị
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setDefaultRenderer(new CustomHeaderRenderer());
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(36);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        // Cuộn bảng
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Cập nhật contentPanel
        contentPanel.removeAll();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(dashboardPanel);
        contentPanel.add(titlePanel);
        contentPanel.add(scrollPane);

        // Làm mới giao diện
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    static class CustomHeaderRenderer extends DefaultTableCellRenderer {

        public CustomHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER); // Căn giữa tiêu đề
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(new Font("Arial", Font.BOLD, 14)); // Đặt font chữ
            c.setForeground(Color.BLACK); // Màu chữ
            c.setBackground(new Color(240, 240, 240)); // Màu nền (Xanh steel blue)
            ((JLabel) c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Viền
            return c;
        }
    }
}
