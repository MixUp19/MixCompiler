package org.compiler;


import org.compiler.controller.Controller;
import org.compiler.view.Pantalla;

public class Main {
    public static void main(String[] args) {
        Pantalla pantalla = new Pantalla();
        Controller controller = new Controller(pantalla);
        pantalla.colocarControlador(controller);
    }
}