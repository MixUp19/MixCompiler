package org.compiler.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
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

    public static void runIntermediateCode(String intermediateCode) {
        String filePath = "/home/mixup/Documentos/intermediate_code.asm";

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(intermediateCode);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            Process compileProcess = Runtime.getRuntime().exec("nasm -f elf64 " + filePath);
            compileProcess.waitFor();
            Process linkProcess = Runtime.getRuntime().exec("gcc -pie -o output " + filePath.replace(".asm", ".o"));
            linkProcess.waitFor();
            Runtime.getRuntime().exec(new String[]{"gnome-terminal", "--", "sh", "-c", "./output; exec bash"});
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}