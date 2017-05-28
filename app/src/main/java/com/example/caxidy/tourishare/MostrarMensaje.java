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
    public String nuevaPass;

    public MostrarMensaje(Context c){

        contx = c;
        nuevaPass = "";
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

    public void mensajeMainIntent(final Activity act, String titulo, String mensaje, String boton, boolean conCancelar){
        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(contx);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setMessage(mensaje);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);
        alertDialogBu.setPositiveButton(boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //parar la musica, si esta activa
                contx.stopService(new Intent(contx,ServicioMusicaFondo.class));
                //Ir a la actividad del Main Activity
                Intent i = new Intent(act,MainActivity.class);
                //limpiar la pila de actividades
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                act.startActivity(i);
            }
        });
        if(conCancelar) {
            alertDialogBu.setNeutralButton(contx.getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        AlertDialog alertDialog = alertDialogBu.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void cambiarPass(String titulo, String boton, final String urlUp, final int idUsuario){

        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(contx);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);

        final EditText edpass = new EditText(contx);
        edpass.setHint(contx.getString(R.string.introduzcapass));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        edpass.setLayoutParams(lp);
        alertDialogBu.setView(edpass);

        alertDialogBu.setPositiveButton(boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //devuelve la nueva contraseña
                nuevaPass = getTexto(edpass);
                opBd = new OperacionesBD();
                new ActualizarPass(urlUp,nuevaPass,idUsuario).start();
                mostrarMensaje(contx.getString(R.string.titulocambiopassexito),contx.getString(R.string.textocambiopassexito),
                        contx.getString(R.string.aceptar));
            }
        });
        AlertDialog alertDialog = alertDialogBu.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    protected String getTexto(EditText edt){
        return edt.getText().toString();
    }

    //Hilo para actualizar contraseña del usuario
    class ActualizarPass extends Thread {

        String url_update, nuevaPass;
        int idU;

        ActualizarPass(String url_update, String nuevaPass, int idU){
            this.url_update = url_update;
            this.nuevaPass = nuevaPass;
            this.idU = idU;
        }

        public void run() {
            opBd.modificarPass(url_update,nuevaPass,idU);
        }
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
                opBd.borrarRegistroId(url,tabla,cond,id);

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
                            opBd.borrarRegistroId(url,"items","IdItem",arrayItems.get(i));
                    }
                }

                //...se borra de ciudadesfav y de colaboradores
                opBd.borrarRegistro(url, "ciudadesfav", "IdCiudad = " + id);

                opBd.borrarRegistro(url, "colaboradores", "IdCiudad = " + id);
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

    //Borrar perfil de usuario
    public void mostrarBorrarPerfil(String titulo, String mensaje, String boton, final int id,
                              final String tabla, final String cond,
                              final String url_del, final String url_sel, final String ip, final Activity act){

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
                    new BorrarPerfilAsyncTask(url_del,tabla,cond,id, url_sel, ip, act).execute();
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

    //Tarea asincrona para borrar el perfil
    class BorrarPerfilAsyncTask extends AsyncTask<Void, Void, Void> {

        String tabla, cond, url, urlSel, ip;
        int id;
        Activity act;
        boolean borrado;

        protected BorrarPerfilAsyncTask(String url, String tabla, String cond, int id, String urlSel, String ip, Activity act){
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

            if(borrado) {
                opBd.borrarRegistroId(url, tabla, cond, id);

                //se borran tambien sus...
                //...amigos
                opBd.borrarRegistro(url, "amigos", "IdUsuario = " + id + " OR IdAmigo = " + id);

                //...ciudades favoritas
                opBd.borrarRegistro(url, "ciudadesfav", "IdUsuario = " + id);

                //...colaboradores
                opBd.borrarRegistro(url, "colaboradores", "IdUsuario = " + id);

                //...y mensajes
                opBd.borrarRegistro(url, "mensajes", "IdUsuario = " + id + " OR IdEmisor = " + id);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //Volvemos al intent principal (de registro)
            mensajeMainIntent(act,contx.getString(R.string.registroborrado),contx.getString(R.string.registroborrado),
                    contx.getString(R.string.aceptar),false);
        }
    }

    //Borrar mensaje
    public void mostrarBorrarMensaje(String titulo, String mensaje, String boton, final int id,
                                    final String tabla, final String cond,
                                    final String url_del, final String url_sel, final String ip, final Activity act){

        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(contx);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setMessage(mensaje);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);
        alertDialogBu.setPositiveButton(boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Borrar registro
                opBd = new OperacionesBD();
                new BorrarMensajeAsyncTask(url_del,tabla,cond,id, url_sel, ip, act).execute();
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

    //Tarea asincrona para borrar el mensaje
    class BorrarMensajeAsyncTask extends AsyncTask<Void, Void, Void> {

        String tabla, cond, url, urlSel, ip;
        int id;
        Activity act;

        protected BorrarMensajeAsyncTask(String url, String tabla, String cond, int id, String urlSel, String ip, Activity act){
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

            opBd.borrarRegistroId(url, tabla, cond, id);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //Volvemos a la bandeja de entrada
            mensajeFinish(act,contx.getString(R.string.titulomensajeborrado),contx.getString(R.string.textomensajeborrado),
                    contx.getString(R.string.aceptar));
        }
    }
}
