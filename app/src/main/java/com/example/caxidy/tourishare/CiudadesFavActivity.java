package com.example.caxidy.tourishare;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CiudadesFavActivity extends ListActivity {

    int idUser;
    AdaptadorPrincipal adaptadorP;
    ArrayList<Ciudad> listaCiudades;
    ListView listview;
    private String ip_server;
    private String url_select;
    private ProgressDialog pDialog;
    private OperacionesBD opDb;
    private ConexionFtp conexFtp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_listaciudades);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            idUser = extras.getInt("miId");
        }

        conexFtp = new ConexionFtp();

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        ip_server = sharedPref.getString("ipServer","192.168.1.101");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        opDb = new OperacionesBD();

        //Creamos la lista de ciudades
        listaCiudades = new ArrayList<>();
        llenarLista();

        listview = (ListView) findViewById(android.R.id.list);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapter, View view, int position, long arg)
            {
                long ciu = listview.getAdapter().getItemId(position);
                //Abrir la actividad para mostrar la ciudad
                abrirCiudad(ciu);
            }
        });

    }

    public void abrirCiudad(long ciu){
        //!!probar esta clase antes que nada y luego pasar a la parte de reciclar o no MostrarCiudad
        //!!reciclar???
        //Se abre la actividad que muestra la ciudad, pasandole la id de la ciudad, para recuperar los datos en la nueva actividad
        Intent i = new Intent(this,MostrarCiudad.class);
        i.putExtra("codigoCiu",ciu);
        i.putExtra("miUserId",idUser);
        startActivity(i);
    }

    public void llenarLista(){

        listaCiudades.clear();
        //Llenar la lista de ciudades
        new GetCiudadesFavAsyncTask().execute();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        llenarLista();
    }

    //Tarea asincrona para llenar la lista de ciudades fav
    class GetCiudadesFavAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean downloadok = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(CiudadesFavActivity.this);
            pDialog.setMessage(getString(R.string.esperellenarlistaprincipal));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //1-Actualizar las fotos
            //Borramos las fotos, si las hubiere
            File miRuta = getExternalFilesDir(null);
            File archivos[] = miRuta.listFiles();

            if(archivos.length > 0) {
                for (int i = 0; i < archivos.length; i++) {
                    //si el archivo no es un directorio y es una imagen, se borra
                    if (archivos[i].isFile() && archivos[i].getName().contains(".jpg")) {
                        archivos[i].delete();
                    }
                }
            }

            try {
                downloadok = conexFtp.bajarArchivos(ip_server, CiudadesFavActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //2-Llenar la lista de ciudades
            listaCiudades = opDb.getCiudadesFav(url_select, idUser);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(listaCiudades != null && downloadok) {
                adaptadorP = new AdaptadorPrincipal(CiudadesFavActivity.this, listaCiudades);
                adaptadorP.notifyDataSetChanged();
                setListAdapter(adaptadorP);
            }
            else
                new MostrarMensaje(CiudadesFavActivity.this).mostrarMensaje(getString(R.string.tituloproblemaactulistaprincipal),
                        getString(R.string.textoproblemaactulistaprincipal),getString(R.string.aceptar));
        }
    }

}
