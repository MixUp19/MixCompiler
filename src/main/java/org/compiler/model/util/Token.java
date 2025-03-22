package org.compiler.model.util;

public class Token {
    private final TiposDeTokens tipo;
    private final String valor;
    private final int linea;

    public Token(TiposDeTokens tipo, String valor, int linea){
        this.tipo = tipo;
        this.valor = valor;
        this.linea = linea;
    }

    public int getLinea() {
        return linea;
    }

    public String getValor() {
        return valor;
    }

    public TiposDeTokens getTipo() {
        return tipo;
    }
}
