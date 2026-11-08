package com.rpg.dao;

import com.rpg.model.RankingItem;
import com.rpg.model.Victoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Victoria.
 * Incluye consultas relacionales avanzadas con JOIN, GROUP BY y proyecciones a objetos POJO/DTO.
 */
public class VictoriaDAO {

    /**
     * Registra una nueva victoria en la base de datos insertando la fecha actual.
     *
     * @param jugadorId  identificador del jugador ganador
     * @param monstruoId identificador del monstruo derrotado
     * @return true si se registro correctamente; false en caso contrario
     */
    public boolean registrar(int jugadorId, int monstruoId) {
        String sql = "INSERT INTO victorias (jugador_id, monstruo_id, fecha) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jugadorId);
            pstmt.setInt(2, monstruoId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VictoriaDAO] Error al registrar victoria: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el ranking de jugadores agrupado por victorias.
     * Ejecuta una consulta con JOIN y GROUP BY ordenada descendentemente.
     *
     * @return lista de RankingItem ordenados de mayor a menor cantidad de victorias
     */
    public List<RankingItem> rankingPorJugador() {
        List<RankingItem> ranking = new ArrayList<>();
        String sql = "SELECT j.usuario, COUNT(v.id) AS total_victorias " +
                     "FROM jugadores j " +
                     "INNER JOIN victorias v ON j.id = v.jugador_id " +
                     "GROUP BY j.id, j.usuario " +
                     "ORDER BY total_victorias DESC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ranking.add(new RankingItem(
                        rs.getString("usuario"),
                        rs.getInt("total_victorias")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[VictoriaDAO] Error al consultar rankingPorJugador: " + e.getMessage());
        }
        return ranking;
    }

    /**
     * Lista todas las victorias de un jugador especifico indicando el monstruo derrotado y la fecha.
     *
     * @param usuario nombre de usuario del jugador
     * @return lista de objetos Victoria asociados al jugador
     */
    public List<Victoria> victoriasDeJugador(String usuario) {
        List<Victoria> lista = new ArrayList<>();
        String sql = "SELECT v.id, v.jugador_id, v.monstruo_id, j.usuario AS nombre_jugador, " +
                     "       m.nombre AS nombre_monstruo, v.fecha " +
                     "FROM victorias v " +
                     "INNER JOIN jugadores j ON v.jugador_id = j.id " +
                     "INNER JOIN monstruos m ON v.monstruo_id = m.id " +
                     "WHERE j.usuario = ? " +
                     "ORDER BY v.fecha DESC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha");
                    LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    lista.add(new Victoria(
                            rs.getInt("id"),
                            rs.getInt("jugador_id"),
                            rs.getInt("monstruo_id"),
                            rs.getString("nombre_jugador"),
                            rs.getString("nombre_monstruo"),
                            fecha
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VictoriaDAO] Error al consultar victoriasDeJugador: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Cuenta cuantas victorias estan asociadas a un monstruo especifico.
     * Este metodo permite al Controller comprobar integridad antes de permitir una eliminacion,
     * evitando violaciones de la clave foranea FOREIGN KEY.
     *
     * @param monstruoId ID del monstruo
     * @return numero de victorias registradas con este monstruo
     */
    public int contarPorMonstruo(int monstruoId) {
        String sql = "SELECT COUNT(*) FROM victorias WHERE monstruo_id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, monstruoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VictoriaDAO] Error al contar victorias por monstruo: " + e.getMessage());
        }
        return 0;
    }
}
