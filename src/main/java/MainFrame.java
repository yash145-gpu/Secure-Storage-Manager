import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MainFrame {
    private final JComboBox<File> driveComboBox;
    private final JPanel filePanel;
    private final JButton backButton;
    private final DefaultTableModel tableModel;    
    protected static JTextArea feedbackArea =  new JTextArea(5, 40);;
    protected static JFrame mainframe;

    public MainFrame(){
        feedbackArea.setFont(new Font("Arial",0,16));
        feedbackArea.setEditable(false);
        JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);
        JPanel Menu = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        driveComboBox = new JComboBox<>(File.listRoots());
        backButton = new JButton("Back");
        topPanel.add(driveComboBox, BorderLayout.WEST);
        topPanel.add(backButton, BorderLayout.EAST);
        Menu.add(topPanel, BorderLayout.NORTH);
        filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(filePanel);
        scrollPane.setPreferredSize(new Dimension(200,1000));
        Menu.add(scrollPane, BorderLayout.CENTER);
        driveComboBox.addActionListener(e -> FileManager.listFiles((File)(Objects.requireNonNull(driveComboBox.getSelectedItem())),filePanel,backButton));
        driveComboBox.setSelectedIndex(0);
        backButton.addActionListener(e -> FileManager.navigateBack(filePanel,backButton));

        mainframe = new JFrame();
        mainframe.setIconImage(GUI.imz.getImage());
        mainframe.setLayout(new BorderLayout());
        mainframe.setSize(GUI.screenSize.width, GUI.screenSize.height);
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel bp = new JPanel();
        bp.setLayout(new BorderLayout());

        JPanel fileButtonPanel = new JPanel();
        fileButtonPanel.setPreferredSize(new Dimension(200,1000));
        JButton saveFileButton = new JButton("Save File to DB");
        JButton SHA5 = new JButton("SHA-512 Checksum");
        JButton retrieveFileButton = new JButton("Retrieve File from DB");

        tableModel = new DefaultTableModel();
        JTable resultTable = new JTable(tableModel);
        JButton enc = new JButton("AES File Encryption");
        JButton dec = new JButton("AES File Decryption");
        JButton vf = new JButton("View Files");
        JButton rm_file = new JButton("Remove Files");
        JButton SHA2 = new JButton("SHA-256 File Checksum");
        JButton vk = new JButton("View Keys");
        JButton reset = new JButton("Reset username or password");
        vk.addActionListener(e -> DbHandler.executeSQLQuery(feedbackArea,"SELECT* FROM KEYS",tableModel));
        fileButtonPanel.add(SHA2);
        fileButtonPanel.add(SHA5);
        fileButtonPanel.add(vf);
        fileButtonPanel.add(vk);
        fileButtonPanel.add(enc);
        fileButtonPanel.add(dec);
        fileButtonPanel.add(rm_file);
        fileButtonPanel.add(reset);
        fileButtonPanel.add(saveFileButton);
        fileButtonPanel.add(retrieveFileButton);
        for (Component component : fileButtonPanel.getComponents()) {
                component.setBackground(Color.GRAY);
                component.setPreferredSize(new Dimension(200,66));
                component.setFocusable(false);
        }
        JScrollPane tableScrollPane = new JScrollPane(resultTable);
        feedbackScrollPane.setPreferredSize(new Dimension(600,500));
        feedbackScrollPane.setBackground(Color.GRAY);
        Menu.add(fileButtonPanel,BorderLayout.WEST);
        bp.add(feedbackScrollPane,BorderLayout.EAST);
        bp.add(Menu,BorderLayout.WEST);
        bp.add(tableScrollPane,BorderLayout.CENTER);
        JButton Logout= new JButton("Logout");
        Logout.addActionListener(e-> {
            feedbackArea.setText(" ");
            mainframe.dispose();
            GUI.login.setVisible(true);});
            bp.add(Logout,BorderLayout.SOUTH);
            
        mainframe.add(bp);

        rm_file.addActionListener( e -> {
            String[] op = {"ID","Name"};
            int choice = JOptionPane.showOptionDialog(null,"Enter file to delete","Delete mode",JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,op,op[0]);
            if(choice == 0){
                int id = Integer.parseInt(JOptionPane.showInputDialog("Enter file ID"));
                DbHandler.deletefile(feedbackArea,null,id);
            }
            else if(choice==1){
                String fs = JOptionPane.showInputDialog("Enter File name");
                DbHandler.deletefile(feedbackArea, fs,0);
            }
            else{
                return;
            }
        });
          enc.addActionListener(e -> SecurityTools.encryptFileToDatabase());
        dec.addActionListener(e -> SecurityTools.decryptFile());
        vf.addActionListener(e -> {  String query = "select * from files where username= "+"'"+GUI.loggedInUser+"'"; DbHandler.executeSQLQuery(feedbackArea,query,tableModel);});
        SHA2.addActionListener(e -> {    JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                SecurityTools.hashfile(file,0);}});
        SHA5.addActionListener(e -> {    JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                SecurityTools.hashfile(file,1);}});
        saveFileButton.addActionListener(e ->BackupManager.saveFileToDatabase());
        retrieveFileButton.addActionListener(e ->BackupManager.retrieveFileFromDatabase());
         reset.addActionListener(e -> {
            JFrame tmp = new JFrame("Update Credentials");
            tmp.setLayout(new GridLayout(5, 1));
            tmp.setSize(600, 500);
            tmp.setVisible(true);
            JLabel lb_on = new JLabel("Current Name");
            JTextField ct = new JTextField();
            JLabel lb_nn = new JLabel("New Name");
            JTextField nt = new JTextField();
            JLabel lb_op = new JLabel("Current Password");
            JTextField opt = new JTextField();
            JLabel lb_np = new JLabel("New Password");
            JTextField npt = new JTextField();
            JButton updt = new JButton("Update");
            tmp.add(lb_on);
            tmp.add(ct);
            tmp.add(lb_nn);
            tmp.add(nt);
            tmp.add(lb_op);
            tmp.add(opt);
            tmp.add(lb_np);
            tmp.add(npt);
            tmp.add(updt);

            updt.addActionListener(ee -> {
                String ctt = ct.getText();
                String cpp = opt.getText();
                String cnn = nt.getText();
                String ppnt = npt.getText();
                changeUserCredentials(ctt, cpp, cnn, ppnt);
            });
        });
    }
    public static void changeUserCredentials(String currentName, String currentPassword, String newName,String newPassword) {
if(UserManager.userNameAlreadyExists(newName)){
    JOptionPane.showMessageDialog(null, "Username already exists.");
    return;
}
        currentPassword = UserManager.hashPassword(currentPassword);
        newPassword = UserManager.hashPassword(newPassword);
        try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL)) {

            String validateQuery = "SELECT * FROM Userdata WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(validateQuery)) {
                pstmt.setString(1, currentName);
                pstmt.setString(2, currentPassword);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String updateQuery = "UPDATE userdata SET username = ?, password = ? WHERE username = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                        updatePstmt.setString(1, newName);
                        updatePstmt.setString(2, newPassword);
                        updatePstmt.setString(3, currentName);

                        int rowsAffected = updatePstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(null, "Credentials updated successfully.");
                        } else {
                            JOptionPane.showMessageDialog(null, "Failed to update credentials.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid current credentials.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }
}
