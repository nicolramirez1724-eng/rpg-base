package com.rpg.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Imprime cada evento del combate, lo acumula en memoria y lo respalda en disco
 * mediante BufferedWriter y FileWriter (modo append).
 */
public class RegistroCombate {

    private static final String ARCHIVO_LOG = "registro_combate.log";
    private final StringBuilder texto = new StringBuilder();

    public void agregar(String linea) {
        System.out.println(linea);
        texto.append(linea).append(System.lineSeparator());
        volcarADisco(linea);
    }

    public void agregar(String formato, Object... argumentos) {
        agregar(String.format(formato, argumentos));
    }

    public void limpiar() {
        texto.setLength(0);
    }

    private void volcarADisco(String linea) {
        File archivo = new File(ARCHIVO_LOG);
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8, true))) {
            escritor.write(linea);
            escritor.newLine();
            escritor.flush();
        } catch (IOException e) {
            System.err.printf("[RegistroCombate] Error escribiendo log a disco: %s%n", e.getMessage());
        }
    }

    public String getTexto() {
        return texto.toString();
    }
}
