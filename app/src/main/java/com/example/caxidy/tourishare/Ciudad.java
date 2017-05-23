package com.example.caxidy.tourishare;

public class Ciudad {
    String nombre, descripcion, urlfoto;
    double latitud, longitud;
    int id;

    public Ciudad(String nombre, String descripcion, String urlfoto, double latitud, double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlfoto = urlfoto;
        this.latitud = latitud;
        this.longitud = longitud;
        id = -1;
    }

    public Ciudad(String nombre, String descripcion, double latitud, double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlfoto = "";
        this.latitud = latitud;
        this.longitud = longitud;
        id = -1;
    }

    public Ciudad (int id, String nombre, String descripcion, String urlfoto, double latitud, double longitud){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlfoto = urlfoto;
        this.latitud = latitud;
        this.longitud = longitud;
        this.id = id;
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

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public int getId() {
        return id;
    }
}
