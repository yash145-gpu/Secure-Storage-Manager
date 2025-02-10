import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
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

/**
 * SecurityTools.java
 * AES-256 CBC PKCS5 Padding encryption/decryption
 * SHA-256/512 message digest for file checksums
 * Custom AES 128/256,CBC/ECB,PKCS5 file decryption
 * @author Yash Shinde
 * @version 1.1.0
 */

public class SecurityTools {

    // Multithreading with 4 executor threads for concurrent cryptographic operations
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final String DB_URL = "jdbc:sqlite:unified.db";

    static void encryptFileToDatabase(File ffile) {
        executor.submit(() -> {
            File file = null;
            if (ffile == null) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                }
            } else {
                file = ffile;
            }
            float size = file.length();
            boolean localD = false;

            /*
             * 500Mb limit for safety,Sqlite constraints BLOB at 1Gb can be increased to 2Gb (Not recommended),
             * uploading larger files to SQLite can make JVM run out of allocated RAM .
             * in such case the file is encrypted in local mode and stored locally and only
             * metadata is stored on database (filename,key,IV).
             */

            int Jch = JOptionPane.showConfirmDialog(null,
                    "Size of file is " + size / (1024 * 1024) + "Mb"
                            + "\nFiles greater than 500mb will be encrypted locally"
                            + "\nWant to save file on local directory instead of database ?");
            if (Jch == JOptionPane.YES_OPTION) {
                localD = true;
            } else if (Jch == JOptionPane.CANCEL_OPTION || Jch == JOptionPane.CLOSED_OPTION) {
                return;
            }

            if (size > 500 * 1024 * 1024) {
                localD = true; // Local Encryption
                MainFrame.feedbackArea.append("\nStoring file locally");
            }
            try {
                MainFrame.feedbackArea.append("\n-> AES ENCRYPTION IN PROGRESS\n");

                KeyGenerator keyGen = KeyGenerator.getInstance("AES"); // Secure Random AES-256 key generation
                keyGen.init(256, new SecureRandom());
                SecretKey secretKey = keyGen.generateKey();

                // Key encoded for storing in database as an encoded string
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

                // Cipher instance generation
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                byte[] iv = new byte[16]; // Secure Random Initialization Vector 16 Bytes
                new SecureRandom().nextBytes(iv);
                String encodedIV = Base64.getEncoder().encodeToString(iv);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);

                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

                if (localD) { // Local Mode encryption
                    localMode(file, cipher, encodedKey, iv);
                    return;
                }

                // Database mode encryption
                ByteArrayOutputStream encryptedStream = new ByteArrayOutputStream();
                try (FileInputStream fis = new FileInputStream(file);

                     // Cipher output stream for buffered r/w and realtime cipher update
                     CipherOutputStream cos = new CipherOutputStream(encryptedStream, cipher)) {
                    byte[] buffer = new byte[1024 * 1024]; // 1Mb buffer
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        cos.write(buffer, 0, bytesRead);
                    }
                }
                // Storing in byte array for saving encrypted data in database
                byte[] encryptedData = encryptedStream.toByteArray();

                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement fileStmt = conn.prepareStatement(
                             "INSERT INTO files (filename, filedata, username, isEncrypted) VALUES (?, ?, ?, ?)",
                             PreparedStatement.RETURN_GENERATED_KEYS);
                     PreparedStatement keyStmt = conn.prepareStatement(
                             "INSERT INTO keys (id, filename, username, key, IniVec) VALUES (?, ?, ?, ?, ?)")) {
                    fileStmt.setString(1, file.getName());
                    fileStmt.setBytes(2, encryptedData);
                    fileStmt.setString(3, GUI.loggedInUser);
                    fileStmt.setBoolean(4, true);
                    fileStmt.executeUpdate();

                    ResultSet generatedKeys = fileStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int fileId = generatedKeys.getInt(1); // Key id needs to have same id as file
                        keyStmt.setInt(1, fileId);
                        keyStmt.setString(2, file.getName());
                        keyStmt.setString(3, GUI.loggedInUser);
                        keyStmt.setString(4, encodedKey);
                        keyStmt.setString(5, encodedIV);
                        keyStmt.executeUpdate();
                        MainFrame.feedbackArea
                                .append("\nFile encrypted and saved to database with ID: " + fileId + "\n");
                    }
                }
            } catch (Exception e) {
                MainFrame.feedbackArea.append("\nError encrypting and saving file: " + e.getMessage() + "\n");
            }

        });
    }

    static void decryptFile() {
        /*
         * Decryption modes :
         * Database mode if file is encrypted on database.
         * local mode : if file is encypted locally (local/<username>/<file>)
         */
        executor.submit(() -> {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Decrypt from database?",
                    "Decryption Mode",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                MainFrame.feedbackArea.append("\nDecryption canceled by the user.\n");
                return;
            }
            boolean fromLocal = (choice == JOptionPane.NO_OPTION);
            try {
                int fileId;
                File inputFile = null;

                if (fromLocal) {
                    // Opens users local directory where encypted files are stored 
                    JFileChooser fileChooser = new JFileChooser("local\\" + GUI.loggedInUser + "\\");
                    int result = fileChooser.showOpenDialog(null);
                    if (result != JFileChooser.APPROVE_OPTION) {
                        MainFrame.feedbackArea.append("\nNo file selected for decryption.\n");
                        return;
                    }
                    inputFile = fileChooser.getSelectedFile();
                    fileId = Integer.parseInt(JOptionPane.showInputDialog(
                            null,
                            "Enter the key ID for decryption:"));
                } else {
                    fileId = Integer.parseInt(JOptionPane.showInputDialog(
                            null,
                            "Enter the file ID to decrypt:"));
                }
                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement fileStmt = conn.prepareStatement(
                             "SELECT filedata, filename, isEncrypted FROM files WHERE id = ? AND username = ?");
                     PreparedStatement keyStmt = conn.prepareStatement(
                             "SELECT key,IniVec FROM keys WHERE id = ? AND username = ?")) {
                    fileStmt.setInt(1, fileId);
                    fileStmt.setString(2, GUI.loggedInUser);
                    ResultSet fileRs = fileStmt.executeQuery();

                    InputStream encryptedStream;
                    String filename;

                    if (fromLocal) {
                        encryptedStream = new BufferedInputStream(new FileInputStream(inputFile));
                        filename = inputFile.getName().replace(".enc", ""); // locally encrypted files have .enc extension
                    } else if (fileRs.next()) {
                        encryptedStream = fileRs.getBinaryStream("filedata");
                        filename = fileRs.getString("filename");
                        boolean isEncrypted = fileRs.getBoolean("isEncrypted");

                        if (!isEncrypted) {
                            MainFrame.feedbackArea.append("\nThe selected file is not encrypted.\n");
                            return;
                        }
                    } else {
                        MainFrame.feedbackArea.append("\nFile not found for the logged-in user.\n");
                        return;
                    }

                    keyStmt.setInt(1, fileId);
                    keyStmt.setString(2, GUI.loggedInUser);
                    ResultSet keyRs = keyStmt.executeQuery();

                    if (keyRs.next()) {
                        String encodedKey = keyRs.getString("key");
                        byte[] decodedKey = Base64.getDecoder().decode(encodedKey); // Key decoded to byte array for decryption

                        String ivs = keyRs.getString("IniVec"); // iv is varbinary
                        byte[] iv = Base64.getDecoder().decode(ivs);
                        IvParameterSpec ivspec = new IvParameterSpec(iv);

                        SecretKey secretKey = new SecretKeySpec(decodedKey, "AES"); // key spec from decoded key

                        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);

                        File saveFile = getSaveFile(filename); // saving file locally after decryption
                        if (saveFile == null) {
                            MainFrame.feedbackArea.append("\nDecryption canceled.\n");
                            return;
                        }
                        try (CipherInputStream cis = new CipherInputStream(encryptedStream, cipher);
                             OutputStream decryptedOutputStream = new BufferedOutputStream(
                                     new FileOutputStream(saveFile))) {

                            byte[] buffer = new byte[1024 * 1024]; // 1Mb output buffer
                            int bytesRead;
                            while ((bytesRead = cis.read(buffer)) != -1) {
                                decryptedOutputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        MainFrame.feedbackArea
                                .append("\nFile decrypted and saved: " + saveFile.getAbsolutePath() + "\n");
                    } else {
                        MainFrame.feedbackArea.append("\nKey not found for the selected file.\n");
                    }
                }
            } catch (Exception e) {
                MainFrame.feedbackArea.append("\nError decrypting file: " + e.getMessage() + "\n");
            }
        });
    }

    private static File getSaveFile(String defaultName) {
        // Gets location from user to save file
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setSelectedFile(new File(defaultName));
        int result = saveChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return saveChooser.getSelectedFile();
        }
        return null;
    }

    private static File getLocalFile(File file, Cipher cipher) throws IOException {
        /*
         * Creates local directory for user in source root if not exists
         * encrypts the file in local mode
         */
        File userDirectory = new File("local/" + GUI.loggedInUser);
        if (!userDirectory.exists())
            userDirectory.mkdirs();
        File localFile = new File(userDirectory, file.getName() + ".enc");
        // locally encrypted file with .enc extension
        try (FileInputStream fis = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(localFile);

             // Cipher output stream for buffered r/w and realtime cipher update
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            byte[] buffer = new byte[64 * 1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
        return localFile;
    }

    private static void localMode(File file, Cipher cipher, String encodedKey, byte[] iv) {
        try {
            String encodedIV = Base64.getEncoder().encodeToString(iv);
            File localFile = getLocalFile(file, cipher);
            MainFrame.feedbackArea.append("\n<- File saved locally at: " + localFile.getAbsolutePath() + "\n");

            //Saving meta-data in database
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement fileStmt = conn.prepareStatement(
                         "INSERT INTO files (filename, filedata, username, isEncrypted) VALUES (?,?,?,?)");
                 PreparedStatement keyStmt = conn.prepareStatement(
                         "INSERT INTO keys (id, username, filename, key,IniVec) VALUES (?, ?, ?, ?,?)")) {
                fileStmt.setString(1, file.getName());
                fileStmt.setString(2, "Local file"); //Storing meta string in file data as filedata is already stored locally
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
                    keyStmt.setString(5, encodedIV);
                    keyStmt.executeUpdate();
                }
            }
        } catch (IOException | SQLException e) {
            MainFrame.feedbackArea.append("Error in file encryption");
        }
        return;
    }

    protected static void customDecryption() {
        /*
         * Custom decryption for user files
         * select AES encrypted file , encoded key , encoded IV
         * Can be used to decrypt files from other clients
         */
        executor.submit(() -> {
            try {
                Cipher cipher = null;
                File inputFile = null;
                JFileChooser fileChooser = new JFileChooser("local\\" + GUI.loggedInUser + "\\");
                int result = fileChooser.showOpenDialog(null);
                if (result != JFileChooser.APPROVE_OPTION) {
                    MainFrame.feedbackArea.append("\nNo file selected for decryption.\n");
                    return;
                }
                String[] options = {"Cipher-Block-Chaining", "Electronic Code Book"};
                int dectype = JOptionPane.showOptionDialog(null, "Select mode of Decryption", "Select mode", -1, 3, null, options, options[1]);
                int padding = JOptionPane.showConfirmDialog(null, "Is the file encrypted with PKCS5 padding ?", "Select mode", JOptionPane.YES_NO_CANCEL_OPTION);
                String pkcs = (padding == 0 ? "/PKCS5Padding" : "");
                inputFile = fileChooser.getSelectedFile();
                InputStream encryptedStream = null;
                String filename = null;

                encryptedStream = new BufferedInputStream(new FileInputStream(inputFile));
                filename = inputFile.getName();
                String encodedKey = JOptionPane.showInputDialog(null, "Enter the key  for decryption:");
                if (encodedKey == null || encodedKey == "") {
                    return;
                }
                // Key decoded to byte array for decryption
                byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");  // key spec from decoded key
                if (dectype == 0) {
                    //TYPE : CBC
                    String encodedIV = JOptionPane.showInputDialog(null, "Enter the Initialization vector for decryption:");
                    byte[] iv = Base64.getDecoder().decode(encodedIV);
                    IvParameterSpec ivspec = new IvParameterSpec(iv);
                    cipher = Cipher.getInstance("AES/CBC" + pkcs);
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
                } else {
                    //TYPE : ECB
                    cipher = Cipher.getInstance("AES" + pkcs);
                    cipher.init(Cipher.DECRYPT_MODE, secretKey);
                }
                File saveFile = getSaveFile(filename);
                // saving file locally after decryption
                if (saveFile == null) {
                    MainFrame.feedbackArea.append("\nDecryption canceled.\n");
                    return;
                }
                try (CipherInputStream cis = new CipherInputStream(encryptedStream, cipher);
                     OutputStream decryptedOutputStream = new BufferedOutputStream(
                             new FileOutputStream(saveFile))) {
                    byte[] buffer = new byte[1024 * 1024]; // 1Mb output buffer
                    int bytesRead;
                    while ((bytesRead = cis.read(buffer)) != -1) {
                        decryptedOutputStream.write(buffer, 0, bytesRead);
                    }
                }

                MainFrame.feedbackArea
                        .append("\nFile decrypted and saved: " + saveFile.getAbsolutePath() + "\n");
            } catch (Exception e) {
                MainFrame.feedbackArea.append("\nError decrypting file: " + e.getMessage() + "\n");

            }
        });
    }

    protected static void hashfile(File file, int mode) {
        /*
         * Calculates file checksum with SHA-256/512
         * Buffered for large files
         */
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
                MainFrame.feedbackArea.setText("\n" + Algo + " Hash: " + hexString.toString() + "\n");
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
