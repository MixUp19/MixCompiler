package org.compiler.model;

import org.compiler.model.util.*;

import java.util.HashSet;

public class Semantic {
    private TablaID tablaID;
    private ASTNode root;
    private int relationalOperatorsCount = 0;
    private final HashSet<String> iDsWithValue = new HashSet<>();
    private boolean error;
    private String message;

    public Semantic(boolean error, String message, TablaID tablaID, ASTNode root) {
        this.error = error;
        this.message = message;
        this.tablaID = tablaID;
        this.root = root;
        if (error) {
            return;
        }
        semantic();
    }

    public void semantic() {
        try {
            semanticCheck(root);
            this.message = "todo bien";
        } catch (Exception e) {
            this.error = true;
            this.message = e.getMessage();
        }
    }

    private void semanticCheck(ASTNode node) throws Exception {
        ASTNode current = node.getChild();
        while (current != null) {
            switch (current) {
                case ExpressionNode expressionNode:
                    validateExNode(expressionNode);
                    break;
                case IfNode ifNode:
                    validateIfNode(ifNode);
                    break;
                case WhileNode whileNode:
                    validateWhile(whileNode);
                    break;
                default:
                    break;
            }
            current = node.getChild();
        }
    }

    private void validateExNode(ExpressionNode node) throws Exception {
        switch (node.getToken().getTipo()) {
            case ASIGNACION:
                validateAssignation(node);
                break;
            case PRINT:
            case PRINTLN:
                validatePrint(node);
                break;
            case READ:
                validateRead(node);
                break;
            default:
                throw new Exception("Use las expresiones en una asignación o para evaluar algo");
        }

    }

    private void validateAssignation(ExpressionNode node) throws Exception {
        ExpressionNode left = node.getLeft();
        ExpressionNode right = node.getRight();

        if (left.getToken().getTipo() != TiposDeTokens.ID) {
            throw new Exception("Error en línea " + left.getToken().getLinea() + 
                                ": se esperaba un ID en la asignación");
        }

        String id = left.getToken().getValor();
        ID idObj = tablaID.revisarID(id);
        if (idObj == null) {
            throw new Exception("Error en línea " + left.getToken().getLinea() + 
                                ": la variable " + id + " no ha sido declarada");
        }

        TiposDeTokens tipo = idObj.getTipo();
        if(tipo == TiposDeTokens.BOOLEAN && right.getToken().getTipo() == TiposDeTokens.TRUE || right.getToken().getTipo() == TiposDeTokens.FALSE) {
            if(right.getRight() != null || right.getLeft() != null) {
                throw new Exception("Error en la linea: " + right.getToken().getLinea() +
                                    ": con expresar true o false basta");
            }
            iDsWithValue.add(id);
            return;
        }
        checkIDsTree(right, tipo);
        relationalOperatorsCount = 0;
        iDsWithValue.add(id);
    }

    private void validateWhile(WhileNode node) throws Exception {
        ExpressionNode condition = node.getCondition();
        checkIDsTree(condition, TiposDeTokens.WHILE);
        relationalOperatorsCount = 0;
        ASTNode block = node.getBlock();
        if (block != null) {
            tablaID = node.getAmbito();
            semanticCheck(block);
            tablaID.getPadre();
        }
    }

    private void validateIfNode(IfNode node) throws Exception {
        ExpressionNode condition = node.getCondition();
        checkIDsTree(condition, TiposDeTokens.IF);
        relationalOperatorsCount = 0;
        ASTNode thenBlock = node.getThenBlock();
        if (thenBlock != null) {
            tablaID = node.getAmbitoThen();
            semanticCheck(thenBlock);
            tablaID = tablaID.getPadre();
        }
        ASTNode elseBlock = node.getElseBlock();
        if (elseBlock != null) {
            tablaID = node.getAmbitoElse();
            semanticCheck(elseBlock);
            tablaID = tablaID.getPadre();
        }
    }

    private void validatePrint(ExpressionNode node) throws Exception {
        if (node.getRight() == null) {
            throw new Exception("Error en línea " + node.getToken().getLinea() + 
                                ": se esperaba una expresión en la instrucción PRINT/PRINTLN");
        }
        ExpressionNode expression = node.getRight();
        checkIDsTree(expression, TiposDeTokens.PRINT);
        relationalOperatorsCount = 0;
    }

    private void validateRead(ExpressionNode node) throws Exception {
        if (node.getRight() == null || node.getRight().getToken().getTipo() != TiposDeTokens.ID) {
            throw new Exception("Error en línea " + node.getToken().getLinea() + 
                                ": se esperaba un ID para la función READ");
        }
        String id = node.getRight().getToken().getValor();
        if (tablaID.revisarID(id) == null) {
            throw new Exception("Error en línea " + node.getRight().getToken().getLinea() + 
                                ": la variable " + id + " no ha sido declarada");
        }
        iDsWithValue.add(id);
    }

    private void checkIDsTree(ExpressionNode tree, TiposDeTokens tipoEsperado) throws Exception {
        if (tree == null) {
            return;
        }
        if(tipoEsperado == TiposDeTokens.IF || tipoEsperado == TiposDeTokens.WHILE) {
            if(!validarExpresionBloque(tree)){
                tipoEsperado = TiposDeTokens.BOOLEAN;
            }else{
                return;
            }
        }

        if (isOperator(tree.getToken().getTipo())) {
            if (isRelationalOperator(tree.getToken().getTipo()) && tipoEsperado != TiposDeTokens.BOOLEAN) {
                throw new Exception("Error en línea " + tree.getToken().getLinea() + 
                                    ": se esperaba un tipo BOOLEAN para la expresión");
            }
            if (isRelationalOperator(tree.getToken().getTipo())) {
                relationalOperatorsCount++;
            }
            if (relationalOperatorsCount > 1) {
                throw new Exception("Error en línea " + tree.getToken().getLinea() + 
                                    ": solo se permite un operador relacional por expresión");
            }
            if (tipoEsperado == TiposDeTokens.STRING && tree.getToken().getTipo() != TiposDeTokens.SUMA) {
                throw new Exception("Error en línea " + tree.getToken().getLinea() + 
                                    ": solo se permite la concatenación de cadenas");
            }
        }
        if (tipoEsperado == TiposDeTokens.BOOLEAN && tree.getToken().getTipo() == TiposDeTokens.CADENA) {
            throw new Exception("Error en línea " + tree.getToken().getLinea() + 
                                ": no se puede comparar cadenas");
        }
        if (tipoEsperado == TiposDeTokens.STRING) {
            if (tree.getToken().getTipo() != TiposDeTokens.CADENA ) {
                throw new Exception("Error en línea " + tree.getToken().getLinea() + 
                                    ": se esperaba una cadena");
            }
        }

        if (tree.getToken().getTipo() == TiposDeTokens.N_FRACCION) {
            if (tipoEsperado == TiposDeTokens.INT) {
                throw new Exception("Error en línea " + tree.getToken().getLinea() + 
                                    ": se esperaba un tipo de variable FLOAT");
            }
        }
        if (tree.getToken().getTipo() == TiposDeTokens.ID) {
            ID idObj = tablaID.revisarID(tree.getToken().getValor());
            if (idObj == null) {
                throw new Exception("Error en línea " + tree.getToken().getLinea() + 
                                    ": la variable " + tree.getToken().getValor() + " no ha sido declarada");
            }
            if (!iDsWithValue.contains(tree.getToken().getValor()) && idObj.getValor() == "?") {
                throw new Exception("Error en línea " + tree.getToken().getLinea() + 
                                    ": la variable " + tree.getToken().getValor() + " no ha sido inicializada");
            }
            if (tipoEsperado == TiposDeTokens.INT) {
                validateForInt(idObj.getTipo(), tree.getToken().getLinea());
            }
            if (tipoEsperado == TiposDeTokens.FLOAT) {
                validateForFloat(idObj.getTipo(), tree.getToken().getLinea());
            }
        }
        checkIDsTree(tree.getLeft(), tipoEsperado);
        checkIDsTree(tree.getRight(), tipoEsperado);
        if(relationalOperatorsCount < 1 && tipoEsperado == TiposDeTokens.BOOLEAN) {
            throw new Exception("Error en línea " + tree.getToken().getLinea() +
                    ": se esperaba un operador relacional");
        }
    }
    private void validateForInt(TiposDeTokens tipo, int linea) throws Exception {
        if (tipo != TiposDeTokens.INT ) {
            throw new Exception("Error, se esperaba un tipo de variable INT Linea "+ linea);
        }
    }
    private void validateForFloat(TiposDeTokens tipo, int linea) throws Exception {
        if (tipo != TiposDeTokens.FLOAT) {
            throw new Exception("Error, se esperaba un tipo de variable FLOAT Linea "+ linea);
        }
    }
    // Método auxiliar para obtener el tipo de un nodo
    private TiposDeTokens getTipo(ExpressionNode node) throws Exception {
        if (node == null) {
            return null;
        }
        if (node.getToken().getTipo() == TiposDeTokens.ID) {
            ID idObj = tablaID.revisarID(node.getToken().getValor());
            if (idObj == null) {
                throw new Exception("Error, la variable " + node.getToken().getValor() + " no ha sido declarada");
            }
            return idObj.getTipo();
        }
        return node.getToken().getTipo();
    }

    private boolean isRelationalOperator(TiposDeTokens token) {
        return token == TiposDeTokens.MAYOR || token == TiposDeTokens.MENOR || 
               token == TiposDeTokens.MAYOR_IGUAL || token == TiposDeTokens.IGUAL || token == TiposDeTokens.MENOR_IGUAL;
    }

    private boolean isOperator(TiposDeTokens token) {
        return token == TiposDeTokens.SUMA || token == TiposDeTokens.RESTA ||
                token == TiposDeTokens.MULTIPLICACION || token == TiposDeTokens.DIVISION ||
                isRelationalOperator(token);
    }
    private boolean validarExpresionBloque(ExpressionNode node) throws Exception {
        if (node == null) {
            return false;
        }
        if (node.getToken().getTipo() == TiposDeTokens.ID) {
            ID idObj = tablaID.revisarID(node.getToken().getValor());
            if (idObj == null) {
                throw new Exception("Error, la variable " + node.getToken().getValor() + " no ha sido declarada");
            }
            if(idObj.getTipo() != TiposDeTokens.BOOLEAN) {
                throw new Exception("Error, la variable " + node.getToken().getValor() + " no es de tipo BOOLEAN");
            }
            if(idObj.getValor().equals("?") && !iDsWithValue.contains(node.getToken().getValor())) {
                throw new Exception("Error, la variable " + node.getToken().getValor() + " no ha sido inicializada");
            }
            return node.getLeft() == null && node.getRight() == null;
        }
        return node.getToken().getTipo() == TiposDeTokens.TRUE || node.getToken().getTipo() == TiposDeTokens.FALSE;
    }
    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}