package com.rpg.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * =========================================================================================
 * EXPLICACION REQUERIDA (Parte A - Infraestructura y conexion JDBC):
 * =========================================================================================
 * 1. ¿Que es JDBC?
 *    JDBC (Java Database Connectivity) es la especificacion y API estandar de Java (ubicada en
 *    el paquete java.sql) que define un conjunto uniforme de interfaces (Connection, Statement,
 *    PreparedStatement, ResultSet, etc.) para interactuar con bases de datos relacionales.
 *    JDBC desacopla el codigo de la aplicacion de las particularidades de cada fabricante de base
 *    de datos, actuando como un puente de infraestructura comun.
 *
 * 2. ¿Que hace DriverManager?
 *    DriverManager es el administrador central de controladores en JDBC. Mantiene un registro
 *    de todos los drivers disponibles en el classpath (cargados mediante SPI / ServiceLoader).
 *    Cuando se solicita una conexion con DriverManager.getConnection(url, usuario, clave),
 *    DriverManager recorre los drivers registrados, consulta cual de ellos reconoce el prefijo
 *    del protocolo (por ejemplo "jdbc:mysql:"), y delega en el driver correspondiente la apertura
 *    del socket de red y el protocolo de negociacion de la conexion fisica.
 *
 * 3. ¿Que pasa si falta el driver (reproduccion del error)?
 *    Si falta la dependencia del driver (por ejemplo, comentando com.mysql:mysql-connector-j en
 *    el pom.xml o sin el JAR en el classpath), DriverManager no encuentra ningun controlador
 *    capaz de atender la URL jdbc:mysql://... Al invocar DriverManager.getConnection(...),
 *    se produce inmediatamente la siguiente excepcion comprobada:
 *
 *    Mensaje exacto del error:
 *    java.sql.SQLException: No suitable driver found for jdbc:mysql://localhost:3306/rpg_arena_db
 * =========================================================================================
 */
public final class ConexionDB {

    private static final String ARCHIVO_CONFIG = "config.txt";
    private static final Properties propiedades = new Properties();
    private static boolean configurado = false;

    // Modo mock / in-memory fallback en caso de que MySQL no este levantado localmente
    private static String urlOverride = null;

    private ConexionDB() {
    }

    private static synchronized void cargarConfiguracion() {
        if (configurado) {
            return;
        }
        File archivo = new File(ARCHIVO_CONFIG);
        if (!archivo.exists()) {
            System.err.printf("[ConexionDB] ADVERTENCIA: No se encontro '%s'. Creando configuracion por defecto.%n",
                    ARCHIVO_CONFIG);
            propiedades.setProperty("url", "jdbc:mysql://localhost:3306/rpg_arena_db");
            propiedades.setProperty("usuario", "root");
            propiedades.setProperty("clave", "");
            configurado = true;
            return;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            propiedades.load(lector);
            configurado = true;
        } catch (IOException e) {
            System.err.printf("[ConexionDB] Error leyendo '%s': %s%n", ARCHIVO_CONFIG, e.getMessage());
        }
    }

    /**
     * Obtiene una nueva conexion fisica a la base de datos segun config.txt.
     * Quien invoque este metodo debe cerrar la conexion con try-with-resources.
     *
     * @return Connection activa
     * @throws SQLException si falla la conexion
     */
    public static Connection obtenerConexion() throws SQLException {
        cargarConfiguracion();

        if (urlOverride != null) {
            return DriverManager.getConnection(urlOverride);
        }

        String url = propiedades.getProperty("url", "jdbc:mysql://localhost:3306/rpg_arena_db");
        String usuario = propiedades.getProperty("usuario", "root");
        String clave = propiedades.getProperty("clave", "");

        return DriverManager.getConnection(url, usuario, clave);
    }

    /**
     * Permite sobreescribir la URL de conexion para pruebas o ejecuciones sin servidor MySQL (p.ej. H2 en memoria).
     */
    public static synchronized void setUrlOverride(String url) {
        urlOverride = url;
    }

    public static synchronized void resetConfiguracion() {
        configurado = false;
        urlOverride = null;
        propiedades.clear();
    }
}
