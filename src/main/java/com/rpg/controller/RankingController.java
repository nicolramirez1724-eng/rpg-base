package com.rpg.controller;

import com.rpg.dao.VictoriaDAO;
import com.rpg.model.RankingItem;
import com.rpg.model.Victoria;

import java.util.List;

/**
 * Controlador de Ranking y Registro de Victorias (MVC - Capa Controller).
 */
public class RankingController {

    private final VictoriaDAO victoriaDAO;

    public RankingController(VictoriaDAO victoriaDAO) {
        this.victoriaDAO = victoriaDAO;
    }

    /**
     * Obtiene el ranking general de jugadores ordenado descendentemente por victorias.
     */
    public List<RankingItem> obtenerRanking() {
        return victoriaDAO.rankingPorJugador();
    }

    /**
     * Obtiene el historial de victorias detallado de un usuario especifico.
     */
    public List<Victoria> obtenerHistorialJugador(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return List.of();
        }
        return victoriaDAO.victoriasDeJugador(usuario.trim());
    }

    /**
     * Registra una nueva victoria en la base de datos tras un combate ganado.
     */
    public boolean registrarVictoria(int jugadorId, int monstruoId) {
        if (jugadorId <= 0 || monstruoId <= 0) {
            System.out.println("[RankingController] Error: Identificadores invalidos para registrar victoria.");
            return false;
        }
        return victoriaDAO.registrar(jugadorId, monstruoId);
    }
}
