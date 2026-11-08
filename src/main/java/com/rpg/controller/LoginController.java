package com.rpg.controller;

import com.rpg.dao.JugadorDAO;
import com.rpg.model.Jugador;

/**
 * Controlador de Autenticacion y Gestion de Sesion (MVC - Capa Controller).
 *
 * Reglas de negocio implementadas en el Controller:
 * - Maximo 3 intentos fallidos de login; al tercer fallo la sesion se bloquea.
 * - Centraliza el estado de la sesion del jugador activo.
 */
public class LoginController {

    private static final int MAX_INTENTOS = 3;

    private final JugadorDAO jugadorDAO;
    private int intentosFallidos = 0;
    private boolean sesionBloqueada = false;
    private Jugador jugadorAutenticado = null;

    public LoginController(JugadorDAO jugadorDAO) {
        this.jugadorDAO = jugadorDAO;
    }

    /**
     * Intenta autenticar al usuario aplicando la regla de negocio de bloqueo de intentos.
     *
     * @param usuario nombre de usuario
     * @param clave   contraseña
     * @return objeto Jugador si tiene exito; null si falla o esta bloqueado
     */
    public Jugador iniciarSesion(String usuario, String clave) {
        if (sesionBloqueada) {
            System.out.println("[SEGURIDAD] La sesion esta bloqueada tras 3 intentos fallidos consecutivos.");
            return null;
        }

        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            System.out.println("[LoginController] El usuario y la contraseña no pueden estar vacios.");
            return null;
        }

        Jugador jugador = jugadorDAO.autenticar(usuario, clave);

        if (jugador != null) {
            // Exito: reiniciar contador de fallos y guardar sesion
            this.intentosFallidos = 0;
            this.jugadorAutenticado = jugador;
            return jugador;
        } else {
            // Fallo: aplicar regla de negocio de conteo
            intentosFallidos++;
            System.out.printf("[LoginController] Credenciales incorrectas. Intento %d de %d.%n",
                    intentosFallidos, MAX_INTENTOS);

            if (intentosFallidos >= MAX_INTENTOS) {
                sesionBloqueada = true;
                System.out.println("[SEGURIDAD] ¡ALERTA! Has superado el limite de 3 intentos. La sesion ha sido bloqueada.");
            }
            return null;
        }
    }

    /**
     * Registra un nuevo usuario con rol de jugador estandar.
     */
    public boolean registrarJugador(String usuario, String clave) {
        if (usuario == null || usuario.trim().length() < 3) {
            System.out.println("[LoginController] El usuario debe tener al menos 3 caracteres.");
            return false;
        }
        if (clave == null || clave.length() < 4) {
            System.out.println("[LoginController] La clave debe tener al menos 4 caracteres.");
            return false;
        }

        if (jugadorDAO.buscarPorUsuario(usuario.trim()) != null) {
            System.out.printf("[LoginController] El usuario '%s' ya se encuentra registrado.%n", usuario.trim());
            return false;
        }

        Jugador nuevo = new Jugador(usuario.trim(), clave, Jugador.ROL_JUGADOR);
        return jugadorDAO.registrar(nuevo);
    }

    public void cerrarSesion() {
        this.jugadorAutenticado = null;
    }

    public boolean estaAutenticado() {
        return jugadorAutenticado != null;
    }

    public boolean esAdmin() {
        return jugadorAutenticado != null && jugadorAutenticado.esAdmin();
    }

    public Jugador getJugadorAutenticado() {
        return jugadorAutenticado;
    }

    public boolean isSesionBloqueada() {
        return sesionBloqueada;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void desbloquearSesion() {
        this.sesionBloqueada = false;
        this.intentosFallidos = 0;
    }
}
