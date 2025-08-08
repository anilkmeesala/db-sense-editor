package ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import db.DynamicSuggestions;
import org.fife.ui.autocomplete.AutoCompletion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SQLEditorPanel extends JPanel {
    private RSyntaxTextArea textArea;
    private AutoCompletion ac;

    public SQLEditorPanel() {
        setLayout(new BorderLayout());
        textArea = new RSyntaxTextArea(20, 80);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textArea.setMargin(new Insets(8, 10, 8, 10));
        textArea.setAntiAliasingEnabled(true);
        
        // Apply default theme
        try {
            Theme.load(getClass().getResourceAsStream(
                "/org/fife/ui/rsyntaxtextarea/themes/default.xml")).apply(textArea);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Debugging (can be removed later)
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // System.out.println("Key typed: " + e.getKeyChar());
            }
        });

        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setLineNumbersEnabled(true);
        add(sp, BorderLayout.CENTER);

        // Install auto-completion (dynamic)
        try {
            ac = DynamicSuggestions.install(textArea);
        } catch (Exception e) {
            System.err.println("AutoComplete install failed: " + e.getMessage());
        }
    }
    
    // Removed custom colors as we now use RSyntaxTextArea's built-in themes
    
    

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }
}
