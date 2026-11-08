package com.rpg.model;

/**
 * Objeto de transferencia (DTO/POJO) para proyectar el resultado del ranking de victorias.
 */
public class RankingItem {

    private final String usuario;
    private final int totalVictorias;

    public RankingItem(String usuario, int totalVictorias) {
        this.usuario = usuario;
        this.totalVictorias = totalVictorias;
    }

    public String getUsuario() {
        return usuario;
    }

    public int getTotalVictorias() {
        return totalVictorias;
    }

    @Override
    public String toString() {
        return String.format("%-15s : %d victorias", usuario, totalVictorias);
    }
}
