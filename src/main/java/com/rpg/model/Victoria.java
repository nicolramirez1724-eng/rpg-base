package com.rpg.model;

import java.time.LocalDateTime;

/**
 * Entidad POJO que representa el registro de una victoria en la tabla 'victorias'.
 */
public class Victoria {

    private int id;
    private int jugadorId;
    private int monstruoId;
    private String nombreJugador;
    private String nombreMonstruo;
    private LocalDateTime fecha;

    public Victoria() {
    }

    public Victoria(int jugadorId, int monstruoId) {
        this(0, jugadorId, monstruoId, null, null, LocalDateTime.now());
    }

    public Victoria(int id, int jugadorId, int monstruoId, String nombreJugador, String nombreMonstruo, LocalDateTime fecha) {
        this.id = id;
        this.jugadorId = jugadorId;
        this.monstruoId = monstruoId;
        this.nombreJugador = nombreJugador;
        this.nombreMonstruo = nombreMonstruo;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(int jugadorId) {
        this.jugadorId = jugadorId;
    }

    public int getMonstruoId() {
        return monstruoId;
    }

    public void setMonstruoId(int monstruoId) {
        this.monstruoId = monstruoId;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getNombreMonstruo() {
        return nombreMonstruo;
    }

    public void setNombreMonstruo(String nombreMonstruo) {
        this.nombreMonstruo = nombreMonstruo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return String.format("Victoria[id=%d, jugador='%s', monstruo='%s', fecha=%s]",
                id, nombreJugador != null ? nombreJugador : jugadorId,
                nombreMonstruo != null ? nombreMonstruo : monstruoId, fecha);
    }
}
