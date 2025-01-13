import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminTools {
    static String loggedInUser;
    private final JTextArea queryInput;
    private final DefaultTableModel tableModel;
    private final JTextArea feedbackArea;
    AdminTools(){

JButton view = new JButton("View Users");
JFrame  mainfr = new JFrame("WELCOME ADMIN");
        ImageIcon imz = new ImageIcon(getClass().getResource("/Images/ism.png"));
        mainfr.setIconImage(imz.getImage());
        mainfr.setLayout(new BorderLayout());
        mainfr.setSize(1280, 720);
        mainfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        JPanel queryInputPanel = new JPanel(new BorderLayout());
        queryInput = new JTextArea(10, 50);
        queryInput.setLineWrap(true);
        JScrollPane queryScrollPane = new JScrollPane(queryInput);
        JButton executeQueryButton = new JButton("Execute SQL Query");

        queryInputPanel.add(new JLabel("Enter SQL Query:"), BorderLayout.NORTH);
        queryInputPanel.add(queryScrollPane, BorderLayout.CENTER);
        JPanel toolPanel = new JPanel(new FlowLayout());
       toolPanel.setPreferredSize(new Dimension(100,100));
       toolPanel.add(view);
        JButton viewF = new JButton("View  Files");
       toolPanel.add(viewF);
        JButton rm_usr = new JButton("Remove User");
       toolPanel.add(rm_usr);
        JButton rm_fl = new JButton("Remove Files");
       toolPanel.add(rm_fl);
        JButton vm = new JButton("VACUUM DB");
       toolPanel.add(vm);

        queryInputPanel.add(toolPanel,BorderLayout.WEST);
        queryInputPanel.add(executeQueryButton, BorderLayout.SOUTH);

        feedbackArea = new JTextArea(5, 50);
        feedbackArea.setEditable(false);

        tableModel = new DefaultTableModel();
        JTable resultTable = new JTable(tableModel);

        JPanel jpl = new JPanel();
        jpl.setLayout(new BorderLayout());
        jpl.add(new JScrollPane(feedbackArea),BorderLayout.NORTH);
        jpl.add(new JScrollPane(resultTable),BorderLayout.CENTER);
        JScrollPane tableScrollPane = new JScrollPane(jpl);


        infoPanel.add(tableScrollPane,BorderLayout.EAST);
        infoPanel.add(queryInputPanel,BorderLayout.CENTER);
        mainfr.add(infoPanel);
        mainfr.setVisible(true);
        rm_usr.addActionListener(e -> { String usr = JOptionPane.showInputDialog("Enter Username");

            if (usr== null || usr.trim().isEmpty()) {
                feedbackArea.append("name cannot be empty.\n");
                return;
            } DbHandler.deleteuser(feedbackArea,usr);});

       rm_fl.addActionListener( e -> {
          String[] op = {"ID","Name"};
                   int choice = JOptionPane.showOptionDialog(toolPanel.getParent(),"Enter file to delete","Delete mode",JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,op,op[0]);
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
        JButton lo = new JButton("Logout");
        lo.addActionListener(e-> { GUI.login.setVisible(true); mainfr.dispose();});
       toolPanel.add(lo);
       vm.addActionListener(e -> {  String query = "VACUUM";DbHandler.executeSQLQuery(feedbackArea,query,tableModel);});

        viewF.addActionListener(e -> {  String query = "select * from files";DbHandler.executeSQLQuery(feedbackArea,query,tableModel);});
        view.addActionListener(e -> {  String query = "select * from userdata";DbHandler.executeSQLQuery(feedbackArea,query,tableModel);});
        executeQueryButton.addActionListener(e -> {  String query = queryInput.getText().trim(); DbHandler.executeSQLQuery(feedbackArea,query,tableModel);});

    }
}

