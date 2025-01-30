import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileManager {
    protected static File currentDirectory;

    static void listFiles(File directory, JPanel filePanel, JButton backButton) {
        filePanel.removeAll();
        currentDirectory = directory;
        backButton.setEnabled(directory.getParentFile() != null);

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                JButton fileButton = new JButton(file.getName());
                fileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                fileButton.setPreferredSize(new Dimension(30, 100));
                fileButton.addActionListener(e -> {
                    if (file.isDirectory()) {
                        listFiles(file, filePanel, backButton);
                    } else {
                        String[] options = { "Save to DB", "Encrypt and Save", "SHA File Checksum", "Cancel" };
                        int choice = JOptionPane.showOptionDialog(null,
                                "What would you like to do with the file?",
                                "File Action",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);
                        switch (choice) {
                            case 0:
                                BackupManager.saveFileToDatabase(file);
                                break;
                            case 1:
                                SecurityTools.encryptFileToDatabase(file);
                                break;
                            case 2:
                                String[] shs = { "SHA256", "SHA512" };
                                int ch = JOptionPane.showOptionDialog(null,"Please Select Secure Hashing Algorithm ",  "Select SHA Algorithm",
                                        JOptionPane.DEFAULT_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null, shs,
                                        shs[0]);
                                        if(ch==JOptionPane.CLOSED_OPTION || ch == JOptionPane.CANCEL_OPTION){return;}
                                        SecurityTools.hashfile(file, ch);
                                    break;    

                                    }
                    }
                });
                filePanel.add(fileButton);
            }
        }
        filePanel.revalidate();
        filePanel.repaint();
    }

    protected static void navigateBack(JPanel filePanel, JButton backButton) {
        if (currentDirectory != null && currentDirectory.getParentFile() != null) {
            listFiles(currentDirectory.getParentFile(), filePanel, backButton);
        }
    }
}
