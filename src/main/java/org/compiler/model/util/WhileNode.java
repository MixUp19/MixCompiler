package org.compiler.model.util;

public class WhileNode extends ASTNode {
    private final ExpressionNode condition;
    private final TablaID ambito;
    private final ASTNode block;

    public WhileNode(ExpressionNode condition, TablaID ambito) {
        super();
        this.condition = condition;
        this.block = new ASTNode();
        this.ambito = ambito;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public ASTNode getBlock() {
        return block;
    }

    public TablaID getAmbito() {
        return ambito;
    }

    @Override
    public String toString() {
        return "WhileNode{" +
                "condition=" + condition +
                ", block=" + block +
                '}';
    }
}
