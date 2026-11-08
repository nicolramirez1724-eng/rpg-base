package com.rpg.concurrency;

import com.rpg.core.TurnoCallback;

/**
 * Tarea Runnable para la cuenta regresiva del turno (Thread.sleep e InterruptedException).
 */
public class TemporizadorTurnoTask implements Runnable {

    private final int segundos;
    private final TurnoCallback callback;

    public TemporizadorTurnoTask(int segundos, TurnoCallback callback) {
        this.segundos = segundos;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            for (int i = segundos; i > 0; i--) {
                Thread.sleep(1000);
            }
            if (callback != null) {
                callback.onTiempoAgotado();
            }
        } catch (InterruptedException e) {
            // El jugador respondio a tiempo y el temporizador fue cancelado limpiamente
            Thread.currentThread().interrupt();
        }
    }
}
