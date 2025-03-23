package org.compiler.model;

import org.compiler.model.util.ExpressionNode;
import org.compiler.model.util.Pair;
import org.compiler.model.util.Token;
import org.compiler.model.util.TiposDeTokens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class Parser {
    private final ArrayList<Token> tokens;
    private final ArrayList<Pair<String, Integer, Integer>> estruturasDeFlujo;
    private final HashMap<String, ArrayList<String>> identificadores = new HashMap<>();
    private final HashMap<Vector<String>, ExpressionNode> expressionTrees = new HashMap<>();
    private boolean semanticError;
    private String semanticErrorMessage;
    private String message;
    private boolean error;
    private int pos;

    public Parser(ArrayList<Token> codigo) {
        this.estruturasDeFlujo = new ArrayList<>();
        this.tokens = codigo;
        pos = 0;
        semanticError = false;
        parse();
    }

    public void parse() {
        try {
            declaracion();
            consume(TiposDeTokens.FIN.getTipoInt());
            message = "todo bien";
            error = false;
        } catch (Exception e) {
            message = e.getMessage();
            error = true;
        }
    }

    private void consume(int tipoToken) throws Exception {
        if (tokens.get(pos).getTipo().getTipoInt() != tipoToken) {
            throw new Exception("Error, se esperaba " + TiposDeTokens.getEnumByInt(tipoToken) + " se encontró " + tokens.get(pos).getValor() + " en la linea " + tokens.get(pos).getLinea());
        }
        pos++;
    }

    private boolean verificar(int tipoToken) {
        return pos < tokens.size() && tokens.get(pos).getTipo().getTipoInt() == tipoToken;
    }

    private boolean isHighOperator(TiposDeTokens token) {
        return token == TiposDeTokens.DIVISION || token == TiposDeTokens.MULTIPLICACION;
    }

    private boolean isLowOperator(TiposDeTokens token) {
        return token == TiposDeTokens.SUMA || token == TiposDeTokens.RESTA;
    }

    private boolean isRelationalOperator(TiposDeTokens token) {
        return token == TiposDeTokens.MAYOR || token == TiposDeTokens.MENOR || token == TiposDeTokens.MAYOR_IGUAL || token == TiposDeTokens.MENOR_IGUAL || token == TiposDeTokens.IGUAL;
    }

    private void declaracion() throws Exception {
        switch (tokens.get(pos).getTipo()) {
            case INT, STRING, FLOAT, BOOLEAN:
                definirID();
                break;
            case ID:
                asignarValor();
                break;
            case IF:
                ifMetodo();
                break;
            case WHILE:
                whileMetodo();
                break;
            case PRINT:
                print();
                break;
            case PRINTLN:
                println();
                break;
            case READ:
                read();
                break;
            default:
                return;
        }
        declaracion();
    }

    private void definirID() throws Exception {
        ArrayList<String> tipo = new ArrayList<>(2);
        tipo.addAll(List.of(tokens.get(pos).getTipo().toString(), String.valueOf(tokens.get(pos).getLinea())));
        consume(tokens.get(pos).getTipo().getTipoInt());
        if (identificadores.containsKey(tokens.get(pos).getValor())) {
            semanticErrorMessage = "Error, la variable " + tokens.get(pos).getValor() + " en la linea " + tokens.get(pos).getLinea() + " ya ha sido declarada";
            semanticError = true;
            consume(TiposDeTokens.ID.getTipoInt());
            consume(TiposDeTokens.PC.getTipoInt());
            return;
        }
        identificadores.put(tokens.get(pos).getValor(), tipo);
        consume(TiposDeTokens.ID.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void asignarValor() throws Exception {
        validateAssignation();
        String variable = tokens.get(pos).getValor();
        int linea = tokens.get(pos).getLinea();
        consume(tokens.get(pos).getTipo().getTipoInt());
        consume(TiposDeTokens.ASIGNACION.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of(variable, String.valueOf(linea)));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private ExpressionNode buildTreeExpression() throws Exception {
        return parseRelational();
    }

    private ExpressionNode parseRelational() throws Exception {
        ExpressionNode left = parseExpression();
        while (pos < tokens.size() && isRelationalOperator(tokens.get(pos).getTipo())) {
            Token operador = tokens.get(pos);
            consume(tokens.get(pos).getTipo().getTipoInt());
            ExpressionNode right = parseExpression();
            left = new ExpressionNode(operador, left, right);
        }
        return left;
    }

    private ExpressionNode parseExpression() throws Exception {
        ExpressionNode left = parseTerm();
        while (pos < tokens.size() && (isLowOperator(tokens.get(pos).getTipo()))) {
            Token operador = tokens.get(pos);
            consume(tokens.get(pos).getTipo().getTipoInt());
            ExpressionNode right = parseTerm();
            left = new ExpressionNode(operador, left, right);
        }
        return left;
    }

    private ExpressionNode parseTerm() throws Exception {
        ExpressionNode left = parseFactor();
        while (pos < tokens.size() && (isHighOperator(tokens.get(pos).getTipo()))) {
            Token operador = tokens.get(pos);
            consume(tokens.get(pos).getTipo().getTipoInt());
            ExpressionNode right = parseFactor();
            left = new ExpressionNode(operador, left, right);
        }
        return left;
    }

    private ExpressionNode parseFactor() throws Exception {
        switch (tokens.get(pos).getTipo()) {
            case ID:
                Token id = tokens.get(pos);
                consume(TiposDeTokens.ID.getTipoInt());
                return new ExpressionNode(id, null, null);
            case NUMERO:
                Token numero = tokens.get(pos);
                consume(TiposDeTokens.NUMERO.getTipoInt());
                return new ExpressionNode(numero, null, null);
            case N_FRACCION:
                Token fraccion = tokens.get(pos);
                consume(TiposDeTokens.N_FRACCION.getTipoInt());
                return new ExpressionNode(fraccion, null, null);
            case CADENA:
                Token cadena = tokens.get(pos);
                consume(TiposDeTokens.CADENA.getTipoInt());
                return new ExpressionNode(cadena, null, null);
            case TRUE:
                Token trueToken = tokens.get(pos);
                consume(TiposDeTokens.TRUE.getTipoInt());
                return new ExpressionNode(trueToken, null, null);
            case FALSE:
                Token falseToken = tokens.get(pos);
                consume(TiposDeTokens.FALSE.getTipoInt());
                return new ExpressionNode(falseToken, null, null);
            default:
                throw new Exception("Error, se desconoce el token " + tokens.get(pos).getValor() + " en una expresión");
        }
    }

    private void read() throws Exception {
        consume(TiposDeTokens.READ.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("read", String.valueOf(tokens.get(pos).getLinea())));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void print() throws Exception {
        consume(TiposDeTokens.PRINT.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("print", String.valueOf(tokens.get(pos).getLinea())));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void println() throws Exception {
        consume(TiposDeTokens.PRINTLN.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("println", String.valueOf(tokens.get(pos).getLinea())));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void whileMetodo() throws Exception {
        consume(TiposDeTokens.WHILE.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("WHILE", String.valueOf(tokens.get(pos).getLinea())));
        expressionTrees.put(key, exprTree);
        Pair<String, Integer, Integer> pair = new Pair<>("WHILE", tokens.get(pos).getLinea(), 0);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.APERTO_LLA.getTipoInt());
        declaracion();
        pair.setThird(tokens.get(pos).getLinea());
        estruturasDeFlujo.add(pair);
        consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
    }

    private void ifMetodo() throws Exception {
        consume(TiposDeTokens.IF.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("IF", String.valueOf(tokens.get(pos).getLinea())));
        Pair<String, Integer, Integer> pair = new Pair<>("IF", tokens.get(pos).getLinea(), 0);
        estruturasDeFlujo.add(pair);
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.APERTO_LLA.getTipoInt());
        declaracion();
        pair.setThird(tokens.get(pos).getLinea());
        consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
        if (verificar(TiposDeTokens.ELSE.getTipoInt())) {
            pair = new Pair<>("ELSE", tokens.get(pos).getLinea(), 0);
            estruturasDeFlujo.add(pair);
            consume(TiposDeTokens.ELSE.getTipoInt());
            consume(TiposDeTokens.APERTO_LLA.getTipoInt());
            declaracion();
            pair.setThird(tokens.get(pos).getLinea());
            consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
        }
    }

    private void validateAssignation() {
        if (!semanticError && !identificadores.containsKey(tokens.get(pos).getValor())) {
            semanticErrorMessage = "Error, la variable " + tokens.get(pos).getValor() + " en la linea " + tokens.get(pos).getLinea() + " no ha sido declarada";
            semanticError = true;
        }
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }

    public boolean isSemanticError() {
        return semanticError;
    }

    public String getSemanticErrorMessage() {
        return semanticErrorMessage;
    }

    public HashMap<String, ArrayList<String>> getIdentificadores() {
        return identificadores;
    }

    public HashMap<Vector<String>, ExpressionNode> getExpressionTrees() {
        return expressionTrees;
    }

    public ArrayList<Pair<String, Integer, Integer>> getEstruturasDeFlujo() {
        return estruturasDeFlujo;
    }
}
