package com.example.caxidy.tourishare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.SupportMapFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MostrarMensaje {

    Context contx;
    boolean ok = false, esciudad=false;
    OperacionesBD opBd;
    ConexionFtp conexFtp;

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

    public void mensajeFinish(final Activity act, String titulo, String mensaje, String boton){
        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(contx);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setMessage(mensaje);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);
        alertDialogBu.setPositiveButton(boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Terminar actividad tras pulsar en Aceptar
                act.finish();
            }
        });
        AlertDialog alertDialog = alertDialogBu.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void mostrarBorrar(String titulo, String mensaje, String boton, final int id,
                              final String tabla, final String cond,
                              final String url_del, final String url_sel, final String ip, final Activity act,
                              final boolean ciudad){
        esciudad = ciudad;
        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(contx);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setMessage(mensaje);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);
        final EditText edpass = new EditText(contx);
        edpass.setHint(contx.getString(R.string.introduzcaadmin));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        edpass.setLayoutParams(lp);
        alertDialogBu.setView(edpass);
        alertDialogBu.setPositiveButton(boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(edpass.getText().toString().equals("adminmanager")) {
                    //Borrar registro
                    opBd = new OperacionesBD();
                    new BorrarRegistroAsyncTask(url_del,tabla,cond,id, url_sel, ip, act).execute();
                }

                else
                    mostrarMensaje(contx.getString(R.string.titulopassincorrecta),
                            contx.getString(R.string.textopassincorrecta),contx.getString(R.string.aceptar));
            }
        });
        alertDialogBu.setNeutralButton(contx.getString(R.string.cancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = alertDialogBu.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    //Tarea asincrona para borrar un registro
    class BorrarRegistroAsyncTask extends AsyncTask<Void, Void, Void> {

        String tabla, cond, url, urlSel, ip;
        int id;
        Activity act;
        boolean borrado;

        protected BorrarRegistroAsyncTask(String url, String tabla, String cond, int id, String urlSel, String ip, Activity act){
            this.url = url;
            this.tabla = tabla;
            this.cond = cond;
            this.id = id;
            this.urlSel = urlSel;
            this.ip = ip;
            this.act = act;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //borramos la foto
            conexFtp = new ConexionFtp();
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("host",ip);
            parametros.put("nombreFoto",opBd.getNombreFoto(urlSel,tabla,cond,id));
            try {
                borrado = conexFtp.borrarArchivo(parametros);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(borrado)
                opBd.borrarRegistro(url,tabla,cond,id);

            //...si estamos borrando una ciudad
            if(esciudad){
                //...se borran tambien sus subcategorias
                ArrayList<Integer> arrayItems = new ArrayList<>();
                arrayItems = opBd.getIdsSubcategorias(urlSel,id);

                if(arrayItems != null && arrayItems.size() > 0){
                    for (int i=0; i<arrayItems.size();i++){
                        HashMap<String, String> params = new HashMap<>();
                        params.put("host",ip);
                        params.put("nombreFoto",opBd.getNombreFoto(urlSel, "items", "IdItem", arrayItems.get(i)));
                        try {
                            borrado = conexFtp.borrarArchivo(params);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(borrado)
                            opBd.borrarRegistro(url,"items","IdItem",arrayItems.get(i));
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mensajeFinish(act,contx.getString(R.string.registroborrado),contx.getString(R.string.registroborrado),
                    contx.getString(R.string.aceptar));
        }
    }
}
