package com.rpg.dao;

import com.rpg.model.Monstruo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Monstruo.
 * Implementa CRUD completo y consultas especificas mediante PreparedStatement.
 */
public class MonstruoDAO {

    /**
     * Inserta un nuevo monstruo en la base de datos (Create).
     */
    public boolean crear(Monstruo monstruo) {
        String sql = "INSERT INTO monstruos (nombre, vida, ataque, defensa) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, monstruo.getNombre());
            pstmt.setInt(2, monstruo.getVida());
            pstmt.setInt(3, monstruo.getAtaque());
            pstmt.setInt(4, monstruo.getDefensa());

            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        monstruo.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[MonstruoDAO] Error al crear monstruo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene un monstruo por su ID primario (Read).
     */
    public Monstruo obtenerPorId(int id) {
        String sql = "SELECT id, nombre, vida, ataque, defensa FROM monstruos WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearMonstruo(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MonstruoDAO] Error obteniendo monstruo por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualiza las estadisticas de un monstruo existente (Update).
     */
    public boolean actualizar(Monstruo monstruo) {
        String sql = "UPDATE monstruos SET nombre = ?, vida = ?, ataque = ?, defensa = ? WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, monstruo.getNombre());
            pstmt.setInt(2, monstruo.getVida());
            pstmt.setInt(3, monstruo.getAtaque());
            pstmt.setInt(4, monstruo.getDefensa());
            pstmt.setInt(5, monstruo.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MonstruoDAO] Error al actualizar monstruo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un monstruo por su ID primario (Delete).
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM monstruos WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MonstruoDAO] Error al eliminar monstruo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un monstruo por su nombre exacto (ignorando mayusculas/minusculas segun collation).
     */
    public Monstruo buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre, vida, ataque, defensa FROM monstruos WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearMonstruo(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MonstruoDAO] Error buscando monstruo por nombre: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lista todos los monstruos cuya vida sea mayor o igual al umbral especificado.
     */
    public List<Monstruo> listarPorVidaMinima(int vidaMinima) {
        List<Monstruo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, vida, ataque, defensa FROM monstruos WHERE vida >= ? ORDER BY vida ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vidaMinima);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMonstruo(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MonstruoDAO] Error listando por vida minima: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista todos los monstruos registrados en el bestiario.
     */
    public List<Monstruo> listarTodos() {
        List<Monstruo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, vida, ataque, defensa FROM monstruos ORDER BY id ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMonstruo(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MonstruoDAO] Error listando monstruos: " + e.getMessage());
        }
        return lista;
    }

    private Monstruo mapearMonstruo(ResultSet rs) throws SQLException {
        return new Monstruo(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getInt("vida"),
                rs.getInt("ataque"),
                rs.getInt("defensa")
        );
    }
}
