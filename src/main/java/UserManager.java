import javax.swing.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

class UserManager {

    protected static void registerUser(JTextField usernameField, JPasswordField passwordField) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null,"Fields cannot be empty");
            return;
        }else
        if(userNameAlreadyExists(username)){
            JOptionPane.showMessageDialog(null,"Username already exists");
            return;
        } else if (username.length() > 12 || password.length() > 12) {
                  JOptionPane.showMessageDialog(null,"Username & Password length must be under 12 characters");
                return;
        }

        try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO userdata(username,password) VALUES (?,?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Error logging in: " + e.getMessage() + "\n");

        }
       
    }
    protected static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null,"Error in Hashing/Algo not found" + e.getMessage() + "\n");
            return null;
        }
    }
private static boolean userNameAlreadyExists(String username){

    try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL);
         PreparedStatement pstmt = conn.prepareStatement("SELECT 1 from userdata where username=?")) {
        pstmt.setString(1, username);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Error logging in: " + e.getMessage() + "\n");

        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null,"Error logging in: " + e.getMessage() + "\n");

    }
    return false;
    }
}

