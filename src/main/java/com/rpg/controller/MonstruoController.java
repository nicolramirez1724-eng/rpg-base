package com.rpg.controller;

import com.rpg.dao.MonstruoDAO;
import com.rpg.dao.VictoriaDAO;
import com.rpg.model.Jugador;
import com.rpg.model.Monstruo;

import java.util.List;

/**
 * Controlador para la Gestion del Bestiario y Monstruos (MVC - Capa Controller).
 *
 * Reglas de negocio implementadas en el Controller:
 * 1. Solo usuarios con rol 'admin' pueden crear, modificar o eliminar monstruos.
 * 2. No se puede eliminar un monstruo con victorias registradas: el Controller lo detecta
 *    consultando a VictoriaDAO antes de invocar la eliminacion, evitando la excepcion
 *    por violacion de FOREIGN KEY.
 */
public class MonstruoController {

    private final MonstruoDAO monstruoDAO;
    private final VictoriaDAO victoriaDAO;

    public MonstruoController(MonstruoDAO monstruoDAO, VictoriaDAO victoriaDAO) {
        this.monstruoDAO = monstruoDAO;
        this.victoriaDAO = victoriaDAO;
    }

    /**
     * Regla de negocio: Solo admin puede crear monstruos.
     */
    public boolean crearMonstruo(Jugador usuarioActual, String nombre, int vida, int ataque, int defensa) {
        if (!validarRolAdmin(usuarioActual, "crear")) {
            return false;
        }

        if (nombre == null || nombre.isBlank()) {
            System.out.println("[MonstruoController] Error: El nombre del monstruo no puede estar vacio.");
            return false;
        }
        if (vida <= 0 || ataque <= 0 || defensa < 0) {
            System.out.println("[MonstruoController] Error: Estadisticas invalidas (vida > 0, ataque > 0, defensa >= 0).");
            return false;
        }
        if (monstruoDAO.buscarPorNombre(nombre.trim()) != null) {
            System.out.printf("[MonstruoController] Error: Ya existe un monstruo con el nombre '%s'.%n", nombre.trim());
            return false;
        }

        Monstruo nuevo = new Monstruo(nombre.trim(), vida, ataque, defensa);
        return monstruoDAO.crear(nuevo);
    }

    /**
     * Regla de negocio: Solo admin puede editar monstruos.
     */
    public boolean actualizarMonstruo(Jugador usuarioActual, int id, String nombre, int vida, int ataque, int defensa) {
        if (!validarRolAdmin(usuarioActual, "editar")) {
            return false;
        }

        Monstruo existente = monstruoDAO.obtenerPorId(id);
        if (existente == null) {
            System.out.printf("[MonstruoController] Error: No existe monstruo con ID %d.%n", id);
            return false;
        }
        if (vida <= 0 || ataque <= 0 || defensa < 0) {
            System.out.println("[MonstruoController] Error: Estadisticas invalidas.");
            return false;
        }

        existente.setNombre(nombre.trim());
        existente.setVida(vida);
        existente.setAtaque(ataque);
        existente.setDefensa(defensa);

        return monstruoDAO.actualizar(existente);
    }

    /**
     * Regla de negocio: Solo admin puede eliminar monstruos Y no se puede eliminar
     * si tiene victorias registradas en la tabla victorias.
     */
    public boolean eliminarMonstruo(Jugador usuarioActual, int id) {
        if (!validarRolAdmin(usuarioActual, "eliminar")) {
            return false;
        }

        Monstruo monstruo = monstruoDAO.obtenerPorId(id);
        if (monstruo == null) {
            System.out.printf("[MonstruoController] Error: No existe ningun monstruo con ID %d.%n", id);
            return false;
        }

        // Regla de integridad referencial gestionada por el Controller:
        int victoriasAsociadas = victoriaDAO.contarPorMonstruo(id);
        if (victoriasAsociadas > 0) {
            System.out.printf("[MonstruoController] Operacion denegada: No se puede eliminar el monstruo '%s' (ID %d) " +
                            "porque tiene %d victoria(s) registrada(s) en la arena. " +
                            "La integridad referencial ha sido protegida.%n",
                    monstruo.getNombre(), id, victoriasAsociadas);
            return false;
        }

        return monstruoDAO.eliminar(id);
    }

    // Consultas de solo lectura (permitidas para cualquier rol de jugador)

    public List<Monstruo> listarTodos() {
        return monstruoDAO.listarTodos();
    }

    public Monstruo buscarPorNombre(String nombre) {
        return monstruoDAO.buscarPorNombre(nombre);
    }

    public List<Monstruo> listarPorVidaMinima(int vidaMinima) {
        return monstruoDAO.listarPorVidaMinima(vidaMinima);
    }

    private boolean validarRolAdmin(Jugador usuario, String accion) {
        if (usuario == null || !usuario.esAdmin()) {
            System.out.printf("[MonstruoController] Acceso Denegado: Solo usuarios con rol 'admin' pueden %s monstruos.%n",
                    accion);
            return false;
        }
        return true;
    }
}
