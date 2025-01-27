import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class DbHandler {
    static final String DB_URL = "jdbc:sqlite:unified.db";
    private static final String CREATE_USERDATA_TABLE_SQL = "CREATE TABLE IF NOT EXISTS userdata (id INTEGER PRIMARY KEY AUTOINCREMENT ,username TEXT NOT NULL UNIQUE , password TEXT NOT NULL, Last_Login TEXT)";
    static final String CREATE_FILES_TABLE_SQL = "CREATE TABLE IF NOT EXISTS files(id INTEGER PRIMARY KEY AUTOINCREMENT,filename TEXT NOT NULL,filedata BLOB NOT NULL,username TEXT NOT NULL, isEncrypted INTEGER NOT NULL DEFAULT 0)";
    static final String CREATE_KEYS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS keys (keyId INTEGER PRIMARY KEY AUTOINCREMENT,id INTEGER, username TEXT NOT NULL , filename TEXT NOT NULL, key TEXT NOT NULL,FOREIGN KEY (id) REFERENCES files(id) ON DELETE CASCADE,FOREIGN KEY (username) REFERENCES userdata(username) ON DELETE CASCADE)";

    static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(CREATE_FILES_TABLE_SQL);
            stmt.execute(CREATE_USERDATA_TABLE_SQL);
            stmt.execute(CREATE_KEYS_TABLE_SQL);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Error setting up database: \n" + e.getMessage() + "\n");
        }
    }

    static void executeSQLQuery(JTextArea feedbackArea, String query, DefaultTableModel tableModel) {

        if (query.isEmpty()) {
            feedbackArea.setText("Please enter a query.\n");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            if (query.toLowerCase().startsWith("select")) {
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
        String query;
        {
            if (fs == null) {
                query = "Delete FROM files where id = ? AND username=" + "'" + GUI.loggedInUser + "'";
                fs = String.valueOf(id);
            } else {
                query = "DELETE FROM files WHERE filename = ? AND username=" + "'" + GUI.loggedInUser + "'";
            }
            try (Connection conn = DriverManager.getConnection(DB_URL);
                    PreparedStatement stmt = conn.prepareStatement(query);
                    PreparedStatement keyStmt = conn.prepareStatement(
                            "DELETE FROM keys WHERE filename=? AND username=" + "'" + GUI.loggedInUser + "'")) {
                stmt.setString(1, fs);
                int rowsAffected = stmt.executeUpdate();
                keyStmt.setString(1, fs);
                rowsAffected += keyStmt.executeUpdate();
                feedbackArea.append(rowsAffected > 0 ? rowsAffected + " rows deleted."
                        : rowsAffected + " Error , File does not exist for user");

            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    static void deleteuser(JTextArea feedbackArea, String urs) {
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
            feedbackArea.append(rowsAffected + " rows deleted.");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

}
