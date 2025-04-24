package org.compiler.model.util;

public class IfNode extends ASTNode {
    private final ExpressionNode condition;
    private final TablaID ambitoThen;
    private TablaID ambitoElse;
    private final ASTNode thenBlock;
    private ASTNode elseBlock;

    public IfNode(ExpressionNode condition, ASTNode thenBlock, ASTNode elseBlock, TablaID ambito) {
        super();
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        this.ambitoThen = ambito;
    }

    public IfNode(ExpressionNode condition, ASTNode thenBlock, TablaID ambito) {
        this(condition, thenBlock, null, ambito);
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public ASTNode getThenBlock() {
        return thenBlock;
    }

    public ASTNode getElseBlock() {
        return elseBlock;
    }

    public TablaID getAmbitoThen() {
        return ambitoThen;
    }

    public TablaID getAmbitoElse() {
        return ambitoElse;
    }

    public void setElse(ASTNode elseBlock, TablaID ambito) {
        this.elseBlock = elseBlock;
        ambitoElse = ambito;
    }

    @Override
    public String toString() {
        return "IfNode{" +
                "condition=" + condition +
                ", thenBlock=" + thenBlock +
                ", elseBlock=" + elseBlock +
                '}';
    }
}
