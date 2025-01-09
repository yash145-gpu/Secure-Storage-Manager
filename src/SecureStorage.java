import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecurityTools {
    static final ExecutorService executor = Executors.newFixedThreadPool(4);

    static final String DB_URL = "jdbc:sqlite:unified.db";
    static void encryptFileToDatabase(JTextArea feedbackArea, GUI obj) {
        executor.submit(() -> {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(obj);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {

                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128, new SecureRandom());
                SecretKey secretKey = keyGen.generateKey();

                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());


                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] fileData = Files.readAllBytes(file.toPath());
                byte[] encryptedData = cipher.doFinal(fileData);


                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement fileStmt = conn.prepareStatement(
                             "INSERT INTO files (filename, filedata, user, isEncrypted) VALUES (?, ?, ?, ?)");
                     PreparedStatement keyStmt = conn.prepareStatement(
                             "INSERT INTO keys (id,user,filename,key) VALUES ( ? ,?,?, ?)")) {


                    fileStmt.setString(1, file.getName());
                    fileStmt.setBytes(2, encryptedData);
                    fileStmt.setString(3, GUI.loggedInUser);
                    fileStmt.setBoolean(4, true);
                    fileStmt.executeUpdate();
                    ResultSet generatedKeys = fileStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int fileId = generatedKeys.getInt(1);

                    keyStmt.setInt(1,fileId);
                    keyStmt.setString(2, GUI.loggedInUser);
                    keyStmt.setString(3,file.getName());
                    keyStmt.setString(4, encodedKey);
                    keyStmt.executeUpdate();

                    feedbackArea.append("\nFile encrypted and saved to database: " + file.getName() + "\n");
                }
                    }
            } catch (Exception e) {
                feedbackArea.append("\nError saving file: " + e.getMessage() + "\n");
            }
        }
        });
    }


    static void decryptFileFromDatabase(JTextArea feedbackArea, GUI obj) {
        executor.submit(() -> {
            int fileidx = Integer.parseInt(JOptionPane.showInputDialog(obj, "Enter file index to decrypt:"));
            System.out.println(fileidx);
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement fileStmt = conn.prepareStatement(
                         "SELECT filedata,filename, isEncrypted FROM files WHERE id = ? AND user = ?");
                 PreparedStatement keyStmt = conn.prepareStatement(
                         "SELECT key FROM keys WHERE id = ? AND user = ?")) {


                fileStmt.setInt(1, fileidx);
                fileStmt.setString(2, GUI.loggedInUser);
                ResultSet fileRs = fileStmt.executeQuery();


                if (fileRs.next()) {
                    byte[] encryptedData = fileRs.getBytes("filedata");
                    String filename = fileRs.getString("filename");
                    boolean isEncrypted = fileRs.getBoolean("isEncrypted");

                    if (!isEncrypted) {
                        feedbackArea.append("\nFile is not encrypted. Cannot decrypt.\n");
                        return;
                    }



                    keyStmt.setInt(1, fileidx);
                    keyStmt.setString(2, GUI.loggedInUser);
                    ResultSet keyRs = keyStmt.executeQuery();

                    if (keyRs.next()) {
                        String encodedKey = keyRs.getString("key");


                        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.DECRYPT_MODE, secretKey);
                        byte[] decryptedData = cipher.doFinal(encryptedData);



                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setSelectedFile(new File(filename));
                        int result = fileChooser.showSaveDialog(obj);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File saveFile = fileChooser.getSelectedFile();
                            Files.write(saveFile.toPath(), decryptedData);
                            feedbackArea.append("\nFile decrypted and saved: " + saveFile.getName() + "\n");
                        }
                    } else {
                        feedbackArea.append("\nKey not found for the file.\n");
                    }
                } else {
                    feedbackArea.append("\nFile not found for the logged-in user.\n");
                }
            } catch (Exception e) {
                feedbackArea.append("\n Error decrypting file: " + e.getMessage() + "\n");
            }
        });
    }
    static void encryptFileToDatabase(JTextArea feedbackArea, File file) {
        executor.submit(() -> {
            try {
                // Generate AES key
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128, new SecureRandom());
                SecretKey secretKey = keyGen.generateKey();
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

                // Encrypt the file
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] fileData = Files.readAllBytes(file.toPath());
                byte[] encryptedData = cipher.doFinal(fileData);

                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement fileStmt = conn.prepareStatement(
                             "INSERT INTO files (filename, filedata, user, isEncrypted) VALUES (?, ?, ?, ?)",
                             PreparedStatement.RETURN_GENERATED_KEYS);
                     PreparedStatement keyStmt = conn.prepareStatement(
                             "INSERT INTO keys (id, user,filename, key) VALUES (?, ?,?, ?)")) {

                    fileStmt.setString(1, file.getName());
                    fileStmt.setBytes(2, encryptedData);
                    fileStmt.setString(3, GUI.loggedInUser);
                    fileStmt.setBoolean(4, true);
                    fileStmt.executeUpdate();

                    ResultSet generatedKeys = fileStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int fileId = generatedKeys.getInt(1);


                        keyStmt.setInt(1, fileId);
                        keyStmt.setString(2, GUI.loggedInUser);
                        keyStmt.setString(3,file.getName());
                        keyStmt.setString(4, encodedKey);
                        keyStmt.executeUpdate();

                        feedbackArea.append("\nFile encrypted and saved to database with ID: " + fileId + "\n");
                    } else {
                        feedbackArea.append("\nFailed to retrieve the file ID.\n");
                    }
                }
            } catch (Exception e) {
                feedbackArea.append("\nError encrypting and saving file: " + e.getMessage() + "\n");
            }
        });
    }

    protected static String hashfile(File file,JTextArea feedbackArea) {
        executor.submit(() -> {
        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");


            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }


            byte[] hashBytes = digest.digest();


            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);

            }
            feedbackArea.setText("\nSHA256 Hash: "+hexString.toString()+"\n");
System.out.println( hexString.toString());
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Hashing algorithm not found: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
        });
    return null;
    }
}



