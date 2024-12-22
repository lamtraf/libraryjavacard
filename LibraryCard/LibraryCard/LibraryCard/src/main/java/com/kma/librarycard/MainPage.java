package com.kma.librarycard;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class MainPage extends JFrame {

    private final JPanel contentPanel;
    private JPanel rightAppBar; // Khai báo rightAppBar

//    private List<CardTerminal> terminal;
    private Card card; // Giữ kết nối thẻ
    private CardChannel channel;

    public MainPage() {
        setTitle("Trang chủ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Tạo AppBar chính
        JPanel appBar = createAppBar();

        // Tạo TabBar bên trái
        JPanel tabBar = createTabBar();

        // Tạo nội dung hiển thị
        contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BorderLayout());
        JLabel initialContent = new JLabel("Chào mừng! Hãy chọn một tab.");
        initialContent.setHorizontalAlignment(SwingConstants.CENTER);
        initialContent.setFont(new Font("Arial", Font.BOLD, 20));
        contentPanel.add(initialContent, BorderLayout.CENTER);

        add(appBar, BorderLayout.NORTH);
        add(tabBar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createAppBar() {
        JPanel appBar = new JPanel();
        appBar.setLayout(new BorderLayout());
        appBar.setPreferredSize(new Dimension(getWidth(), 80));

        // Tạo AppBar bên trái
        JPanel leftAppBar = createLeftAppBar();

        // Gán rightAppBar một cách an toàn
        rightAppBar = createRightAppBar(); // Khởi tạo chắc chắn

        // Thêm AppBar bên trái và bên phải vào `appBar`
        appBar.add(leftAppBar, BorderLayout.WEST);
        appBar.add(rightAppBar, BorderLayout.EAST);

        return appBar;
    }

    private JPanel createLeftAppBar() {
        JPanel leftAppBar = new JPanel();
        leftAppBar.setBackground(new Color(0, 28, 68)); // Màu nền xanh dương
        leftAppBar.setPreferredSize(new Dimension(200, 80));
        leftAppBar.setLayout(new GridBagLayout());

        ImageIcon libraryIcon = new ImageIcon("src/main/java/image/graduate_hat.png");
        Image scaledImage = libraryIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
        JLabel titleLabel = new JLabel("Thư viện");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(null);
        centerPanel.setOpaque(true);
        centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerPanel.add(iconLabel);
        centerPanel.add(titleLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        leftAppBar.add(centerPanel, gbc);

        return leftAppBar;
    }

    private static JPanel createRightAppBar() {
        JPanel rightAppBar = new JPanel();
        rightAppBar.setLayout(new BorderLayout());
        return rightAppBar;
    }

    private JPanel createTabBar() {
        JPanel tabBar = new JPanel();
        tabBar.setBackground(new Color(0, 28, 68));
        tabBar.setLayout(new BoxLayout(tabBar, BoxLayout.Y_AXIS));

        // Tạo các nút với icon
        JButton tab1 = createTabButtonWithIcon("Trang chủ", "src/main/java/image/home.png");
        JButton tab2 = createTabButtonWithIcon("Thông tin cá nhân", "src/main/java/image/user.png");
        JButton tab3 = createTabButtonWithIcon("Mượn trả sách", "src/main/java/image/book.png");
        JButton tab4 = createTabButtonWithIcon("Thanh toán", "src/main/java/image/payment.png");
        JButton tab5 = createTabButtonWithIcon("Lịch sử hoạt động", "src/main/java/image/history.png");

        // Gắn sự kiện
        tab1.addActionListener(e -> switchTab("Trang chủ", "Đây là nội dung Tab 1"));
        tab2.addActionListener(e -> switchTab("Thông tin cá nhân", "Đây là nội dung Tab 2"));
        tab3.addActionListener(e -> switchTab("Mượn trả sách", "Đây là nội dung Tab 3"));
        tab4.addActionListener(e -> switchTab("Thanh toán", "Đây là nội dung Tab 4"));
        tab5.addActionListener(e -> switchTab("Lịch sử hoạt động", "Đây là nội dung Tab 5"));

        tabBar.add(tab1);
        tabBar.add(tab2);
        tabBar.add(tab3);
        tabBar.add(tab4);
        tabBar.add(tab5);

        return tabBar;
    }

    private JButton createTabButtonWithIcon(String text, String iconPath) {
        ImageIcon originalIcon = new ImageIcon(iconPath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JButton button = new JButton(text, scaledIcon);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(200, 50));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(0, 28, 68));
        button.setOpaque(true);
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 3);
        button.setBorder(blackBorder);
        button.setForeground(Color.WHITE);
        button.setIconTextGap(15);
        return button;
    }

    static class RoundedBorder extends AbstractBorder {

        private int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(c.getForeground());
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(5, 5, 5, 5); // Reduce the border inset to avoid clipping
        }
    }

    class CustomHeaderRenderer extends DefaultTableCellRenderer {

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

    private byte[] convertImageToBytes(File imageFile) throws IOException {
        // Đọc file hình ảnh vào mảng byte
        FileInputStream fis = new FileInputStream(imageFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        // Đảm bảo đóng stream sau khi sử dụng
        fis.close();
        baos.close();

        return baos.toByteArray();
    }

// Giả sử bạn có một phương thức để gửi mảng byte tới Smart Card
    private boolean sendToSmartCard(byte[] imageBytes) {
        try {
            // Kết nối đến thẻ
            TerminalFactory factory = TerminalFactory.getDefault();
            CardTerminals terminals = factory.terminals();
            if (terminals.list().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy trình đọc thẻ.");
                return false;
            }

            CardTerminal terminal = terminals.list().get(0);
            Card card = terminal.connect("T=0");
            CardChannel channel = card.getBasicChannel();

            // Xác định kích thước tối đa của APDU
            int maxApduSize = 2048; // Thẻ thường hỗ trợ tối đa 255 byte mỗi lần
            int offset = 0;

            while (offset < imageBytes.length) {
                // Tính toán kích thước phần dữ liệu cần gửi
                int chunkSize = Math.min(maxApduSize, imageBytes.length - offset);

                // Tạo lệnh Extended APDU (dữ liệu có thể lớn hơn 255 byte)
                byte[] command = new byte[5 + chunkSize];  // 5 byte cho header
                command[0] = (byte) 0xA4; // CLA: Class byte cho APDU mở rộng
                command[1] = (byte) 0x05; // INS: Instruction byte (thay đổi nếu cần)
                command[2] = (byte) 0x00; // P1: Parameter 1
                command[3] = (byte) 0x00; // P2: Parameter 2
                command[4] = (byte) chunkSize; // Lc: Độ dài dữ liệu (byte)

                // Sao chép dữ liệu ảnh vào phần APDU
                System.arraycopy(imageBytes, offset, command, 5, chunkSize);

                // Gửi lệnh APDU
                CommandAPDU apduCommand = new CommandAPDU(command);
                ResponseAPDU response = channel.transmit(apduCommand);

                // Kiểm tra phản hồi của thẻ
                if (response.getSW() != 0x9000) {
                    JOptionPane.showMessageDialog(null, "Lỗi từ thẻ: " + Integer.toHexString(response.getSW()));
                    return false;
                }

                // Cập nhật offset để gửi phần tiếp theo
                offset += chunkSize;
            }

            JOptionPane.showMessageDialog(null, "Dữ liệu đã được gửi thành công.");
            return true;

        } catch (CardException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kết nối thẻ: " + e.getMessage());
            return false;
        }
    }

//  private String getUserNameFromCard() {
//    String name = "";
//    try {
//        // Kết nối đến thẻ
//        TerminalFactory factory = TerminalFactory.getDefault();
//        CardTerminals terminals = factory.terminals();
//        if (terminals.list().isEmpty()) {
//            JOptionPane.showMessageDialog(null, "Không tìm thấy trình đọc thẻ.");
//            return name;  // Thêm return ở đây để tránh tiếp tục nếu không tìm thấy thẻ
//        }
//        CardTerminal terminal = terminals.list().get(0);
//        Card card = terminal.connect("T=0");
//        CardChannel channel = card.getBasicChannel();
//
//        // Gửi lệnh APDU để chọn ứng dụng của thẻ
//        CommandAPDU getNameCommand = new CommandAPDU(0xA4, 0x15, 0x00, 0x00, new byte[]{}, 0x0F);
//
//        ResponseAPDU nameResponse = channel.transmit(getNameCommand);
//
//        if (nameResponse.getSW() == 0x9000) {
//            byte[] data = nameResponse.getData();
//            System.out.println("Dữ liệu nhận được từ thẻ: " + Arrays.toString(data));
//
//            // Chuyển mảng byte thành chuỗi UTF-8 và xử lý chuỗi
//            name = new String(data, "UTF-8");
//
//            // Loại bỏ khoảng trắng ở đầu và cuối chuỗi
//            name = name.trim();
//
//        } else {
//            JOptionPane.showMessageDialog(this, "Không thể đọc tên từ thẻ.");
//        }
//    } catch (CardException e) {
//        JOptionPane.showMessageDialog(this, "Lỗi khi kết nối với thẻ.");
//        e.printStackTrace();
//    } catch (UnsupportedEncodingException e) {
//        JOptionPane.showMessageDialog(this, "Lỗi giải mã dữ liệu từ thẻ.");
//        e.printStackTrace();
//    }
//    return name;
//}
// public String getUserDOBFromCard() {
//        String dob = "";
//        try {
//            // Kết nối đến thẻ
//            TerminalFactory factory = TerminalFactory.getDefault();
//            CardTerminals terminals = factory.terminals();
//            if (terminals.list().isEmpty()) {
//                JOptionPane.showMessageDialog(null, "Không tìm thấy trình đọc thẻ.");
//                return null;
//            }
//
//            CardTerminal terminal = terminals.list().get(0);
//            Card card = terminal.connect("T=0");
//            CardChannel channel = card.getBasicChannel();
//
//            // Gửi lệnh APDU để chọn ứng dụng của thẻ và lấy dữ liệu ngày sinh
//            CommandAPDU getDOBCommand = new CommandAPDU(0xA4, 0x17, 0x00, 0x00, new byte[]{}, 0x0F);  // Lệnh tùy chỉnh
//            ResponseAPDU dobResponse = channel.transmit(getDOBCommand);
//
//            if (dobResponse.getSW() == 0x9000) {
//                byte[] data = dobResponse.getData();
//                System.out.println("Dữ liệu nhận được từ thẻ: " + Arrays.toString(data));
//
//                // Giả sử dữ liệu ngày sinh từ thẻ là định dạng YYYY-MM-DD
//                 dob = new String(data, "UTF-8");
//                
//                
//
//                // Kiểm tra và chuyển đổi định dạng ngày sinh nếu cần thiết
//               
//            } else {
//                JOptionPane.showMessageDialog(null, "Không thể đọc ngày sinh từ thẻ.");
//            }
//        } catch (CardException e) {
//            JOptionPane.showMessageDialog(null, "Lỗi khi kết nối với thẻ.");
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            JOptionPane.showMessageDialog(null, "Lỗi giải mã dữ liệu từ thẻ.");
//            e.printStackTrace();
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(null, "Lỗi xử lý ngày sinh.");
//            e.printStackTrace();
//        }
//        return dob;
//    }




    private String getInfoCard() {
        String chuoiByte = "";
        try {
            // Kết nối đến thẻ
            TerminalFactory factory = TerminalFactory.getDefault();
            CardTerminals terminals = factory.terminals();
            if (terminals.list().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy trình đọc thẻ.");

            }
            CardTerminal terminal = terminals.list().get(0);
            Card card = terminal.connect("T=0");
            CardChannel channel = card.getBasicChannel();

            // Gửi lệnh APDU để chọn ứng dụng của thẻ
            CommandAPDU getNameCommand = new CommandAPDU(0xA4, 0x16, 0x00, 0x00);

            ResponseAPDU nameResponse = channel.transmit(getNameCommand);

            if (nameResponse.getSW() == 0x9000) {
                byte[] data = nameResponse.getData();
                System.out.println("Dữ liệu nhận được từ thẻ: " + Arrays.toString(data));
                chuoiByte = new String(data, "UTF-8");
            } else {
                JOptionPane.showMessageDialog(this, "Không thể đọc tên từ thẻ.");
            }
        } catch (CardException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi kết nối với thẻ.");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            JOptionPane.showMessageDialog(this, "Lỗi giải mã dữ liệu từ thẻ.");
            e.printStackTrace();
        }
        return chuoiByte;
    }
    
    
    
private boolean updateUserNameWithExtendedAPDU(String ID , String Name , String Address  , String phone , String Pin ) {
    try {
        // Kết nối đến thẻ
        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals terminals = factory.terminals();
        
        if (terminals.list().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy trình đọc thẻ.");
            return false;
        }
        
        CardTerminal terminal = terminals.list().get(0);
        Card card = terminal.connect("T=0");
        CardChannel channel = card.getBasicChannel();
        
        ID = ID.trim(); 
        Name = Name.trim(); 
        Address = Address.trim(); 
        phone = phone.trim(); 
        Pin = Pin.trim(); 

        // Chuyển dữ liệu UserID sang byte array
        byte[] userIDBytes = ID.getBytes("UTF-8");
        byte[] userNameBytes = Name.getBytes("UTF-8");
        byte[] AddressBytes = Address.getBytes("UTF-8");
        byte[] PhoneBytes = phone.getBytes("UTF-8");
        byte[] PinBytes = Pin.getBytes("UTF-8");
        
        
                // Tính tổng số byte cần thiết
        int totalLength = userIDBytes.length + userNameBytes.length + AddressBytes.length +
                          PhoneBytes.length + PinBytes.length + 4 * 1; // 4 lần 1 byte cho các dấu phân cách 0x03

        // Tạo mảng byte mới đủ chứa tất cả các mảng byte và dấu phân cách
        byte[] combinedBytes = new byte[totalLength];

        // Chèn mảng byte vào và thêm dấu phân cách 0x03
        int currentIndex = 0;

        System.arraycopy(userIDBytes, 0, combinedBytes, currentIndex, userIDBytes.length);
        currentIndex += userIDBytes.length;
        combinedBytes[currentIndex++] = 0x03; // Dấu phân cách

        System.arraycopy(userNameBytes, 0, combinedBytes, currentIndex, userNameBytes.length);
        currentIndex += userNameBytes.length;
        combinedBytes[currentIndex++] = 0x03;

        System.arraycopy(AddressBytes, 0, combinedBytes, currentIndex, AddressBytes.length);
        currentIndex += AddressBytes.length;
        combinedBytes[currentIndex++] = 0x03;

        System.arraycopy(PhoneBytes, 0, combinedBytes, currentIndex, PhoneBytes.length);
        currentIndex += PhoneBytes.length;
        combinedBytes[currentIndex++] = 0x03;
        
        System.arraycopy(PinBytes, 0, combinedBytes, currentIndex, PinBytes.length);
        
        System.out.println("aa");
        
        
        
        
            // Tạo lệnh APDU mở rộng để ghi dữ liệu ngày sinh
        CommandAPDU updateCommand = new CommandAPDU(0xA4, 0x11, 0x00, 0x00, combinedBytes, 0x00);

        // Gửi lệnh đến thẻ
        ResponseAPDU response = channel.transmit(updateCommand);


        
   

        // Kiểm tra mã trạng thái
        if (response.getSW() == 0x9000) {
            JOptionPane.showMessageDialog(null, "Cập nhật User ID thành công.");
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Không thể cập nhật User ID. Mã lỗi: " + Integer.toHexString(response.getSW()));
            return false;
        }
    } catch (CardException e) {
        JOptionPane.showMessageDialog(null, "Lỗi khi kết nối với thẻ.");
        e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
        JOptionPane.showMessageDialog(null, "Lỗi mã hóa dữ liệu.");
        e.printStackTrace();
    }
    return false;
}

    public static String encodeToBase64(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    public void switchTab(String appBarContent, String tabContent) {


        String infoCard =getInfoCard();

         String nameCard ="";
       String idCard = "";
        String addressCard = "";
        String phoneCard= "";
        String pinCard ="";
        
         String[] parts = infoCard.split("\u0003"); // \u0003 là ký tự phân cách 0x03

        // Gán các phần đã tách vào các biến thông tin thẻ
        if (parts.length == 4) {
            idCard = parts[0];
            nameCard = parts[1];      // Họ và tên
            addressCard = parts[2];   // Địa chỉ
            phoneCard = parts[3];     // Số điện thoại
//            pinCard = parts[4];       // Mã PIN
        }else{
        System.out.println("Dữ liệu thẻ không hợp lệ.");
    return; // Dừng phương thức nếu dữ liệu không hợp lệ
        }
        
    // In ra các giá trị sau khi tách chuỗi
    System.out.println("idCard: " + idCard);
    System.out.println("nameCard: " + nameCard);
    System.out.println("addressCard: " + addressCard);
    System.out.println("phoneCard: " + phoneCard);
    System.out.println("pinCard: " + pinCard);

        
        if (rightAppBar != null) {
            rightAppBar.removeAll();
            contentPanel.removeAll();
            switch (appBarContent) {
                case "Thông tin cá nhân":
                    JPanel profilePanel = new JPanel();
                    profilePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
                    profilePanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // FlowLayout để không kéo dài

                    // Ảnh đại diện
                    JPanel avatarPanel = new JPanel();
                    avatarPanel.setPreferredSize(new Dimension(100, 120)); // Kích thước cố định
                    avatarPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Thêm viền đen 2px

                    // Tạo JLabel cho ảnh đại diện
                    JLabel avatarLabel = new JLabel();
                    avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    avatarLabel.setVerticalAlignment(SwingConstants.CENTER);

                    avatarLabel.setText("Ảnh đại diện");
                    avatarLabel.setFont(new Font("Arial", Font.BOLD, 14));

                    // Thêm JLabel vào avatarPanel
                    avatarPanel.setLayout(new BorderLayout());
                    avatarPanel.add(avatarLabel, BorderLayout.CENTER);

                    // Nút "Thay đổi"
                    JButton changeButton = new JButton("Thay đổi");
                    changeButton.setPreferredSize(new Dimension(100, 25));
                    changeButton.setFont(new Font("Arial", Font.PLAIN, 12));

                    // Thêm sự kiện cho nút "Thay đổi"
                    changeButton.addActionListener(e -> {
                        // Mở JFileChooser để chọn ảnh
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
                        int result = fileChooser.showOpenDialog(null);

                        if (result == JFileChooser.APPROVE_OPTION) {
                            File selectedFile = fileChooser.getSelectedFile();
                            try {
                                // Tải ảnh từ file đã chọn
                                ImageIcon avatarIcon = new ImageIcon(selectedFile.getAbsolutePath());
                                Image scaledImage = avatarIcon.getImage().getScaledInstance(100, 120, Image.SCALE_SMOOTH);
                                avatarLabel.setIcon(new ImageIcon(scaledImage));
                                avatarLabel.setText(null); // Xóa văn bản mặc định nếu có ảnh

                                // Chuyển ảnh thành mảng byte
                                byte[] imageBytes = convertImageToBytes(selectedFile);

//                            String base64String = encodeToBase64(imageBytes);
//                            System.out.println("Base64 Encoded Image: " + base64String);
                                // Gửi mảng byte xuống Smart Card (giả sử bạn có phương thức gửi)
                                sendToSmartCard(imageBytes);

                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Không thể tải ảnh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });

                    // Panel chứa cả avatar và nút "Thay đổi"
                    JPanel avatarContainer = new JPanel();
                    avatarContainer.setLayout(new BoxLayout(avatarContainer, BoxLayout.Y_AXIS)); // Sắp xếp theo chiều dọc
                    avatarContainer.add(avatarPanel); // Thêm khung avatar
                    avatarContainer.add(Box.createRigidArea(new Dimension(0, 5))); // Khoảng cách giữa ảnh và nút
                    avatarContainer.add(changeButton); // Thêm nút "Thay đổi"
                    avatarContainer.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa toàn bộ nội dung

                    // Thêm avatarContainer vào profilePanel
                    profilePanel.add(avatarContainer);
                    // Thông tin cá nhân
                    JPanel infoPanel = new JPanel();
                    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                    JLabel idLabel = new JLabel("Mã thẻ: " + idCard);
                    JLabel nameLabel = new JLabel("Họ và tên: " + nameCard);
                    JLabel addressLabel = new JLabel("Địa chỉ: " + addressCard);
                    JLabel phoneLabel = new JLabel("Số điện thoại: " + phoneCard);
                    
                infoPanel.add(idLabel);
                infoPanel.add(nameLabel);
                infoPanel.add(addressLabel);
                infoPanel.add(phoneLabel);

                    //                   Thêm từ đây
                    // Thêm nút "Cập nhật thông tin" vào infoPanel
                    JButton updateButton = new JButton("Cập nhật thông tin");
                    updateButton.setFont(new Font("Arial", Font.PLAIN, 14));
                    updateButton.setPreferredSize(new Dimension(150, 30));
                    updateButton.setBackground(new Color(0, 123, 255));
                    updateButton.setForeground(Color.WHITE);
                    updateButton.setFocusPainted(false); // Loại bỏ viền khi chọn nút

// Gắn sự kiện cho nút
updateButton.addActionListener(e -> {
    // Tạo một JDialog để cập nhật thông tin
    JDialog updateDialog = new JDialog((Frame) null, "Cập nhật thông tin", true);
    updateDialog.setLayout(new GridLayout(6, 2, 10, 10)); // Điều chỉnh số dòng và cột
    updateDialog.setSize(400, 300); // Thay đổi kích thước cho phù hợp

    // Các trường thông tin
    JLabel nameLabelDialog = new JLabel("Họ và tên:");
    JTextField nameField = new JTextField();
    JLabel addressLabelDialog = new JLabel("Địa chỉ:");
    JTextField addressField = new JTextField(); // Trường địa chỉ
    JLabel phoneLabelDialog = new JLabel("Số điện thoại:");
    JTextField phoneField = new JTextField(); // Trường số điện thoại
    JLabel pinLabelDialog = new JLabel("Mã PIN:");
    JTextField pinField = new JTextField(); // Trường mã PIN
    JLabel idLabelDialog = new JLabel("ID thẻ:");
    JTextField idField = new JTextField(); // Trường ID thẻ

    // Đặt khoảng cách cho các nhãn
    nameLabelDialog.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
    addressLabelDialog.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
    phoneLabelDialog.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
    pinLabelDialog.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
    idLabelDialog.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));

    // Nút lưu
    JButton saveButton = new JButton("Lưu");
    saveButton.addActionListener(saveEvent -> {
        // Lấy dữ liệu từ các trường và cập nhật thông tin
        String updatedName = nameField.getText();
        String updatedAddress = addressField.getText();
        String updatedPhone = phoneField.getText();
        String updatedPin = pinField.getText();
        String updatedId = idField.getText();
        
       Boolean isUpdate = updateUserNameWithExtendedAPDU(updatedId,updatedName,updatedAddress,updatedPhone,updatedPin);

        // Cập nhật các JLabel trong infoPanel
        nameLabel.setText("Họ và tên: " + updatedName);
        addressLabel.setText("Địa chỉ: " + updatedAddress);
        phoneLabel.setText("Số điện thoại: " + updatedPhone);
        pinField.setText("Mã PIN: " + updatedPin);
        idLabel.setText("ID thẻ: " + updatedId);

        // Đóng dialog
        updateDialog.dispose();
    });

    // Thêm các thành phần vào dialog
    updateDialog.add(nameLabelDialog);
    updateDialog.add(nameField);
    updateDialog.add(addressLabelDialog);
    updateDialog.add(addressField);
    updateDialog.add(phoneLabelDialog);
    updateDialog.add(phoneField);
    updateDialog.add(pinLabelDialog);
    updateDialog.add(pinField);
    updateDialog.add(idLabelDialog);
    updateDialog.add(idField);
    updateDialog.add(new JLabel()); // Placeholder cho vị trí trống
    updateDialog.add(saveButton);

    // Hiển thị dialog
    updateDialog.setLocationRelativeTo(null); // Đặt vị trí cửa sổ ở giữa màn hình
    updateDialog.setVisible(true);
});

// Thêm nút vào infoPanel
                    infoPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Khoảng cách
                    infoPanel.add(updateButton);

//                    Kết thúc
                    infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
                    infoPanel.add(Box.createRigidArea(new Dimension(0, 0))); // Khoảng cách trên
                    infoPanel.add(idLabel);
                    infoPanel.add(Box.createRigidArea(new Dimension(0, 0))); // Khoảng cách trên
                    infoPanel.add(nameLabel);
                    infoPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách giữa các dòng
                    infoPanel.add(addressLabel);
                    infoPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách giữa các dòng
                    infoPanel.add(phoneLabel);
                    infoPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách giữa các dòng
                    infoPanel.add(idLabel);

//                    Bảng mượn trả
                    JPanel titlePanelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JLabel tableTitleInfo = new JLabel("Lịch sử mượn/trả sách", SwingConstants.LEFT);
                    tableTitleInfo.setFont(new Font("Arial", Font.BOLD, 16));
                    tableTitleInfo.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
                    titlePanelInfo.add(tableTitleInfo);

                    String[] columnNamesInfo = {"Tên sách", "Ngày mượn", "Ngày trả", "Trạng thái"};
                    Object[][] dataInfo = {
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},};

// Cài đặt cho JTable
                    DefaultTableCellRenderer centerRendererInfo = new DefaultTableCellRenderer();
                    centerRendererInfo.setHorizontalAlignment(SwingConstants.CENTER);
                    JTable tableInfo = new JTable(dataInfo, columnNamesInfo);
                    JTableHeader headerInfo = tableInfo.getTableHeader();
                    headerInfo.setPreferredSize(new Dimension(0, 36));
                    headerInfo.setDefaultRenderer(new CustomHeaderRenderer());

                    for (int i = 0; i < tableInfo.getColumnCount(); i++) {
                        tableInfo.getColumnModel().getColumn(i).setCellRenderer(centerRendererInfo);
                    }
                    tableInfo.setFont(new Font("Arial", Font.PLAIN, 14));
                    tableInfo.setRowHeight(36);
                    tableInfo.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

                    JScrollPane scrollPaneInfo = new JScrollPane(tableInfo);
                    scrollPaneInfo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
// Cập nhật lại nội dung của contentPanel
//                    Bảng mượn trả

                    // Bố trí layout tổng
                    profilePanel.add(avatarPanel); // Ảnh đại diện
                    profilePanel.add(infoPanel);   // Thông tin
                    JPanel buttonPanel = new JPanel();
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Căn giữa và khoảng cách
                    buttonPanel.setLayout(new FlowLayout());
// Tạo 2 nút
                    JButton button1 = new JButton("Chỉnh sửa");
                    JButton button2 = new JButton("Gia hạn thẻ");
                    button1.setBackground(new Color(0, 28, 68));
                    button2.setBackground(new Color(0, 28, 68));
                    button1.setMargin(new Insets(10, 20, 10, 20));  // Adds padding inside the button
                    button2.setMargin(new Insets(10, 20, 10, 20));  // Adds padding inside the button

                    button1.setForeground(Color.WHITE);
                    button2.setForeground(Color.WHITE);
//                    button1.setBorder(new RoundedBorder(20));
//                    button2.setBorder(new RoundedBorder(20));

// Đặt kích thước cho các nút (tùy chọn)
                    button1.setPreferredSize(new Dimension(150, 40));
                    button2.setPreferredSize(new Dimension(150, 40));
// Thêm nút vào buttonPanel
                    buttonPanel.add(button1);
                    buttonPanel.add(button2);

                    contentPanel.removeAll();
                    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                    contentPanel.add(profilePanel, BorderLayout.NORTH);

                    contentPanel.add(titlePanelInfo);
                    contentPanel.add(scrollPaneInfo);
                    contentPanel.add(buttonPanel);
                    contentPanel.revalidate();
                    contentPanel.repaint();

                    break;

//                    Trang chủ
                case "Trang chủ":
                    // Tạo bảng với 4 cột và dữ liệu mẫu
                    JPanel dashboardPanel = new JPanel(new GridLayout(1, 4, 30, 10)); // Sử dụng GridLayout để chia đều 4 ô
                    dashboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10)); // Thêm khoảng cách ngoài
//                    const
// Khai báo các JLabel cho các ô
                    JLabel box1 = new JLabel("<html><div style='text-align: center;display:inline-block'>"
                            + "<p style='vertical-align: middle;margin-bottom:4px'>"
                            + "<img src='https://icons.iconarchive.com/icons/microsoft/fluentui-emoji-3d/256/Books-3d-icon.png' width='50' height='50' /></p>"
                            + "<span style='display: inline-block; vertical-align: middle;'>"
                            + "Tổng số sách đang mượn<br>> 10</span></div></html>");

                    JLabel box2 = new JLabel("<html><div style='text-align: center;'>"
                            + "<p style='vertical-align: middle;margin-bottom:4px'>"
                            + "<img src='https://icons.iconarchive.com/icons/custom-icon-design/flatastic-4/256/Wallet-icon.png' width='50' height='50' /></p>"
                            + "<span style='display: inline-block; vertical-align: middle;'>"
                            + "Số dư phí<br>></span></div></html>");

                    JLabel box3 = new JLabel("<html><div style='text-align: center;'>"
                            + "<p style='vertical-align: middle;margin-bottom:4px'>"
                            + "<img src='https://icons.iconarchive.com/icons/oxygen-icons.org/oxygen/128/Status-media-playlist-repeat-icon.png' width='50' height='50' /></p>"
                            + "<span style='display: inline-block; vertical-align: middle;'>"
                            + "Trạng thái thẻ<br>></span></div></html>");

                    JLabel box4 = new JLabel("<html><div style='text-align: center;'>"
                            + "<p style='vertical-align: middle;margin-bottom:4px'>"
                            + "<img src='https://icons.iconarchive.com/icons/graphicloads/long-shadow-documents/256/document-tick-icon.png' width='50' height='50' /></p>"
                            + "<span style='display: inline-block; vertical-align: middle;'>"
                            + "Tài liệu khả dụng<br></span></div></html>");

// Định dạng từng ô
                    JLabel[] boxes = {box1, box2, box3, box4};
                    for (JLabel box : boxes) {
                        box.setHorizontalAlignment(SwingConstants.CENTER);
                        box.setVerticalAlignment(SwingConstants.CENTER);
                        box.setFont(new Font("Arial", Font.BOLD, 20));
                        box.setOpaque(true);
                        box.setBackground(new Color(240, 240, 240)); // Màu nền
                        box.setBorder(BorderFactory.createRaisedSoftBevelBorder()); // Viền
                        dashboardPanel.add(box);
                        box.setPreferredSize(new Dimension(230, 150));
                    }
                    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JLabel tableTitle = new JLabel("Danh sách mượn trả gần đây", SwingConstants.LEFT);
                    tableTitle.setFont(new Font("Arial", Font.BOLD, 16));
                    tableTitle.setOpaque(true);
                    tableTitle.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
                    titlePanel.add(tableTitle);

                    String[] columnNames = {"Tên sách", "Ngày mượn", "Ngày trả", "Trạng thái"};
                    Object[][] data = {
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},};

// Cài đặt cho JTable
                    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                    centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
                    JTable table = new JTable(data, columnNames);
                    JTableHeader header = table.getTableHeader();
                    header.setPreferredSize(new Dimension(0, 36));
                    header.setDefaultRenderer(new CustomHeaderRenderer());

                    for (int i = 0; i < table.getColumnCount(); i++) {
                        table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                    }
                    table.setFont(new Font("Arial", Font.PLAIN, 14));
                    table.setRowHeight(36);
                    table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

// Đặt bảng vào JScrollPane để hỗ trợ cuộn
//                    JScrollPane scrollPane = new JScrollPane(table);
//                    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                    JScrollPane scrollPane = new JScrollPane(table);
                    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
// Cập nhật lại nội dung của contentPanel
                    contentPanel.removeAll();
                    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // Layout theo chiều dọc
                    contentPanel.add(dashboardPanel); // Thêm dashboardPanel ở phía trên
                    contentPanel.add(titlePanel); // Thêm tiêu đề bảng
                    contentPanel.add(scrollPane); // Thêm bảng vào phần giữa

                    contentPanel.revalidate();
                    contentPanel.repaint();

                    break;
//                    Trang chủ

//                Mượn trả sách
                case "Mượn trả sách":
                    // Dashboard Panel with 1 box
                    JPanel dashboardPanelBorrow = new JPanel();
                    dashboardPanelBorrow.setLayout(new FlowLayout(FlowLayout.LEFT)); // Thay GridLayout bằng FlowLayout
                    dashboardPanelBorrow.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
                    dashboardPanelBorrow.setPreferredSize(new Dimension(200, 100)); // Đặt kích thước cố định cho dashboardPanelBorrow

                    // Define a box for total borrowed books
                    JLabel boxBorrow = new JLabel("<html><div style='text-align: center;'>"
                            + "<img src='https://icons.iconarchive.com/icons/icons8/windows-8/128/Ecommerce-Qr-Code-icon.png' width='50' height='50' /><br>"
                            + "<span>Quét mã</span></div></html>");
                    boxBorrow.setHorizontalAlignment(SwingConstants.CENTER);
                    boxBorrow.setFont(new Font("Arial", Font.BOLD, 16));
                    boxBorrow.setBackground(new Color(240, 240, 240));
                    boxBorrow.setBorder(BorderFactory.createRaisedSoftBevelBorder());
                    dashboardPanelBorrow.add(boxBorrow);

                    // Set fixed dimensions for boxBorrow
                    boxBorrow.setPreferredSize(new Dimension(200, 100)); // Đặt kích thước cố định cho box

                    // Title Panel for Borrow Table
                    JPanel titlePanelBorrow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    titlePanelBorrow.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));
                    JLabel tableTitleBorrow = new JLabel("Danh sách đang mượn");
                    tableTitleBorrow.setFont(new Font("Arial", Font.BOLD, 16));
                    titlePanelBorrow.add(tableTitleBorrow);

                    // Table for Borrowed Books
                    String[] columnNamesBorrow = {"Tên sách", "Ngày mượn", "Ngày trả", "Trạng thái"};
                    Object[][] dataBorrow = {
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""},
                        {"", "", "", ""}
                    };

                    DefaultTableCellRenderer centerRendererBorrow = new DefaultTableCellRenderer();
                    centerRendererBorrow.setHorizontalAlignment(SwingConstants.CENTER);

                    JTable tableBorrow = new JTable(dataBorrow, columnNamesBorrow);
                    JTableHeader headerBorrow = tableBorrow.getTableHeader();
                    headerBorrow.setPreferredSize(new Dimension(0, 36));
                    headerBorrow.setFont(new Font("Arial", Font.BOLD, 14));
                    headerBorrow.setDefaultRenderer(new CustomHeaderRenderer());

                    // Center align the cells
                    for (int i = 0; i < tableBorrow.getColumnCount(); i++) {
                        tableBorrow.getColumnModel().getColumn(i).setCellRenderer(centerRendererBorrow);
                    }
                    tableBorrow.setFont(new Font("Arial", Font.PLAIN, 14));
                    tableBorrow.setRowHeight(36);

                    // Scroll Pane for Table
                    JScrollPane scrollPaneBorrow = new JScrollPane(tableBorrow);
                    scrollPaneBorrow.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

                    // Button Panel for Actions
                    JPanel buttonPanelBorrow = new JPanel();
                    buttonPanelBorrow.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

                    // Create Buttons
                    JButton buttonEdit = new JButton("Mượn sách");
                    JButton buttonExtend = new JButton("Trả sách");

                    // Set size and style for buttons
                    buttonEdit.setPreferredSize(new Dimension(150, 40));
                    buttonEdit.setBackground(new Color(0, 28, 68));
                    buttonExtend.setPreferredSize(new Dimension(150, 40));
                    buttonEdit.setBackground(new Color(0, 28, 68));
                    buttonExtend.setBackground(new Color(0, 28, 68));
                    buttonEdit.setForeground(Color.WHITE); // Màu chữ trắng
                    buttonExtend.setForeground(Color.WHITE); // Màu chữ xanh

                    // Add buttons to the button panel
                    buttonPanelBorrow.add(buttonEdit);
                    buttonPanelBorrow.add(buttonExtend);

                    // Update Content Panel
                    contentPanel.removeAll();
                    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                    contentPanel.add(dashboardPanelBorrow);
                    contentPanel.add(titlePanelBorrow);
                    contentPanel.add(scrollPaneBorrow);
                    contentPanel.add(buttonPanelBorrow);

                    contentPanel.revalidate();
                    contentPanel.repaint();
                    break;

//                Mượn trả sách
//                    Thanh toán
                case "Thanh toán":
                    // Title Panel for Payment Table
                    JPanel titlePanelPayment = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JLabel tableTitlePayment = new JLabel("Thông tin thanh toán");
                    tableTitlePayment.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));
                    tableTitlePayment.setFont(new Font("Arial", Font.BOLD, 16));
                    titlePanelPayment.add(tableTitlePayment);

                    // Table for Payment Information
                    String[] columnNamesPayment = {"Số tiền cần thanh toán", "Ngày hết hạn thanh toán", "Trạng thái thanh toán"};
                    Object[][] dataPayment = {
                        {"500,000 VND", "2024-12-31", "Chưa thanh toán"},
                        {"200,000 VND", "2024-12-20", "Đã thanh toán"},
                        {"150,000 VND", "2024-12-15", "Chưa thanh toán"},
                        {"300,000 VND", "2024-12-10", "Đã thanh toán"},
                        {"400,000 VND", "2024-12-05", "Chưa thanh toán"},
                        {"250,000 VND", "2024-11-30", "Đã thanh toán"}
                    };

                    DefaultTableCellRenderer centerRendererPayment = new DefaultTableCellRenderer();
                    centerRendererPayment.setHorizontalAlignment(SwingConstants.CENTER);

                    JTable tablePayment = new JTable(dataPayment, columnNamesPayment);
                    JTableHeader headerPayment = tablePayment.getTableHeader();
                    headerPayment.setPreferredSize(new Dimension(0, 36));
                    headerPayment.setFont(new Font("Arial", Font.BOLD, 14));
                    headerPayment.setDefaultRenderer(new CustomHeaderRenderer());

                    // Center align the cells
                    for (int i = 0; i < tablePayment.getColumnCount(); i++) {
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

                    // Set size and style for buttons
                    buttonPayNow.setPreferredSize(new Dimension(150, 40));
                    buttonPayNow.setBackground(new Color(0, 123, 255));
                    buttonPayNow.setForeground(Color.WHITE);

                    // Add buttons to the button panel
                    buttonPanelPayment.add(buttonPayNow);

                    // Update Content Panel
                    contentPanel.removeAll();
                    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                    contentPanel.add(titlePanelPayment);
                    contentPanel.add(scrollPanePayment);
                    contentPanel.add(buttonPanelPayment);

                    contentPanel.revalidate();
                    contentPanel.repaint();
                    break;

//                    Thanh toán
//                    Lịch sử hoạt động\
                case "Lịch sử hoạt động":
                    // Title Panel for Activity History Table
                    JPanel titlePanelActivity = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JLabel tableTitleActivity = new JLabel("Lịch sử hoạt động");
                    tableTitleActivity.setFont(new Font("Arial", Font.BOLD, 16));
                    titlePanelActivity.add(tableTitleActivity);
                    tableTitleActivity.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));
                    // Table for Activity History
                    String[] columnNamesActivity = {"Ngày", "Hành động", "Kết quả"};
                    Object[][] dataActivity = {
                        {"2024-12-01", "Đăng nhập", "Thành công"},
                        {"2024-12-02", "Đăng xuất", "Thành công"},
                        {"2024-12-03", "Thay đổi mật khẩu", "Thành công"},
                        {"2024-12-04", "Cập nhật thông tin", "Thành công"},
                        {"2024-12-05", "Xóa tài khoản", "Thất bại"},
                        {"2024-12-06", "Tạo tài khoản mới", "Thành công"}
                    };

                    DefaultTableCellRenderer centerRendererActivity = new DefaultTableCellRenderer();
                    centerRendererActivity.setHorizontalAlignment(SwingConstants.CENTER);

                    JTable tableActivity = new JTable(dataActivity, columnNamesActivity);
                    JTableHeader headerActivity = tableActivity.getTableHeader();
                    headerActivity.setPreferredSize(new Dimension(0, 36));
                    headerActivity.setFont(new Font("Arial", Font.BOLD, 14));
                    headerActivity.setDefaultRenderer(new CustomHeaderRenderer());

                    // Center align the cells
                    for (int i = 0; i < tableActivity.getColumnCount(); i++) {
                        tableActivity.getColumnModel().getColumn(i).setCellRenderer(centerRendererActivity);
                    }
                    tableActivity.setFont(new Font("Arial", Font.PLAIN, 14));
                    tableActivity.setRowHeight(36);

                    // Scroll Pane for Table
                    JScrollPane scrollPaneActivity = new JScrollPane(tableActivity);
                    scrollPaneActivity.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

                    // Button Panel for Actions
                    JPanel buttonPanelActivity = new JPanel();
                    buttonPanelActivity.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

                    // Create Buttons
                    JButton buttonClearHistory = new JButton("Tải xuống");

                    // Set size and style for buttons
                    buttonClearHistory.setPreferredSize(new Dimension(150, 40));
                    buttonClearHistory.setBackground(new Color(220, 53, 69));
                    buttonClearHistory.setForeground(Color.WHITE);

                    // Add buttons to the button panel
                    buttonPanelActivity.add(buttonClearHistory);

                    // Update Content Panel
                    contentPanel.removeAll();
                    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                    contentPanel.add(titlePanelActivity);
                    contentPanel.add(scrollPaneActivity);
                    contentPanel.add(buttonPanelActivity);

                    contentPanel.revalidate();
                    contentPanel.repaint();
                    break;

//Lịch sử hoạt động
                default:
                    // Giao diện mặc định cho các tab khác
                    JLabel newContent = new JLabel(tabContent);
                    newContent.setHorizontalAlignment(SwingConstants.CENTER);
                    newContent.setFont(new Font("Arial", Font.BOLD, 20));
                    contentPanel.add(newContent, BorderLayout.CENTER);
                    break;
            }

            rightAppBar.revalidate();
            rightAppBar.repaint();
            contentPanel.revalidate();
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainPage::new);
    }
}
