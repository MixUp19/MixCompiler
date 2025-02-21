package org.compiler.model;

import org.compiler.model.util.ExpressionNode;
import org.compiler.model.util.Pair;
import org.compiler.model.util.TiposDeTokens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class Parser {
    private final ArrayList<Pair<TiposDeTokens,String,Integer>> tokens;
    private final HashMap<String, ArrayList<String>> identificadores = new HashMap<>();
    private final HashMap<Vector<String>, ExpressionNode> expressionTrees = new HashMap<>();
    private boolean semanticError;
    private String semanticErrorMessage;
    private String message;
    private boolean error;
    private int pos;

    public Parser(ArrayList<Pair<TiposDeTokens,String,Integer>> codigo){
        this.tokens = codigo;
        pos = 0;
        semanticError = false;
        try {
            declaracion();
            consume(TiposDeTokens.FIN.getTipoInt());
            message = "todo bien";
            error = false;
        }catch (Exception e){
            message = e.getMessage();
            error = true;
        }

    }
    private void consume(int tipoToken) throws Exception{
        if (tokens.get(pos).getFirst().getTipoInt() != tipoToken){
            throw new Exception("Error, se esperaba " + TiposDeTokens.getEnumByInt(tipoToken)+"se encontró <br>"+ tokens.get(pos).getSecond()+" en la linea "+tokens.get(pos).getThird());
        }
        pos++;
    }
    private boolean verificar(int tipoToken) {
        return pos < tokens.size() && tokens.get(pos).getFirst().getTipoInt() == tipoToken;
    }
    private boolean esOperador(TiposDeTokens token) {
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
    private void declaracion()throws Exception{
        switch (tokens.get(pos).getFirst()) {
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
    private void definirID() throws Exception{
        ArrayList<String> tipo = new ArrayList<>(2);
        tipo.addAll(List.of(new String[]{tokens.get(pos).getFirst().toString(), tokens.get(pos).getThird().toString()}));
        consume(tokens.get(pos).getFirst().getTipoInt());
        identificadores.put(tokens.get(pos).getSecond(), tipo);
        consume(TiposDeTokens.ID.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void asignarValor() throws Exception{
        validateAssignation();
        String variable = tokens.get(pos).getSecond();
        int linea = tokens.get(pos).getThird();
        consume(tokens.get(pos).getFirst().getTipoInt());
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
        while (pos < tokens.size() && (tokens.get(pos).getFirst() == TiposDeTokens.MAYOR || tokens.get(pos).getFirst() == TiposDeTokens.MENOR ||
                tokens.get(pos).getFirst() == TiposDeTokens.MAYOR_IGUAL || tokens.get(pos).getFirst() == TiposDeTokens.MENOR_IGUAL ||
                tokens.get(pos).getFirst() == TiposDeTokens.IGUAL)) {
            String operador = tokens.get(pos).getSecond();
            consume(tokens.get(pos).getFirst().getTipoInt());
            ExpressionNode right = parseExpression();
            left = new ExpressionNode(operador, left, right);
        }
        return left;
    }

    private ExpressionNode parseExpression() throws Exception {
        ExpressionNode left = parseTerm();
        while (pos < tokens.size() && (tokens.get(pos).getFirst() == TiposDeTokens.SUMA || tokens.get(pos).getFirst() == TiposDeTokens.RESTA)) {
            String operador = tokens.get(pos).getSecond();
            consume(tokens.get(pos).getFirst().getTipoInt());
            ExpressionNode right = parseTerm();
            left = new ExpressionNode(operador, left, right);
        }
        return left;
    }

    private ExpressionNode parseTerm() throws Exception {
        ExpressionNode left = parseFactor();
        while (pos < tokens.size() && (tokens.get(pos).getFirst() == TiposDeTokens.MULTIPLICACION || tokens.get(pos).getFirst() == TiposDeTokens.DIVISION)) {
            String operador = tokens.get(pos).getSecond();
            consume(tokens.get(pos).getFirst().getTipoInt());
            ExpressionNode right = parseFactor();
            left = new ExpressionNode(operador, left, right);
        }
        return left;
    }

    private ExpressionNode parseFactor() throws Exception {
        switch (tokens.get(pos).getFirst()) {
            case ID:
                String id = tokens.get(pos).getSecond();
                consume(TiposDeTokens.ID.getTipoInt());
                return new ExpressionNode(id, null, null);
            case NUMERO:
                String numero = tokens.get(pos).getSecond();
                consume(TiposDeTokens.NUMERO.getTipoInt());
                return new ExpressionNode(numero, null, null);
            case N_FRACCION:
                String fraccion = tokens.get(pos).getSecond();
                consume(TiposDeTokens.N_FRACCION.getTipoInt());
                return new ExpressionNode(fraccion, null, null);
            case CADENA:
                String cadena = tokens.get(pos).getSecond();
                consume(TiposDeTokens.CADENA.getTipoInt());
                return new ExpressionNode(cadena, null, null);
            case TRUE:
                consume(TiposDeTokens.TRUE.getTipoInt());
                return new ExpressionNode("true", null, null);
            case FALSE:
                consume(TiposDeTokens.FALSE.getTipoInt());
                return new ExpressionNode("false", null, null);
            default:
                throw new Exception("Error, se desconoce el token " + tokens.get(pos).getSecond() + " en una expresión");
        }
    }
    private void read() throws Exception {
        consume(TiposDeTokens.READ.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("read", String.valueOf(tokens.get(pos).getThird())));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void print() throws Exception {
        consume(TiposDeTokens.PRINT.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("print", String.valueOf(tokens.get(pos).getThird())));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void println() throws Exception {
        consume(TiposDeTokens.PRINTLN.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("println", String.valueOf(tokens.get(pos).getThird())));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void whileMetodo() throws Exception {
        consume(TiposDeTokens.WHILE.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("while", String.valueOf(tokens.get(pos).getThird())));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.APERTO_LLA.getTipoInt());
        declaracion();
        consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
    }

    private void ifMetodo() throws Exception {
        consume(TiposDeTokens.IF.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        Vector<String> key = new Vector<>(List.of("if", String.valueOf(tokens.get(pos).getThird())));
        expressionTrees.put(key, exprTree);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.APERTO_LLA.getTipoInt());
        declaracion();
        consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
        if (verificar(TiposDeTokens.ELSE.getTipoInt())) {
            consume(TiposDeTokens.ELSE.getTipoInt());
            consume(TiposDeTokens.APERTO_LLA.getTipoInt());
            declaracion();
            consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
        }
    }

    private void validateAssignation(){
        if (!semanticError &&!identificadores.containsKey(tokens.get(pos).getSecond())){
            semanticErrorMessage = "Error, la variable "+tokens.get(pos).getSecond()+" en la linea "+ tokens.get(pos).getThird()+" no ha sido declarada";
            semanticError = true;
        }
    }
    public String getMessage() {
        return message;
    }
    public boolean isError() {return error;}

}
