import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecurityTools {
   private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final String DB_URL = "jdbc:sqlite:unified.db";
    static void encryptFileToDatabase(JTextArea feedbackArea, GUI obj) {
        executor.submit(() -> {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(obj);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            float size = file.length();
            boolean localD = false;
            int Jch = JOptionPane.showConfirmDialog(null, "Size of file is " + size / 1024 + "kb" + "\nFiles greater than 500mb will be encrypted locally" + "\nWant to save file on local directory instead of database ?");
            if (Jch == JOptionPane.YES_OPTION) {
                localD = true;
            } else if (Jch == JOptionPane.CANCEL_OPTION || Jch == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (size > 500 * 1024 * 1024) {
                localD = true;
                feedbackArea.append("Storing file locally");
            }
            try {
                feedbackArea.append("\n-> AES ENCRYPTION IN PROGRESS\n");
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256, new SecureRandom());
                SecretKey secretKey = keyGen.generateKey();
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                if (localD) {
                    localMode(file,cipher,feedbackArea,encodedKey);
                }

                ByteArrayOutputStream encryptedStream = new ByteArrayOutputStream();
                try (FileInputStream fis = new FileInputStream(file);
                     CipherOutputStream cos = new CipherOutputStream(encryptedStream, cipher)) {

                    byte[] buffer = new byte[1024 * 1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        cos.write(buffer, 0, bytesRead);
                    }
                }

                byte[] encryptedData = encryptedStream.toByteArray();

                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement fileStmt = conn.prepareStatement(
                             "INSERT INTO files (filename, filedata, username, isEncrypted) VALUES (?, ?, ?, ?)",
                             PreparedStatement.RETURN_GENERATED_KEYS);
                     PreparedStatement keyStmt = conn.prepareStatement(
                             "INSERT INTO keys (id, filename, username, key) VALUES (?, ?, ?, ?)")) {
                    fileStmt.setString(1, file.getName());
                    fileStmt.setBytes(2, encryptedData);
                    fileStmt.setString(3, GUI.loggedInUser);
                    fileStmt.setBoolean(4, true);
                    fileStmt.executeUpdate();

                    ResultSet generatedKeys = fileStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int fileId = generatedKeys.getInt(1);
                        keyStmt.setInt(1, fileId);
                        keyStmt.setString(2, file.getName());
                        keyStmt.setString(3, GUI.loggedInUser);
                        keyStmt.setString(4, encodedKey);
                        keyStmt.executeUpdate();
                        feedbackArea.append("\nFile encrypted and saved to database with ID: " + fileId + "\n");
                    }
                }
            } catch (Exception e) {
                feedbackArea.append("\nError encrypting and saving file: " + e.getMessage() + "\n");
            }
        }
        });
    }

    static void decryptFile(JTextArea feedbackArea, GUI obj) {
        executor.submit(() -> {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Decrypt from database?",
                    "Decryption Mode",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );
            if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                feedbackArea.append("\nDecryption canceled by the user.\n");
                return;
            }
            boolean fromLocal = (choice == JOptionPane.NO_OPTION);
            try {
                int fileId = -1;
                File inputFile = null;

                if (fromLocal) {
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showOpenDialog(obj);
                    if (result != JFileChooser.APPROVE_OPTION) {
                        feedbackArea.append("\nNo file selected for decryption.\n");
                        return;
                    }
                    inputFile = fileChooser.getSelectedFile();
                    fileId = Integer.parseInt(JOptionPane.showInputDialog(
                            obj,
                            "Enter the key ID for decryption:"
                    ));
                } else {
                    fileId = Integer.parseInt(JOptionPane.showInputDialog(
                            obj,
                            "Enter the file ID to decrypt:"
                    ));
                }
                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement fileStmt = conn.prepareStatement(
                             "SELECT filedata, filename, isEncrypted FROM files WHERE id = ? AND username = ?");
                     PreparedStatement keyStmt = conn.prepareStatement(
                             "SELECT key FROM keys WHERE id = ? AND username = ?")) {
                    fileStmt.setInt(1, fileId);
                    fileStmt.setString(2, GUI.loggedInUser);
                    ResultSet fileRs = fileStmt.executeQuery();

                    InputStream encryptedStream = null;
                    String filename = null;

                    if (fromLocal) {
                        encryptedStream = new BufferedInputStream(new FileInputStream(inputFile));
                        filename = inputFile.getName().replace(".enc", "");
                    } else if (fileRs.next()) {
                        encryptedStream = fileRs.getBinaryStream("filedata");
                        filename = fileRs.getString("filename");
                        boolean isEncrypted = fileRs.getBoolean("isEncrypted");

                        if (!isEncrypted) {
                            feedbackArea.append("\nThe selected file is not encrypted.\n");
                            return;
                        }
                    } else {
                        feedbackArea.append("\nFile not found for the logged-in user.\n");
                        return;
                    }

                    keyStmt.setInt(1, fileId);
                    keyStmt.setString(2, GUI.loggedInUser);
                    ResultSet keyRs = keyStmt.executeQuery();

                    if (keyRs.next()) {
                        String encodedKey = keyRs.getString("key");
                        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                        SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.DECRYPT_MODE, secretKey);

                        File saveFile = getSaveFile(filename);
                        if (saveFile == null) {
                            feedbackArea.append("\nDecryption canceled.\n");
                            return;
                        }
                        try (CipherInputStream cis = new CipherInputStream(encryptedStream, cipher);
                             OutputStream decryptedOutputStream = new BufferedOutputStream(new FileOutputStream(saveFile))) {

                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = cis.read(buffer)) != -1) {
                                decryptedOutputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        feedbackArea.append("\nFile decrypted and saved: " + saveFile.getAbsolutePath() + "\n");
                    } else {
                        feedbackArea.append("\nKey not found for the selected file.\n");
                    }
                }
            } catch (Exception e) {
                feedbackArea.append("\nError decrypting file: " + e.getMessage() + "\n");
            }
        });
    }

    private static File getSaveFile(String defaultName) {
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setSelectedFile(new File(defaultName));
        int result = saveChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return saveChooser.getSelectedFile();
        }
        return null;
    }

    static void encryptFileToDatabase(JTextArea feedbackArea, File file) {
        executor.submit(() -> {
            float sizeMB = file.length() / (1024 * 1024);
            boolean localD = false;

            int choice = JOptionPane.showConfirmDialog(null,
                    "Size of file is " + sizeMB + " MB\n" +
                            "Files larger than 500 MB will be encrypted locally.\n" +
                            "Do you want to save the file locally instead of the database?");
            if (choice == JOptionPane.YES_OPTION) {
                localD = true;
            } else if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                return;
            }

            if (sizeMB > 500) {
                localD = true;
                feedbackArea.append("-> Storing file locally due to size.\n");
            }

            try {
                feedbackArea.append("\n-> AES ENCRYPTION IN PROGRESS\n");

                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256, new SecureRandom());
                SecretKey secretKey = keyGen.generateKey();
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                if (localD) {
                localMode(file,cipher,feedbackArea,encodedKey);
                }

                ByteArrayOutputStream encryptedStream = new ByteArrayOutputStream();
                try (FileInputStream fis = new FileInputStream(file);
                     CipherOutputStream cos = new CipherOutputStream(encryptedStream, cipher)) {
                    byte[] buffer = new byte[1024 * 1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        cos.write(buffer, 0, bytesRead);
                    }
                }

                byte[] encryptedData = encryptedStream.toByteArray();

                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement fileStmt = conn.prepareStatement(
                             "INSERT INTO files (filename, filedata, username, isEncrypted) VALUES (?, ?, ?, ?)",
                             PreparedStatement.RETURN_GENERATED_KEYS);
                     PreparedStatement keyStmt = conn.prepareStatement(
                             "INSERT INTO keys (id, filename, username, key) VALUES (?, ?, ?, ?)")) {
                    fileStmt.setString(1, file.getName());
                    fileStmt.setBytes(2, encryptedData);
                    fileStmt.setString(3, GUI.loggedInUser);
                    fileStmt.setBoolean(4, true);
                    fileStmt.executeUpdate();

                    ResultSet generatedKeys = fileStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int fileId = generatedKeys.getInt(1);
                        keyStmt.setInt(1, fileId);
                        keyStmt.setString(2, file.getName());
                        keyStmt.setString(3, GUI.loggedInUser);
                        keyStmt.setString(4, encodedKey);
                        keyStmt.executeUpdate();
                        feedbackArea.append("\nFile encrypted and saved to database with ID: " + fileId + "\n");
                    }
                }
            } catch (Exception e) {
                feedbackArea.append("\nError encrypting and saving file: " + e.getMessage() + "\n");
            }
        });
    }

    private static File getLocalFile(File file, Cipher cipher) throws IOException {
        File userDirectory = new File("local/" + GUI.loggedInUser);
        if (!userDirectory.exists()) userDirectory.mkdirs();
        File localFile = new File(userDirectory, file.getName() + ".enc");

        try (FileInputStream fis = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(localFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            byte[] buffer = new byte[64 * 1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
        return localFile;
    }
    private static void  localMode(File file,Cipher cipher,JTextArea feedbackArea,String encodedKey){
        try {
            File localFile = getLocalFile(file, cipher);
            feedbackArea.append("<- File saved locally at: " + localFile.getAbsolutePath() + "\n");
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement fileStmt = conn.prepareStatement(
                         "INSERT INTO files (filename, filedata, username, isEncrypted) VALUES (?,?, ?, ?)");
                 PreparedStatement keyStmt = conn.prepareStatement(
                         "INSERT INTO keys (id, username, filename, key) VALUES (?, ?, ?, ?)")) {
                fileStmt.setString(1, file.getName());
                fileStmt.setString(2, encodedKey);
                fileStmt.setString(3, GUI.loggedInUser);
                fileStmt.setBoolean(4, true);
                fileStmt.executeUpdate();
                ResultSet generatedKeys = fileStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int fileId = generatedKeys.getInt(1);
                    keyStmt.setInt(1, fileId);
                    keyStmt.setString(2, GUI.loggedInUser);
                    keyStmt.setString(3, file.getName());
                    keyStmt.setString(4, encodedKey);
                    keyStmt.executeUpdate();
                }
            }
        }catch (IOException | SQLException e){}
        return;
    }

    protected static void hashfile(File file, JTextArea feedbackArea, int mode) {
        executor.submit(() -> {
            String Algo = "SHA-256";
            if (mode == 1) {
                Algo = "SHA-512";
            }
            try {
                MessageDigest digest = MessageDigest.getInstance(Algo);

                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
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
                feedbackArea.setText("\n" + Algo + " Hash: " + hexString.toString() + "\n");
                return hexString.toString();

            } catch (NoSuchAlgorithmException e) {
                System.err.println("Hashing algorithm not found: " + e.getMessage());
                return null;
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                return null;
            }
        });
    }
}


