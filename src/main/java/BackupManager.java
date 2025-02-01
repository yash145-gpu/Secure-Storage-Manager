import javax.swing.*;
import java.io.*;
import java.sql.*;

public class BackupManager {
    static final String DB_URL = "jdbc:sqlite:unified.db";
    static final long MAX_FILE_SIZE = 900 * 1024 * 1024; 
    static final int BUFFER_SIZE = 8192; 

  
    static void saveFileToDatabase(File file) {
        if (file.length() > MAX_FILE_SIZE) {
            MainFrame.feedbackArea.append("\nFile exceeds the maximum allowed size of 900MB: " + file.getName() + "\n");
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO files (filename, filedata, username) VALUES (?, ?, ?)")) {
                        byte[] fileData = new byte[(int) file.length()];
                        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                            bis.read(fileData);
                        }
                    pstmt.setString(1, file.getName());
                    pstmt.setBytes(2, fileData);     
                   pstmt.setString(3, GUI.loggedInUser);
                    pstmt.executeUpdate();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    MainFrame.feedbackArea.append("\nFile saved to database: " + file.getName() + "\n");
                } catch (Exception e) {
                    MainFrame.feedbackArea.append("\nError saving file: " + e.getMessage() + "\n");
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    
    static void saveFileToDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveFileToDatabase(file);
        }
    }

    
    static void retrieveFileFromDatabase() {
        String filename = JOptionPane.showInputDialog(null, "Enter filename to retrieve:");
        if (filename == null || filename.trim().isEmpty()) {
            MainFrame.feedbackArea.append("\nFilename cannot be empty.\n");
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
                        byte[] filedata = rs.getBytes("filedata");
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setSelectedFile(new File(filename));
                            int result = fileChooser.showSaveDialog(null);
                            if (result == JFileChooser.APPROVE_OPTION) {
                                File saveFile = fileChooser.getSelectedFile();
                                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                                    outputStream.write(filedata);
                                }
                                MainFrame.feedbackArea.append("\nFile retrieved and saved: " + saveFile.getName() + "\n");
                            }
                        }
                     else {
                        MainFrame.feedbackArea.append("\nFile not found for the logged-in user.\n");
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    MainFrame.feedbackArea.append("\nError retrieving file: " + e.getMessage() + "\n");
                }
            }
        };
        worker.execute();
    }
}
