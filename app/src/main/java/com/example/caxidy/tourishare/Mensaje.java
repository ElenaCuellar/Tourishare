package com.example.caxidy.tourishare;

public class Mensaje {
    int idMensaje, idUsuario, idEmisor;
    String cabecera, cuerpo;

    public Mensaje(int idMensaje, int idUsuario, int idEmisor, String cabecera, String cuerpo) {
        this.idMensaje = idMensaje;
        this.idUsuario = idUsuario;
        this.idEmisor = idEmisor;
        this.cabecera = cabecera;
        this.cuerpo = cuerpo;
    }

    public int getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(int idMensaje) {
        this.idMensaje = idMensaje;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(int idEmisor) {
        this.idEmisor = idEmisor;
    }

    public String getCabecera() {
        return cabecera;
    }

    public void setCabecera(String cabecera) {
        this.cabecera = cabecera;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }
}
