package com.kma.librarycard;

import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.*;
import javax.swing.*;

public class Connect_CardJava extends JFrame {

    public static final byte[] AID_APPLET = {(byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x01};
    private Card card;
    private TerminalFactory factory;
    private CardChannel channel;
    private CardTerminal terminal;
    private List<CardTerminal> terminals;
    private ResponseAPDU response;

    private static boolean connectFrameShown = false; // Kiểm soát ConnectFrame
    private static boolean otpInputShown = false; // Kiểm soát OTPInput

    public Connect_CardJava() {

        setVisible(true);

        if (!connectFrameShown) {
            showConnectCardFrame();
        }
    }
    // Chuyển nội dung hiển thị khi chọn Tab
    // Phương thức tạo nút với chiều cao cố định
    // Tạo TabBar

    private synchronized void showConnectCardFrame() {
        if (connectFrameShown) return; // Chỉ hiển thị nếu chưa được mở
        connectFrameShown = true;

        JFrame connectFrame = new JFrame("Kết nối thẻ");
        connectFrame.setSize(400, 200);
        connectFrame.setLocationRelativeTo(this);
        connectFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        connectFrame.setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel("Đang kết nối với thẻ, vui lòng chờ...");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        connectFrame.add(messageLabel, BorderLayout.CENTER);

        // Giả lập kết nối thẻ (thời gian trễ 2 giây)
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Giả lập thời gian kết nối
                SwingUtilities.invokeLater(() -> {
                    boolean connected = connectToCard();
                    if (connected) {
                        messageLabel.setText("Kết nối thẻ thành công!");
                        JOptionPane.showMessageDialog(connectFrame, "Thẻ đã kết nối thành công!");
                        connectFrame.dispose();
                  
                        if (!otpInputShown) {
                            otpInputShown = true; // Đánh dấu đã mở OTPInput
                            OTPInput otpInput = new OTPInput();
                            otpInput.setVisible(true);
                        }
                    } else {
                        messageLabel.setText("Kết nối thẻ thất bại!");
                        JOptionPane.showMessageDialog(connectFrame, "Không thể kết nối với thẻ. Đóng ứng dụng.");
                        System.exit(0);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        connectFrame.setVisible(true);
    }

    private boolean connectToCard() {
        try {
            factory = TerminalFactory.getDefault();
            terminals = factory.terminals().list();
            terminal = terminals.get(0);
            card = terminal.connect("T=1");
            channel = card.getBasicChannel();
            if (channel == null) {
                return false;
            }
            response = channel.transmit(new CommandAPDU(0x00, (byte) 0xA4, 0x04, 0x00, AID_APPLET));
            return response.getSW() == 0x9000;
        } catch (CardException ex) {
            Logger.getLogger(Connect_CardJava.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Connect_CardJava::new);
    }
}
