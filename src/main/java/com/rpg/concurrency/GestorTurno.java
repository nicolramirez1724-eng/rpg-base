package com.rpg.concurrency;

import com.rpg.core.TurnoCallback;

public class GestorTurno {

    private Thread hiloTemporizador;

    public void iniciarTemporizador(int segundos, TurnoCallback callback) {
        cancelar();
        hiloTemporizador = new Thread(new TemporizadorTurnoTask(segundos, callback), "Hilo-TemporizadorTurno");
        hiloTemporizador.start();
    }

    public void cancelar() {
        if (hiloTemporizador != null && hiloTemporizador.isAlive()) {
            hiloTemporizador.interrupt();
        }
    }
}
