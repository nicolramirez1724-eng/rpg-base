package com.rpg.concurrency;

import com.rpg.entities.Personaje;

public class EfectoContinuoTask implements Runnable {

    private final String nombreEfecto;
    private final int intervaloMs;
    private final int valor;
    private final boolean esDanio;
    private final Personaje objetivo;

    public EfectoContinuoTask(String nombreEfecto, int intervaloMs, int valor, boolean esDanio, Personaje objetivo) {
        this.nombreEfecto = nombreEfecto;
        this.intervaloMs = intervaloMs;
        this.valor = valor;
        this.esDanio = esDanio;
        this.objetivo = objetivo;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && objetivo.estaVivo()) {
                Thread.sleep(intervaloMs);
                if (esDanio) {
                    objetivo.recibirDanio(valor);
                } else {
                    objetivo.curar(valor);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            objetivo.quitarEfecto(nombreEfecto);
        }
    }
}
