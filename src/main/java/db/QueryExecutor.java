package db;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class QueryExecutor {
    public static DefaultTableModel executeQuery(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            DefaultTableModel model = new DefaultTableModel();
            // column names
            for (int i = 1; i <= cols; i++) {
                model.addColumn(meta.getColumnLabel(i));
            }
            // rows
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 1; i <= cols; i++) {
                    row[i-1] = rs.getObject(i);
                }
                model.addRow(row);
            }
            return model;
        }
    }
}
