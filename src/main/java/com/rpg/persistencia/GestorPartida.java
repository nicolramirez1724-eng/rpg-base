package com.rpg.persistencia;

import com.rpg.entities.PerfilJugador;
import com.rpg.entities.Personaje;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class GestorPartida {

    private static final String CARPETA_PARTIDAS = "partidas";
    private static final String EXTENSION = ".dat";

    public static void asegurarDirectorio() {
        File dir = new File(CARPETA_PARTIDAS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static boolean guardar(String nombreArchivo, Object objeto) {
        asegurarDirectorio();
        File archivo = new File(CARPETA_PARTIDAS, nombreArchivo + EXTENSION);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(objeto);
            oos.flush();
            return true;
        } catch (IOException e) {
            System.err.printf("[GestorPartida] Error al serializar partida: %s%n", e.getMessage());
            return false;
        }
    }

    public static Object cargar(String nombreArchivo) throws FileNotFoundException, ClassNotFoundException {
        File archivo = new File(CARPETA_PARTIDAS, nombreArchivo + EXTENSION);
        if (!archivo.exists()) {
            throw new FileNotFoundException("El archivo de partida no existe: " + archivo.getAbsolutePath());
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return ois.readObject();
        } catch (IOException e) {
            System.err.printf("[GestorPartida] Error leyendo archivo serializado: %s%n", e.getMessage());
            return null;
        }
    }

    public static boolean borrar(String nombreArchivo) {
        Path ruta = Path.of(CARPETA_PARTIDAS, nombreArchivo + EXTENSION);
        try {
            return Files.deleteIfExists(ruta);
        } catch (IOException e) {
            System.err.printf("[GestorPartida] Error borrando archivo: %s%n", e.getMessage());
            return false;
        }
    }
}
