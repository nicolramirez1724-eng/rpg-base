package com.rpg.model;

/**
 * Entidad POJO que representa a un enemigo en la tabla 'monstruos'.
 */
public class Monstruo {

    private int id;
    private String nombre;
    private int vida;
    private int ataque;
    private int defensa;

    public Monstruo() {
    }

    public Monstruo(String nombre, int vida, int ataque) {
        this(0, nombre, vida, ataque, 5);
    }

    public Monstruo(String nombre, int vida, int ataque, int defensa) {
        this(0, nombre, vida, ataque, defensa);
    }

    public Monstruo(int id, String nombre, int vida, int ataque, int defensa) {
        this.id = id;
        this.nombre = nombre;
        this.vida = vida;
        this.ataque = ataque;
        this.defensa = defensa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = vida;
    }

    public int getAtaque() {
        return ataque;
    }

    public void setAtaque(int ataque) {
        this.ataque = ataque;
    }

    public int getDefensa() {
        return defensa;
    }

    public void setDefensa(int defensa) {
        this.defensa = defensa;
    }

    @Override
    public String toString() {
        return String.format("Monstruo[id=%d, nombre='%s', vida=%d, ataque=%d, defensa=%d]",
                id, nombre, vida, ataque, defensa);
    }
}
