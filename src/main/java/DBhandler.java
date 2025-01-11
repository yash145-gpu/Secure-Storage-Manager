import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
public class DbHandler {
    static final String DB_URL = "jdbc:sqlite:unified.db";

    private static final String CREATE_USERDATA_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS userdata (id INTEGER PRIMARY KEY AUTOINCREMENT ,name TEXT NOT NULL, password TEXT NOT NULL)";

    static final String CREATE_FILES_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS files(id INTEGER PRIMARY KEY AUTOINCREMENT,filename TEXT NOT NULL,filedata BLOB NOT NULL,user TEXT NOT NULL, isEncrypted BOOLEAN NOT NULL DEFAULT 0)";

    static final String CREATE_KEYS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS keys (kid INTEGER PRIMARY KEY AUTOINCREMENT,id INTEGER,user TEXT NOT NULL,filename TEXT NOT NULL, key TEXT NOT NULL,FOREIGN KEY (id) REFERENCES files(id) ON DELETE CASCADE)";

  static void setupDatabase() {

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(CREATE_FILES_TABLE_SQL);
            stmt.execute(CREATE_USERDATA_TABLE_SQL);
            stmt.execute(CREATE_KEYS_TABLE_SQL);
            JOptionPane.showMessageDialog(null,"Database setup successful");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Error setting up database: \n" + e.getMessage() + "\n");
        }
    }
    static void executeSQLQuery(JTextArea feedbackArea,String query , DefaultTableModel tableModel){

        if (query.isEmpty()) {
            feedbackArea.setText("Please enter a query.\n");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            if (query.toLowerCase().startsWith("select")) {
                ResultSet rs = stmt.executeQuery(query);
                displayResultSet(rs,tableModel);
            } else {
                int rowsAffected = stmt.executeUpdate(query);
                feedbackArea.append("\nQuery executed successfully. Rows affected: " + rowsAffected + "\n");
            }
        } catch (SQLException e) {
            feedbackArea.append("\nError executing query: " + e.getMessage() + "\n");
        }
    }

    static void displayResultSet(ResultSet rs , DefaultTableModel tableModel) throws SQLException {
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

    static void deletefile(JTextArea feedbackArea,String fs,int id) {
        String query;
        {
            if (fs == null) {
                query = "Delete FROM files where id = ?";
                fs = String.valueOf(id);
            } else {
                query = "DELETE FROM files WHERE filename = ?";
            }
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement(query); PreparedStatement keyStmt = conn.prepareStatement(
                    "DELETE FROM keys WHERE filename=?")) {

                stmt.setString(1, fs);
                int rowsAffected = stmt.executeUpdate();
                keyStmt.setString(1, fs);
                rowsAffected += keyStmt.executeUpdate();
                feedbackArea.append(rowsAffected + " rows deleted.");
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }
    static void deleteuser(JTextArea feedbackArea , String urs)

    {
        String query = "DELETE FROM userdata WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, urs);
            int rowsAffected = stmt.executeUpdate();

            feedbackArea.append( rowsAffected + " rows deleted.");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        String query2 ="delete from files where user= ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query2)) {
            stmt.setString(1, urs);
            int rowsAffected = stmt.executeUpdate();
            feedbackArea.append( rowsAffected + " rows deleted.");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    }


