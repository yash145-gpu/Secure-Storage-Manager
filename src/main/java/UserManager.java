import javax.swing.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class UserManager {

    protected static void registerUser(JTextField usernameField,JPasswordField passwordField) {
        DbHandler.setupDatabase();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null,"Fields cannot be empty");
            return;
        }
        try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO userdata (name, password) VALUES (?, ?)")) {
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
            JOptionPane.showMessageDialog(null,"Error in Hashing ,Algorithm class not found" + e.getMessage() + "\n");
            return null;
        }
    }
}
