package org.compiler.model;

import org.compiler.model.util.*;

import java.util.HashSet;

public class Semantic {
    private TablaID tablaID;
    private ASTNode root;
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
                checkIDsTree(node, null); // Permitir cualquier tipo
        }

    }

    private void validateAssignation(ExpressionNode node) throws Exception {
        ExpressionNode left = node.getLeft();
        ExpressionNode right = node.getRight();

        if (left.getToken().getTipo() != TiposDeTokens.ID) {
            throw new Exception("Error, se esperaba un ID en la asignación");
        }

        String id = left.getToken().getValor();
        ID idObj = tablaID.revisarID(id);
        if (idObj == null) {
            throw new Exception("Error, la variable " + id + " no ha sido declarada");
        }

        TiposDeTokens tipo = idObj.getTipo();
        checkIDsTree(right, tipo);
        iDsWithValue.add(id);
    }

    private void validateWhile(WhileNode node) throws Exception {
        ExpressionNode condition = node.getCondition();
        checkIDsTree(condition, TiposDeTokens.BOOLEAN);
    }

    private void validateIfNode(IfNode node) throws Exception {
        ExpressionNode condition = node.getCondition();
        checkIDsTree(condition, TiposDeTokens.BOOLEAN);
    }

    private void validatePrint(ExpressionNode node) throws Exception {
        if (node.getRight() == null) {
            throw new Exception("Error, se esperaba una expresión en la instrucción PRINT/PRINTLN");
        }
        ExpressionNode expression = node.getRight();
        checkIDsTree(expression, null); // Permitir cualquier tipo
    }

    private void validateRead(ExpressionNode node) throws Exception {
        if (node.getRight() == null || node.getRight().getToken().getTipo() != TiposDeTokens.ID) {
            throw new Exception("Error, se esperaba un ID para la función READ");
        }
        String id = node.getRight().getToken().getValor();
        if (tablaID.revisarID(id) == null) {
            throw new Exception("Error, la variable " + id + " no ha sido declarada");
        }
        iDsWithValue.add(id);
    }

    private void checkIDsTree(ExpressionNode tree, TiposDeTokens tipoEsperado) throws Exception {
        if (tree == null) {
            return;
        }

        TiposDeTokens tipoActual = tree.getToken().getTipo();

        // Validar operadores
        if (isOperator(tipoActual)) {
            TiposDeTokens tipoIzquierdo = getTipo(tree.getLeft());
            TiposDeTokens tipoDerecho = getTipo(tree.getRight());

            if (isRelationalOperator(tipoActual)) {
                if (tipoIzquierdo != tipoDerecho) {
                    throw new Exception("Error, los operandos de un operador relacional deben ser del mismo tipo");
                }
                if (tipoEsperado != null && tipoEsperado != TiposDeTokens.BOOLEAN) {
                    throw new Exception("Error, se esperaba un tipo BOOLEAN para la expresión relacional");
                }
            } else if (tipoActual == TiposDeTokens.SUMA && tipoEsperado == TiposDeTokens.STRING) {
                if (tipoIzquierdo != TiposDeTokens.STRING || tipoDerecho != TiposDeTokens.STRING && tipoIzquierdo != TiposDeTokens.Cadena || tipoDerecho != TiposDeTokens.Cadena) {
                    throw new Exception("Error, solo se permite la concatenación de cadenas con el operador '+'");
                }
            } else if (tipoIzquierdo != tipoDerecho) {
                throw new Exception("Error, los operandos deben ser del mismo tipo");
            }
        }

        // Validar IDs
        if (tipoActual == TiposDeTokens.ID) {
            ID idObj = tablaID.revisarID(tree.getToken().getValor());
            if (idObj == null) {
                throw new Exception("Error, la variable " + tree.getToken().getValor() + " no ha sido declarada");
            }
            if (tipoEsperado != null && idObj.getTipo() != tipoEsperado) {
                throw new Exception("Error, se esperaba un tipo " + tipoEsperado + " pero se encontró " + idObj.getTipo());
            }
        }

        // Validar hijos recursivamente
        checkIDsTree(tree.getLeft(), tipoEsperado);
        checkIDsTree(tree.getRight(), tipoEsperado);
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
               token == TiposDeTokens.MAYOR_IGUAL || token == TiposDeTokens.MENOR_IGUAL || 
               token == TiposDeTokens.IGUAL;
    }

    private boolean isOperator(TiposDeTokens token) {
        return token == TiposDeTokens.SUMA || token == TiposDeTokens.RESTA || 
               token == TiposDeTokens.MULTIPLICACION || token == TiposDeTokens.DIVISION || 
               isRelationalOperator(token);
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
