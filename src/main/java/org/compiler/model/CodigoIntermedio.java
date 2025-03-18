package org.compiler.model;

import org.compiler.model.util.ExpressionNode;
import org.compiler.model.util.Pair;
import org.compiler.model.util.TiposDeTokens;

import java.util.*;

public class CodigoIntermedio {
    private final StringBuilder codigoIntermedioData;
    private final StringBuilder codigoIntermedioBss;
    private final ArrayList<Pair<TiposDeTokens, String, Integer>> tokens;
    private final HashMap<String, ArrayList<String>> identificadores;
    private final HashMap<Vector<String>, ExpressionNode> expressionTrees;

    public CodigoIntermedio(ArrayList<Pair<TiposDeTokens, String, Integer>> tokens, HashMap<String, ArrayList<String>> identificadores, HashMap<Vector<String>, ExpressionNode> expressionTrees) {
        this.tokens = tokens;
        this.identificadores = identificadores;
        this.expressionTrees = expressionTrees;
        this.codigoIntermedioData = new StringBuilder();
        this.codigoIntermedioBss = new StringBuilder();
        directiveGenerator();
    }

    private void directiveGenerator() {
        for (String key : identificadores.keySet()) {
            if(identificadores.get(key).getFirst().equals("INT")){
                if(getFirstExpression(key).equals("?"))
                    codigoIntermedioBss.append(key).append("\t DW\t ?\n");
                else
                    codigoIntermedioData.append(key).append("\t DW\t ").append(getFirstExpression(key)).append("\n");
            }
            if (identificadores.get(key).getFirst().equals("FLOAT")) {
                if(getFirstExpression(key).equals("?"))
                    codigoIntermedioBss.append(key).append("\t DD\t ?\n");
                else
                    codigoIntermedioData.append(key).append("\t DD\t ").append(getFirstExpression(key)).append("\n");
            }
            if (identificadores.get(key).getFirst().equals("STRING")) {
                if(getFirstExpression(key).equals("?"))
                    codigoIntermedioBss.append(key).append("\t DB\t ?\n");
                else {
                    codigoIntermedioData.append(key).append("\t DB\t \"").append(getFirstExpression(key)).append("\"\n");
                }
            }
            if (identificadores.get(key).getFirst().equals("BOOLEAN")) {
                if(getFirstExpression(key).equals("?"))
                    codigoIntermedioBss.append(key).append("\t DB\t ?\n");
                else {
                    codigoIntermedioData.append(key).append("\t DB\t ").append(getFirstExpression(key)).append("\n");
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
                return "1";
            } else if (tree.getValue().equals("false")) {
                return "0";
            }
            return tree.getValue();
        }
        return "";
    }

    public String getCodigoIntermedio() {
        return "section .data \n" +
                codigoIntermedioData +
                "section .bss \n" +
                codigoIntermedioBss +
                "section .note.GNU-stack noalloc noexec nowrite progbits \n";
    }
}