import javax.swing.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuthenticationModule{

    static void loginUser(JTextField usernameField , JPasswordField passwordField,JFrame logi , JFrame bod) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(null,"Fields cannot be empty");
            return;
        }
        if (username.equals("admin") && password.equals("admin")) {
            AdminTools ad = new AdminTools();
            logi.setVisible(false);
            return;
        }
        try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM userdata WHERE username = ? AND password = ?")) {
            pstmt.setString(1, username);
            pstmt.setString(2, UserManager.hashPassword(password));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                GUI.loggedInUser = username;
                logi.setVisible(false);
                bod.setTitle("Welcome "+GUI.loggedInUser);
                bod.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(null,"Invalid username or password");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Error logging in: " + e.getMessage() + "\n");
        }
    }
    public static void lastLogin(JTextArea feedbackArea) {
        try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL)) {
            if (conn != null) {
                String selectQuery = "SELECT Last_Login FROM userdata WHERE username = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
                    pstmt.setString(1, GUI.loggedInUser);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        String lastLogin = rs.getString("Last_Login");
                       feedbackArea.setText("Welcome  " +GUI.loggedInUser+ (lastLogin != null ? "  Last login : "+lastLogin : " this is your First login"));
                    } 
                }
                String updateQuery = "UPDATE userdata SET Last_Login = ? WHERE username = ?";
                try (PreparedStatement ustmt = conn.prepareStatement(updateQuery)) {
                    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    ustmt.setString(1, currentTime);
                    ustmt.setString(2, GUI.loggedInUser);
                    ustmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
