package org.compiler.model.util;

public class ExpressionNode {
    private final String value;
    private final TiposDeTokens tipo;
    private final ExpressionNode left;
    private final ExpressionNode right;

    public ExpressionNode(String value, ExpressionNode left, ExpressionNode right, TiposDeTokens tipo) {
        this.value = value;
        this.tipo = tipo;
        this.left = left;
        this.right = right;
    }

    public String getValue() {
        return value;
    }
    public TiposDeTokens getTipo() {
        return tipo;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }
}
