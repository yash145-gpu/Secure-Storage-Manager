import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminTools {
    static String loggedInUser;
    private final JTextArea queryInput;
    private final DefaultTableModel tableModel;
    protected static  JTextArea feedbackArea;

    AdminTools() {

        JButton view = new JButton("View Users");
        JFrame mainfr = new JFrame("WELCOME ADMIN");
        ImageIcon imz = new ImageIcon(getClass().getResource("ism.png"));
        mainfr.setIconImage(imz.getImage());
        mainfr.setLayout(new BorderLayout());
        mainfr.setSize(GUI.screenSize);
        mainfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainfr.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        JPanel queryInputPanel = new JPanel(new BorderLayout());
        queryInput = new JTextArea("Enter SQL query here",10, 50);
        queryInput.setLineWrap(true);
        queryInput.setFont(new Font("Arial", 0, 16));
       
        JButton executeQueryButton = new JButton("Execute SQL Query");

        queryInputPanel.add(new JLabel("Enter SQL Query:"), BorderLayout.NORTH);
        queryInputPanel.add(queryInput, BorderLayout.CENTER);
        JPanel toolPanel = new JPanel();
        toolPanel.setPreferredSize(new Dimension(200, 500));
       
        JButton viewF = new JButton("View  Files");
        JButton rm_usr = new JButton("Remove User");
        JButton rm_fl = new JButton("Remove Files");
        JButton vm = new JButton("VACUUM DB");
        JButton reset = new JButton("Reset Admin Name/Password");
        JButton lo = new JButton("Log Out");
        JButton DM = new JButton("Dark Mode");
        JButton LM = new JButton("Light Mode");
        toolPanel.add(view);
        toolPanel.add(viewF);
        toolPanel.add(rm_usr);
        toolPanel.add(rm_fl);
        toolPanel.add(reset);
        toolPanel.add(vm); 
        toolPanel.add(DM);
        toolPanel.add(LM);
        toolPanel.add(lo);
        int ht = GUI.screenSize.height/12;
        for(Component c : toolPanel.getComponents()){
            c.setPreferredSize(new Dimension(180,ht));
            c.setFocusable(false);
        }
        queryInputPanel.add(toolPanel, BorderLayout.WEST);
        queryInputPanel.add(executeQueryButton, BorderLayout.SOUTH);

        feedbackArea = new JTextArea(5, 50);
        feedbackArea.setFont(new Font("Arial", 0, 16));
        feedbackArea.setEditable(false);

        tableModel = new DefaultTableModel();
        JTable resultTable = new JTable(tableModel);
        JPanel jpl = new JPanel();
        jpl.setLayout(new BorderLayout());
        jpl.add(new JScrollPane(feedbackArea), BorderLayout.NORTH);
        jpl.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        JScrollPane tableScrollPane = new JScrollPane(jpl);
        tableScrollPane.setPreferredSize(new Dimension(700, 1000));
        

        infoPanel.add(tableScrollPane, BorderLayout.EAST);
        infoPanel.add(queryInputPanel, BorderLayout.CENTER);
        mainfr.add(infoPanel);
        mainfr.setVisible(true);
        rm_usr.addActionListener(e -> {
            String usr = JOptionPane.showInputDialog("Enter Username");

            if (usr == null || usr.trim().isEmpty()) {
                feedbackArea.append("name cannot be empty.\n");
                return;
            }
            DbHandler.deleteuser(feedbackArea, usr);
        });

        rm_fl.addActionListener(e -> {
            String[] op = { "ID", "Name" };
            int choice = JOptionPane.showOptionDialog(toolPanel.getParent(), "Enter file to delete", "Delete mode",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, op, op[0]);
            if (choice == 0) {
                int id = Integer.parseInt(JOptionPane.showInputDialog("Enter file ID"));
                DbHandler.deletefile(feedbackArea, null, id);
            } else {
                String fs = JOptionPane.showInputDialog("Enter File name");
                DbHandler.deletefile(feedbackArea, fs, 0);
            }
        });
      
        lo.addActionListener(e -> {
            GUI.login.setVisible(true);
            mainfr.dispose();
        });
        
        vm.addActionListener(e -> {
            String query = "VACUUM";
            DbHandler.executeSQLQuery(feedbackArea, query, tableModel);
        });

        viewF.addActionListener(e -> {
            String query = "select * from files";
            DbHandler.executeSQLQuery(feedbackArea, query, tableModel);
        });
        view.addActionListener(e -> {
            String query = "select * from userdata";
            DbHandler.executeSQLQuery(feedbackArea, query, tableModel);
        });
        executeQueryButton.addActionListener(e -> {
            String query = queryInput.getText().trim();
            DbHandler.executeSQLQuery(feedbackArea, query, tableModel);
        });
        reset.addActionListener(e -> {
            JFrame tmp = new JFrame("Update Credentials");
            tmp.setLayout(new GridLayout(5, 1));
            tmp.setSize(600, 500);
            tmp.setVisible(true);
            JLabel lon = new JLabel("Current Name");
            JTextField ct = new JTextField("current name");
            JLabel lnn = new JLabel("New Name");
            JTextField nt = new JTextField("new name");
            JLabel lop = new JLabel("Current Password");
            JTextField opt = new JTextField("old Password");
            JLabel lnp = new JLabel("New Password");
            JTextField npt = new JTextField("New Password");
            JButton updt = new JButton("Update");
            tmp.add(lon);
            tmp.add(ct);
            tmp.add(lnn);
            tmp.add(nt);
            tmp.add(lop);
            tmp.add(opt);
            tmp.add(lnp);
            tmp.add(npt);
            tmp.add(updt);

            updt.addActionListener(ee -> {
                String ctt = ct.getText();
                String cpp = opt.getText();
                String cnn = nt.getText();
                String ppnt = npt.getText();
                changeAdminCredentials(ctt, cpp, cnn, ppnt);
            });
        });
        DM.addActionListener(e -> {
            for(Component c : toolPanel.getComponents()){
                c.setBackground(Color.BLACK);
                c.setForeground(Color.WHITE);
            }
          feedbackArea.setBackground(Color.BLACK);
          feedbackArea.setForeground(Color.WHITE);
          for(Component c : queryInputPanel.getComponents()){
            c.setBackground(Color.BLACK);
            c.setForeground(Color.WHITE);
        }
       resultTable.setBackground(Color.BLACK);
       resultTable.setForeground(Color.WHITE);
       resultTable.setFillsViewportHeight(true);
       tableScrollPane.getViewport().setBackground(Color.BLACK);
       toolPanel.setBackground(Color.GRAY);
        });
    
    LM.addActionListener(e -> {
        for(Component c : toolPanel.getComponents()){
            c.setBackground(Color.WHITE);
            c.setForeground(Color.BLACK);
        }
      feedbackArea.setBackground(Color.WHITE);
      feedbackArea.setForeground(Color.BLACK);
      for(Component c : queryInputPanel.getComponents()){
        c.setBackground(Color.WHITE);
        c.setForeground(Color.BLACK);
    }
    toolPanel.setBackground(Color.GRAY);
   resultTable.setBackground(Color.WHITE);
   resultTable.setForeground(Color.BLACK);
   resultTable.setFillsViewportHeight(true);
       tableScrollPane.getViewport().setBackground(Color.WHITE);
    });
}

    public static void changeAdminCredentials(String currentAdminName, String currentPassword, String newAdminName,String newPassword) {
        currentPassword = UserManager.hashPassword(currentPassword);
        newPassword = UserManager.hashPassword(newPassword);
        try (Connection conn = DriverManager.getConnection(DbHandler.DB_URL)) {

            String validateQuery = "SELECT * FROM Admindata WHERE AdminName = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(validateQuery)) {
                pstmt.setString(1, currentAdminName);
                pstmt.setString(2, currentPassword);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String updateQuery = "UPDATE Admindata SET AdminName = ?, password = ? WHERE AdminName = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                        updatePstmt.setString(1, newAdminName);
                        updatePstmt.setString(2, newPassword);
                        updatePstmt.setString(3, currentAdminName);

                        int rowsAffected = updatePstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(null, "Admin credentials updated successfully.");
                        } else {
                            JOptionPane.showMessageDialog(null, "Failed to update admin credentials.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid current admin credentials.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }


}
