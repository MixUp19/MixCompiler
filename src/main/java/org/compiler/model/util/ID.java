package org.compiler.model.util;

public class ID {
    private String id;
    private TiposDeTokens tipo;
    private String valor;
    private int linea;

    public ID(String id, TiposDeTokens tipo, String valor, int linea) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.linea = linea;
    }
    public ID(String id, TiposDeTokens tipo, int linea) {
        this(id, tipo, "?", linea);
    }

    public String getId() {
        return id;
    }

    public TiposDeTokens getTipo() {
        return tipo;
    }
}
