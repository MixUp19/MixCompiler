package org.compiler.model;

import org.compiler.model.util.ExpressionNode;
import org.compiler.model.util.Pair;
import org.compiler.model.util.TiposDeTokens;

import java.util.*;

public class CodigoIntermedio {
    private final StringBuilder codigoIntermedioData;
    private final StringBuilder codigoIntermedioBss;
    private final StringBuilder codigoIntermedioText;
    private final ArrayList<Pair<TiposDeTokens, String, Integer>> tokens;
    private final HashMap<String, ArrayList<String>> identificadores;
    private final HashMap<Vector<String>, ExpressionNode> expressionTrees;
    private final ArrayList<Pair<String, Integer, Integer>> estructurasControl;
    private Pair<String, Integer, Integer> estructuraControlActual;
    private int numIf = 0;
    private int numWhile = 0;

    public CodigoIntermedio(ArrayList<Pair<TiposDeTokens, String, Integer>> tokens,
                            HashMap<String, ArrayList<String>> identificadores,
                            HashMap<Vector<String>, ExpressionNode> expressionTrees,
                            ArrayList<Pair<String, Integer, Integer>> estructurasControl) {
        this.tokens = tokens;
        this.identificadores = identificadores;
        this.expressionTrees = expressionTrees;
        this.codigoIntermedioData = new StringBuilder();
        this.codigoIntermedioBss = new StringBuilder();
        this.codigoIntermedioText = new StringBuilder();
        this.estructurasControl = estructurasControl;
        directiveGenerator();
        estructuraControlActual = estructurasControl.getFirst();
        estructurasControl.removeFirst();
        codeGenerator();
    }

    private void codeGenerator(){
        List<Vector<String>> keys = new ArrayList<>(expressionTrees.keySet());
        keys.sort(Comparator.comparingInt(key -> Integer.parseInt(key.get(1))));
        for(Vector<String> key: keys){
            System.out.printf("variable %s en linea %s con expresión: ", key.getFirst(), key.get(1));
            expressionTrees.get(key).preoder();
            System.out.println();

            String id = key.getFirst();
            ExpressionNode tree = expressionTrees.get(key);
            if (id.equals("read")) {
                readGenerator(tree);
                continue;
            }
            if (id.equals("print") || id.equals("println")) {
                printGenerator(tree);
                continue;
            }
            if (id.equals("IF")) {
                ifGenerator(tree);
                numIf++;
                continue;
            }
            if (id.equals("WHILE")) {
                whileGenerator(tree);
                numWhile++;
                continue;
            }
            System.out.println();
        }
    }

    private void whileGenerator(ExpressionNode tree) {
    }

    private void printGenerator(ExpressionNode tree) {

    }

    private void readGenerator(ExpressionNode tree) {

    }

    private void ifGenerator(ExpressionNode tree){
        boolean elseFlag = lookForElse(estructuraControlActual.getThird());
        if(tree.getLeft() == null && tree.getRight() == null){
            codigoIntermedioText.append("\tMOV\t").append("AL, ").append("[rel ").append(tree.getValue()).append("]").append("\n");
            codigoIntermedioText.append("\tCMP\t").append("AL, ").append("1\n");
        }else{

        }
        if(elseFlag) {
            codigoIntermedioText.append("\tJNE\t").append("ELSE").append(numIf).append("\n");
            elseFlag = false;
        }else{
            codigoIntermedioText.append("\tJNE\t").append("FINIF").append(numIf).append("\n");
        }
    }

    private boolean lookForElse(int linea){
        for(Pair<String, Integer, Integer> estructura : estructurasControl){
            if((estructura.getSecond() == linea || estructura.getSecond() == linea +1) && estructura.getFirst().equals("ELSE")){
                return true;
            }
        }
        return false;
    }
    private void evaluarExpresion(ExpressionNode tree){

    }
    private void directiveGenerator() {
        for (String key : identificadores.keySet()) {
            var valor = getFirstExpression(key);
            if(identificadores.get(key).getFirst().equals("INT")){
                if(valor.equals("?"))
                    codigoIntermedioBss.append(key).append("\t DW\t ?\n");
                else
                    codigoIntermedioData.append(key).append("\t DW\t ").append(valor).append("\n");
            }
            if (identificadores.get(key).getFirst().equals("FLOAT")) {
                if(valor.equals("?"))
                    codigoIntermedioBss.append(key).append("\t DD\t ?\n");
                else
                    codigoIntermedioData.append(key).append("\t DD\t ").append(valor).append("\n");
            }
            if (identificadores.get(key).getFirst().equals("STRING")) {
                if(valor.equals("?"))
                    codigoIntermedioBss.append(key).append("\t DB\t ?\n");
                else {
                    codigoIntermedioData.append(key).append("\t DB\t ").append(valor).append("\n");
                }
            }
            if (identificadores.get(key).getFirst().equals("BOOLEAN")) {
                if(getFirstExpression(key).equals("?"))
                    codigoIntermedioBss.append(key).append("\t DB\t ?\n");
                else {
                    codigoIntermedioData.append(key).append("\t DB\t ").append(valor).append("\n");
                }
            }
        }
    }

    private String getFirstExpression(String id){
        List<Vector<String>> keys = new ArrayList<>(expressionTrees.keySet());
        keys.sort(Comparator.comparingInt(key -> Integer.parseInt(key.get(1))));
        for(Vector<String> key : keys){
            if(!key.getFirst().equals(id)){
               continue;
            }
            ExpressionNode tree = expressionTrees.get(key);
            if(tree.getLeft() != null && tree.getRight() != null){
                return "?";
            }
            if (tree.getValue().equals("true") ){
                expressionTrees.remove(key);
                return "1";
            } else if (tree.getValue().equals("false")) {
                expressionTrees.remove(key);
                return "0";
            }
            expressionTrees.remove(key);
            return tree.getValue();
        }
        return "";
    }

    public String getCodigoIntermedio() {
        return "section .data \n" +
                codigoIntermedioData +
                "section .bss \n" +
                codigoIntermedioBss +
                "section .note.GNU-stack noalloc noexec nowrite progbits \n\n"+
                "section .text \n"+
                "\tdefault rel \n"+
                "\tglobal _start \n"+
                "\textern printf, scanf, puts\n"+
                "_start: \n"+
                "\tPUSH\tRBP\n"+
                "\tMOV\tRBP, RSP\n"+
                codigoIntermedioText;
    }
}