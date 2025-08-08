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
        
        // Apply custom color scheme for medium theme
        applyCustomColors();

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
    
    private void applyCustomColors() {
        // Unified medium theme colors - same as container
        Color bgColor = new Color(247, 249, 252);     // slightly tinted editor bg
        Color fgColor = new Color(31, 41, 55);        // primary text
        Color selectionColor = new Color(205, 227, 255);
        Color currentLineColor = new Color(238, 242, 247);
        
        textArea.setBackground(bgColor);
        textArea.setForeground(fgColor);
        textArea.setCaretColor(new Color(51, 65, 85));
        textArea.setSelectionColor(selectionColor);
        textArea.setCurrentLineHighlightColor(currentLineColor);
        
        // Syntax highlighting colors with better contrast
        org.fife.ui.rsyntaxtextarea.SyntaxScheme scheme = textArea.getSyntaxScheme();
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.RESERVED_WORD).foreground = new Color(30, 100, 200);
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.RESERVED_WORD).font = new Font(Font.MONOSPACED, Font.BOLD, 14);
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_STRING_DOUBLE_QUOTE).foreground = new Color(0, 128, 0);
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_NUMBER_DECIMAL_INT).foreground = new Color(138, 43, 226);
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_EOL).foreground = new Color(107, 114, 128);
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_MULTILINE).foreground = new Color(107, 114, 128);
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.OPERATOR).foreground = new Color(55, 65, 81);
        textArea.setSyntaxScheme(scheme);
    }
    
    private org.fife.ui.rsyntaxtextarea.SyntaxScheme createCustomSyntaxScheme() {
        org.fife.ui.rsyntaxtextarea.SyntaxScheme scheme = textArea.getSyntaxScheme();
        
        // SQL keywords - blue
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.RESERVED_WORD).foreground = new Color(0, 100, 200);
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.RESERVED_WORD).font = new Font(Font.MONOSPACED, Font.BOLD, 14);
        
        // Strings - green
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_STRING_DOUBLE_QUOTE).foreground = new Color(0, 150, 0);
        
        // Numbers - purple
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_NUMBER_DECIMAL_INT).foreground = new Color(150, 0, 150);
        
        // Comments - gray
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_EOL).foreground = new Color(128, 128, 128);
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_MULTILINE).foreground = new Color(128, 128, 128);
        
        // Operators - dark gray
        scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.OPERATOR).foreground = new Color(80, 80, 80);
        
        return scheme;
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }
}
