package com.rpg.view;

import com.rpg.controller.MonstruoController;
import com.rpg.core.Consola;
import com.rpg.model.Jugador;
import com.rpg.model.Monstruo;

import java.util.List;

/**
 * Vista de Gestion del Bestiario y Monstruos (MVC - Capa View).
 * Regla: Nunca toca la base de datos ni aplica reglas de negocio; delega en MonstruoController.
 */
public class MonstruoUI {

    private final MonstruoController monstruoController;
    private final Consola consola;

    public MonstruoUI(MonstruoController monstruoController, Consola consola) {
        this.monstruoController = monstruoController;
        this.consola = consola;
    }

    public void mostrarMenuBestiario(Jugador jugadorActual) {
        int opcion;
        do {
            System.out.println("\n=================================");
            System.out.println("   BESTIARIO DE LA ARENA (DB)");
            System.out.println("=================================");
            System.out.println("1. Listar todos los monstruos");
            System.out.println("2. Buscar monstruo por nombre");
            System.out.println("3. Filtrar por vida minima");
            if (jugadorActual != null && jugadorActual.esAdmin()) {
                System.out.println("--- Opciones de Administrador ---");
                System.out.println("4. Crear nuevo monstruo");
                System.out.println("5. Editar monstruo existente");
                System.out.println("6. Eliminar monstruo");
            }
            System.out.println("0. Volver al menu principal");

            int maxOpcion = (jugadorActual != null && jugadorActual.esAdmin()) ? 6 : 3;
            opcion = consola.leerEntero("Opcion: ", 0, maxOpcion);

            switch (opcion) {
                case 1 -> listarMonstruos();
                case 2 -> buscarPorNombre();
                case 3 -> filtrarPorVidaMinima();
                case 4 -> crearMonstruo(jugadorActual);
                case 5 -> editarMonstruo(jugadorActual);
                case 6 -> eliminarMonstruo(jugadorActual);
                default -> System.out.println("Volviendo...");
            }
        } while (opcion != 0);
    }

    public void listarMonstruos() {
        List<Monstruo> lista = monstruoController.listarTodos();
        System.out.println("\n--- LISTADO DE MONSTRUOS REGISTRADOS ---");
        if (lista.isEmpty()) {
            System.out.println("No hay monstruos registrados en la base de datos.");
            return;
        }
        System.out.printf("%-4s %-18s %-8s %-8s %-8s%n", "ID", "NOMBRE", "VIDA", "ATAQUE", "DEFENSA");
        System.out.println("--------------------------------------------------");
        for (Monstruo m : lista) {
            System.out.printf("%-4d %-18s %-8d %-8d %-8d%n",
                    m.getId(), m.getNombre(), m.getVida(), m.getAtaque(), m.getDefensa());
        }
    }

    private void buscarPorNombre() {
        String nombre = consola.leerLinea("Nombre del monstruo a buscar: ");
        Monstruo m = monstruoController.buscarPorNombre(nombre);
        if (m != null) {
            System.out.println("\nMonstruo encontrado:");
            System.out.printf("  ID: %d | Nombre: %s | Vida: %d | Ataque: %d | Defensa: %d%n",
                    m.getId(), m.getNombre(), m.getVida(), m.getAtaque(), m.getDefensa());
        } else {
            System.out.printf("No se encontro ningun monstruo llamado '%s'.%n", nombre);
        }
    }

    private void filtrarPorVidaMinima() {
        int vidaMinima = consola.leerEntero("Vida minima para filtrar: ", 1, 9999);
        List<Monstruo> filtrados = monstruoController.listarPorVidaMinima(vidaMinima);
        System.out.printf("\n--- Monstruos con Vida >= %d ---%n", vidaMinima);
        if (filtrados.isEmpty()) {
            System.out.println("Ningun monstruo cumple con el criterio.");
            return;
        }
        for (Monstruo m : filtrados) {
            System.out.printf("  [%d] %-16s HP: %3d | ATK: %2d | DEF: %2d%n",
                    m.getId(), m.getNombre(), m.getVida(), m.getAtaque(), m.getDefensa());
        }
    }

    private void crearMonstruo(Jugador admin) {
        System.out.println("\n--- Crear Nuevo Monstruo ---");
        String nombre = consola.leerLinea("Nombre: ");
        int vida = consola.leerEntero("Vida (HP): ", 1, 5000);
        int ataque = consola.leerEntero("Ataque: ", 1, 500);
        int defensa = consola.leerEntero("Defensa: ", 0, 200);

        if (monstruoController.crearMonstruo(admin, nombre, vida, ataque, defensa)) {
            System.out.println("Monstruo creado exitosamente en la base de datos.");
        } else {
            System.out.println("No se pudo crear el monstruo.");
        }
    }

    private void editarMonstruo(Jugador admin) {
        System.out.println("\n--- Editar Monstruo Existente ---");
        int id = consola.leerEntero("ID del monstruo a editar: ", 1, 99999);
        String nombre = consola.leerLinea("Nuevo nombre: ");
        int vida = consola.leerEntero("Nueva vida: ", 1, 5000);
        int ataque = consola.leerEntero("Nuevo ataque: ", 1, 500);
        int defensa = consola.leerEntero("Nueva defensa: ", 0, 200);

        if (monstruoController.actualizarMonstruo(admin, id, nombre, vida, ataque, defensa)) {
            System.out.println("Monstruo actualizado exitosamente.");
        } else {
            System.out.println("No se pudo actualizar el monstruo.");
        }
    }

    private void eliminarMonstruo(Jugador admin) {
        System.out.println("\n--- Eliminar Monstruo ---");
        int id = consola.leerEntero("ID del monstruo a eliminar: ", 1, 99999);
        String confirmacion = consola.leerLinea("¿Esta seguro de eliminar el monstruo con ID " + id + "? (s/n): ");
        if ("s".equalsIgnoreCase(confirmacion)) {
            if (monstruoController.eliminarMonstruo(admin, id)) {
                System.out.println("Monstruo eliminado correctamente.");
            }
        } else {
            System.out.println("Operacion cancelada.");
        }
    }
}
