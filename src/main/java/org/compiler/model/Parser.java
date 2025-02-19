package org.compiler.model;

import org.compiler.model.util.Pair;
import org.compiler.model.util.TiposDeTokens;

import java.util.ArrayList;

public class Parser {
    private final ArrayList<Pair<TiposDeTokens,String,Integer>> tokens;
    private String mensaje;
    private boolean error;
    private int pos;

    public Parser(ArrayList<Pair<TiposDeTokens,String,Integer>> codigo){
        this.tokens = codigo;
        pos = 0;
        try {
            declaracion();
            consume(TiposDeTokens.FIN.getTipoInt());
            mensaje = "todo bien";
            error = false;
        }catch (Exception e){
            mensaje = e.getMessage();
            error = true;
        }

    }
    private void consume(int tipoToken) throws Exception{
        if (tokens.get(pos).getFirst().getTipoInt() != tipoToken){
            throw new Exception("Error, se esperaba " + TiposDeTokens.getEnumByInt(tipoToken)+" se encontró "+ tokens.get(pos).getSecond()+" en la posición "+pos);
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
        consume(tokens.get(pos).getFirst().getTipoInt());
        consume(TiposDeTokens.ID.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }

    private void asignarValor() throws Exception{
        consume(tokens.get(pos).getFirst().getTipoInt());
        consume(TiposDeTokens.ASIGNACION.getTipoInt());
        expresion();
        consume(TiposDeTokens.PC.getTipoInt());
    }
    private void expresion() throws Exception{
        switch (tokens.get(pos).getFirst()){
            case ID:
                consume(TiposDeTokens.ID.getTipoInt());
                break;
            case NUMERO:
                consume(TiposDeTokens.NUMERO.getTipoInt());
                break;
            case N_FRACCION:
                consume(TiposDeTokens.N_FRACCION.getTipoInt());
                break;
            case CADENA:
                consume(TiposDeTokens.CADENA.getTipoInt());
                break;
            case TRUE:
                consume(TiposDeTokens.TRUE.getTipoInt());
                break;
            case FALSE:
                consume(TiposDeTokens.FALSE.getTipoInt());
                break;
            case FIN:
                return;
            default:
                throw new Exception("Error, se desconoce el token "+ tokens.get(pos).getSecond()+" en una expresión");
        }
        if (pos < tokens.size() && esOperador(tokens.get(pos).getFirst())){
            int operador = tokens.get(pos).getFirst().getTipoInt();
            consume(operador);
            expresion();
        }
    }
    private void read() throws Exception{
        consume(TiposDeTokens.READ.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        expresion();
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }
    private void print() throws Exception{
        consume(TiposDeTokens.PRINT.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        expresion();
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }
    private void println() throws Exception{
        consume(TiposDeTokens.PRINTLN.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        expresion();
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.PC.getTipoInt());
    }
    private void whileMetodo() throws Exception{
        consume(TiposDeTokens.WHILE.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        expresion();
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.APERTO_LLA.getTipoInt());
        declaracion();
        consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
    }
    private void ifMetodo() throws Exception{
        consume(TiposDeTokens.IF.getTipoInt());
        consume(TiposDeTokens.APERTO_PAR.getTipoInt());
        expresion();
        consume(TiposDeTokens.CERRADO_PAR.getTipoInt());
        consume(TiposDeTokens.APERTO_LLA.getTipoInt());
        declaracion();
        consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
        if(verificar(TiposDeTokens.ELSE.getTipoInt())){
            consume(TiposDeTokens.ELSE.getTipoInt());
            consume(TiposDeTokens.APERTO_LLA.getTipoInt());
            declaracion();
            consume(TiposDeTokens.CERRADO_LLA.getTipoInt());
        }
    }
    public String getMensaje() {
        return mensaje;
    }
    public boolean isError() {return error;}

}
