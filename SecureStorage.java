import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecureStorage {

    private static final String AES_ALGORITHM = "AES";

    
    public String encryptFile(File file, String username) throws Exception {
        
        byte[] contentBytes = Files.readAllBytes(file.toPath());

        
        SecretKey secretKey = generateAESKey();

        
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(contentBytes);

        
        String encryptedContent = Base64.getEncoder().encodeToString(encryptedBytes);

        
        File userDir = new File("storage/" + username);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        File encryptedFile = new File(userDir, file.getName() + ".enc");
        Files.write(encryptedFile.toPath(), encryptedContent.getBytes());

        
        saveKey(secretKey, username, file.getName());

        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    
    public File decryptFile(File file, String username) throws Exception {
        
        byte[] encryptedBytes = Base64.getDecoder().decode(Files.readAllBytes(file.toPath()));

        
        SecretKey secretKey = loadKey(username, file.getName().replace(".enc", ""));

        
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        
        File decryptedFile = new File(file.getParent(), file.getName().replace(".enc", ""));
        Files.write(decryptedFile.toPath(), decryptedBytes);

        return decryptedFile;
    }

    
    private SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(128); 
        return keyGen.generateKey();
    }

    
    private void saveKey(SecretKey key, String username, String filename) throws Exception {
        byte[] keyBytes = key.getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

        File keyDir = new File("src/keys");
        if (!keyDir.exists()) {
            keyDir.mkdirs();
        }
        File keyFile = new File(keyDir, username + "_" + filename + ".key");
        Files.write(keyFile.toPath(), encodedKey.getBytes(), StandardOpenOption.CREATE);
    }

    
    private SecretKey loadKey(String username, String filename) throws Exception {
        File keyFile = new File("src/keys/" + username + "_" + filename + ".key");
        byte[] encodedKey = Files.readAllBytes(keyFile.toPath());
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, AES_ALGORITHM);
    }
}
