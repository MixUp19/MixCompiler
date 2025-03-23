package org.compiler.model.util;

public class ExpressionNode {
    private final Token token;
    private final ExpressionNode left;
    private final ExpressionNode right;

    public ExpressionNode(Token token, ExpressionNode left, ExpressionNode right) {
        this.token = token;
        this.left = left;
        this.right = right;
    }

    public void preoder(){
        System.out.print(token.getValor());
        if(left != null){
            left.preoder();
        }
        if(right != null){
            right.preoder();
        }
    }

    public void postorder(){
        if(left != null){
            left.postorder();
        }
        if(right != null){
            right.postorder();
        }
        System.out.print(token.getValor());
    }

    public Token getToken() {
        return token;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }
}