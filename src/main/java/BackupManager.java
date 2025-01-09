import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;

public class BackupManager {
    static final String DB_URL = "jdbc:sqlite:unified.db";
    static void saveFileToDatabase(JTextArea feedbackArea, File file) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO files (filename, filedata, user) VALUES (?, ?, ?)")) {
            pstmt.setString(1, file.getName());
            pstmt.setBytes(2, Files.readAllBytes(file.toPath()));
            pstmt.setString(3, GUI.loggedInUser);
            pstmt.executeUpdate();
            feedbackArea.append("File saved to database: " + file.getName() + "\n");
        } catch (SQLException | IOException e) {
            feedbackArea.append("Error saving file: " + e.getMessage() + "\n");
        }
    }
    static void saveFileToDatabase(JTextArea feedbackArea,GUI obj) {

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(obj);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO files (filename, filedata, user) VALUES (?, ?, ?)")) {
                pstmt.setString(1, file.getName());
                pstmt.setBytes(2, Files.readAllBytes(file.toPath()));
                pstmt.setString(3, GUI.loggedInUser);
                pstmt.executeUpdate();
                feedbackArea.append("File saved to database: " + file.getName() + "\n");
            } catch (SQLException | IOException e) {
                feedbackArea.append("Error saving file: " + e.getMessage() + "\n");
            }
        }
    }
    static void retrieveFileFromDatabase(JTextArea feedbackArea,GUI obj) {
        String filename = JOptionPane.showInputDialog(obj, "Enter filename to retrieve:");
        if (filename == null || filename.trim().isEmpty()) {
            feedbackArea.append("Filename cannot be empty.\n");
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT filedata FROM files WHERE filename = ? AND user = ?")) {
            pstmt.setString(1, filename);
            pstmt.setString(2, GUI.loggedInUser);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] fileData = rs.getBytes("filedata");
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(filename));
                int result = fileChooser.showSaveDialog(obj);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File saveFile = fileChooser.getSelectedFile();
                    Files.write(saveFile.toPath(), fileData);
                    feedbackArea.append("File retrieved and saved: " + saveFile.getName() + "\n");
                }
            } else {
                feedbackArea.append("File not found for the logged-in user.\n");
            }
        } catch (SQLException | IOException e) {
            feedbackArea.append("Error retrieving file: " + e.getMessage() + "\n");
        }
    }
}
