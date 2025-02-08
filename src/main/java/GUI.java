import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**Project : Secure Storage Manager
 * @version 1.1.0
 * This is the main class to the project
 * @author Yash Shinde
 */

public class GUI extends JFrame implements Runnable{

    protected static JFrame login;
    private static BufferedImage img;
    protected static String loggedInUser;
    private final JLabel label1,label2,srl;
    protected static Dimension screenSize;
    protected static JPasswordField passwdField;
    protected static JTextField userField;
    protected static ImageIcon imz;
    GUI(){
      DbHandler.setupDatabase();   //Initializes all database tables at startup
     
      run();                       //GUI login image generation
        JPanel backgroundPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();  //For native screen resolution with factors for layout components
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int sw = screenWidth - 620;     
        int sh = screenHeight - 680;
        imz = new ImageIcon(getClass().getResource("ism.png"));
        login = new JFrame("Login");
        login.setIconImage(imz.getImage());
        login.setLayout(new BorderLayout());
        login.setSize(screenWidth, screenHeight);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login.setExtendedState(JFrame.MAXIMIZED_BOTH);
        backgroundPanel.setLayout(null);

        JLabel label3 = new JLabel("Username");
        label3.setFont(new Font("ARIAL", Font.BOLD, 26));
        label3.setForeground(Color.WHITE);
        label3.setBounds(sw, sh, 300, 50);
        backgroundPanel.add(label3);
        
        userField = new JTextField();
        userField.setBounds(sw, sh+70, 320, 40);
        backgroundPanel.add(userField);

        JLabel label4= new JLabel("Password");
        label4.setFont(new Font("Arial", Font.BOLD, 26));
        label4.setForeground(Color.WHITE);
        label4.setBounds(sw, sh+150, 300, 50);
        backgroundPanel.add(label4);

        passwdField = new JPasswordField();
        passwdField.setBounds(sw, sh+230, 320, 40);
        backgroundPanel.add(passwdField);
     
        JButton Log = new JButton("LOGIN");
        Log.setFont(new Font("Arial", Font.ITALIC,17));
        Log.setBounds(sw, sh+320, 150, 40);
        Log.setForeground(Color.WHITE);
        Log.setContentAreaFilled(false);
        Log.setFocusable(false);
        backgroundPanel.add(Log);

       
        JButton Admlog = new JButton("Login as Administrator");
        Admlog.setFont(new Font("Arial", Font.ITALIC,17));
        Admlog.setBounds(sw, sh+400, 320, 40);
        Admlog.setForeground(Color.WHITE);
        Admlog.setContentAreaFilled(false);
        Admlog.setFocusable(false);
        backgroundPanel.add(Admlog);

        JButton Reg = new JButton();
        Reg.setFont(new Font("Arial", Font.ITALIC,17));
        Reg.setBounds(sw+160, sh+320, 160, 40);
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
        int ox=0,os=0;
        if(screenWidth > 1366) {os = 10; ox=50;};
        label1 = new JLabel(" Secure Storage");
        label1.setFont(new Font("Arial", Font.ITALIC, 64+os));
        label1.setForeground(Color.WHITE);
        label1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label1.setBounds(18+ox, 10, 600, 80);

        label2 = new JLabel("  Manager");
        label2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label2.setFont(new Font("ARIAL", Font.ITALIC, 64+os));
        label2.setForeground(Color.WHITE);
        label2.setBounds(88+ox, 82, 500, 80);

        srl = new JLabel("<html><u>Github</u></html>");
        srl.setVisible(false);
        srl.setBounds(235+ox,510,200,170);
        srl.setForeground(Color.GRAY);
        srl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton help = new JButton("<html><u>Help</u>");
        help.setBorder(null);
        help.setContentAreaFilled(false);
        help.setFocusable(false);
        help.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                help.setForeground(Color.CYAN);
            }            
            public void mouseExited(MouseEvent e) {
                help.setForeground(Color.WHITE);
            }
        });
        help.setBounds(screenWidth-125, screenHeight-150,100,100);
        help.setFont(new Font("Arial",1,18));
        help.setForeground(Color.WHITE);
        help.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backgroundPanel.add(help);
        backgroundPanel.add(srl);
        backgroundPanel.add(label2);
        backgroundPanel.add(label1);
        backgroundPanel.add(yp);
        login.add(backgroundPanel, BorderLayout.CENTER);
        login.setVisible(true);
      
        help.addActionListener(e ->{JOptionPane.showMessageDialog(null,"Secure Storage Manager release 1.0.0 \n\n"
        +"NEW! : Custom AES Decryption CBC+ECB\n\n"+"AES 256 : Symmetric encryption of files with any extension type , files can be stored on embedded database or local directory based on size ,\ngenerates secure random key and initialization vector.\n\n"
        +"SHA File Checksum : Generates SHA256/512 hash for a file.\n\n"+
        "File Database Backup : Encodes file data and stores directly on embedded database without encryption.\n\n"
        +"Admin login : For directly manipulating database with predefined tools (Default ID,Password : (Admin,Admin) , can be updated).\n\n"
        +"User login : Credentials are hashed and stored securely.\n\n");});
 
/*User login , default Admin is created at startup
  *credentials for Admin can be changed in program as well as in source (DbHandler.java)
  * Default Admin login : username = Admin , password = Admin
 */
        Log.addActionListener(e -> {AuthenticationModule.loginUser(0); });  
        Admlog.addActionListener(e -> {AuthenticationModule.loginUser(1);});
        Reg.addActionListener(e -> UserManager.registerUser(userField,passwdField));
    }

    private JButton getButton() {
        //Custom JButton for link to source
        JButton yp = new JButton();
        yp.setBounds(0, 0, 570,(int)screenSize.getHeight());
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
                srl.setFont(new Font("Arial",Font.BOLD,20));
               
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
            img  = ImageIO.read((getClass().getResource("s12.jpg")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}

