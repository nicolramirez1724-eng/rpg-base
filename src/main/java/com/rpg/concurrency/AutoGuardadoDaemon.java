package com.rpg.concurrency;

import com.rpg.core.MotorJuego;

public class AutoGuardadoDaemon implements Runnable {

    private final MotorJuego motor;
    private final int intervaloMs;

    public AutoGuardadoDaemon(MotorJuego motor, int intervaloMs) {
        this.motor = motor;
        this.intervaloMs = intervaloMs;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(intervaloMs);
                motor.guardarProgreso();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Thread iniciar(MotorJuego motor, int intervaloMs) {
        Thread daemon = new Thread(new AutoGuardadoDaemon(motor, intervaloMs), "Hilo-AutoGuardadoDaemon");
        daemon.setDaemon(true); // Obligatorio antes de start()
        daemon.start();
        return daemon;
    }
}
