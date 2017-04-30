package com.example.caxidy.tourishare;

public class Subcategoria {
    int idCategoria, id;
    String nombre, descripcion, urlfoto;
    double latitud, longitud;
    float puntuacion;

    public Subcategoria(int idCategoria, String nombre, String descripcion, String urlfoto, double latitud, double longitud, float puntuacion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlfoto = urlfoto;
        this.urlfoto = urlfoto;
        this.latitud = latitud;
        this.longitud = longitud;
        this.puntuacion = puntuacion;
        id = -1;
    }

    public Subcategoria(int id, int idCategoria, String nombre, String descripcion, String urlfoto, double latitud, double longitud, float puntuacion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlfoto = urlfoto;
        this.urlfoto = urlfoto;
        this.latitud = latitud;
        this.longitud = longitud;
        this.puntuacion = puntuacion;
        this.id = id;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrlfoto() {
        return urlfoto;
    }

    public void setUrlfoto(String urlfoto) {
        this.urlfoto = urlfoto;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(float latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(float longitud) {
        this.longitud = longitud;
    }

    public float getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(float puntuacion) {
        this.puntuacion = puntuacion;
    }

    public int getId() {
        return id;
    }
}
