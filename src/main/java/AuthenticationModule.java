import javax.swing.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuthenticationModule {
    private static String typequery;

    static void loginUser(int adm) {
        String username = GUI.userField.getText().trim();
        String password = new String(GUI.passwdField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(null, "Fields cannot be empty");
            return;
        }
        if (adm == 0) {
            typequery = "SELECT * FROM userdata WHERE username = ? AND password = ?";
        } else {
            typequery = "SELECT * FROM Admindata WHERE AdminName = ? AND password = ?";

        }
        try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(typequery)) {
            pstmt.setString(1, username);
            pstmt.setString(2, UserManager.hashPassword(password));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                if (adm == 0) {
                    GUI.loggedInUser = username;
                    GUI.login.setVisible(false);
                    MainFrame mf = new MainFrame();
                    MainFrame.mainframe.setTitle("Welcome " + GUI.loggedInUser);
                    MainFrame.mainframe.setVisible(true);

                } else if (adm == 1) {
                    AdminTools adminSession = new AdminTools();
                    GUI.login.setVisible(false);

                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error logging in: " + e.getMessage() + "\n");
        }
    }

    public static void lastLogin(int user) {
        String selectQuery;
        String updateQuery;
        String name = GUI.userField.getText();
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (user == 1) {
            selectQuery = "SELECT Last_Login FROM Admindata WHERE AdminName = ?";
            updateQuery = "UPDATE Admindata SET Last_Login = ? WHERE AdminName = ?";
        } else {
            selectQuery = "SELECT Last_Login FROM userdata WHERE username = ?";
            updateQuery = "UPDATE userdata SET Last_Login = ? WHERE username = ?";
        }

        try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL)) {
            conn.setAutoCommit(false);

            String lastLogin = null;
            try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
                pstmt.setString(1, name);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        lastLogin = rs.getString("Last_Login");
                        rs.close();
                    }
                }
            }

            if (user == 0) {
                MainFrame.feedbackArea.setText("Welcome " + name
                        + (lastLogin != null ? "  Last login : " + lastLogin : " this is your First login"));
            } else {
                AdminTools.feedbackArea.setText("Welcome Administrator " + name
                        + (lastLogin != null ? "  Last login : " + lastLogin : " this is your First login"));
            }

            try (PreparedStatement ustmt = conn.prepareStatement(updateQuery)) {
                ustmt.setString(1, currentTime);
                ustmt.setString(2, name);
                ustmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

}
