package com.rpg;

import com.rpg.controller.LoginController;
import com.rpg.controller.MonstruoController;
import com.rpg.controller.RankingController;
import com.rpg.core.Consola;
import com.rpg.core.MotorJuego;
import com.rpg.dao.JugadorDAO;
import com.rpg.dao.MonstruoDAO;
import com.rpg.dao.VictoriaDAO;
import com.rpg.demo.LoginInseguroDemo;
import com.rpg.model.Jugador;
import com.rpg.view.LoginUI;
import com.rpg.view.MonstruoUI;
import com.rpg.view.RankingUI;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Punto de entrada del Simulador de Batalla RPG.
 * Integra la arquitectura MVC + DAO ("Arena Segura") con el flujo de juego original.
 */
public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Consola consola = new Consola(scanner);

            // Inicializacion de DAOs (Capa Model)
            JugadorDAO jugadorDAO = new JugadorDAO();
            MonstruoDAO monstruoDAO = new MonstruoDAO();
            VictoriaDAO victoriaDAO = new VictoriaDAO();

            // Inicializacion de Controllers (Capa Controller)
            LoginController loginController = new LoginController(jugadorDAO);
            MonstruoController monstruoController = new MonstruoController(monstruoDAO, victoriaDAO);
            RankingController rankingController = new RankingController(victoriaDAO);

            // Inicializacion de Vistas (Capa View)
            LoginUI loginUI = new LoginUI(loginController, consola);
            MonstruoUI monstruoUI = new MonstruoUI(monstruoController, consola);
            RankingUI rankingUI = new RankingUI(rankingController, consola);

            // Motor del juego integrado
            MotorJuego motor = new MotorJuego(consola, jugadorDAO, monstruoDAO, victoriaDAO);

            ejecutarMenu(consola, motor, loginController, loginUI, monstruoUI, rankingUI);
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada finalizada. Hasta luego.");
        }
    }

    private static void ejecutarMenu(Consola consola,
                                     MotorJuego motor,
                                     LoginController loginController,
                                     LoginUI loginUI,
                                     MonstruoUI monstruoUI,
                                     RankingUI rankingUI) {
        int opcion;
        do {
            mostrarMenu(loginController.getJugadorAutenticado());
            opcion = consola.leerEntero("Opcion: ", 0, 8);
            switch (opcion) {
                case 1 -> motor.combateRapido();
                case 2 -> motor.jugarTorneo();
                case 3 -> motor.mostrarBestiario();
                case 4 -> {
                    if (loginController.estaAutenticado()) {
                        loginController.cerrarSesion();
                        motor.setJugadorEnSesion(null);
                        System.out.println("Sesion cerrada exitosamente.");
                    } else {
                        Jugador logueado = loginUI.mostrarLogin();
                        if (logueado != null) {
                            motor.setJugadorEnSesion(logueado);
                        }
                    }
                }
                case 5 -> loginUI.mostrarRegistro();
                case 6 -> monstruoUI.mostrarMenuBestiario(loginController.getJugadorAutenticado());
                case 7 -> rankingUI.mostrarMenuRanking();
                case 8 -> LoginInseguroDemo.main(new String[0]);
                default -> System.out.println("Hasta luego. Gracias por jugar!");
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu(Jugador usuarioActual) {
        System.out.println("\n=========================================");
        System.out.println("     SIMULADOR DE BATALLA RPG - ARENA    ");
        if (usuarioActual != null) {
            System.out.printf("  [Sesion activa: %s | Rol: %s]%n",
                    usuarioActual.getUsuario(), usuarioActual.getRol().toUpperCase());
        } else {
            System.out.println("  [Sesion: Invitado / Sin autenticar]");
        }
        System.out.println("=========================================");
        System.out.println("1. Combate rapido");
        System.out.println("2. Torneo");
        System.out.println("3. Ver bestiario en memoria");
        if (usuarioActual != null) {
            System.out.println("4. Cerrar sesion");
        } else {
            System.out.println("4. Iniciar sesion (LoginUI - Seguro)");
        }
        System.out.println("5. Registrar nuevo jugador");
        System.out.println("6. Gestion de Bestiario en BD (MonstruoUI)");
        System.out.println("7. Ver Ranking y Victorias (RankingUI)");
        System.out.println("8. Ejecutar Demo Inyeccion SQL (LoginInseguroDemo)");
        System.out.println("0. Salir");
    }
}
