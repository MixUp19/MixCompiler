package org.compiler.model;

import org.compiler.model.util.*;

import java.util.*;

public class CodigoIntermedio {
    private final StringBuilder codigoIntermedioData;
    private final StringBuilder codigoIntermedioBss;
    private final StringBuilder codigoIntermedioText;
    private TablaID tablaID;
    private int numIf = 0;
    private int numWhile = 0;

    public CodigoIntermedio(
                            TablaID tablaID,
                            ASTNode root) {
        this.tablaID = tablaID;
        this.codigoIntermedioData = new StringBuilder();
        this.codigoIntermedioBss = new StringBuilder();
        this.codigoIntermedioText = new StringBuilder();
        directiveGenerator();
        codeGenerator(root);
    }

    private void codeGenerator(ASTNode root) {
        root.resetCurrent();
        ASTNode current = root.getChild();
        while (current != null) {
            switch (current) {
                case ExpressionNode ex -> expresionEvaluator(ex);
                case WhileNode whileNode -> whileGenerator(whileNode);
                case IfNode ifNode -> ifGenerator(ifNode);
                default -> throw new IllegalStateException("Unexpected value: " + current);
            }
            current = root.getChild();
        }
    }
    private void expresionEvaluator(ExpressionNode expresion) {
        var token = expresion.getToken();
        switch (token.getTipo()){
            case PRINT, PRINTLN -> printGenerator(expresion.getRight(), token.getValor());
            case READ -> readGenerator(expresion.getRight());
            case ASIGNACION -> evaluarExpresion(expresion.getRight(), expresion.getLeft().getToken().getValor());
        }
    }

    private void whileGenerator(WhileNode tree) {
        var esteWhile = numWhile++;
        ExpressionNode expresion = tree.getCondition();
        codigoIntermedioText.append(".WHILE").append(esteWhile).append(":\n");
        colocarComparacionDeVarBool(expresion, "WHILE");
        codigoIntermedioText.append(".FINWHILE").append(esteWhile).append("\n");
        ASTNode block = tree.getBlock();

        codeGenerator(block);

        codigoIntermedioText.append("\tJMP\t").append(".WHILE").append(esteWhile).append("\n");
        codigoIntermedioText.append(".FINWHILE").append(esteWhile).append(":\n");

    }

    private void printGenerator(ExpressionNode tree, String id) {
        var idImprimir = tree.getToken().getValor();
        if(isBooleanID(idImprimir) || isIntID(idImprimir) || isFloatID(idImprimir)){
            String regA = getSizeRegA(idImprimir);
            codigoIntermedioText.append("\tMOV\t RAX, 0\n");
            codigoIntermedioText.append("\tMOV\t").append(regA).append(", [").append(idImprimir).append("]\n");
            codigoIntermedioText.append("\tCALL\t_PRINTRAX\n");
        }
        if(isStringID(idImprimir)){
            codigoIntermedioText.append("\tMOV\tRAX, ").append(idImprimir).append("\n");
            codigoIntermedioText.append("\tCALL\t_PRINT\n");
        }
    }

    private void readGenerator(ExpressionNode tree) {
        var idLeer = tree.getToken().getValor();
        if(isBooleanID(idLeer) || isIntID(idLeer) || isFloatID(idLeer)){
            codigoIntermedioText.append("\tMOV\tRSI, ").append(idLeer).append("\n");
            codigoIntermedioText.append("\tCALL\t_READINT\n");
        }
        if(isStringID(idLeer)){
            codigoIntermedioText.append("\tMOV\tRSI, ").append(idLeer).append("\n");
            codigoIntermedioText.append("\tCALL\t_GETSTRING\n");
        }
    }

    private void ifGenerator(IfNode tree) {
        int esteIf = numIf++;
        var elseFlag = tree.getElseBlock() != null;
        ExpressionNode expresion = tree.getCondition();
        colocarComparacionDeVarBool(expresion, "IF");
        if(elseFlag) {
            codigoIntermedioText.append(".ELSE").append(esteIf).append("\n");
        }else{
            codigoIntermedioText.append(".FINIF").append(esteIf).append("\n");
        }
        var block = tree.getThenBlock();

        codeGenerator(block);

        if(elseFlag){
            codigoIntermedioText.append("\tJMP\t").append(".FINIF").append(esteIf).append("\n");
            codigoIntermedioText.append(".ELSE").append(esteIf).append(":\n");
            var blockElse = tree.getElseBlock();
            codeGenerator(blockElse);
        }
        codigoIntermedioText.append(".FINIF").append(esteIf).append(":\n");
    }

    private void colocarComparacionDeVarBool(ExpressionNode tree, String id) {
        if(tree.getLeft() == null && tree.getRight() == null){
            codigoIntermedioText.append("\tMOV\t").append("AL, ").append("[").append(tree.getToken().getValor()).append("]").append("\n");
            codigoIntermedioText.append("\tCMP\t").append("AL, ").append("1\n");
            codigoIntermedioText.append("\tJNE\t");
        }else{
            evaluarExpresion(tree, id);
        }
    }

    private void evaluarExpresion(ExpressionNode tree, String id) {
        Stack<Token> operadores = new Stack<>();
        Stack<Token> variables = new Stack<>();
        preorderTraversal(tree, operadores, variables);

        while (!operadores.isEmpty()) {
            Token operador = operadores.pop();
            Token var1 = variables.pop();
            Token var2 = variables.pop();
            String var1String =(isNumToken(var1) || isNumnToken(var1)) ? var1.getValor() : "[" + var1.getValor() + "]";
            String var2String =(isNumToken(var2) || isNumnToken(var2)) ? var2.getValor() : "[" + var2.getValor() + "]";
            String registroA = getSizeRegA(var2.getValor());
            String registroB = getSizeRegB(var2.getValor());
            codigoIntermedioText.append("\tMOV\t").append(registroA).append(", ").append(var2String).append("\n");
            switch (operador.getTipo()){
                case SUMA:
                    codigoIntermedioText.append("\tADD\t").append(registroA).append(", ").append(var1String).append("\n");
                    break;
                case RESTA:
                    codigoIntermedioText.append("\tSUB\t").append(registroA).append(", ").append(var1String).append("\n");
                    break;
                case MULTIPLICACION:
                    codigoIntermedioText.append("\tMOV\t").append(registroB).append(", ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tMUL\t").append(registroB).append("\n");
                    break;
                case DIVISION:
                    codigoIntermedioText.append("\tMOV\t").append(registroB).append(", ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tDIV\t").append(registroB).append("\n");
                    break;
                case MAYOR:
                    codigoIntermedioText.append("\tCMP\t").append(registroA).append(", ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tJLE\t");
                    break;
                    case MENOR:
                    codigoIntermedioText.append("\tCMP\t").append(registroA).append(", ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tJGE\t");
                    break;
                    case MAYOR_IGUAL:
                    codigoIntermedioText.append("\tCMP\t").append(registroA).append(", ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tJL\t");
                    break;
                    case MENOR_IGUAL:
                    codigoIntermedioText.append("\tCMP\t").append(registroA).append(", ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tJG\t");
                    break;
                    case IGUAL:
                    codigoIntermedioText.append("\tCMP\t").append(registroA).append(", ").append(var1String).append("\n");
                    codigoIntermedioText.append("\tJNE\t");
            }
            variables.push(new Token(TiposDeTokens.NUMERO, registroA, 0));
        }
        if(isID(id))
            codigoIntermedioText.append("\tMOV\t").append("[").append(id).append("], ").append(variables.pop().getValor()).append("\n");
    }
    private String getSizeRegA(String id){
        ID aux = tablaID.revisarID(id);
        if(aux == null){
            return "RAX";
        }
        switch (aux.getTipo()){
            case FLOAT -> {return "EAX";}
            case INT -> {return "AX";}
            case BOOLEAN -> {return "AL";}
            default -> {return "RAX";}
        }
    }
    private String getSizeRegB(String id){
        ID aux = tablaID.revisarID(id);
        if(aux == null){
            return "RBX";
        }
        switch (aux.getTipo()){
            case FLOAT -> {return "EBX";}
            case INT -> {return "BX";}
            case BOOLEAN -> {return "BL";}
            default -> {return "RBX";}
        }
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
        return tablaID.revisarID(id)!= null;
    }

    private boolean isBooleanID(String id) {
        return tablaID.revisarID(id).getTipo() == TiposDeTokens.BOOLEAN;
    }
    private boolean isIntID(String id) {
        return tablaID.revisarID(id).getTipo() == TiposDeTokens.INT;
    }

    private boolean isFloatID(String id) {
        return tablaID.revisarID(id).getTipo() == TiposDeTokens.FLOAT;
    }
    private boolean isStringID(String id) {
        return tablaID.revisarID(id).getTipo() == TiposDeTokens.STRING;
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
        ID id = tablaID.getActual();
        while (id != null) {
            switch (id.getTipo()) {
                case INT -> handleIntDirective(id);
                case FLOAT -> handleFloatDirective(id);
                case STRING -> handleStringDirective(id);
                case BOOLEAN -> handleBooleanDirective(id);
            }
            id = tablaID.getActual();
        }
    }

    private void handleIntDirective(ID id) {
        if (id.getValor().equals("?")) {
            codigoIntermedioBss.append(id.getId()).append("\t DW\t ?\n");
        } else {
            codigoIntermedioData.append(id.getId()).append("\t DW\t ").append(id.getValor()).append("\n");
        }
    }

    private void handleFloatDirective(ID id) {
        if (id.getValor().equals("?")) {
            codigoIntermedioBss.append(id.getId()).append("\t DD\t ?\n");
        } else {
            codigoIntermedioData.append(id.getId()).append("\t DD\t ").append(id.getValor()).append("\n");
        }
    }

    private void handleStringDirective(ID id) {
        if (id.getValor().equals("?")) {
            codigoIntermedioData.append(id.getId()).append("\t DB\t 256 dup(0)\n");
        } else {
            codigoIntermedioData.append(id.getId()).append("\t DB\t ")
                    .append(id.getValor()).append(", ")
                    .append(258 - id.getValor().length()).append(" dup(0)\n");
        }
    }

    private void handleBooleanDirective(ID id) {
        if (id.getValor().equals("?")) {
            codigoIntermedioBss.append(id.getId()).append("\t DB\t ?\n");
        } else {
            codigoIntermedioData.append(id.getId()).append("\t DB\t ").append((id.getValor().equals("true"))? "1": "0").append("\n");
        }
    }


    public String getCodigoIntermedio() {
        return "section .data \n" +
                codigoIntermedioData +
                "section .bss \n" +
                "INTEGERREADER RESB 100\n" +
                "INTEGERREADERPOS RESB 8\n" +
                "DIGITSPACE RESB 100\n" +
                "DIGITSPACEPOS RESB 8\n"+
                codigoIntermedioBss +
                "section .text \n"+
                "\tglobal _start \n"+
                "_start: \n"+
                codigoIntermedioText+
                "\tMOV\tRAX, 60\n"+
                "\tXOR\tRDI, RDI\n"+
                "\tSYSCALL\n"+
                "_READINT:\n" +
                "\tMOV RAX, 0\n" +
                "\tMOV RDI, 0\n" +
                "\tPUSH RSI\n" +
                "\tMOV RSI, INTEGERREADER\n" +
                "\tMOV RDX, 100\n" +
                "\tSYSCALL\n" +
                "\n" +
                "\tPOP RSI\n" +
                "\tMOV RCX, INTEGERREADER\n" +
                "\tMOV AL, [RCX]\n" +
                "\tINC RCX\n" +
                "\tSUB AL, 48\n" +
                "\tADD [RSI], RAX\n" +
                "\n" +
                "\tMOV BL, [RCX]\n" +
                "\tCMP BL, 10\n" +
                "\tJE _ENDREAD\n" +
                "\n" +
                "_READINTLOOP:\n" +
                "\tMOV RAX, [RSI]\n" +
                "\tMOV RBX, 10\n" +
                "\tMUL RBX,\n" +
                "\tMOV [RSI], RAX\n" +
                "\tMOV RAX, 0\n" +
                "\tMOV AL, [RCX]\n" +
                "\tINC RCX\n" +
                "\tSUB AL, 48\n" +
                "\tADD [RSI], AL\n" +
                "\tMOV BL, [RCX]\n" +
                "\tCMP BL, 10\n" +
                "\tJNE _READINTLOOP\n" +
                "_ENDREAD:\n" +
                "\tRET\n" +
                "\n" +
                "_PRINTRAX:\n" +
                "\tMOV RCX, DIGITSPACE\n" +
                "\tMOV RBX, 10\n" +
                "\tMOV [RCX],RBX\n" +
                "\tINC RCX\n" +
                "\tMOV [DIGITSPACEPOS], RCX\n" +
                "\n" +
                "_PRINTRAXLOOP:\n" +
                "\tMOV RDX, 0\n" +
                "\tMOV RBX, 10\n" +
                "\tDIV RBX\n" +
                "\tPUSH RAX\n" +
                "\tADD RDX, 48\n" +
                "\n" +
                "\tMOV RCX, [DIGITSPACEPOS]\n" +
                "\tMOV [RCX], DL\n" +
                "\tINC RCX\n" +
                "\tMOV [DIGITSPACEPOS], RCX\n" +
                "\n" +
                "\tPOP RAX\n" +
                "\tCMP RAX,0\n" +
                "\tJNE _PRINTRAXLOOP\n" +
                "\n" +
                "_PRINTRAXLOOP2:\n" +
                "\tMOV RCX, [DIGITSPACEPOS]\n" +
                "\n" +
                "\tMOV RAX, 1\n" +
                "\tMOV RDI, 1\n" +
                "\tMOV RSI, RCX\n" +
                "\tMOV RDX, 1\n" +
                "\tSYSCALL\n" +
                "\n" +
                "\tMOV RCX, [DIGITSPACEPOS]\n" +
                "\tDEC RCX\n" +
                "\tMOV [DIGITSPACEPOS], RCX\n" +
                "\n" +
                "\tCMP RCX, DIGITSPACE\n" +
                "\tJGE _PRINTRAXLOOP2\n" +
                "\tRET\n"+
                "_PRINT:\n" +
                "\tPUSH RAX\n" +
                "\tMOV RBX, 0\n" +
                "_PRINTLOOP:\n" +
                "\tINC RAX\n" +
                "\tINC RBX\n" +
                "\tMOV CL, [RAX]\n" +
                "\tCMP CL, 0\n" +
                "\tJNE _PRINTLOOP\n" +
                "\tINC RBX\n"+
                "\tMOV [RAX], byte 10\n"+
                "\n" +
                "\tMOV RAX, 1\n" +
                "\tMOV RDI, 1\n" +
                "\tPOP RSI\n" +
                "\tMOV RDX, RBX\n" +
                "\tSYSCALL\n" +
                "\n" +
                "\tRET\n"+
                "_GETSTRING:\n" +
                "\tMOV RAX, 0\n" +
                "\tMOV RDI, 0\n" +
                "\tMOV RDX, 16\n" +
                "\tSYSCALL\n" +
                "\tRET\n";
    }
}