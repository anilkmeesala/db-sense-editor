package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Objects;

public class DBExplorerPanel extends JPanel {
    private final JTree tree;
    private final DefaultMutableTreeNode rootNode;

    public DBExplorerPanel() {
        setLayout(new BorderLayout());
        rootNode = new DefaultMutableTreeNode("Database");
        tree = new JTree(rootNode);
        tree.setRootVisible(true);
        add(new JScrollPane(tree), BorderLayout.CENTER);
        reload();
    }

    public void reload() {
        rootNode.removeAllChildren();
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            // Database/product info at top
            String product = Objects.toString(meta.getDatabaseProductName(), "Database");
            String url = Objects.toString(meta.getURL(), "");
            rootNode.setUserObject(product + " (" + url + ")");

            // Catalogs or current
            ResultSet catalogs = meta.getCatalogs();
            boolean hasCatalogs = false;
            while (catalogs.next()) {
                hasCatalogs = true;
                String catalog = catalogs.getString(1);
                DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(catalog);
                rootNode.add(catNode);
                addTables(meta, catNode, catalog, null);
            }
            catalogs.close();

            if (!hasCatalogs) {
                // Use schemas/tables without catalog
                DefaultMutableTreeNode defaultNode = new DefaultMutableTreeNode("Tables");
                rootNode.add(defaultNode);
                addTables(meta, defaultNode, null, null);
            }
        } catch (Exception ex) {
            DefaultMutableTreeNode err = new DefaultMutableTreeNode("Error: " + ex.getMessage());
            rootNode.add(err);
        }
        ((DefaultTreeModel) tree.getModel()).reload();
        expandOneLevel();
    }

    private void addTables(DatabaseMetaData meta, DefaultMutableTreeNode parent, String catalog, String schemaPattern) throws Exception {
        ResultSet tables = meta.getTables(catalog, schemaPattern, "%", new String[]{"TABLE","VIEW"});
        while (tables.next()) {
            String table = tables.getString("TABLE_NAME");
            DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
            parent.add(tableNode);

            // columns
            ResultSet cols = meta.getColumns(catalog, schemaPattern, table, "%");
            while (cols.next()) {
                String col = cols.getString("COLUMN_NAME");
                String type = cols.getString("TYPE_NAME");
                tableNode.add(new DefaultMutableTreeNode(col + " : " + type));
            }
            cols.close();
        }
        tables.close();
    }

    private void expandOneLevel() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public JTree getTree() { return tree; }
}
