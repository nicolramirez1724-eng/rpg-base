package com.rpg.model;

/**
 * Entidad POJO que representa a un usuario en la tabla 'jugadores'.
 */
public class Jugador {

    public static final String ROL_ADMIN = "admin";
    public static final String ROL_JUGADOR = "jugador";

    private int id;
    private String usuario;
    private String clave;
    private String rol;

    public Jugador() {
    }

    public Jugador(String usuario, String clave, String rol) {
        this(0, usuario, clave, rol);
    }

    public Jugador(int id, String usuario, String clave, String rol) {
        this.id = id;
        this.usuario = usuario;
        this.clave = clave;
        this.rol = rol != null ? rol : ROL_JUGADOR;
    }

    public boolean esAdmin() {
        return ROL_ADMIN.equalsIgnoreCase(rol);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return String.format("Jugador[id=%d, usuario='%s', rol='%s']", id, usuario, rol);
    }
}
