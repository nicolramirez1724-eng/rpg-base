package com.rpg.view;

import com.rpg.controller.RankingController;
import com.rpg.core.Consola;
import com.rpg.model.RankingItem;
import com.rpg.model.Victoria;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista de Ranking y Victorias (MVC - Capa View).
 * Regla: Muestra la informacion tabularmente al usuario; no contiene SQL ni logica de persistencia.
 */
public class RankingUI {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RankingController rankingController;
    private final Consola consola;

    public RankingUI(RankingController rankingController, Consola consola) {
        this.rankingController = rankingController;
        this.consola = consola;
    }

    public void mostrarMenuRanking() {
        int opcion;
        do {
            System.out.println("\n=================================");
            System.out.println("     SALON DE LA FAMA (RANKING)");
            System.out.println("=================================");
            System.out.println("1. Ver ranking general (Top Victorias)");
            System.out.println("2. Consultar victorias de un jugador");
            System.out.println("0. Volver");

            opcion = consola.leerEntero("Opcion: ", 0, 2);
            switch (opcion) {
                case 1 -> mostrarRankingGeneral();
                case 2 -> mostrarVictoriasJugador();
                default -> System.out.println("Volviendo...");
            }
        } while (opcion != 0);
    }

    public void mostrarRankingGeneral() {
        List<RankingItem> ranking = rankingController.obtenerRanking();
        System.out.println("\n--- RANKING GENERAL DE LA ARENA (JOIN + GROUP BY) ---");
        if (ranking.isEmpty()) {
            System.out.println("Aun no hay victorias registradas en la arena.");
            return;
        }

        System.out.printf("%-6s %-20s %-12s%n", "PUESTO", "JUGADOR", "VICTORIAS");
        System.out.println("----------------------------------------");
        int puesto = 1;
        for (RankingItem item : ranking) {
            System.out.printf("#%-5d %-20s %-12d%n", puesto++, item.getUsuario(), item.getTotalVictorias());
        }
    }

    public void mostrarVictoriasJugador() {
        String usuario = consola.leerLinea("Ingresa el nombre del jugador a consultar: ");
        List<Victoria> victorias = rankingController.obtenerHistorialJugador(usuario);

        System.out.printf("\n--- HISTORIAL DE VICTORIAS DE '%s' ---%n", usuario);
        if (victorias.isEmpty()) {
            System.out.printf("El jugador '%s' no registra victorias.%n", usuario);
            return;
        }

        System.out.printf("%-4s %-20s %-16s%n", "N°", "MONSTRUO DERROTADO", "FECHA");
        System.out.println("----------------------------------------------");
        int contador = 1;
        for (Victoria v : victorias) {
            String fechaStr = v.getFecha() != null ? v.getFecha().format(FORMATO_FECHA) : "N/D";
            System.out.printf("%-4d %-20s %-16s%n", contador++, v.getNombreMonstruo(), fechaStr);
        }
    }
}
