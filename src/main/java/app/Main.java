package app;

import ui.DBEditorUI;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.util.Properties;
import db.DBConnection;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        // Load config
        Properties cfg = new Properties();
        try {
            cfg.load(new FileInputStream("config.properties"));
        } catch (Exception e) {
            System.out.println("Could not load config.properties, using defaults.");
        }
        DBConnection.configure(cfg);

        SwingUtilities.invokeLater(() -> {
            try {
                // Apply custom medium theme
                applyMediumTheme();
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }
            DBEditorUI ui = new DBEditorUI();
            ui.setVisible(true);
        });
    }
    
    private static void applyMediumTheme() {
        // Custom medium theme - not too dark, not too light
        UIManager.put("Panel.background", new Color(245, 245, 245));
        UIManager.put("Panel.foreground", new Color(60, 60, 60));
        UIManager.put("Button.background", new Color(230, 230, 230));
        UIManager.put("Button.foreground", new Color(50, 50, 50));
        UIManager.put("TextField.background", new Color(255, 255, 255));
        UIManager.put("TextField.foreground", new Color(30, 30, 30));
        UIManager.put("TextArea.background", new Color(252, 252, 252));
        UIManager.put("TextArea.foreground", new Color(25, 25, 25));
        UIManager.put("Table.background", new Color(255, 255, 255));
        UIManager.put("Table.foreground", new Color(30, 30, 30));
        UIManager.put("TableHeader.background", new Color(240, 240, 240));
        UIManager.put("TableHeader.foreground", new Color(50, 50, 50));
        UIManager.put("ToolBar.background", new Color(235, 235, 235));
        UIManager.put("ToolBar.foreground", new Color(50, 50, 50));
        UIManager.put("SplitPane.background", new Color(245, 245, 245));
        UIManager.put("ScrollBar.background", new Color(240, 240, 240));
        UIManager.put("ScrollBar.foreground", new Color(180, 180, 180));
        
        // Apply FlatLaf with custom colors
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            // Fallback to system LAF
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }
}
