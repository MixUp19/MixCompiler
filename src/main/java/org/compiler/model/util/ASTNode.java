package org.compiler.model.util;

import java.util.ArrayList;

public class ASTNode {
    private final ArrayList<ASTNode> childs;
    private int current;
    public ASTNode() {
        this.childs = new ArrayList<>();
        this.current = 0;
    }

    public void addChild(ASTNode child) {
        this.childs.add(child);
    }

    public ASTNode getChild (){
        if(current == childs.size()){
            return null;
        }
        return childs.get(current++);}

    public void fowardCurrent(int current) {
        if(current <  childs.size()-1)
            this.current++;
    }
    public void resetCurrent() {
        this.current = 0;
    }
    public void backCurrent() {
        if (current > 0) {
            this.current--;
        }
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
