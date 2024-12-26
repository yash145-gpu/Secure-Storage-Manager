import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthenticationModule {
    private final UserManager userManager;

    public AuthenticationModule(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean authenticateUser(String username, String password) {
        String storedHash = userManager.getPasswordHash(username);
        if (storedHash == null) {
            return false;
        }
        return verifyPassword(password, storedHash);
    }

    

    private boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedPasswordHash = Base64.getDecoder().decode(parts[1]);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] computedHash = md.digest(password.getBytes());

            return MessageDigest.isEqual(storedPasswordHash, computedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
