package org.compiler.controller;

import org.compiler.model.*;
import org.compiler.view.Pantalla;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Controller implements ActionListener{
    Pantalla pantalla;
    Lexer lexer;
    Parser parser;
    Semantic semantic;
    CodigoIntermedio ci;
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
            semantic = new Semantic(parser.isSemanticError(), parser.getSemanticErrorMessage(), parser.getTablaID(), parser.getRoot());
            return;
        }
        if(e.getSource() == pantalla.getSemanticButton()){
            if(semantic == null){
                pantalla.setLog("No se ha realizado el análisis sintáctico", true, "semantic");
                return;
            }
            pantalla.setLog(semantic.getMessage(), semantic.isError(), "semantic");
            ci = new CodigoIntermedio(parser.getTablaID(),
                    parser.getRoot());
            return;
        }
        if (e.getSource() == pantalla.getCIbutton()){
            if (ci == null){
                pantalla.setIntermediateCode("No se ha realizado el análisis semantic");
                return;
            }
            pantalla.setIntermediateCode(ci.getCodigoIntermedio());
        }
        if (e.getSource() == pantalla.getFileMenu()){
            String path = pantalla.fileSelection();
            if (path == null) return;
            LectorMix.leerArchivoMix(path);
            pantalla.setText(LectorMix.leerArchivoMix(path));
        }
        if (e.getSource() == pantalla.getRunButton()){
            LectorMix.runIntermediateCode(pantalla.getIntermediateCode());
        }
    }

}
