import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/** DbHandler.java
* Methods for handling backend of database operations
* Creates necessary tables on startup of system
* @author Yash Shinde
*/

public class DbHandler {
    protected static final String DB_URL = "jdbc:sqlite:unified.db"; //Database URL , name : unified.db
    private static final String CREATE_USERDATA_TABLE_SQL = "CREATE TABLE IF NOT EXISTS userdata (id INTEGER PRIMARY KEY AUTOINCREMENT ,username TEXT NOT NULL UNIQUE , password TEXT NOT NULL, Last_Login TEXT)";
    private static final String CREATE_FILES_TABLE_SQL = "CREATE TABLE IF NOT EXISTS files(id INTEGER PRIMARY KEY AUTOINCREMENT,filename TEXT NOT NULL,filedata BLOB NOT NULL,username TEXT NOT NULL, isEncrypted INTEGER NOT NULL DEFAULT 0)";
    private static final String CREATE_KEYS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS keys (keyId INTEGER PRIMARY KEY AUTOINCREMENT,id INTEGER, username TEXT NOT NULL , filename TEXT NOT NULL, key TEXT NOT NULL,IniVec VARBINARY(16) NOT NULL,FOREIGN KEY (id) REFERENCES files(id) ON DELETE CASCADE,FOREIGN KEY (username) REFERENCES userdata(username) ON DELETE CASCADE)";
    private static final String CREATE_ADMIN_DATA_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Admindata (id INTEGER PRIMARY KEY AUTOINCREMENT ,AdminName TEXT NOT NULL UNIQUE , password TEXT NOT NULL, Last_Login TEXT)";
    static void setupDatabase() {
     
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON"); //Foreign key support
            stmt.execute(CREATE_FILES_TABLE_SQL);
            stmt.execute(CREATE_USERDATA_TABLE_SQL);
            stmt.execute(CREATE_KEYS_TABLE_SQL);
            stmt.execute(CREATE_ADMIN_DATA_TABLE_SQL);
            AdminInit();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Error setting up database: \n" + e.getMessage() + "\n");
        }
    }
static void AdminInit(){  //Initializes default Admin 
    String initAdm = UserManager.hashPassword("Admin");
    String checkAdminSQL = "SELECT COUNT(*) FROM Admindata";
    try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmtCheck = conn.prepareStatement(checkAdminSQL)) { 
        ResultSet rs = pstmtCheck.executeQuery();
    if(rs.next()){
        if (rs.getInt(1) == 0) {  //Creates default admin when no admin exists
            String insertAdminSQL = "INSERT INTO Admindata (AdminName, password) VALUES (?, ?)";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertAdminSQL)) {
                pstmtInsert.setString(1, "Admin");  
                pstmtInsert.setString(2, initAdm); 
                pstmtInsert.executeUpdate(); 
            }
        } else {
            return;
        }
    }  }
 catch (SQLException e) {
    JOptionPane.showMessageDialog(null, "Error setting up database: \n" + e.getMessage() + "\n");
}
}
 
static void executeSQLQuery(JTextArea feedbackArea, String query, DefaultTableModel tableModel){
//Admin tool , executes sql query from string and displays output in tablemodel        
    if (query.isEmpty()) {
            feedbackArea.setText("Please enter a query.\n");
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            if (query.toLowerCase().startsWith("select")) { //Display
                ResultSet rs = stmt.executeQuery(query);
                displayResultSet(rs, tableModel);
            } else {
                int rowsAffected = stmt.executeUpdate(query);
                feedbackArea.append("\nQuery executed successfully. Rows affected: " + rowsAffected + "\n");
            }
        } catch (SQLException e) {
            feedbackArea.append("\nError executing query: " + e.getMessage() + "\n");
        }
    }
    static void displayResultSet(ResultSet rs, DefaultTableModel tableModel) throws SQLException {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            tableModel.addColumn(metaData.getColumnName(i));
        }
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            tableModel.addRow(row);
        }
    }

    static void deletefile(JTextArea feedbackArea, String fs, int id) { 
        //Deletes file and associated key by id or filename
        String query,kquery;
        {
            if (fs == null) {
                query = "DELETE FROM files where id = ? AND username= ?";
                kquery = "DELETE FROM keys WHERE id = ? AND username= ?";;
            } else {
                query = "DELETE FROM files WHERE filename = ? AND username= ?";;
                kquery = "DELETE FROM keys WHERE filename = ?AND username= ?";;
            }
            try (Connection conn = DriverManager.getConnection(DB_URL);
                    PreparedStatement stmt = conn.prepareStatement(query);
                    PreparedStatement keyStmt = conn.prepareStatement(kquery);) {
                        if(fs == null){
                            stmt.setInt(1, id);
                            stmt.setString(2,GUI.loggedInUser);
                            keyStmt.setInt(1, id);
                            keyStmt.setString(2,GUI.loggedInUser);
                        }
                        else{ 
                            stmt.setString(2,GUI.loggedInUser);
                            stmt.setString(1, fs);
                            keyStmt.setString(1, fs);
                            keyStmt.setString(2,GUI.loggedInUser);
                        }
                        
                int rowsAffected = stmt.executeUpdate();
                rowsAffected +=  keyStmt.executeUpdate();

                feedbackArea.append(rowsAffected > 0 ? rowsAffected + " rows affected." : rowsAffected + "\n Error , File does not exist for user");
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    static void deleteuser(JTextArea feedbackArea, String urs) {
        //Removes user,userfiles,userkeys
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error setting up database: \n" + e.getMessage() + "\n");
        }
        String query = "DELETE FROM userdata WHERE username = ?";
        String query2 = "DELETE FROM files WHERE username= ?";
        String query3 = "DELETE FROM keys where username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(query);
                PreparedStatement s2tmt = conn.prepareStatement(query2);
                PreparedStatement s3mt = conn.prepareStatement(query3)) {
            stmt.setString(1, urs);
            s2tmt.setString(1, urs);
            s3mt.setString(1, urs);
            int rowsAffected = stmt.executeUpdate() + s2tmt.executeUpdate() + s3mt.executeUpdate();
            feedbackArea.append(rowsAffected + " rows deleted.\n");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

}
