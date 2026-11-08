package com.rpg.view;

import com.rpg.controller.LoginController;
import com.rpg.core.Consola;
import com.rpg.model.Jugador;

/**
 * Vista de Autenticacion (MVC - Capa View).
 * Regla: Solo maneja la interaccion de consola y delega toda logica en LoginController.
 * Nunca interactua con la base de datos directamente.
 */
public class LoginUI {

    private final LoginController loginController;
    private final Consola consola;

    public LoginUI(LoginController loginController, Consola consola) {
        this.loginController = loginController;
        this.consola = consola;
    }

    /**
     * Muestra el dialogo de inicio de sesion por consola.
     *
     * @return Jugador autenticado o null si cancela o falla
     */
    public Jugador mostrarLogin() {
        if (loginController.isSesionBloqueada()) {
            System.out.println("\n[ERROR] La sesion esta bloqueada. No se permiten mas intentos.");
            return null;
        }

        System.out.println("\n=================================");
        System.out.println("        INICIO DE SESION");
        System.out.println("=================================");
        String usuario = consola.leerLinea("Usuario: ");
        String clave = consola.leerLinea("Clave: ");

        Jugador jugador = loginController.iniciarSesion(usuario, clave);
        if (jugador != null) {
            System.out.printf("\nBienvenido, %s! (Rol: %s)%n", jugador.getUsuario(), jugador.getRol());
        }
        return jugador;
    }

    /**
     * Muestra el formulario de registro de nuevo jugador.
     */
    public void mostrarRegistro() {
        System.out.println("\n=================================");
        System.out.println("      REGISTRO DE JUGADOR");
        System.out.println("=================================");
        String usuario = consola.leerLinea("Nuevo usuario: ");
        String clave = consola.leerLinea("Nueva clave: ");

        if (loginController.registrarJugador(usuario, clave)) {
            System.out.println("Registro exitoso. Ya puedes iniciar sesion.");
        } else {
            System.out.println("No se pudo completar el registro.");
        }
    }
}
