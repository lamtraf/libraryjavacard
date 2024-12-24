/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kma.librarycard;
import java.awt.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 *
 * @author lamtr
 */

public class ConnectDatabase {
    private static final String URL = "jdbc:mariadb://localhost:3306/javacard";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connect sucessfully to database");
        } catch (SQLException e) {
            System.out.println("Không thể kết nối tới database!");
        }
        return connection;
    }
//    public static void main(String[] args) {
//        try {
//            // Lấy thông tin từ thẻ
//            byte[] cardId = getCardID();
//            byte[] publicKey = getPublicKey();
//
//            if (cardId != null && publicKey != null) {
//                // Lưu thông tin vào cơ sở dữ liệu
//                saveToDatabase(cardId, publicKey);
//            } else {
//                System.out.println("Không lấy được thông tin từ thẻ.");
//            }
//        } catch (Exception e) {
//        }
//    }
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

        CommandAPDU getCardIdCommand = new CommandAPDU(0xA4, 0x1D, 0x00, 0x00); // Command APDU để lấy card ID
        ResponseAPDU response = channel.transmit(getCardIdCommand);

        if (response.getSW() == 0x9000) {
            return response.getData();
        } else {
            System.out.println("Không lấy được Card ID từ thẻ.");
            return null;
        }
    }

    // Hàm để lấy Public Key từ thẻ
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

        CommandAPDU getPubKeyCommand = new CommandAPDU(0xA4, 0x1A, 0x00, 0x0); // Command APDU để lấy public key
        ResponseAPDU response = channel.transmit(getPubKeyCommand);

        if (response.getSW() == 0x9000) {
            return response.getData();
        } else {
            System.out.println("Không lấy được Public Key từ thẻ.");
            return null;
        }
    }

    // Hàm lưu thông tin vào cơ sở dữ liệu
    private static void saveToDatabase(byte[] cardId, byte[] publicKey) {
        String insertSQL = "INSERT INTO card_info (card_id, pubkey) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            // Gán giá trị cho các tham số
            preparedStatement.setString(1, bytesToHex(cardId));
            preparedStatement.setString(2, bytesToHex(publicKey));

            // Thực thi câu lệnh
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Lưu thông tin thành công.");
            } else {
                System.out.println("Không thể lưu thông tin vào cơ sở dữ liệu.");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối cơ sở dữ liệu.");
        }
    }

    // Hàm chuyển byte[] sang chuỗi Hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    

}
