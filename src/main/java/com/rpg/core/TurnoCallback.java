package com.rpg.core;

/**
 * Interfaz callback para notificar cuando se agota el tiempo del turno (Seccion 1 Concurrencia).
 */
public interface TurnoCallback {
    void onTiempoAgotado();
}
