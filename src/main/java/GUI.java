import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
public class GUI extends JFrame implements Runnable{
    protected static JFrame login,mainfr;
    private static BufferedImage img;
    protected static String loggedInUser;
    private final JLabel label1,label2,srl;
    private final DefaultTableModel tableModel;
    private final JTextArea feedbackArea;
    private final JComboBox<File> driveComboBox;
    private final JPanel filePanel;
    private final JButton backButton;
   
    GUI(){
      run();
        JPanel backgroundPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        ImageIcon imz = new ImageIcon(getClass().getResource("/Images/ism.png"));
        login = new JFrame("Login");
        login.setIconImage(imz.getImage());
        login.setLayout(new BorderLayout());
        login.setSize(1280, 720);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        backgroundPanel.setLayout(null);

        JLabel label3 = new JLabel("Username");
        label3.setFont(new Font("ARIAL", Font.BOLD, 25));
        label3.setForeground(Color.WHITE);
        label3.setBounds(750, 140, 300, 50);
        backgroundPanel.add(label3);
        JTextField usrfield = new JTextField();
        usrfield.setBounds(750, 200, 320, 40);
        backgroundPanel.add(usrfield);

        JLabel label4= new JLabel("Password");
        label4.setFont(new Font("Arial", Font.BOLD, 25));
        label4.setForeground(Color.WHITE);
        label4.setBounds(750, 260, 300, 50);

        JPasswordField passwdField = new JPasswordField();
        passwdField.setBounds(750, 320, 320, 40);
        backgroundPanel.add(passwdField);
        backgroundPanel.add(label4);
        JButton Log = new JButton("LOGIN");
        Log.setFont(new Font("Arial",2,17));
        Log.setBounds(750, 410, 150, 40);
        Log.setForeground(Color.WHITE);
        Log.setContentAreaFilled(false);
        Log.setFocusable(false);
        backgroundPanel.add(Log);
        JButton Reg = new JButton();
        Reg.setFont(new Font("Arial",2,17));
        Reg.setBounds(910, 410, 160, 40);
        Reg.setText("REGISTER");
        Reg.setForeground(Color.WHITE);
        Reg.setContentAreaFilled(false);
        Reg.setFocusable(false);
        backgroundPanel.add(Reg);
        JButton yp = getButton();
        for(Component c: backgroundPanel.getComponents()){
            Rectangle r = c.getBounds();
            r.x+=70;
            c.setBounds(r);
        }
        label1 = new JLabel("  Secure Storage");
        label1.setFont(new Font("Arial", Font.ITALIC, 62));
        label1.setForeground(Color.WHITE);
        label1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label1.setBounds(18, 10, 800, 75);
        label2 = new JLabel("   Manager");
        label2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label2.setFont(new Font("ARIAL", Font.ITALIC, 62));
        label2.setForeground(Color.WHITE);
        label2.setBounds(88, 82, 800, 80);
        srl = new JLabel("<html><u>Github</u></html>");
        srl.setVisible(false);
        srl.setBounds(250,360,200,170);
        srl.setForeground(Color.GRAY);
        srl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        srl.setFont(new Font("Arial",1,16));
        backgroundPanel.add(srl);
        backgroundPanel.add(label2);
        backgroundPanel.add(label1);
        backgroundPanel.add(yp);
        login.add(backgroundPanel, BorderLayout.CENTER);
        login.setVisible(true);
        feedbackArea = new JTextArea(5, 40);
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
        driveComboBox.addActionListener(e -> FileManager.listFiles((File)(Objects.requireNonNull(driveComboBox.getSelectedItem())),filePanel,backButton,feedbackArea,this));
        driveComboBox.setSelectedIndex(0);
        backButton.addActionListener(e -> FileManager.navigateBack(filePanel,backButton,feedbackArea,this));

        mainfr = new JFrame();
        mainfr.setIconImage(imz.getImage());
        mainfr.setLayout(new BorderLayout());
        mainfr.setSize(1280, 720);
        mainfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        vk.addActionListener(e -> { DbHandler.executeSQLQuery(feedbackArea,"SELECT* FROM KEYS",tableModel);});
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
        feedbackScrollPane.setBackground(Color.GRAY);
        Menu.add(fileButtonPanel,BorderLayout.WEST);
        bp.add(feedbackScrollPane,BorderLayout.EAST);
        bp.add(Menu,BorderLayout.WEST);
        bp.add(tableScrollPane,BorderLayout.CENTER);
        JButton l_out= new JButton("Logout");
        l_out.addActionListener(e-> {mainfr.dispose(); login.setVisible(true);});
;       bp.add(l_out,BorderLayout.SOUTH);
        mainfr.add(bp);

        rm_file.addActionListener( e -> {
            String[] op = {"ID","Name"};
            int choice = JOptionPane.showOptionDialog(this,"Enter file to delete","Delete mode",JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,op,op[0]);
            if(choice == 0){
                int id = Integer.parseInt(JOptionPane.showInputDialog("Enter file ID"));
                System.out.println("ID = "+id);
                DbHandler.deletefile(feedbackArea,null,id);
            }
            else {
                String fs = JOptionPane.showInputDialog("Enter File name");
                DbHandler.deletefile(feedbackArea, fs,0);
            }
        });
        Log.addActionListener(e -> AuthenticationModule.loginUser(usrfield,passwdField,login,mainfr));
        Reg.addActionListener(e -> UserManager.registerUser(usrfield,passwdField));
        enc.addActionListener(e -> SecurityTools.encryptFileToDatabase(feedbackArea,this));
        dec.addActionListener(e -> SecurityTools.decryptFileFromDatabase(feedbackArea,this));
        vf.addActionListener(e -> {  String query = "select * from files where user= "+"'"+loggedInUser+"'"; DbHandler.executeSQLQuery(feedbackArea,query,tableModel);});
        SHA2.addActionListener(e -> {    JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                SecurityTools.hashfile(file,feedbackArea,0);}});
        SHA5.addActionListener(e -> {    JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                SecurityTools.hashfile(file,feedbackArea,1);}});
        saveFileButton.addActionListener(e ->BackupManager.saveFileToDatabase(feedbackArea,this));
        retrieveFileButton.addActionListener(e ->BackupManager.retrieveFileFromDatabase(feedbackArea,this));
    }

    private JButton getButton() {
        JButton yp = new JButton();
        yp.setBounds(0, 0, 570, 720);
        yp.setOpaque(false);
        yp.setContentAreaFilled(false);
        yp.setBorderPainted(false);
        yp.setFocusable(false);
        yp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        yp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label1.setForeground(Color.CYAN);
                label2.setForeground(Color.CYAN);
                srl.setVisible(true);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                label1.setForeground(Color.WHITE);
                label2.setForeground(Color.WHITE);
                srl.setVisible(false);
            }
        });
        yp.addActionListener(e->  {try { Desktop.getDesktop().browse(new URI("https://github.com/yash145-gpu/Secure-Storage-Manager"));

        } catch (IOException | URISyntaxException | UnsupportedOperationException ex) {  String[] cmd = {"xdg-open", "https://github.com/yash145-gpu/Secure-Storage-Manager"};
            try{ Runtime.getRuntime().exec(cmd);}catch(Exception _){}

        }});
        return yp;
    }

    public void  run(){
        try{
            img  = ImageIO.read((Objects.requireNonNull(getClass().getResource("/Images/s12.jpg"))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    
public static void main(String[] args) {
        SwingUtilities.invokeLater(SecureStorageManagerGUI::new);
    }
}
