import java.awt.BorderLayout;
import java.awt.*;
import java.io.File;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class MainFrame {
    private final JComboBox<File> driveComboBox;
    private final JPanel filePanel;
    private final JButton backButton;
    private final DefaultTableModel tableModel;    
    protected static JTextArea feedbackArea;
    protected static JFrame mainframe;

    public MainFrame(){
          feedbackArea = new JTextArea(5, 40);
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
        vk.addActionListener(e -> DbHandler.executeSQLQuery(feedbackArea,"SELECT* FROM KEYS",tableModel));
        fileButtonPanel.add(SHA2);
        fileButtonPanel.add(SHA5);
        fileButtonPanel.add(vf);
        fileButtonPanel.add(vk);
        fileButtonPanel.add(enc);
        fileButtonPanel.add(dec);
        fileButtonPanel.add(rm_file);
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
    }
}
