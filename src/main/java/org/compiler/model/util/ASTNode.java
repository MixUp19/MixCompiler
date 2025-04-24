package org.compiler.model.util;

import java.util.ArrayList;

public class ASTNode {
    private final ArrayList<ASTNode> childs;
    public ASTNode() {
        this.childs = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        this.childs.add(child);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ASTNode{");
        for (ASTNode child : childs) {
            sb.append("\n  ").append(child.toString());
        }
        sb.append("\n}");
        return sb.toString();
    }
}
