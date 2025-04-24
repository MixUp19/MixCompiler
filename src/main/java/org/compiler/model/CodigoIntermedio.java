package org.compiler.model;

import org.compiler.model.util.ExpressionNode;
import org.compiler.model.util.Pair;
import org.compiler.model.util.Token;
import org.compiler.model.util.TiposDeTokens;

import java.util.*;

public class CodigoIntermedio {
    private final StringBuilder codigoIntermedioData;
    private final StringBuilder codigoIntermedioBss;
    private final StringBuilder codigoIntermedioText;
    private final ArrayList<Token> tokens;
    private final HashMap<String, ArrayList<String>> identificadores;
    private final HashMap<Vector<String>, ExpressionNode> expressionTrees;
    private final ArrayList<Pair<String, Integer, Integer>> estructurasControl;
    private Pair<String, Integer, Integer> estructuraControlActual;
    private final Stack<Pair<String, Integer, Integer>> elseStack;
    private final List<Vector<String>> keys;
    private int numIf = 0;
    private int numWhile = 0;

    public CodigoIntermedio(ArrayList<Token> tokens,
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
        elseStack = new Stack<>();
        directiveGenerator();
        keys = new ArrayList<>(expressionTrees.keySet());
        keys.sort(Comparator.comparingInt(key -> Integer.parseInt(key.get(1))));
        try {
            estructuraControlActual = estructurasControl.getFirst();
            estructurasControl.removeFirst();
        } catch (Exception e) {

        }
        while(!keys.isEmpty())
            codeGenerator();
    }

    private void codeGenerator(){
        if(keys.isEmpty()){
            return;
        }
        for(Vector<String> key: keys){
            System.out.printf("variable %s en linea %s con expresión: ", key.get(0), key.get(1));
            System.out.println(expressionTrees.get(key));
            System.out.println();
        }
        var key = keys.getFirst();
        keys.removeFirst();
        String id = key.getFirst();
        ExpressionNode tree = expressionTrees.get(key);
        switch (id) {
            case "read" ->
                readGenerator(tree, id);
            case "print", "println" ->
                printGenerator(tree, id);
            case "IF" -> {
                var esteIf = ifGenerator(tree, id);
                codigoIntermedioText.append(".FINIF").append(esteIf).append(":\n");
            }
            case "WHILE" -> {
                whileGenerator(tree, id);
                numWhile++;
            }
            default -> {
                evaluarExpresion(tree, id);
            }
        }
    }

    private void whileGenerator(ExpressionNode tree, String id) {
        var lineaFinal = estructuraControlActual.getThird();
        var esteWhile = numWhile++;
        try {
            estructuraControlActual = estructurasControl.getFirst();
            estructurasControl.removeFirst();
        } catch (Exception e) {
            System.out.println("No hay más estructuras de control");
        }
        codigoIntermedioText.append(".WHILE").append(esteWhile).append(":\n");
        colocarComparacionDeVarBool(tree, id);
        codigoIntermedioText.append(".FINWHILE").append(esteWhile).append("\n");
        while (!Objects.equals(estructuraControlActual.getSecond(), lineaFinal) && !keys.isEmpty()) {
            codeGenerator();
        }
        codigoIntermedioText.append("\tJMP\t").append(".WHILE").append(esteWhile).append("\n");
        codigoIntermedioText.append(".FINWHILE").append(esteWhile).append(":\n");

    }

    private void printGenerator(ExpressionNode tree, String id) {
        var idImprimir = tree.getToken().getValor();
        if(isBooleanID(idImprimir) || isIntID(idImprimir)){
            codigoIntermedioText.append("\tMOV\tAX, [rel ").append(idImprimir).append("]").append("\n");
            codigoIntermedioText.append("\tMOVSX\tESI, AX\n");
            codigoIntermedioText.append("\tLEA\tRDI, [rel fmtint]\n");
            codigoIntermedioText.append("\tXOR\tEAX, EAX\n");
            codigoIntermedioText.append("\tCALL\tprintf wrt ..plt\n");
        }
        if(isFloatID(idImprimir)){
            codigoIntermedioText.append("\tMOV\tEAX, [rel ").append(idImprimir).append("]").append("\n");
            codigoIntermedioText.append("\tMOV\tESI, AX\n");
            codigoIntermedioText.append("\tLEA\tRDI, [rel fmtfloat]\n");
            codigoIntermedioText.append("\tXOR\tEAX, EAX\n");
            codigoIntermedioText.append("\tCALL\tprintf wrt ..plt\n");
        }
        if(isStringID(idImprimir)){
            codigoIntermedioText.append("\tLEA\tRDI, [rel ").append(idImprimir).append("]\n");
            codigoIntermedioText.append("\tCALL\tputs wrt ..plt\n");
        }
    }

    private void readGenerator(ExpressionNode tree, String id) {
        var idLeer = tree.getToken().getValor();
        if(isBooleanID(idLeer) || isIntID(idLeer)){
            codigoIntermedioText.append("\tLEA\tRDI, [rel in_fmtint]\n");
            codigoIntermedioText.append("\tLEA\tRSI, [rel ").append(idLeer).append("]\n");
            codigoIntermedioText.append("\tCALL\tscanf wrt ..plt\n");
        }
        if(isFloatID(idLeer)){
            codigoIntermedioText.append("\tLEA\tRDI, [rel in_fmtfloat]\n");
            codigoIntermedioText.append("\tLEA\tRSI, [rel ").append(idLeer).append("]\n");
            codigoIntermedioText.append("\tCALL\tscanf wrt ..plt\n");
        }
        if(isStringID(idLeer)){
            codigoIntermedioText.append("\tLEA\tRDI, [rel in_fmtstr]\n");
            codigoIntermedioText.append("\tLEA\tRSI, [rel ").append(idLeer).append("]\n");
            codigoIntermedioText.append("\tCALL\tscanf wrt ..plt\n");
        }
    }

    private int ifGenerator(ExpressionNode tree, String id) {
        boolean elseFlag = lookForElse(estructuraControlActual.getThird());
        int esteIf = numIf++;
        var lineaFinal = estructuraControlActual.getThird();
        try {
            estructuraControlActual = estructurasControl.getFirst();
            estructurasControl.removeFirst();
        } catch (Exception e) {
            System.out.println("No hay más estructuras de control");
        }
        colocarComparacionDeVarBool(tree, id);
        if(elseFlag) {
            codigoIntermedioText.append(".ELSE").append(esteIf).append("\n");
        }else{
            codigoIntermedioText.append(".FINIF").append(esteIf).append("\n");
        }
        var key = keys.getFirst();
        var num = expressionTrees.get(key).getToken().getLinea();
        while(num < lineaFinal){
            codeGenerator();
            key = keys.getFirst();
            num = expressionTrees.get(key).getToken().getLinea();
        }
        if(elseFlag){
            codigoIntermedioText.append("\tJMP\t").append(".FINIF").append(esteIf).append("\n");
            codigoIntermedioText.append(".ELSE").append(esteIf).append(":\n");
            lineaFinal = elseStack.pop().getThird();
            while(num < lineaFinal) {
                codeGenerator();
                try {
                    key = keys.getFirst();
                    num = expressionTrees.get(key).getToken().getLinea();
                } catch (Exception e) {
                    num++;
                }
            }
        }
        return esteIf;
    }

    private void colocarComparacionDeVarBool(ExpressionNode tree, String id) {
        if(tree.getLeft() == null && tree.getRight() == null){
            codigoIntermedioText.append("\tMOV\t").append("AL, ").append("[rel ").append(tree.getToken().getValor()).append("]").append("\n");
            codigoIntermedioText.append("\tCMP\t").append("AL, ").append("1\n");
            codigoIntermedioText.append("\tJNE\t");
        }else{
            evaluarExpresion(tree, id);
        }
    }

    private boolean lookForElse(int linea){
        for(Pair<String, Integer, Integer> estructura : estructurasControl){
            if((estructura.getSecond() == linea || estructura.getSecond() == linea +1) && estructura.getFirst().equals("ELSE")){
                estructurasControl.remove(estructura);
                elseStack.push(estructura);
                return true;
            }
        }
        return false;
    }
    private void evaluarExpresion(ExpressionNode tree, String id) {
        Stack<Token> operadores = new Stack<>();
        Stack<Token> variables = new Stack<>();
        preorderTraversal(tree, operadores, variables);

        while (!operadores.isEmpty()) {
            Token operador = operadores.pop();
            Token var1 = variables.pop();
            Token var2 = variables.pop();
            String var1String =(isNumToken(var1) || isNumnToken(var1)) ? var1.getValor() : "[rel " + var1.getValor() + "]";
            String var2String =(isNumToken(var2) || isNumnToken(var2)) ? var2.getValor() : "[rel " + var2.getValor() + "]";
            String registro = (var2.getTipo() == TiposDeTokens.FLOAT)? "EAX, ": "AX, ";
            codigoIntermedioText.append("\tMOV\t").append(registro).append(var2String).append("\n");
            switch (operador.getTipo()){
                case SUMA:
                    codigoIntermedioText.append("\tADD\t").append(registro).append(var1String).append("\n");
                    break;
                case RESTA:
                    codigoIntermedioText.append("\tSUB\t").append(registro).append(var1String).append("\n");
                    break;
                case MULTIPLICACION:
                    codigoIntermedioText.append("\tMOV\t").append("BX, ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tMUL\t").append("BX").append("\n");
                    break;
                case DIVISION:
                    codigoIntermedioText.append("\tMOV\t").append("BX, ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tDIV\t").append("BX").append("\n");
                    break;
                case MAYOR:
                    codigoIntermedioText.append("\tCMP\t").append(registro).append(var1String).append("\n");
                    codigoIntermedioText.append("\tJLE\t");
                    break;
                    case MENOR:
                    codigoIntermedioText.append("\tCMP\t").append(registro).append(var1String).append("\n");
                    codigoIntermedioText.append("\tJGE\t");
                    break;
                    case MAYOR_IGUAL:
                    codigoIntermedioText.append("\tCMP\t").append(registro).append(var1String).append("\n");
                    codigoIntermedioText.append("\tJL\t");
                    break;
                    case MENOR_IGUAL:
                    codigoIntermedioText.append("\tCMP\t").append(registro).append(var1String).append("\n");
                    codigoIntermedioText.append("\tJG\t");
                    break;
                    case IGUAL:
                    codigoIntermedioText.append("\tCMP\t").append(registro).append(var1String).append("\n");
                    codigoIntermedioText.append("\tJNE\t");
            }
            variables.push(new Token(TiposDeTokens.NUMERO, "AX", 0));
        }
        if(isID(id))
            codigoIntermedioText.append("\tMOV\t").append("[rel ").append(id).append("], ").append(variables.pop().getValor()).append("\n");
    }

    private void preorderTraversal(ExpressionNode node, Stack<Token> operadores, Stack<Token> variables) {
        if (node == null) {
            return;
        }
        if (isOperator(node.getToken().getTipo())) {
            operadores.push(node.getToken());
        } else {
            variables.push(node.getToken());
        }
        preorderTraversal(node.getLeft(), operadores, variables);
        preorderTraversal(node.getRight(), operadores, variables);
    }

    private boolean isOperator(TiposDeTokens tipo) {
        return tipo == TiposDeTokens.SUMA || tipo == TiposDeTokens.RESTA ||
                tipo == TiposDeTokens.MULTIPLICACION || tipo == TiposDeTokens.DIVISION ||
                tipo == TiposDeTokens.MAYOR || tipo == TiposDeTokens.MENOR ||
                tipo == TiposDeTokens.MAYOR_IGUAL || tipo == TiposDeTokens.MENOR_IGUAL ||
                tipo == TiposDeTokens.IGUAL;
    }
    private boolean isID(String id) {
        return identificadores.containsKey(id);
    }

    private boolean isBooleanID(String id) {
        return identificadores.get(id).getFirst().equals("BOOLEAN");
    }
    private boolean isIntID(String id) {
        return identificadores.get(id).getFirst().equals("INT");
    }

    private boolean isFloatID(String id) {
        return identificadores.get(id).getFirst().equals("FLOAT");
    }
    private boolean isStringID(String id) {
        return identificadores.get(id).getFirst().equals("STRING");
    }
    private boolean isNumToken(Token token) {
        return token.getTipo() == TiposDeTokens.NUMERO ;
    }
    private boolean isNumnToken(Token token) {
        return token.getTipo() == TiposDeTokens.N_FRACCION;
    }

    private boolean isBooleanToken(Token token) {
        return token.getTipo() == TiposDeTokens.TRUE || token.getTipo() == TiposDeTokens.FALSE;
    }


    private void directiveGenerator() {
        for (String key : identificadores.keySet()) {
            var valor = getFirstExpression(key);
            if(identificadores.get(key).get(0).equals("INT")){
                if(valor.equals("?"))
                    codigoIntermedioBss.append(key).append("\t DW\t ?\n");
                else
                    codigoIntermedioData.append(key).append("\t DW\t ").append(valor).append("\n");
            }
            if (identificadores.get(key).get(0).equals("FLOAT")) {
                if(valor.equals("?"))
                    codigoIntermedioBss.append(key).append("\t DD\t ?\n");
                else
                    codigoIntermedioData.append(key).append("\t DD\t ").append(valor).append("\n");
            }
            if (identificadores.get(key).get(0).equals("STRING")) {
                if(valor.equals("?"))
                    codigoIntermedioBss.append(key).append("\t DB\t 256 dup(0)\n");
                else {
                    codigoIntermedioData.append(key).append("\t DB\t ").append(valor).append(", ").append(258-valor.length()).append(" dup(0)\n");
                }
            }
            if (identificadores.get(key).get(0).equals("BOOLEAN")) {
                if(valor.equals("?"))
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
            if (tree.getToken().getTipo() == TiposDeTokens.TRUE){
                expressionTrees.remove(key);
                return "1";
            } else if (tree.getToken().getTipo() == TiposDeTokens.FALSE) {
                expressionTrees.remove(key);
                return "0";
            }
            expressionTrees.remove(key);
            return tree.getToken().getValor();
        }
        return "?";
    }

    public String getCodigoIntermedio() {
        return "section .data \n" +
                codigoIntermedioData +
                "fmtint\sdb\s\"%d\",10,0\n"+
                "in_fmtint\sdb\s\"%d\",0\n"+
                "in_fmtstr\sdb\s\"%s\",0\n"+
                "fmtfloat\sdb\s\"%f\",10,0\n"+
                "in_fmtfloat\sdb\s\"%f\",0\n"+
                "section .bss \n" +
                codigoIntermedioBss +
                "section .note.GNU-stack noalloc noexec nowrite progbits \n\n"+
                "section .text \n"+
                "\tdefault rel \n"+
                "\tglobal main \n"+
                "\textern printf, scanf, puts\n"+
                "main: \n"+
                "\tPUSH\tRBP\n"+
                "\tMOV\tRBP, RSP\n"+
                codigoIntermedioText+
                "\tMOV\tEAX, 60\n"+
                "\tXOR\tEDI, EDI\n"+
                "\tSYSCALL\n";
    }
}