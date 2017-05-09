package com.example.caxidy.tourishare;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class BandejaEntrada extends ListActivity{

    int idUser;
    AdaptadorMensaje adaptadorM;
    ArrayList<Mensaje> listaMensajes;
    ListView listview;
    private String ip_server;
    private String url_select;
    private ProgressDialog pDialog;
    private OperacionesBD opDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bandeja_entrada);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            idUser = extras.getInt("miId");
        }

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        ip_server = sharedPref.getString("ipServer","192.168.1.101");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        opDb = new OperacionesBD();

        //Creamos la lista de usuarios amigos
        listaMensajes = new ArrayList<>();
        llenarLista();

        listview = (ListView) findViewById(android.R.id.list);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapter, View view, int position, long arg)
            {
                long msg = listview.getAdapter().getItemId(position);
                //Abrir la actividad para mostrar el mensaje
                abrirMensaje((int)msg);
            }
        });
    }

    public void abrirMensaje(int msg){
        Mensaje mensaje;
        boolean encontrado = false;
        int cont = 0;

        //Sacamos el mensaje adecuado de la lista
        do {
            mensaje = listaMensajes.get(cont);
            if(mensaje.getIdMensaje()==msg)
                encontrado = true;
            cont++;
        }while(!encontrado && cont < listaMensajes.size());

        //Se abre la actividad que muestra el mensaje, pasandole sus datos y nuestra id, para recuperar los datos en la nueva actividad
        Intent i = new Intent(this,LeerMensaje.class);
        i.putExtra("idM",mensaje.getIdMensaje());
        i.putExtra("idEmisorM",mensaje.getIdEmisor());
        i.putExtra("cabeceraM",mensaje.getCabecera());
        i.putExtra("cuerpoM",mensaje.getCuerpo());
        i.putExtra("miIdUser",idUser);
        startActivity(i);
    }

    public void llenarLista(){

        listaMensajes.clear();

        //Llenar la lista de mensajes
        new GetMensajesAsyncTask().execute();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        llenarLista();
    }

    //Tarea asincrona para llenar la lista de amigos
    class GetMensajesAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BandejaEntrada.this);
            pDialog.setMessage(getString(R.string.esperebandejaentrada));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Llenar la lista de mensajes
            listaMensajes = opDb.getMensajes(url_select, idUser);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(listaMensajes != null) {
                adaptadorM = new AdaptadorMensaje(BandejaEntrada.this, listaMensajes);
                adaptadorM.notifyDataSetChanged();
                setListAdapter(adaptadorM);
            }
            else
                new MostrarMensaje(BandejaEntrada.this).mostrarMensaje(getString(R.string.tituloproblemaactulistaprincipal),
                        getString(R.string.textoproblemaactulistaprincipal),getString(R.string.aceptar));
        }
    }
}
