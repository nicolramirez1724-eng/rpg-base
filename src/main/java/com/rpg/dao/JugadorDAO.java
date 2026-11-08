package com.rpg.dao;

import com.rpg.model.Jugador;
import com.rpg.model.SeguridadUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Jugador.
 * Implementa 100% consultas parametrizadas con PreparedStatement y gestion de recursos con try-with-resources.
 */
public class JugadorDAO {

    /**
     * Registra un nuevo jugador en la base de datos.
     * Almacena la clave hasheada con SHA-256 (Reto Extra).
     */
    public boolean registrar(Jugador jugador) {
        String sql = "INSERT INTO jugadores (usuario, clave, rol) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, jugador.getUsuario());
            // Guarda clave con hash SHA-256
            String claveHash = SeguridadUtil.sha256(jugador.getClave());
            pstmt.setString(2, claveHash != null ? claveHash : jugador.getClave());
            pstmt.setString(3, jugador.getRol() != null ? jugador.getRol() : Jugador.ROL_JUGADOR);

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rsClaves = pstmt.getGeneratedKeys()) {
                    if (rsClaves.next()) {
                        jugador.setId(rsClaves.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[JugadorDAO] Error al registrar jugador: " + e.getMessage());
            return false;
        }
    }

    /**
     * Autentica un usuario de forma segura utilizando PreparedStatement.
     * El comodin '?' garantiza que cualquier entrada maliciosa como "admin' OR 1=1 -- "
     * sea tratada exclusivamente como un valor literal y no como logica SQL ejecutable.
     *
     * @param usuario nombre de usuario introducido
     * @param clave   contraseña en texto plano
     * @return objeto Jugador si las credenciales son validas, o null si fallan
     */
    public Jugador autenticar(String usuario, String clave) {
        String sql = "SELECT id, usuario, clave, rol FROM jugadores WHERE usuario = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String claveAlmacenada = rs.getString("clave");
                    String claveHashIngresada = SeguridadUtil.sha256(clave);

                    // Valida contra hash SHA-256 o contra texto plano (compatibilidad con semillas antiguas)
                    boolean claveValida = claveAlmacenada != null &&
                            (claveAlmacenada.equalsIgnoreCase(claveHashIngresada) || claveAlmacenada.equals(clave));

                    if (claveValida) {
                        return new Jugador(
                                rs.getInt("id"),
                                rs.getString("usuario"),
                                claveAlmacenada,
                                rs.getString("rol")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[JugadorDAO] Error al autenticar: " + e.getMessage());
        }
        return null;
    }

    /**
     * Cambia el rol de un jugador (p.ej. 'jugador' o 'admin').
     */
    public boolean cambiarRol(int id, String nuevoRol) {
        String sql = "UPDATE jugadores SET rol = ? WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoRol);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JugadorDAO] Error al cambiar rol: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un jugador por su identificador unico.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM jugadores WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JugadorDAO] Error al eliminar jugador: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un jugador por su nombre de usuario.
     */
    public Jugador buscarPorUsuario(String usuario) {
        String sql = "SELECT id, usuario, clave, rol FROM jugadores WHERE usuario = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Jugador(
                            rs.getInt("id"),
                            rs.getString("usuario"),
                            rs.getString("clave"),
                            rs.getString("rol")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[JugadorDAO] Error buscando jugador por usuario: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un jugador por su ID primario.
     */
    public Jugador buscarPorId(int id) {
        String sql = "SELECT id, usuario, clave, rol FROM jugadores WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Jugador(
                            rs.getInt("id"),
                            rs.getString("usuario"),
                            rs.getString("clave"),
                            rs.getString("rol")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[JugadorDAO] Error buscando jugador por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lista todos los jugadores registrados.
     */
    public List<Jugador> listarTodos() {
        List<Jugador> lista = new ArrayList<>();
        String sql = "SELECT id, usuario, clave, rol FROM jugadores ORDER BY id";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Jugador(
                        rs.getInt("id"),
                        rs.getString("usuario"),
                        rs.getString("clave"),
                        rs.getString("rol")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[JugadorDAO] Error listando jugadores: " + e.getMessage());
        }
        return lista;
    }
}
