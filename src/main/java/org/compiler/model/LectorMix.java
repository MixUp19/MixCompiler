package org.compiler.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LectorMix {

    public static String leerArchivoMix(String rutaArchivo) {
        StringBuilder contenido = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                contenido.append(linea);
                contenido.append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return contenido.toString();
    }
}