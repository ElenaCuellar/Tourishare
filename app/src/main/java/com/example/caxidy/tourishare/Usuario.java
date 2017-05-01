package com.example.caxidy.tourishare;

public class Usuario {
    int idRango, id;
    String nombre, pass, urlfoto, ciudad;

    public Usuario(String nombre, String pass, String urlfoto, String ciudad) {
        this.idRango = 1;
        this.nombre = nombre;
        this.pass = pass;
        this.urlfoto = urlfoto;
        this.ciudad = ciudad;
        id = -1;
    }

    public Usuario(int id, String nombre, String pass, String urlfoto, int idRango, String ciudad) {
        this.idRango = idRango;
        this.nombre = nombre;
        this.pass = pass;
        this.urlfoto = urlfoto;
        this.ciudad = ciudad;
        this.id = id;
    }

    public int getIdRango() {
        return idRango;
    }

    public void setIdRango(int idRango) {
        this.idRango = idRango;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUrlfoto() {
        return urlfoto;
    }

    public void setUrlfoto(String urlfoto) {
        this.urlfoto = urlfoto;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public int getId() {
        return id;
    }
}
