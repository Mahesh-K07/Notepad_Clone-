import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;

public class FullNotepadClone extends JFrame implements ActionListener {
    JTextArea textArea;
    JFileChooser fileChooser;
    File currentFile;
    UndoManager undoManager;
    JLabel statusBar;

    public FullNotepadClone() {
        setTitle("Notepad Clone");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane);

        // Setup Undo Manager
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        // Setup Menu
        setJMenuBar(createMenuBar());

        // File Chooser
        fileChooser = new JFileChooser();

        // Status Bar
        statusBar = new JLabel("Ln 1, Col 1");
        add(statusBar, BorderLayout.SOUTH);

        // Update Status Bar
        textArea.addCaretListener(e -> updateStatusBar());

        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        String[] fileItems = {"New", "Open", "Save", "Save As", "Print", "Exit"};
        for (String item : fileItems) addItem(file, item);

        JMenu edit = new JMenu("Edit");
        String[] editItems = {"Undo", "Cut", "Copy", "Paste", "Delete", "Find", "Replace", "Select All", "Time/Date"};
        for (String item : editItems) addItem(edit, item);

        JMenu format = new JMenu("Format");
        addItem(format, "Word Wrap");
        addItem(format, "Font");

        JMenu view = new JMenu("View");
        addItem(view, "Zoom In");
        addItem(view, "Zoom Out");

        JMenu help = new JMenu("Help");
        addItem(help, "About Notepad");

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(format);
        menuBar.add(view);
        menuBar.add(help);

        return menuBar;
    }

    private void addItem(JMenu menu, String name) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(this);
        menu.add(item);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "New": textArea.setText(""); currentFile = null; break;
            case "Open": openFile(); break;
            case "Save": saveFile(false); break;
            case "Save As": saveFile(true); break;
            case "Print": printFile(); break;
            case "Exit": System.exit(0); break;
            case "Undo": if (undoManager.canUndo()) undoManager.undo(); break;
            case "Cut": textArea.cut(); break;
            case "Copy": textArea.copy(); break;
            case "Paste": textArea.paste(); break;
            case "Delete": textArea.replaceSelection(""); break;
            case "Select All": textArea.selectAll(); break;
            case "Time/Date": textArea.insert(new Date().toString(), textArea.getCaretPosition()); break;
            case "Word Wrap": textArea.setLineWrap(!textArea.getLineWrap()); break;
            case "Font": changeFont(); break;
            case "Zoom In": zoomText(2); break;
            case "Zoom Out": zoomText(-2); break;
            case "About Notepad": showAbout(); break;
            case "Find": findText(); break;
            case "Replace": replaceText(); break;
        }
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(reader, null);
            } catch (IOException ex) {
                showError("Error opening file.");
            }
        }
    }

    private void saveFile(boolean saveAs) {
        if (currentFile == null || saveAs) {
            if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            currentFile = fileChooser.getSelectedFile();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            textArea.write(writer);
        } catch (IOException ex) {
            showError("Error saving file.");
        }
    }

    private void printFile() {
        try {
            textArea.print();
        } catch (Exception ex) {
            showError("Error printing.");
        }
    }

    private void changeFont() {
        Font current = textArea.getFont();
        Font newFont = new Font("Consolas", Font.PLAIN, current.getSize() + 2);
        textArea.setFont(newFont);
    }

    private void zoomText(int delta) {
        Font font = textArea.getFont();
        int newSize = font.getSize() + delta;
        if (newSize > 4) textArea.setFont(new Font(font.getFontName(), font.getStyle(), newSize));
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this, "Notepad Clone\nBuilt in Java", "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateStatusBar() {
        int pos = textArea.getCaretPosition();
        try {
            int line = textArea.getLineOfOffset(pos);
            int col = pos - textArea.getLineStartOffset(line);
            statusBar.setText("Ln " + (line + 1) + ", Col " + (col + 1));
        } catch (Exception ignored) {}
    }

    private void findText() {
        String find = JOptionPane.showInputDialog(this, "Find:");
        if (find != null) {
            String content = textArea.getText();
            int index = content.indexOf(find, textArea.getCaretPosition());
            if (index >= 0) {
                textArea.select(index, index + find.length());
            } else {
                showError("Text not found.");
            }
        }
    }

    private void replaceText() {
        String find = JOptionPane.showInputDialog(this, "Find:");
        String replace = JOptionPane.showInputDialog(this, "Replace with:");
        if (find != null && replace != null) {
            String content = textArea.getText();
            textArea.setText(content.replace(find, replace));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FullNotepadClone::new);
    }
}
