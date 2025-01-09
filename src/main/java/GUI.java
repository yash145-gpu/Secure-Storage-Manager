import java.awt.*;
import java.io.File;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
public class SecureStorageManagerGUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea displayArea;
    private JButton registerButton, loginButton, selectFileButton, decryptFileButton, viewEncryptedFilesButton, backupButton, backButton, refreshButton;
    private JFileChooser fileChooserWithDir;
    private SecureStorage secureStorage;
    private UserManager userManager;
    private AuthenticationModule authenticationModule;
    private String loggedInUser;
    private BackupManager backupData;
    private JPanel loginPanel, buttonPanel;
    private JSplitPane mainSplitPane;
    private JComboBox<File> drivesComboBox;
    private JList<File> fileList;
    private DefaultListModel<File> fileListModel;

    public SecureStorageManagerGUI()  {
        secureStorage = new SecureStorage(); // AES-based SecureStorage class
        userManager = new UserManager();
        backupData = new BackupManager();
        authenticationModule = new AuthenticationModule(userManager);
        fileChooserWithDir = new JFileChooser();
        fileChooserWithDir.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooserWithDir.setCurrentDirectory(new File(System.getProperty("user.home")));

        setupLoginPanel();
        setupMainPanel();

        setTitle("Secure Storage Manager");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;//
        loginPanel.add(passwordField, gbc);

        registerButton = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(registerButton, gbc);

        loginButton = new JButton("Login");
        gbc.gridy = 3;
        loginPanel.add(loginButton, gbc);

        add(loginPanel, BorderLayout.CENTER);

        registerButton.addActionListener(e -> registerUser());
        loginButton.addActionListener(e -> loginUser());
    }

    private void setupMainPanel() {
        // Navigation panel 
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.add(new JLabel("Drives:"));

        drivesComboBox = new JComboBox<>();
        refreshButton = new JButton("Refresh Drives");
        navigationPanel.add(drivesComboBox);
        navigationPanel.add(refreshButton);

        // File list panel
        JPanel filePanel = new JPanel(new BorderLayout());
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        JScrollPane fileScrollPane = new JScrollPane(fileList);
        filePanel.add(fileScrollPane, BorderLayout.CENTER);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigationPanel, filePanel);
        mainSplitPane.setDividerLocation(300);

        // Main panel 
        buttonPanel = new JPanel(new GridLayout(6, 1)); // Vertical navigation bar
        selectFileButton = new JButton("Select Files");
        decryptFileButton = new JButton("Decrypt File");
        viewEncryptedFilesButton = new JButton("View Encrypted Files");
        backupButton = new JButton("Backup Data");
        backButton = new JButton("Log Out");

        
        buttonPanel.add(selectFileButton);
        buttonPanel.add(decryptFileButton);
        buttonPanel.add(viewEncryptedFilesButton);
        buttonPanel.add(backupButton);
        
        buttonPanel.add(backButton);
      


        displayArea = new JTextArea(5, 50);
        displayArea.setEditable(false);
        JScrollPane displayScrollPane = new JScrollPane(displayArea);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.WEST);  // left
        mainPanel.add(displayScrollPane, BorderLayout.SOUTH);

        
        selectFileButton.addActionListener(e -> selectAndEncryptFile()); 
        decryptFileButton.addActionListener(e -> decryptFile());
        viewEncryptedFilesButton.addActionListener(e -> viewEncryptedFiles());
        refreshButton.addActionListener(e -> refreshDrives());
        backButton.addActionListener(e -> switchToLoginPanel()); // Back 
             
             backupButton.addActionListener(e -> backupData.backupData(loggedInUser));
        drivesComboBox.addActionListener(e -> updateFileList((File) drivesComboBox.getSelectedItem()));

        
        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                File selectedFile = fileList.getSelectedValue();
                if (selectedFile != null && selectedFile.isDirectory()) {
                    updateFileList(selectedFile);
                }
            }
        });
    }

    private void registerUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (userManager.registerUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Registration successful.");
        } else {
            JOptionPane.showMessageDialog(this, "User already exists.");
        }
    }
   


    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (authenticationModule.authenticateUser(username, password)) {
            loggedInUser = username;
            switchToMainPanel();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
        }
    }

    private void switchToLoginPanel() {
        setContentPane(loginPanel);
        revalidate();
        repaint();
    }

    private void switchToMainPanel() {
        setContentPane(new JPanel(new BorderLayout()));
        add(mainSplitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.WEST);  
        add(new JScrollPane(displayArea), BorderLayout.SOUTH); 
        revalidate();
        repaint();
    }

    private void refreshDrives() {
        drivesComboBox.removeAllItems();
        fileListModel.clear();
        File[] drives = File.listRoots();
        for (File drive : drives) {
            drivesComboBox.addItem(drive);
        }
    }

    private void updateFileList(File directory) {
        fileListModel.clear();
        if (directory != null) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    fileListModel.addElement(file);
                }
            }
        }
    }

   
    private void selectAndEncryptFile() {
        int returnValue = fileChooserWithDir.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooserWithDir.getSelectedFile();
            if (selectedFile.isFile()) {
                try {
                    String key = secureStorage.encryptFile(selectedFile, loggedInUser); 
                    displayArea.setText("File encrypted successfully.\nEncryption Key: " + key);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error encrypting file: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a file, not a directory.");
            }
        }
    }

    private void decryptFile() {
        File selectedFile = fileList.getSelectedValue();
        if (selectedFile != null && selectedFile.getName().endsWith(".enc")) {
            try {
                File decryptedFile = secureStorage.decryptFile(selectedFile, loggedInUser);
                displayArea.setText("File decrypted successfully.\nDecrypted File: " + decryptedFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error decrypting file: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a valid encrypted (.enc) file from the list.");
        }
    }

    private void viewEncryptedFiles() {
        File userDir = new File("storage/" + loggedInUser);
        fileListModel.clear();
        if (userDir.exists()) {
            File[] files = userDir.listFiles((dir, name) -> name.endsWith(".enc"));
            if (files != null) {
                for (File file : files) {
                    fileListModel.addElement(file);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No encrypted files found.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SecureStorageManagerGUI::new);
    }
}
