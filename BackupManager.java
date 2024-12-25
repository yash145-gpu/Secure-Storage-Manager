import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.swing.JOptionPane;

public class BackupManager {
    private final String storageDir = "storage/";
    private final String backupDir = "backup/";


    public String backupData(String username) {
        File sourceDir = new File(storageDir + username);
        File backupDirUser = new File(backupDir + username);

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            return "Source directory does not exist or is not a directory.";
        }

        backupDirUser.mkdirs(); 
        File[] filesToBackup = sourceDir.listFiles();

        if (filesToBackup == null || filesToBackup.length == 0) {
            return "No files found to backup.";
        }

        try {
            for (File file : filesToBackup) {
                if (file.isFile()) { 
                    Path sourcePath = file.toPath();
                    Path backupPath = backupDirUser.toPath().resolve(file.getName());
                    Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return "Backup completed successfully!";
        } catch (IOException e) {
            return "Error during backup: " + e.getMessage();
        }
    }
}
