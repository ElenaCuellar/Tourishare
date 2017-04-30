package com.example.caxidy.tourishare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class MostrarMensaje {

    Context contx;
    boolean ok = false;

    public MostrarMensaje(Context c){
        contx = c;
    }

    public void mostrarMensaje(String titulo, String mensaje, String boton){
        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(contx);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setMessage(mensaje);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);
        alertDialogBu.setPositiveButton(boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = alertDialogBu.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void mostrarMensajeAbreIntent(final Class clase, String titulo, String mensaje, String boton){
        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(contx);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setMessage(mensaje);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);
        alertDialogBu.setPositiveButton(boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(contx,clase);
                contx.startActivity(i);
            }
        });
        AlertDialog alertDialog = alertDialogBu.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void mostrarMensajeAceptar(String titulo, String mensaje, String boton){
        //Al pulsar Aceptar, le damos a ok el valor true, lo que activara algo en el codigo
        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(contx);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setMessage(mensaje);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);
        alertDialogBu.setPositiveButton(boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ok=true;
            }
        });
        AlertDialog alertDialog = alertDialogBu.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
