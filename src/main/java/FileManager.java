import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileManager {
    protected static File currentDirectory;
     static void listFiles(File directory,JPanel filePanel,JButton backButton,JTextArea feedbackArea,GUI gui) {
         filePanel.removeAll();
        currentDirectory = directory;
        backButton.setEnabled(directory.getParentFile() != null);

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                JButton fileButton = new JButton(file.getName());
                fileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                fileButton.setPreferredSize(new Dimension(30,100));
                fileButton.addActionListener(e -> {
                    if (file.isDirectory()) {
                        listFiles(file,filePanel,backButton,feedbackArea,gui);
                    }
                    else {
                        String[] options = {"Save to DB", "Encrypt and Save", "Cancel"};
                        int choice = JOptionPane.showOptionDialog(gui,
                                "What would you like to do with the file?",
                                "File Action",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]
                        );
                        switch (choice) {
                            case 0:
                                BackupManager.saveFileToDatabase(feedbackArea,file);
                                break;
                            case 1:
                                SecurityTools.encryptFileToDatabase(feedbackArea,file);
                                break;
                            default:
                                System.out.println("Action canceled.");
                        }
                    }
                });

                filePanel.add(fileButton);
            }
        }
        filePanel.revalidate();
        filePanel.repaint();
    }

    protected static void navigateBack(JPanel filePanel,JButton backButton,JTextArea feedbackArea,GUI gui) {
        if (currentDirectory != null && currentDirectory.getParentFile() != null) {
            listFiles(currentDirectory.getParentFile(),filePanel,backButton,feedbackArea,gui);
        }
    }
}

