import javax.swing.*;
import java.sql.*;
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
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM userdata WHERE name = ? AND password = ?")) {
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

}

