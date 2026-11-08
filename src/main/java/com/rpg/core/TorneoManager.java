package com.rpg.core;

import com.rpg.entities.Escuadron;
import com.rpg.entities.Guerrero;
import com.rpg.entities.Mago;
import com.rpg.entities.Personaje;

import java.util.List;

/**
 * Torneo de 4 gladiadores: dos semifinales en paralelo con hilos y join, y una final.
 */
public class TorneoManager {

    private final Arena arena;
    private final RegistroCombate registro;

    public TorneoManager(Arena arena, RegistroCombate registro) {
        this.arena = arena;
        this.registro = registro;
    }

    /**
     * Simula el torneo ejecutando las dos semifinales en paralelo mediante hilos (Threads),
     * sincronizandolas con join() antes de disputar la gran final.
     *
     * @return el campeon del torneo
     */
    public Personaje jugarTorneo() {
        List<Personaje> gladiadores = crearGladiadores().getMiembros();

        final Personaje[] ganadores = new Personaje[2];

        Thread semiA = new Thread(() -> {
            ganadores[0] = simularSemifinal(gladiadores.get(0), gladiadores.get(1));
        }, "Hilo-Semifinal-A");

        Thread semiB = new Thread(() -> {
            ganadores[1] = simularSemifinal(gladiadores.get(2), gladiadores.get(3));
        }, "Hilo-Semifinal-B");

        // Estado inicial: NEW
        registro.agregar("[Concurrencia] Estado inicial semiA: %s, semiB: %s", semiA.getState(), semiB.getState());

        semiA.start();
        semiB.start();

        // Estado en ejecucion: RUNNABLE / TIMED_WAITING
        registro.agregar("[Concurrencia] Tras start() - semiA: %s, semiB: %s", semiA.getState(), semiB.getState());

        try {
            semiA.join();
            semiB.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            registro.agregar("[Torneo] Error: El torneo fue interrumpido: %s", e.getMessage());
        }

        // Estado final: TERMINATED
        registro.agregar("[Concurrencia] Tras join() - semiA: %s, semiB: %s", semiA.getState(), semiB.getState());

        return simularFinal(ganadores[0], ganadores[1]);
    }

    public Personaje simularSemifinal(Personaje a, Personaje b) {
        registro.agregar("=== SEMIFINAL: %s vs %s ===", a.getNombre(), b.getNombre());
        Personaje ganador = arena.duelo(a, b);
        registro.agregar("Clasifica a la final: %s", ganador.getNombre());
        return ganador;
    }

    private Personaje simularFinal(Personaje a, Personaje b) {
        a.restaurarVida();
        b.restaurarVida();
        registro.agregar("=== FINAL: %s vs %s ===", a.getNombre(), b.getNombre());
        return arena.duelo(a, b);
    }

    private Escuadron crearGladiadores() {
        Escuadron escuadron = new Escuadron("Gladiadores");
        escuadron.agregar(new Guerrero("Maximo"));
        escuadron.agregar(new Mago("Circe"));
        escuadron.agregar(new Guerrero("Aquiles"));
        escuadron.agregar(new Mago("Merlina"));
        return escuadron;
    }
}
