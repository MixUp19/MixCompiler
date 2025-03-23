package org.compiler.model;

import org.compiler.model.util.ExpressionNode;
import org.compiler.model.util.TiposDeTokens;

import java.util.*;

public class Semantic {
    private final HashMap<String, ArrayList<String>> identificadores = new HashMap<>();
    private final HashMap<Vector<String>, ExpressionNode> expressionTrees = new HashMap<>();
    private final HashSet<String> iDsWithValue = new HashSet<>();
    private boolean error;
    private String message;
    private int relationalOperators = 0;

    public Semantic(boolean error, String message, HashMap<String, ArrayList<String>> identificadores, HashMap<Vector<String>, ExpressionNode> expressionTrees) {
        this.error = error;
        this.message = message;
        this.identificadores.putAll(identificadores);
        this.expressionTrees.putAll(expressionTrees);
        if (error) {
            return;
        }
        semantic();
    }

    public void semantic() {
        try {
            semanticCheck();
            this.message = "todo bien";
        } catch (Exception e) {
            this.error = true;
            this.message = e.getMessage();
        }
    }

    private void semanticCheck() throws Exception {
        List<Vector<String>> keys = new ArrayList<>(expressionTrees.keySet());
        keys.sort(Comparator.comparingInt(key -> Integer.parseInt(key.get(1))));
        for (Vector<String> key : keys) {
            String id = key.get(0);
            ExpressionNode tree = expressionTrees.get(key);

            if (id.equals("read")) {
                validateRead(tree);
                continue;
            }

            if (id.equals("print") || id.equals("println")) {
                validatePrint(tree);
                continue;
            }
            TiposDeTokens tipo = getTipo(id);
            if (isRelationalExpression(tipo)) {
                if (validateForIFindWhileWithJustAnID(tree) && !isBooleanID(tree.getToken().getValor())) {
                    throw new Exception("Error, se esperaba un tipo BOOLEAN para la expresion");
                } else if (validateForIFindWhileWithJustAnID(tree) && isBooleanID(tree.getToken().getValor())) {
                    continue;
                }
                if (validateJustForTrueOrFalse(tree)) {
                    iDsWithValue.add(id);
                    continue;
                }
            }
            checkIDsTree(tree, tipo);
            iDsWithValue.add(id);
            if (isRelationalExpression(tipo) && relationalOperators < 1) {
                throw new Exception("Error, se esperaba un operador relacional en la expresion");
            }
            relationalOperators = 0;
        }
    }

    private void checkIDsTree(ExpressionNode tree, TiposDeTokens tipo) throws Exception {
        if (tree == null) {
            return;
        }
        if (isOperator(tree.getToken().getTipo())) {
            if (isRelationalOperator(tree.getToken().getTipo()) && tipo != TiposDeTokens.BOOLEAN) {
                throw new Exception("Error, se esperaba un tipo BOOLEAN para la expresion");
            }
            if (isRelationalOperator(tree.getToken().getTipo())) {
                relationalOperators++;
            }
            if (relationalOperators > 1) {
                throw new Exception("Error, solo se permite un operador relacional por expresion");
            }
            if (tipo == TiposDeTokens.STRING && tree.getToken().getTipo() != TiposDeTokens.SUMA) {
                throw new Exception("Error, solo se permite la concatenacion de cadenas");
            }
        }
        if (tree.getToken().getTipo() == TiposDeTokens.CADENA) {
            if (tipo != TiposDeTokens.STRING) {
                throw new Exception("Error, se esperaba un tipo de variable STRING");
            }
        }
        if (tree.getToken().getTipo() == TiposDeTokens.N_FRACCION) {
            if (tipo == TiposDeTokens.INT) {
                throw new Exception("Error, se esperaba un tipo de variable FLOAT");
            }
        }
        if (tree.getToken().getTipo() == TiposDeTokens.ID) {
            if (!identificadores.containsKey(tree.getToken().getValor())) {
                throw new Exception("Error, la variable " + tree.getToken().getValor() + " no ha sido declarada");
            }
            if (!iDsWithValue.contains(tree.getToken().getValor())) {
                throw new Exception("Error, la variable " + tree.getToken().getValor() + " no ha sido inicializada");
            }
            if (tipo == TiposDeTokens.INT) {
                validateForInt(TiposDeTokens.getEnumByString(identificadores.get(tree.getToken().getValor()).get(0)));
            }
            if (tipo == TiposDeTokens.FLOAT) {
                validateForFloat(TiposDeTokens.getEnumByString(identificadores.get(tree.getToken().getValor()).get(0)));
            }
        }
        checkIDsTree(tree.getLeft(), tipo);
        checkIDsTree(tree.getRight(), tipo);
    }

    private boolean isRelationalExpression(TiposDeTokens tipo) {
        return tipo == TiposDeTokens.BOOLEAN || tipo == TiposDeTokens.IF || tipo == TiposDeTokens.WHILE;
    }

    private TiposDeTokens getTipo(String id) {
        return (id.equals("IF") || id.equals("WHILE")) ? TiposDeTokens.BOOLEAN : TiposDeTokens.getEnumByString(identificadores.get(id).get(0));
    }

    private void validateForInt(TiposDeTokens tipo) throws Exception {
        if (tipo != TiposDeTokens.INT && tipo != TiposDeTokens.NUMERO) {
            throw new Exception("Error, se esperaba un tipo INT");
        }
    }

    private void validateForFloat(TiposDeTokens tipo) throws Exception {
        if (tipo != TiposDeTokens.INT && tipo != TiposDeTokens.FLOAT && tipo != TiposDeTokens.NUMERO && tipo != TiposDeTokens.N_FRACCION) {
            throw new Exception("Error, se esperaba un tipo Numerico");
        }
    }

    private boolean isRelationalOperator(TiposDeTokens token) {
        return token == TiposDeTokens.MAYOR || token == TiposDeTokens.MENOR || token == TiposDeTokens.MAYOR_IGUAL || token == TiposDeTokens.MENOR_IGUAL || token == TiposDeTokens.IGUAL;
    }

    private boolean isOperator(TiposDeTokens token) {
        return token == TiposDeTokens.SUMA ||
                token == TiposDeTokens.RESTA ||
                token == TiposDeTokens.MULTIPLICACION ||
                token == TiposDeTokens.DIVISION ||
                token == TiposDeTokens.MAYOR ||
                token == TiposDeTokens.MENOR ||
                token == TiposDeTokens.MAYOR_IGUAL ||
                token == TiposDeTokens.MENOR_IGUAL ||
                token == TiposDeTokens.IGUAL;
    }

    private boolean validateForIFindWhileWithJustAnID(ExpressionNode tree) {
        return tree.getToken().getTipo() == TiposDeTokens.ID && tree.getLeft() == null && tree.getRight() == null;
    }

    private boolean validateJustForTrueOrFalse(ExpressionNode tree) {
        return (tree.getLeft() == null && tree.getRight() == null) && (tree.getToken().getTipo() == TiposDeTokens.TRUE || tree.getToken().getTipo() == TiposDeTokens.FALSE);
    }

    private boolean isBooleanID(String id) {
        return identificadores.get(id).get(0).equals("BOOLEAN");
    }

    private void validateRead(ExpressionNode tree) throws Exception {
        if (!validateForIFindWhileWithJustAnID(tree)) {
            throw new Exception("Error, se esperaba un ID para la funcion READ");
        }
        if (!identificadores.containsKey(tree.getToken().getValor())) {
            throw new Exception("Error, la variable " + tree.getToken().getValor() + " no ha sido declarada");
        }
        iDsWithValue.add(tree.getToken().getValor());
    }

    private void validatePrint(ExpressionNode tree) throws Exception {
        try {
            checkIDsTree(tree, TiposDeTokens.INT);
        } catch (Exception e) {
            try {
                checkIDsTree(tree, TiposDeTokens.FLOAT);
            } catch (Exception e1) {
                try {
                    checkIDsTree(tree, TiposDeTokens.STRING);
                } catch (Exception e2) {
                    try {
                        checkIDsTree(tree, TiposDeTokens.BOOLEAN);
                    } catch (Exception e3) {
                        throw new Exception("Expresión invalida: " + e3.getMessage());
                    }
                }
            }
        }
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}