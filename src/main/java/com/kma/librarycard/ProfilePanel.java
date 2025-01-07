package com.kma.librarycard;

import com.kma.librarycard.MainPage;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class ProfilePanel extends JPanel {

    private final JPanel contentPanel;
    private final String nameCard;
    private final String addressCard;
    private final String phoneCard;
    private String validatedOtp;
    private final MainPage mainPage;
    public String card_id = "";
    public String publicKey = "";
    private JLabel avatarLabel;

    public ProfilePanel(JPanel contentPanel, String idCard, String nameCard, String addressCard, String phoneCard, String validatedOtp, Double balance, MainPage mainPage) {
        this.contentPanel = contentPanel;
        this.nameCard = nameCard;
        this.addressCard = addressCard;
        this.phoneCard = phoneCard;
        this.validatedOtp = validatedOtp;
        this.mainPage = mainPage;

        // Retrieve card ID and public key during initialization
        try {
            byte[] cardIdBytes = getCardID();
            if (cardIdBytes != null) {
                this.card_id = new String(cardIdBytes);
            } else {
                this.card_id = "N/A";
            }
            byte[] publicKeyBytes = getPublicKey();
            if (publicKeyBytes != null) {
                this.publicKey = new String(publicKeyBytes);
            } else {
                this.publicKey = "N/A";
            }
        } catch (CardException ex) {
            this.card_id = "N/A";
            this.publicKey = "N/A";
            Logger.getLogger(ProfilePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static byte[] getCardID() throws CardException {
        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals terminals = factory.terminals();
        if (terminals.list().isEmpty()) {
            System.out.println("Không tìm thấy trình đọc thẻ.");
            return null;
        }

        CardTerminal terminal = terminals.list().get(0);
        Card card = terminal.connect("T=1");
        CardChannel channel = card.getBasicChannel();

        CommandAPDU getCardIdCommand = new CommandAPDU(0xA4, 0x1D, 0x00, 0x00);
        ResponseAPDU response = channel.transmit(getCardIdCommand);
        if (response.getSW() == 0x9000) {
            System.out.println("card_id:" + Arrays.toString(response.getData()));
            return response.getData();
        } else {
            System.out.println("Không lấy được Card ID từ thẻ.");
            return null;
        }
    }

    private static byte[] getPublicKey() throws CardException {
        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals terminals = factory.terminals();
        if (terminals.list().isEmpty()) {
            System.out.println("Không tìm thấy trình đọc thẻ.");
            return null;
        }

        CardTerminal terminal = terminals.list().get(0);
        Card card = terminal.connect("T=1");
        CardChannel channel = card.getBasicChannel();

        CommandAPDU getPubKeyCommand = new CommandAPDU(0xA4, 0x1A, 0x01, 0x01);
        ResponseAPDU response = channel.transmit(getPubKeyCommand);

        if (response.getSW() == 0x9000) {
            System.out.println("publicKey:" + Arrays.toString(response.getData()));
            return response.getData();
        } else {
            System.out.println("Không lấy được Public Key từ thẻ.");
            return null;
        }
    }

    public void changeImage(byte[] image) {
        int length = image.length;
        System.out.println(length);
        int pointer = 0;
        boolean first = true;

        while (pointer < length) {
            int size = Math.min(length - pointer, 128);
            byte[] buf = new byte[size];
            System.arraycopy(image, pointer, buf, 0, size);

            try {
                TerminalFactory factory = TerminalFactory.getDefault();
                CardTerminal terminal = factory.terminals().list().get(0);
                Card card = terminal.connect("T=1");
                CardChannel channel = card.getBasicChannel();

                int p2 = pointer + size < length ? (first ? 0x00 : 0x01) : 0x02;
                CommandAPDU command = new CommandAPDU(0xA4, 0x18, 0x00, p2, buf);
                ResponseAPDU response = channel.transmit(command);

                if (response.getSW() == 0x9000) {
                    System.out.println("Gửi dữ liệu thành công!");
                } else {
                    System.err.println("Lỗi khi gửi dữ liệu: " + Integer.toHexString(response.getSW()));
                    break;
                }
            } catch (Exception e) {
                break;
            }

            first = false;
            pointer += size;
        }
    }

    public byte[] getImage() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int pointer = 0;
        boolean first = true;
        byte[] data = new byte[0];

        try {
            // Kết nối với thẻ
            TerminalFactory factory = TerminalFactory.getDefault();
            CardTerminal terminal = factory.terminals().list().get(0);
            Card card = terminal.connect("T=1");
            CardChannel channel = card.getBasicChannel();

            // Lấy dữ liệu từ thẻ cho đến khi không còn dữ liệu
            while (true) {
                // Xác định P2 và kích thước
                int p2 = first ? 0x00 : 0x01; // P2 = 0x00 cho gói đầu tiên, P2 = 0x01 cho các gói tiếp theo
                CommandAPDU command = new CommandAPDU(0xA4, 0x19, 0x00, p2);
                ResponseAPDU response = channel.transmit(command);

                // Kiểm tra phản hồi
                if (response.getSW() == 0x9000) {
                    byte[] responseData = response.getData();
                    if (responseData.length == 0) {
                        break; // Dừng khi không còn dữ liệu
                    }

                    // Ghi dữ liệu nhận được vào outputStream
                    outputStream.write(responseData);

                    // Cập nhật biến first
                    first = false;
                } else {
                    System.err.println("Lỗi khi nhận dữ liệu: " + Integer.toHexString(response.getSW()));
                    break; // Dừng khi gặp lỗi
                }
            }

            // Chuyển dữ liệu đã nhận vào mảng byte
            data = outputStream.toByteArray();

        } catch (Exception e) {
        }

        return data;
    }

    private boolean updateUserNameWithExtendedAPDU(String Name, String Address, String phone, String Pin) throws UnsupportedEncodingException {
        try {
            // Kết nối đến thẻ
            TerminalFactory factory = TerminalFactory.getDefault();
            CardTerminals terminals = factory.terminals();

            if (terminals.list().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy trình đọc thẻ.");
                return false;
            }

            CardTerminal terminal = terminals.list().get(0);
            Card card = terminal.connect("T=1");
            CardChannel channel = card.getBasicChannel();

            Name = Name.trim();
            Address = Address.trim();
            phone = phone.trim();
            Pin = Pin.trim();

            // Chuyển dữ liệu UserID sang byte array
            byte[] userNameBytes = Name.getBytes("UTF-8");
            byte[] AddressBytes = Address.getBytes("UTF-8");
            byte[] PhoneBytes = phone.getBytes("UTF-8");
            byte[] PinBytes = Pin.getBytes("UTF-8");

            // Tính tổng số byte cần thiết
            int totalLength = userNameBytes.length + AddressBytes.length
                    + PhoneBytes.length + PinBytes.length + 4 * 1; // 4 lần 1 byte cho các dấu phân cách 0x03

            // Tạo mảng byte mới đủ chứa tất cả các mảng byte và dấu phân cách
            byte[] combinedBytes = new byte[totalLength];

            // Chèn mảng byte vào và thêm dấu phân cách 0x03
            int currentIndex = 0;
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
                JOptionPane.showMessageDialog(null, "Cập nhật thông tin người dùng thành công.");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Không thể cập nhật User ID. Mã lỗi: " + Integer.toHexString(response.getSW()));
                return false;
            }
        } catch (CardException e) {
            JOptionPane.showMessageDialog(null, "Lỗi khi kết nối với thẻ.");
        } catch (UnsupportedEncodingException e) {
            JOptionPane.showMessageDialog(null, "Lỗi mã hóa dữ liệu.");
        }
        return false;
    }

    private ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
        Image image = icon.getImage();
        double originalWidth = image.getWidth(null);
        double originalHeight = image.getHeight(null);

        double widthRatio = (double) width / originalWidth;
        double heightRatio = (double) height / originalHeight;

        double scaleRatio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * scaleRatio);
        int newHeight = (int) (originalHeight * scaleRatio);
        Image resizedImage = image.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    private void updateAvatarImage() {
        byte[] infoIMGbyte = getImage();
        if (infoIMGbyte.length > 0) {
            try {
                ImageIcon icon = new ImageIcon(infoIMGbyte);
                ImageIcon resizedIcon = resizeImageIcon(icon, avatarLabel.getWidth() - 10, avatarLabel.getHeight() - 10);
                if (resizedIcon != null) {
                    avatarLabel.setIcon(resizedIcon);
                    avatarLabel.setText("");
                } else {
                    avatarLabel.setText("Ảnh đại diện");
                }

            } catch (Exception ex) {
                avatarLabel.setText("Ảnh đại diện");
            }
        } else {
            avatarLabel.setText("Ảnh đại diện");
        }
    }

    @Override
    public void show() {

        JPanel profilePanel = new JPanel();
        profilePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        profilePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Avatar
        JPanel avatarContainer = new JPanel();
        avatarContainer.setLayout(new BoxLayout(avatarContainer, BoxLayout.Y_AXIS));

        JPanel avatarPanel = new JPanel();
        avatarPanel.setPreferredSize(new Dimension(100, 120));
        avatarPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Load image from card on startup
        updateAvatarImage();

        avatarPanel.setLayout(new BorderLayout());
        avatarPanel.add(avatarLabel, BorderLayout.CENTER);

        JButton changeButton = new JButton("Thay đổi");
        changeButton.setPreferredSize(new Dimension(120, 30));
        changeButton.setBackground(new Color(0, 28, 68));
        changeButton.setMargin(new Insets(10, 20, 10, 20));
        changeButton.setForeground(Color.WHITE);

        changeButton.setFont(new Font("Arial", Font.PLAIN, 12));

        changeButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                BufferedImage bimage;
                try {
                    bimage = ImageIO.read(selectedFile);
                    if (bimage == null) {
                        JOptionPane.showMessageDialog(null, "Không thể đọc ảnh từ tệp được chọn. Vui lòng kiểm tra tệp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ByteArrayOutputStream _imageBytes = new ByteArrayOutputStream();
                    boolean success = ImageIO.write(bimage, "jpg", _imageBytes);
                    if (!success) {
                        JOptionPane.showMessageDialog(null, "Không thể chuyển đổi ảnh thành định dạng JPG.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    byte[] img = _imageBytes.toByteArray();
                    System.out.println("Kích thước mảng byte: " + img.length);

                    if (img.length == 0) {
                        JOptionPane.showMessageDialog(null, "Ảnh trống hoặc không thể đọc nội dung!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (img.length >= 32767) {
                        JOptionPane.showMessageDialog(this, "Ảnh bạn chọn lớn hơn kích thước tối đa (32Kb), có thể xảy ra lỗi!\nHãy chọn ảnh khác!", "Warning!!!", JOptionPane.WARNING_MESSAGE);
                    } else {
                        ImageIcon originalIcon = new ImageIcon(bimage);
                        ImageIcon resizedIcon = resizeImageIcon(originalIcon, avatarPanel.getWidth() - 10, avatarPanel.getHeight() - 10);
                        if (resizedIcon != null) {
                            avatarLabel.setIcon(resizedIcon);
                            avatarLabel.setText("");
                        }
                        changeImage(img);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Không thể tải ảnh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        avatarContainer.add(avatarPanel);
        avatarContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        avatarContainer.add(changeButton);
        avatarContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Information
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        GridBagConstraints infoGbc = new GridBagConstraints();
        infoGbc.insets = new Insets(5, 10, 5, 10);
        infoGbc.anchor = GridBagConstraints.WEST;

        JLabel cardIDLabel = new JLabel("ID: " + card_id);
        cardIDLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoGbc.gridx = 0;
        infoGbc.gridy = 0;
        infoPanel.add(cardIDLabel, infoGbc);

        JLabel nameLabel = new JLabel("Họ và tên: " + nameCard);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoGbc.gridy = 1;
        infoPanel.add(nameLabel, infoGbc);

        JLabel addressLabel = new JLabel("Địa chỉ: " + addressCard);
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoGbc.gridy = 2;
        infoPanel.add(addressLabel, infoGbc);

        JLabel phoneLabel = new JLabel("Số điện thoại: " + phoneCard);
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoGbc.gridy = 3;
        infoPanel.add(phoneLabel, infoGbc);

        JButton updateButton = new JButton("Cập nhật thông tin");
        updateButton.setPreferredSize(new Dimension(170, 30));
        updateButton.setBackground(new Color(0, 28, 68));
        updateButton.setMargin(new Insets(10, 20, 10, 20));
        updateButton.setForeground(Color.WHITE);
        updateButton.setFont(new Font("Arial", Font.PLAIN, 14));
        infoGbc.gridy = 4;
        infoGbc.gridx = 0;
        infoGbc.gridwidth = 2;
        infoGbc.anchor = GridBagConstraints.CENTER;
        infoPanel.add(updateButton, infoGbc);
        updateButton.addActionListener(e -> {
            
            
//             byte[] infoIMGbyte = getImage();
//    if (infoIMGbyte.length > 0) {
//        try {
//            System.out.println(infoIMGbyte.length);
//            // Chuyển mảng byte thành ImageIcon
//            ImageIcon icon = new ImageIcon(infoIMGbyte);
//
//            // Đặt ảnh cho JLabel
//            avatarLabel.setIcon(icon);
//        } catch (Exception ex) {  // Sử dụng 'ex' thay vì 'e' trong catch block
//            // Xử lý lỗi nếu không thể đọc ảnh
//            ex.printStackTrace();
//        }
//    }
//            
            
            
            JDialog updateDialog = new JDialog((Frame) null, "Cập nhật thông tin", true);
            updateDialog.setLayout(new GridBagLayout());
            GridBagConstraints dialogGbc = new GridBagConstraints();
            dialogGbc.insets = new Insets(5, 10, 5, 10);
            dialogGbc.anchor = GridBagConstraints.WEST;

            updateDialog.setSize(420, 300);
            Font labelFont = new Font("Arial", Font.PLAIN, 14);
            Font textFont = new Font("Arial", Font.PLAIN, 14);

            JLabel nameLabelDialog = new JLabel("Họ và tên:");
            nameLabelDialog.setFont(labelFont);
            JTextField nameField = new JTextField();
            nameField.setPreferredSize(new Dimension(150, 30));
            nameField.setFont(textFont);
            nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            JLabel addressLabelDialog = new JLabel("Địa chỉ:");
            addressLabelDialog.setFont(labelFont);
            JTextField addressField = new JTextField();
            addressField.setFont(textFont);
            addressField.setPreferredSize(new Dimension(150, 30));
            addressField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            JLabel phoneLabelDialog = new JLabel("Số điện thoại:");
            phoneLabelDialog.setFont(labelFont);
            JTextField phoneField = new JTextField();
            phoneField.setPreferredSize(new Dimension(150, 30));

            phoneField.setFont(textFont);
            phoneField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            JLabel pinLabelDialog = new JLabel("Mã PIN:");
            pinLabelDialog.setFont(labelFont);
            JPasswordField pinField = new JPasswordField();
            pinField.setPreferredSize(new Dimension(150, 30));

            pinField.setFont(textFont);
            pinField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            dialogGbc.gridx = 0;
            dialogGbc.gridy = 0;
            updateDialog.add(nameLabelDialog, dialogGbc);
            dialogGbc.gridx = 1;
            updateDialog.add(nameField, dialogGbc);
            dialogGbc.gridx = 0;
            dialogGbc.gridy = 1;
            updateDialog.add(addressLabelDialog, dialogGbc);
            dialogGbc.gridx = 1;
            updateDialog.add(addressField, dialogGbc);
            dialogGbc.gridx = 0;
            dialogGbc.gridy = 2;
            updateDialog.add(phoneLabelDialog, dialogGbc);
            dialogGbc.gridx = 1;
            updateDialog.add(phoneField, dialogGbc);
            dialogGbc.gridx = 0;
            dialogGbc.gridy = 3;
            updateDialog.add(pinLabelDialog, dialogGbc);
            dialogGbc.gridx = 1;
            updateDialog.add(pinField, dialogGbc);

            JButton saveButton = new JButton("Lưu");
            saveButton.setBackground(new Color(0, 28, 68));
            saveButton.setForeground(Color.WHITE);
            saveButton.setFont(new Font("Arial", Font.BOLD, 14));
            saveButton.setFocusPainted(false);

            saveButton.setPreferredSize(new Dimension(150, 40));
            saveButton.setBorder(BorderFactory.createEmptyBorder());
            dialogGbc.gridy = 4;
            dialogGbc.gridx = 0;
            dialogGbc.gridwidth = 2;
            dialogGbc.anchor = GridBagConstraints.CENTER;
            updateDialog.add(saveButton, dialogGbc);

            saveButton.addActionListener(saveEvent -> {
                // Lấy dữ liệu từ các trường và cập nhật thông tin
                String updatedName = nameField.getText();
                String updatedAddress = addressField.getText();
                String updatedPhone = phoneField.getText();
                String updatedPin = pinField.getText();
                if (!updatedPhone.matches("\\d{10}")) {
                    JOptionPane.showMessageDialog(updateDialog, "Số điện thoại phải có đúng 10 chữ số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String correctOtp = validatedOtp; // Đây là mã OTP giả định, thay bằng mã thực tế của bạn
                if (!updatedPin.equals(correctOtp)) {
                    JOptionPane.showMessageDialog(updateDialog, "Mã pin không chính xác.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Boolean isUpdate = updateUserNameWithExtendedAPDU(updatedName, updatedAddress, updatedPhone, updatedPin);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(ProfilePanel.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Cập nhật các JLabel trong infoPanel
                nameLabel.setText("Họ và tên: " + updatedName);
                addressLabel.setText("Địa chỉ: " + updatedAddress);
                phoneLabel.setText("Số điện thoại: " + updatedPhone);
                pinField.setText("Mã PIN: " + updatedPin);

                byte[] pubkeys = null;
                try {
                    pubkeys = getPublicKey();
                } catch (CardException ex) {
                    Logger.getLogger(ProfilePanel.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(updateDialog, "Lỗi khi đọc Public Key từ thẻ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                saveToDatabaseInformation(card_id, pubkeys, updatedName, updatedAddress, updatedPhone);
                updateDialog.dispose();
            });

            updateDialog.setLocationRelativeTo(null);
            updateDialog.setVisible(true);
        });
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.X_AXIS));
        combinedPanel.add(avatarContainer);
        combinedPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        combinedPanel.add(infoPanel);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        profilePanel.add(combinedPanel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton changePinButton = new JButton("Đổi mã PIN");
        changePinButton.setBackground(new Color(0, 28, 68));
        changePinButton.setMargin(new Insets(10, 20, 10, 20));
        changePinButton.setForeground(Color.WHITE);
        changePinButton.setPreferredSize(new Dimension(150, 40));
        buttonPanel.add(changePinButton);
        JButton button2 = new JButton("Gia hạn thẻ");
        button2.setBackground(new Color(0, 28, 68));
        button2.setMargin(new Insets(10, 20, 10, 20));
        button2.setPreferredSize(new Dimension(150, 40));
        button2.setForeground(Color.WHITE);
        buttonPanel.add(button2);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        profilePanel.add(buttonPanel, gbc);

        changePinButton.addActionListener(e -> {
            showChangePinDialog();
        });

        profilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.removeAll();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints contentGbc = new GridBagConstraints();
        contentGbc.weightx = 1;
        contentGbc.weighty = 1;
        contentGbc.fill = GridBagConstraints.BOTH;
        contentGbc.anchor = GridBagConstraints.CENTER;

        contentGbc.gridy = 1;
        contentPanel.add(profilePanel, contentGbc);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showChangePinDialog() {
        JDialog changePinDialog = new JDialog((Frame) null, "Đổi mã PIN", true);
        changePinDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel oldOtpLabel = new JLabel("Mã pin cũ:");
        oldOtpLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JPasswordField oldOtpField = new JPasswordField(15);
        oldOtpField.setPreferredSize(new Dimension(150, 30));
        oldOtpField.setFont(new Font("Arial", Font.PLAIN, 14));
        oldOtpField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 0;
        gbc.gridy = 0;
        changePinDialog.add(oldOtpLabel, gbc);
        gbc.gridx = 1;
        changePinDialog.add(oldOtpField, gbc);

        JLabel newOtpLabel = new JLabel("Mã pin mới:");
        newOtpLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JPasswordField newOtpField = new JPasswordField(15);
        newOtpField.setPreferredSize(new Dimension(150, 30));
        newOtpField.setFont(new Font("Arial", Font.PLAIN, 14));
        newOtpField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 0;
        gbc.gridy = 1;
        changePinDialog.add(newOtpLabel, gbc);
        gbc.gridx = 1;
        changePinDialog.add(newOtpField, gbc);

        JLabel newOtpConfirmLabel = new JLabel("Xác nhận mã pin mới:");
        newOtpConfirmLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JPasswordField newOtpConfirmField = new JPasswordField(15);
        newOtpConfirmField.setPreferredSize(new Dimension(150, 30));
        newOtpConfirmField.setFont(new Font("Arial", Font.PLAIN, 14));
        newOtpConfirmField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 0;
        gbc.gridy = 2;
        changePinDialog.add(newOtpConfirmLabel, gbc);
        gbc.gridx = 1;
        changePinDialog.add(newOtpConfirmField, gbc);

        JButton submitButton = new JButton("Xác nhận");
        submitButton.setBackground(new Color(0, 28, 68));
        submitButton.setMargin(new Insets(10, 20, 10, 20));
        submitButton.setForeground(Color.WHITE);
        submitButton.setPreferredSize(new Dimension(150, 40));
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        changePinDialog.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String oldOtp = new String(oldOtpField.getPassword());
            String newOtp = new String(newOtpField.getPassword());
            String newOtpConfirm = new String(newOtpConfirmField.getPassword());

            if (oldOtp.isEmpty() || newOtp.isEmpty() || newOtpConfirm.isEmpty()) {
                JOptionPane.showMessageDialog(contentPanel, "Phải điền mã vào!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!newOtp.equals(newOtpConfirm)) {
                JOptionPane.showMessageDialog(contentPanel, "PIN mới và PIN xác nhận không giống nhau!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (oldOtp.equals(newOtp)) {
                JOptionPane.showMessageDialog(contentPanel, "Pin mới và pin cũ không được giống nhau!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!oldOtp.equals(validatedOtp)) {
                JOptionPane.showMessageDialog(contentPanel, "Old OTP is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                validatedOtp = newOtp;
                mainPage.setValidatedOtp(validatedOtp);
                mainPage.changePin(oldOtp, newOtp);
                changePinDialog.dispose();
            }
            oldOtpField.setText("");
            newOtpField.setText("");
            newOtpConfirmField.setText("");
        });
        changePinDialog.setSize(400, 250);
        changePinDialog.setLocationRelativeTo(null);
        changePinDialog.setVisible(true);
    }

    private static void saveToDatabaseInformation(String id_card, byte[] pubkeys, String fullname, String address, String phone) {
        String url = "jdbc:mysql://localhost:3306/mysql";
        String user = "root";
        String password = "";
        String insertSQL = "INSERT INTO card_info (card_ID, publicKey, fullname, address, phone) VALUES (?, ?, ?, ?, ?)";

        try (java.sql.Connection connection = DriverManager.getConnection(url, user, password); java.sql.PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            String cardId = "";
            String publicKey = "";
            if (id_card != null) {
                cardId = id_card;
            }
            if (pubkeys != null) {
                publicKey = new String(pubkeys);
            }

            preparedStatement.setString(1, cardId);
            preparedStatement.setString(2, publicKey);
            preparedStatement.setString(3, fullname);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, phone);
            // Thực thi câu lệnh
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Lưu thông tin thành công.");
            } else {
                System.out.println("Không thể lưu thông tin vào cơ sở dữ liệu.");
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
