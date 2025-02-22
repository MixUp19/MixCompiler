package org.compiler.controller;

import org.compiler.model.LectorMix;
import org.compiler.model.Lexer;
import org.compiler.model.Parser;
import org.compiler.model.Semantic;
import org.compiler.view.Pantalla;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Controller implements ActionListener{
    Pantalla pantalla;
    Lexer lexer;
    Parser parser;
    Semantic semantic;
    public Controller(
            Pantalla pantalla
    ){
        this.pantalla = pantalla;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("dbugger");
        if(e.getSource() == pantalla.getScanButton()){
            lexer = new Lexer(pantalla.getText());
            parser = new Parser(lexer.getTokens());
            pantalla.colocarTokens(lexer.getTablaTokens());
            return;
        }
        if(e.getSource() == pantalla.getParseButton()){
            if(parser == null){
                pantalla.setLog("No se ha realizado el análisis léxico", true, "parser");
                return;
            }
            pantalla.setLog(parser.getMessage(), parser.isError(), "parser");
            semantic = new Semantic(parser.isSemanticError(), parser.getSemanticErrorMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
            return;
        }
        if(e.getSource() == pantalla.getSemanticButton()){
            if(semantic == null){
                pantalla.setLog("No se ha realizado el análisis sintáctico", true, "semantic");
                return;
            }
            pantalla.setLog(semantic.getMessage(), semantic.isError(), "semantic");
            return;
        }
        if (e.getSource() == pantalla.getFileMenu()){
            String path = pantalla.fileSelection();
            if (path == null) return;
            LectorMix.leerArchivoMix(path);
            pantalla.setText(LectorMix.leerArchivoMix(path));
        }
    }

}
