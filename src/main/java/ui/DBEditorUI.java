package ui;

import db.QueryExecutor;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class DBEditorUI extends JFrame {
    private SQLEditorPanel editorPanel;
    private JTable resultTable;
    private JButton runButton;
    private JLabel statusLabel;

    private void setEditorTheme(String theme) {
        RSyntaxTextArea textArea = editorPanel.getTextArea();
        try {
            switch (theme) {
                case "Dark":
                    org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/dark.xml")).apply(textArea);
                    break;
                case "Monokai":
                    org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml")).apply(textArea);
                    break;
                case "Eclipse":
                    org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml")).apply(textArea);
                    break;
                case "VS Code":
                    org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/vs.xml")).apply(textArea);
                    break;
                case "Solarized Light":
                    org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml")).apply(textArea);
                    break;
                case "Solarized Dark":
                    org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/dark.xml")).apply(textArea);
                    break;
                default:
                    org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/default.xml")).apply(textArea);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading theme: " + e.getMessage(), 
                "Theme Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public DBEditorUI() {
        setTitle("DB Editor");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu themesMenu = new JMenu("Themes");
        String[] themes = {
            "Default", "Dark", "Monokai", "Eclipse", "VS Code", "Solarized Light", "Solarized Dark"
        };
        for (String theme : themes) {
            JMenuItem themeItem = new JMenuItem(theme);
            themeItem.addActionListener(e -> setEditorTheme(theme));
            themesMenu.add(themeItem);
        }
        menuBar.add(themesMenu);
        setJMenuBar(menuBar);

        // Top toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        runButton = new JButton("Run (Ctrl+Enter)");
        runButton.addActionListener(this::onRun);
        toolBar.add(runButton);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> editorPanel.getTextArea().setText(""));
        toolBar.add(clearBtn);
        add(toolBar, BorderLayout.NORTH);

        // Left DB explorer
        DBExplorerPanel explorer = new DBExplorerPanel();

        // Right side: editor and results in vertical split
        editorPanel = new SQLEditorPanel();
        resultTable = new JTable();
        JScrollPane resultScroll = new JScrollPane(resultTable);
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, resultScroll);
        rightSplit.setResizeWeight(0.6);
        rightSplit.setDividerLocation(420);

        // Main split: explorer on left, right side on right
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, explorer, rightSplit);
        mainSplit.setResizeWeight(0.22);
        mainSplit.setDividerLocation(250);
        add(mainSplit, BorderLayout.CENTER);

        // Apply unified colors
        applyUnifiedColors(toolBar, resultScroll);

        // Status bar
        JPanel status = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Ready");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.add(statusLabel, BorderLayout.WEST);
        add(status, BorderLayout.SOUTH);

        // Key binding for Ctrl+Enter to run SQL
        RSyntaxTextArea ta = editorPanel.getTextArea();
        ta.getInputMap().put(KeyStroke.getKeyStroke("control ENTER"), "runQuery");
        ta.getActionMap().put("runQuery", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runButton.doClick();
            }
        });
    }

    private void applyUnifiedColors(JToolBar toolBar, JScrollPane resultScroll) {
        // Unified medium theme with subtle contrast
        Color windowBg = new Color(242, 245, 249);   // app background
        Color panelBg  = new Color(247, 249, 252);   // panels
        Color textFg   = new Color(31, 41, 55);      // primary text
        Color headerBg = new Color(234, 238, 243);   // table header / toolbar
        Color gridCol  = new Color(213, 221, 229);   // grid lines/borders
        Color rowEven  = new Color(250, 252, 255);   // zebra even
        Color rowOdd   = new Color(242, 247, 252);   // zebra odd
        Color selBg    = new Color(205, 227, 255);   // selection

        getContentPane().setBackground(windowBg);

        // Toolbar styling
        toolBar.setBackground(headerBg);
        toolBar.setForeground(textFg);

        // Table styling
        resultTable.setBackground(panelBg);
        resultTable.setForeground(textFg);
        resultTable.setGridColor(gridCol);
        resultTable.setSelectionBackground(selBg);
        resultTable.setSelectionForeground(textFg);
        resultTable.setRowHeight(24);
        resultTable.setShowHorizontalLines(true);
        resultTable.setShowVerticalLines(false);
        resultTable.setIntercellSpacing(new Dimension(0, 0));

        // Zebra striping renderer
        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground((row % 2 == 0) ? rowEven : rowOdd);
                    c.setForeground(textFg);
                }
                return c;
            }
        };
        resultTable.setDefaultRenderer(Object.class, zebra);

        // Header styling
        var header = resultTable.getTableHeader();
        header.setBackground(headerBg);
        header.setForeground(textFg);
        header.setFont(header.getFont().deriveFont(Font.BOLD));

        // Scroll styling
        resultScroll.getViewport().setBackground(panelBg);

        // UI defaults for consistency
        UIManager.put("Panel.background", windowBg);
        UIManager.put("ScrollPane.background", panelBg);
        UIManager.put("SplitPane.background", windowBg);
        UIManager.put("SplitPane.dividerSize", 6);
    }

    private void onRun(ActionEvent ev) {
        String sql = editorPanel.getTextArea().getText();
        if (sql == null || sql.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter SQL to execute.");
            return;
        }
        long start = System.currentTimeMillis();
        try {
            var model = QueryExecutor.executeQuery(sql);
            resultTable.setModel(model);
            long took = System.currentTimeMillis() - start;
            int rows = model.getRowCount();
            statusLabel.setText("Executed successfully: " + rows + " rows in " + took + " ms");
        } catch (SQLException ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "SQL Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
