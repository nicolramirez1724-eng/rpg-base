package com.rpg.demo;

import com.rpg.dao.ConexionDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * =========================================================================================
 * DEMOSTRACION DIDACTICA DE VULNERABILIDAD: INYECCION SQL (SQL Injection)
 * Parte B - Puntos 3 y 4 de EJERCICIOS.md
 *
 * ADVERTENCIA: Esta clase existe UNICAMENTE con fines educativos para evidenciar
 * la vulnerabilidad critica producida al concatenar cadenas directamente en sentencias SQL.
 * NUNCA debe usarse en produccion.
 * =========================================================================================
 */
public class LoginInseguroDemo {

    /**
     * Valida el login concatenando directamente los parametros en la consulta SQL usando Statement.
     * Esto permite romper la sintaxis SQL esperada e inyectar logica arbitraria.
     *
     * @param usuario entrada del usuario
     * @param clave   contraseña introducida
     * @return true si la consulta retorna al menos un registro; false en caso contrario
     */
    public boolean autenticarInseguro(String usuario, String clave) {
        // VULNERABILIDAD: Concatenacion directa de cadenas
        String sql = "SELECT * FROM jugadores WHERE usuario = '" + usuario + "' AND clave = '" + clave + "'";

        System.out.println("[LoginInseguroDemo] Ejecutando consulta SQL vulnerable:");
        System.out.println(">> " + sql);

        try (Connection conn = ConexionDB.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String usuarioAutenticado = rs.getString("usuario");
                String rol = rs.getString("rol");
                System.out.printf("[LoginInseguroDemo] ACCESO CONCEDIDO -> Usuario: %s, Rol: %s%n",
                        usuarioAutenticado, rol);
                return true;
            } else {
                System.out.println("[LoginInseguroDemo] ACCESO DENEGADO: Credenciales incorrectas.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[LoginInseguroDemo] Error SQL durante autenticacion: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("DEMOSTRACION DE INYECCION SQL CON ENTRADA MALICIOSA");
        System.out.println("===============================================================");

        LoginInseguroDemo demo = new LoginInseguroDemo();

        // Vector de ataque clasico: admin' OR 1=1 -- 
        String payload = "admin' OR 1=1 -- ";
        String claveFalsa = "clave_completamente_desconocida";

        System.out.printf("Intentando ingresar con usuario malicioso: \"%s\"%n", payload);
        System.out.printf("Contraseña utilizada: \"%s\"%n%n", claveFalsa);

        boolean resultado = demo.autenticarInseguro(payload, claveFalsa);

        if (resultado) {
            System.out.println("\n[EVIDENCIA DE VULNERABILIDAD]");
            System.out.println("El atacante logro ingresar como administrador sin conocer la clave secreta!");
            System.out.println("Motivo: ' OR 1=1 altera la clausula WHERE convirtiendola en una tautologia (siempre verdadera),");
            System.out.println("y '-- ' anula la evaluacion de la contraseña.");
        } else {
            System.out.println("\nNo se pudo conectar a la base de datos o fallo la consulta.");
        }
    }
}
