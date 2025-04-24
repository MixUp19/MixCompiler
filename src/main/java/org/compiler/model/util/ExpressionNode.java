package org.compiler.model.util;

public class ExpressionNode extends ASTNode {
    private final Token token;
    private ExpressionNode left;
    private ExpressionNode right;

    public ExpressionNode(Token token, ExpressionNode left, ExpressionNode right) {
        super();
        this.token = token;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(token.getValor());
        if (left != null) {
            result.append(left.toString());
        }
        if (right != null) {
            result.append(right.toString());
        }
        return result.toString();
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

    public void setLeft(ExpressionNode left) {this.left = left;}

    public void setRight(ExpressionNode right){this.right = right;}

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }
}
