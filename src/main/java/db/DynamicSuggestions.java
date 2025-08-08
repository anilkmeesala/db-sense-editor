package db;

import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class DynamicSuggestions {

    public static class ContextAwareCompletionProvider extends DefaultCompletionProvider {
        private List<String> tableNames = new ArrayList<>();
        private java.util.Map<String, List<VariableCompletion>> tableColumns = new java.util.HashMap<>();

        public void setTableNames(List<String> tables) {
            this.tableNames = tables;
        }

        public void setColumnCompletions(List<VariableCompletion> columns) {
            // Clear existing columns
            tableColumns.clear();
            
            // Group columns by table and store column names
            for (VariableCompletion col : columns) {
                String name = col.getName();
                if (name.contains(".")) {
                    String[] parts = name.split("\\.");
                    String table = parts[0].toUpperCase(); // Normalize table name case
                    String column = parts[1];
                    // Store with normalized table name
                    List<VariableCompletion> tableCols = tableColumns.computeIfAbsent(table, k -> new ArrayList<>());
                    // Check for duplicates before adding
                    boolean isDuplicate = tableCols.stream()
                        .anyMatch(existing -> existing.getName().split("\\.")[1].equals(column));
                    if (!isDuplicate) {
                        VariableCompletion newCol = new VariableCompletion(this, table + "." + column, col.getType());
                        tableCols.add(newCol);
                    }
                }
            }
        }

        @Override
        public List<Completion> getCompletions(JTextComponent comp) {
            String sql = comp.getText().toUpperCase(); // Convert all SQL to uppercase for consistent matching
            List<Completion> completions = new ArrayList<>();

            int caret = comp.getCaretPosition();
            String beforeCaret = sql.substring(0, caret);

            // Table alias detection (supports FROM ... AS ..., JOIN ... AS ..., FROM ... ...)
            java.util.Map<String, String> aliasToTable = new java.util.HashMap<>();
            java.util.regex.Pattern aliasPattern = java.util.regex.Pattern.compile(
                "(?:FROM|JOIN)\\s+(\\w+)(?:\\s+(?:AS\\s+)?(\\w+))?\\b", java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher aliasMatcher = aliasPattern.matcher(sql);
            while (aliasMatcher.find()) {
                String table = aliasMatcher.group(1);
                String alias = aliasMatcher.group(2);
                // If no explicit alias is given, use the table name as its own alias
                aliasToTable.put(alias != null ? alias : table, table);
            }

            // If user types alias. (e.g. e.i or e.) suggest columns for that table filtered by prefix, anywhere in the SQL
            java.util.regex.Pattern aliasColPattern = java.util.regex.Pattern.compile("(\\w+)[.](\\w*)$");  // No CASE_INSENSITIVE needed since input is uppercase
            java.util.regex.Matcher aliasColMatcher = aliasColPattern.matcher(beforeCaret);
            if (aliasColMatcher.find()) {
                String alias = aliasColMatcher.group(1);
                String prefix = aliasColMatcher.group(2);
                String table = aliasToTable.get(alias); // Already uppercase from earlier
                if (table != null) {
                    List<VariableCompletion> cols = tableColumns.get(table.toUpperCase());
                    if (cols != null) {
                        for (VariableCompletion col : cols) {
                            String colName = col.getName().split("\\.")[1]; // Get just the column name
                            // Show all columns after dot, or filter by prefix if typing
                            if (prefix.isEmpty() || colName.toUpperCase().startsWith(prefix)) {
                                // Just add the column name since the alias part is already typed by the user
                                completions.add(new VariableCompletion(this, colName, col.getType()));
                            }
                        }
                    }
                    // Only show column completions for alias context
                    return completions;
                }
            }

            // FROM/JOIN context: suggest tables if caret is after FROM or JOIN keywords
            java.util.regex.Pattern fromPattern = java.util.regex.Pattern.compile("(FROM|JOIN)\\s*(\\w*)$", java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher fromMatcher = fromPattern.matcher(beforeCaret);
            if (fromMatcher.find()) {
                String prefix = fromMatcher.group(2);
                for (String table : tableNames) {
                    if (prefix.isEmpty() || (prefix.length() >= 2 && table.toUpperCase().startsWith(prefix.toUpperCase()))) {
                        completions.add(new BasicCompletion(this, table));
                    }
                }
                return completions;
            }

            // SELECT or WHERE context: suggest columns if after SELECT/WHERE/AND/OR/ON or after operators
            java.util.regex.Pattern selectPattern = java.util.regex.Pattern.compile("(SELECT|WHERE|AND|OR|ON|[=<>!+-])\\s*([\\w\\.,\\s]*)$", java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher selectMatcher = selectPattern.matcher(beforeCaret);
            if (selectMatcher.find()) {
                String prefix = selectMatcher.group(2).trim().toUpperCase();
                // Add column suggestions
                for (List<VariableCompletion> tableCols : tableColumns.values()) {
                    for (VariableCompletion col : tableCols) {
                        String colName = col.getName().split("\\.")[1];
                        // Show suggestions after typing 2 chars or if empty (show all)
                        if (prefix.isEmpty() || (prefix.length() >= 2 && colName.toUpperCase().startsWith(prefix))) {
                            completions.add(new VariableCompletion(this, colName, col.getType()));
                        }
                    }
                }
                return completions;
            }

            // Default: show keywords
            completions.addAll(super.getCompletions(comp));
            return completions;
        }

        @Override
        public boolean isAutoActivateOkay(JTextComponent comp) {
            String text = getAlreadyEnteredText(comp);
            return text != null && text.length() >= 1;
        }
    }

    public static DefaultCompletionProvider buildProvider() {
        ContextAwareCompletionProvider provider = new ContextAwareCompletionProvider();

        // Add basic SQL keywords and functions
        String[] keywords = {
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER",
            "TABLE", "VIEW", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON", "GROUP", "BY",
            "ORDER", "LIMIT", "HAVING", "UNION", "DISTINCT", "AS", "IN", "BETWEEN", "LIKE",
            "IS", "NULL", "NOT", "AND", "OR", "COUNT", "SUM", "AVG", "MAX", "MIN", "DISTINCT"
        };

        for (String keyword : keywords) {
            provider.addCompletion(new BasicCompletion(provider, keyword));
        }

        // Add function completions
        String[] functions = {
            "COUNT(*)", "SUM()", "AVG()", "MAX()", "MIN()", "LENGTH()", "UPPER()", "LOWER()"
        };

        for (String function : functions) {
            provider.addCompletion(new FunctionCompletion(provider, function, "function"));
        }

        // Store tables and columns separately for context-aware completion
        List<String> tables = new ArrayList<>();
        List<VariableCompletion> columns = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String catalog = conn.getCatalog();  // Get current database name
            String schema = conn.getSchema();    // Get current schema
            
            ResultSet tablesRs = meta.getTables(catalog, schema, "%", new String[]{"TABLE","VIEW"});
            while (tablesRs.next()) {
                String table = tablesRs.getString("TABLE_NAME");
                tables.add(table);

                ResultSet cols = meta.getColumns(catalog, schema, table, "%");
                while (cols.next()) {
                    String col = cols.getString("COLUMN_NAME");
                    String type = cols.getString("TYPE_NAME");
                    columns.add(new VariableCompletion(provider, table + "." + col, type)); // Store only table.column format
                }
                cols.close();
            }
            tablesRs.close();
        } catch (SQLException ex) {
            System.err.println("Could not load DB metadata: " + ex.getMessage());
        }

        provider.setTableNames(tables);
        provider.setColumnCompletions(columns);

        return provider;
    }

    public static AutoCompletion install(RSyntaxTextArea textArea) {
        DefaultCompletionProvider provider = buildProvider();
        AutoCompletion ac = new AutoCompletion(provider);

        // Configure auto-completion settings for automatic triggering
        ac.setParameterAssistanceEnabled(true);
        ac.setShowDescWindow(true);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(120); // slightly higher delay for reliable popup

        // Set manual trigger key (Ctrl+Space) as backup
        ac.setTriggerKey(KeyStroke.getKeyStroke("control SPACE"));

        // Install the auto-completion
        ac.install(textArea);

        System.out.println("Case-insensitive auto-completion installed (2+ letters)");
        return ac;
    }
}
