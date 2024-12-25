import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationModule {

    private static final String USERS_FILE = "users.txt";
    private static final String ALGORITHM = "AES";
    
    private Map<String, String> users;

    public AuthenticationModule() {
        users = new HashMap<>();
        loadUsers();
    }

    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; 
        }
        String encryptedPassword = encryptPassword(password);
        users.put(username, encryptedPassword);
        saveUsers();
        return true;
    }

    public boolean authenticateUser(String username, String password) {
        String encryptedPassword = users.get(username);
        if (encryptedPassword == null) {
            return false; 
        }
        String inputEncryptedPassword = encryptPassword(password);
        return encryptedPassword.equals(inputEncryptedPassword);
    }

    private String encryptPassword(String password) {
        try {
            
            return Base64.getEncoder().encodeToString(password.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting password", e);
        }
    }

    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading user data.");
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving user data.");
        }
    }
}
