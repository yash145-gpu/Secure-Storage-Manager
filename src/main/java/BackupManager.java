import javax.swing.*;
import java.io.*;
import java.sql.*;

public class BackupManager {
    static final String DB_URL = "jdbc:sqlite:unified.db";
    static final long MAX_FILE_SIZE = 900 * 1024 * 1024; 
    static final int BUFFER_SIZE = 8192; 

  
    static void saveFileToDatabase(JTextArea feedbackArea, File file) {
        if (file.length() > MAX_FILE_SIZE) {
            feedbackArea.append("File exceeds the maximum allowed size of 1GB: " + file.getName() + "\n");
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO files (filename, filedata, username) VALUES (?, ?, ?)")) {

                    pstmt.setString(1, file.getName());
                    pstmt.setBinaryStream(2, new BufferedInputStream(new FileInputStream(file)), file.length());
                    pstmt.setString(3, GUI.loggedInUser);
                    pstmt.executeUpdate();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    feedbackArea.append("File saved to database: " + file.getName() + "\n");
                } catch (Exception e) {
                    feedbackArea.append("Error saving file: " + e.getMessage() + "\n");
                }
            }
        };
        worker.execute();
    }

    
    static void saveFileToDatabase(JTextArea feedbackArea, GUI obj) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(obj);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveFileToDatabase(feedbackArea, file);
        }
    }

    
    static void retrieveFileFromDatabase(JTextArea feedbackArea, GUI obj) {
        String filename = JOptionPane.showInputDialog(obj, "Enter filename to retrieve:");
        if (filename == null || filename.trim().isEmpty()) {
            feedbackArea.append("Filename cannot be empty.\n");
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("SELECT filedata FROM files WHERE filename = ? AND username = ?")) {

                    pstmt.setString(1, filename);
                    pstmt.setString(2, GUI.loggedInUser);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        try (InputStream inputStream = rs.getBinaryStream("filedata")) {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setSelectedFile(new File(filename));
                            int result = fileChooser.showSaveDialog(obj);

                            if (result == JFileChooser.APPROVE_OPTION) {
                                File saveFile = fileChooser.getSelectedFile();
                                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                                    byte[] buffer = new byte[BUFFER_SIZE];
                                    int bytesRead;

                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, bytesRead);
                                    }
                                }
                                feedbackArea.append("File retrieved and saved: " + saveFile.getName() + "\n");
                            }
                        }
                    } else {
                        feedbackArea.append("File not found for the logged-in user.\n");
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    feedbackArea.append("Error retrieving file: " + e.getMessage() + "\n");
                }
            }
        };
        worker.execute();
    }
}
