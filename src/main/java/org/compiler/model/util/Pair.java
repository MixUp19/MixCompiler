package org.compiler.model.util;

public class Pair <T,E,F>{
    private final T first;
    private final E second;
    private final F third;

    public Pair(T first, E second, F third){
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst(){return first;}
    public E getSecond(){return second;}
    public F getThird(){return third;}

    public String toString(){return first + ", " + second;}
}
