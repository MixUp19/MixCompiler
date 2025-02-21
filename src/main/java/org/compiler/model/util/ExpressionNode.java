package org.compiler.model.util;

public class ExpressionNode {
    private final String value;
    private final ExpressionNode left;
    private final ExpressionNode right;

    public ExpressionNode(String value, ExpressionNode left, ExpressionNode right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public String getValue() {
        return value;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }
}
