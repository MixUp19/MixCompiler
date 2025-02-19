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
    private JMenuItem fileMenu;
    private JLabel textLog;

    public Pantalla() {
        setTitle("Analizador Léxico");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        hazPantalla();

        setVisible(true);

    }

    private void hazPantalla(){
        JMenuBar menu = hazMenu();
        add(menu , BorderLayout.NORTH);

        JPanel panelPrincipal = new JPanel(new GridLayout());

        textArea = new JTextArea();
        textArea.setFont(new Font("Courier New", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(textArea);
        panelPrincipal.add(scrollPane);

        JPanel tablaScannerPanel = new JPanel(new GridLayout(3,1));

        Vector<String> columnNames = new Vector<>();
        columnNames.add("Token");
        columnNames.add("Tipo");
        Vector<Vector<String>> data = new Vector<>();
        tokenTable = new JTable(data, columnNames);
        tokenTable.setFont(new Font("Courier New", Font.PLAIN, 20));

        JScrollPane tableScrollPane = new JScrollPane(tokenTable);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scanButton = new JButton("Escanear");

        textLog = new JLabel();
        textLog.setForeground(Color.GREEN);
        textLog.setFont(new Font("Courier New", Font.PLAIN, 20));

        tablaScannerPanel.add(tableScrollPane);
        tablaScannerPanel.add(buttonPanel);
        tablaScannerPanel.add(textLog);
        panelPrincipal.add(tablaScannerPanel);
        add(panelPrincipal, BorderLayout.CENTER);


        buttonPanel.add(scanButton);

    }

    private JMenuBar hazMenu(){
        JMenuBar menuBar = new JMenuBar();
        JMenu fileSection = new JMenu("Archivo");

        fileMenu = new JMenuItem("Seleccionar Archivo");

        fileSection.add(fileMenu);
        menuBar.add(fileSection);

        return menuBar;
    }

    public String fileSelection(){
        JFileChooser fileChooser = new JFileChooser();
        int selection = fileChooser.showOpenDialog(this);
        if(selection == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            return file.getAbsolutePath();
        }
        return null;
    }

    public void colocarTokens(Vector<Vector<String>> tokens){
        DefaultTableModel model = (DefaultTableModel) tokenTable.getModel();
        model.setRowCount(0);
        for (Vector<String> token : tokens) {
            model.addRow(token);
        }

    }
    public void colocarControlador(Controller controller){
        fileMenu.addActionListener(controller);
        scanButton.addActionListener(controller);
    }
    public void setLog(String log, boolean error){
        textLog.setText(log);
        if(error){
            textLog.setForeground(Color.RED);
        }else{
            textLog.setForeground(Color.GREEN);
        }
    }

    public JButton getScanButton(){return scanButton;}
    public JMenuItem getFileMenu(){return fileMenu;}
    public String getText(){return textArea.getText();}
    public void setText(String text){textArea.setText(text);}
}