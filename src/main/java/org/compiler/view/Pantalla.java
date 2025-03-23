package org.compiler.view;

import org.compiler.controller.Controller;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Vector;

public class Pantalla extends JFrame {

    private JTextArea textArea;
    private JTable tokenTable;
    private JButton scanButton;
    private JButton parseButton;
    private JButton semanticButton;
    private JButton ciButton;
    private JMenuItem fileMenu;
    private JLabel parserMessage;
    private JLabel semanticMessage;
    private JTextArea intermediateCode;
    private JButton runButton;

    public Pantalla() {
        setTitle("Analizador Léxico");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        createScreen();

        setVisible(true);
    }

    private void createScreen() {
        add(createMenuBar(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileSection = new JMenu("Archivo");
        fileMenu = new JMenuItem("Seleccionar Archivo");
        fileSection.add(fileMenu);
        menuBar.add(fileSection);
        return menuBar;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout());
        mainPanel.add(createTextAreaPanel());
        mainPanel.add(createDebugPanel());
        mainPanel.add(createCodeIntermediatePanel());
        return mainPanel;
    }
    private JPanel createCodeIntermediatePanel(){
        JPanel ciPanel = new JPanel(new BorderLayout());
        intermediateCode = new JTextArea();
        intermediateCode.setFont(new Font("Courier New", Font.PLAIN, 20));
        ciPanel.add(new JScrollPane(intermediateCode), BorderLayout.CENTER);
        ciButton = new JButton("Codigo intermedio");
        runButton = new JButton("Correr");
        var panel = new JPanel(new GridLayout(1, 0));
        panel.add(createButtonPanel(ciButton));
        panel.add(createButtonPanel(runButton));
        ciPanel.add(panel, BorderLayout.SOUTH);
        return ciPanel;
    }
    private JScrollPane createTextAreaPanel() {
        textArea = new JTextArea();
        textArea.setFont(new Font("Courier New", Font.PLAIN, 20));
        return new JScrollPane(textArea);
    }

    private JPanel createDebugPanel() {
        JPanel debugPanel = new JPanel(new GridLayout(3, 1));
        debugPanel.add(createTokenPanel());
        debugPanel.add(createParserPanel());
        debugPanel.add(createSemanticPanel());
        return debugPanel;
    }

    private JPanel createTokenPanel() {
        JPanel tokenPanel = new JPanel(new BorderLayout());
        tokenPanel.add(createTokenTableScrollPane(), BorderLayout.CENTER);
        scanButton = new JButton("Escanear");
        tokenPanel.add(createButtonPanel(scanButton), BorderLayout.SOUTH);
        return tokenPanel;
    }

    private JScrollPane createTokenTableScrollPane() {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Token");
        columnNames.add("Tipo");
        tokenTable = new JTable(new Vector<>(), columnNames);
        tokenTable.setFont(new Font("Courier New", Font.PLAIN, 20));
        return new JScrollPane(tokenTable);
    }

    private JPanel createButtonPanel(JButton boton) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(boton);
        return buttonPanel;
    }

    private JPanel createParserPanel() {
        JPanel parserPanel = new JPanel(new BorderLayout());
        parserMessage = new JLabel();
        parserMessage.setFont(new Font("Courier New", Font.PLAIN, 20));
        parseButton = new JButton("Parsear");
        parserPanel.add(parserMessage, BorderLayout.CENTER);
        parserPanel.add(createButtonPanel(parseButton), BorderLayout.SOUTH);
        return parserPanel;
    }

    private JPanel createSemanticPanel() {
        JPanel semanticPanel = new JPanel(new BorderLayout());
        semanticMessage = new JLabel();
        semanticMessage.setFont(new Font("Courier New", Font.PLAIN, 20));
        semanticButton = new JButton("Analizar Semántica");
        semanticPanel.add(semanticMessage, BorderLayout.CENTER);
        semanticPanel.add(createButtonPanel(semanticButton), BorderLayout.SOUTH);
        return semanticPanel;
    }

    public String fileSelection() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("/home/mixup/Documentos/Universidad/Len y Aut 2"));
        int selection = fileChooser.showOpenDialog(this);
        if (selection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            return file.getAbsolutePath();
        }
        return null;
    }

    public void colocarTokens(Vector<Vector<String>> tokens) {
        DefaultTableModel model = (DefaultTableModel) tokenTable.getModel();
        model.setRowCount(0);
        for (Vector<String> token : tokens) {
            model.addRow(token);
        }
    }

    public void colocarControlador(Controller controller) {
        fileMenu.addActionListener(controller);
        scanButton.addActionListener(controller);
        parseButton.addActionListener(controller);
        semanticButton.addActionListener(controller);
        ciButton.addActionListener(controller);
        runButton.addActionListener(controller);
    }

    public void setLog(String log, boolean error, String type) {
        switch (type) {
            case "parser":
                setParserMessage(log, error);
                break;
            case "semantic":
                setSemanticMessage(log, error);
                break;
        }
    }

    private void setParserMessage(String message, boolean error) {
        parserMessage.setText(formatMessage(message, error));
    }

    private void setSemanticMessage(String message, boolean error) {
        semanticMessage.setText(formatMessage(message, error));
    }

    private String formatMessage(String message, boolean error) {
        String color = error ? "red" : "green";
        return "<html><body style='width: 200px; color: " + color + ";'>" + message + "</body></html>";
    }

    public JButton getScanButton() {
        return scanButton;
    }

    public JButton getParseButton() {
        return parseButton;
    }

    public JButton getSemanticButton() {
        return semanticButton;
    }

    public JMenuItem getFileMenu() {
        return fileMenu;
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }
    public void setIntermediateCode(String text){ intermediateCode.setText(text);}
    public String getIntermediateCode(){return intermediateCode.getText();}
    public JButton getCIbutton(){return ciButton;}
    public JButton getRunButton() {
        return runButton;
    }
}