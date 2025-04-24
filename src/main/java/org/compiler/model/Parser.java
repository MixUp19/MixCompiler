package org.compiler.model;

import org.compiler.model.util.*;

import java.util.*;

public class Parser {
    private final ArrayList<Token> tokens;
    private TablaID tablaID;
    private ASTNode root;
    private final Stack<ASTNode> stack;
    private boolean semanticError;
    private String semanticErrorMessage;
    private String message;
    private boolean error;
    private int pos;

    public Parser(ArrayList<Token> codigo) {
        tablaID = new TablaID();
        this.stack = new Stack<>();
        this.tokens = codigo;
        this.root = new ASTNode();
        pos = 0;
        semanticError = false;
        parse();
        System.out.print(root);
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
        TiposDeTokens tipo = tokens.get(pos).getTipo();
        consume(tokens.get(pos).getTipo().getTipoInt());
        if (tablaID.revisarID(tokens.get(pos).getValor())!= null) {
            semanticErrorMessage = "Error, la variable " + tokens.get(pos).getValor() + " en la linea " + tokens.get(pos).getLinea() + " ya ha sido declarada";
            semanticError = true;
            consume(TiposDeTokens.ID.getTipoInt());
            consume(TiposDeTokens.PC.getTipoInt());
            return;
        }
        String id = tokens.get(pos).getValor();
        int linea = tokens.get(pos).getLinea();
        String valor = null;
        consume(TiposDeTokens.ID.getTipoInt());
        if (tokens.get(pos).getTipo() == TiposDeTokens.ASIGNACION) {
            consume(TiposDeTokens.ASIGNACION.getTipoInt());
            if (tokens.get(pos + 1).getTipo() == TiposDeTokens.PC) {
                valor = tokens.get(pos).getValor();
            } else {
                pos -= 2;
                asignarValor();
            }
        } else {
            consume(TiposDeTokens.PC.getTipoInt());
        }
        ID idObj = new ID(id, tipo, valor, linea);
        tablaID.agregarID(idObj);
    }

    private void asignarValor() throws Exception {
        validateAssignation();
        ExpressionNode left = new ExpressionNode(tokens.get(pos), null, null);
        consume(tokens.get(pos).getTipo().getTipoInt());
        ExpressionNode root = new ExpressionNode(tokens.get(pos), left, null);
        consume(TiposDeTokens.ASIGNACION.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        root.setRight(exprTree);
        this.root.addChild(root);
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
        ExpressionNode root = new ExpressionNode(tokens.get(pos), null, null);
        consume(TiposDeTokens.READ.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        root.setRight(exprTree);
        this.root.addChild(root);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void print() throws Exception {
        ExpressionNode root = new ExpressionNode(tokens.get(pos), null, null);
        consume(TiposDeTokens.PRINT.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        root.setRight(exprTree);
        this.root.addChild(root);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void println() throws Exception {
        ExpressionNode root = new ExpressionNode(tokens.get(pos), null, null);
        consume(TiposDeTokens.PRINTLN.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        root.setRight(exprTree);
        this.root.addChild(root);
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void whileMetodo() throws Exception {
        consume(TiposDeTokens.WHILE.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        tablaID = new TablaID(tablaID);
        stack.push(root);
        WhileNode whileNode = new WhileNode(exprTree, tablaID);
        root = whileNode.getBlock();
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.APERTO_LLA.getTipoInt());
        declaracion();
        consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
        tablaID = tablaID.getPadre();
        root = stack.pop();
        root.addChild(whileNode);
    }

    private void ifMetodo() throws Exception {
        consume(TiposDeTokens.IF.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        ExpressionNode exprTree = buildTreeExpression();
        tablaID = new TablaID(tablaID);
        stack.push(root);
        IfNode ifNode = new IfNode(exprTree,new ASTNode(),  tablaID);
        root = ifNode.getThenBlock();
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.APERTO_LLA.getTipoInt());
        declaracion();
        consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
        tablaID = tablaID.getPadre();
        root = stack.pop();
        if (verificar(TiposDeTokens.ELSE.getTipoInt())) {
            tablaID = new TablaID(tablaID);
            stack.push(root);
            root = new ASTNode();
            consume(TiposDeTokens.ELSE.getTipoInt());
            consume(TiposDeTokens.APERTO_LLA.getTipoInt());
            declaracion();
            ifNode.setElse(root, tablaID);
            consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
            tablaID = tablaID.getPadre();
            root = stack.pop();
        }
        root.addChild(ifNode);
    }

    private void validateAssignation() {
        if (!semanticError && tablaID.revisarID(tokens.get(pos).getValor())!=null) {
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

    public ASTNode getRoot() {
        return root;
    }

    public TablaID getTablaID() {
        return tablaID;
    }
}
