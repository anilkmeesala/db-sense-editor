package db;

import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class DynamicSuggestions {

    public static class CaseInsensitiveCompletionProvider extends DefaultCompletionProvider {
        
        @Override
        public List<Completion> getCompletions(JTextComponent comp) {
            List<Completion> completions = super.getCompletions(comp);
            List<Completion> filtered = new ArrayList<>();
            
            String text = getAlreadyEnteredText(comp);
            if (text == null) {
                return completions;
            }
            String lowerText = text.toLowerCase();
            
            for (Completion completion : completions) {
                String inputText = completion.getInputText();
                if (inputText != null && inputText.toLowerCase().startsWith(lowerText)) {
                    filtered.add(completion);
                }
            }
            
            return filtered;
        }

        @Override
        public boolean isAutoActivateOkay(JTextComponent comp) {
            String text = getAlreadyEnteredText(comp);
            return text != null && text.length() >= 1; // trigger after 1+ chars (so 2 letters work)
        }
    }

    public static DefaultCompletionProvider buildProvider() {
        CaseInsensitiveCompletionProvider provider = new CaseInsensitiveCompletionProvider();

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

        // Try to add DB-specific metadata
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE","VIEW"});
            while (tables.next()) {
                String table = tables.getString("TABLE_NAME");
                provider.addCompletion(new BasicCompletion(provider, table));

                // columns
                ResultSet cols = meta.getColumns(null, null, table, "%");
                while (cols.next()) {
                    String col = cols.getString("COLUMN_NAME");
                    String type = cols.getString("TYPE_NAME");
                    provider.addCompletion(new VariableCompletion(provider, table + "." + col, type));
                    provider.addCompletion(new VariableCompletion(provider, col, type));
                }
                cols.close();
            }
            tables.close();
        } catch (SQLException ex) {
            System.err.println("Could not load DB metadata: " + ex.getMessage());
        }

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
