package com.example.caxidy.tourishare;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ListaSubcategoria extends ListActivity {

    TextView titulolista;
    AdaptadorSubcategoria adaptadorSubc;
    ArrayList<Subcategoria> listaSubcats;
    ListView listview;
    String ip_server, url_select;
    private ProgressDialog pDialog;
    private OperacionesBD opDb;
    private ConexionFtp conexFtp;
    int idTipo, idCiu, idUser;
    String cabecera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_listasubcategoria);

        titulolista = (TextView) findViewById(R.id.titulosubcatlista);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            idTipo = extras.getInt("tipoCat");
            idCiu = extras.getInt("idCiu");
            idUser = extras.getInt("idUser");
        }

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        opDb = new OperacionesBD();

        conexFtp = new ConexionFtp();

        //Creamos la lista de ciudades
        listaSubcats = new ArrayList<>();
        llenarLista();

        listview = (ListView) findViewById(android.R.id.list);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapter, View view, int position, long arg)
            {
                long subc = listview.getAdapter().getItemId(position);
                //Abrir la actividad para mostrar la subcat
                abrirSubcat(subc);
            }
        });
    }

    public void abrirSubcat(long subc){
        //Se abre la actividad que muestra la subcategoria, pasandole la id de la subcat, para recuperar los datos en la nueva actividad
        Intent i = new Intent(this,MostrarSubcategoria.class);
        i.putExtra("codigoSubc",subc);
        i.putExtra("idCiu",idCiu);
        i.putExtra("idUser",idUser);
        i.putExtra("idTipo",idTipo);
        startActivity(i);
    }

    public void llenarLista(){
        listaSubcats.clear();
        //Llenar la lista de ciudades
        new GetSubcatsAsyncTask().execute();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        llenarLista();
    }

    //Tarea asincrona para llenar la lista de subcategorias
    class GetSubcatsAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean downloadok = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ListaSubcategoria.this);
            pDialog.setMessage(getString(R.string.esperellenarlistaprincipal));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Cabecera de la lista
             cabecera = opDb.getCabeceraSubcat(url_select,idTipo);

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

            //Nos bajamos las fotos actualizadas
            try {
                downloadok = conexFtp.bajarArchivos(ip_server, ListaSubcategoria.this);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //2-Llenar la lista de un tipo de subcategoria
            listaSubcats = opDb.getListaSubcategoriaCiudad(url_select, idCiu, idTipo);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            //Reducir la fuente si es el tipo "Lugares de interes" (no cabe)
            if(idTipo==5)
                titulolista.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titulolista.setText(cabecera);

            if(listaSubcats != null && downloadok) {
                adaptadorSubc = new AdaptadorSubcategoria(ListaSubcategoria.this, listaSubcats);
                adaptadorSubc.notifyDataSetChanged();
                setListAdapter(adaptadorSubc);
            }
            else
                new MostrarMensaje(ListaSubcategoria.this).mostrarMensaje(getString(R.string.tituloproblemaactulistaprincipal),
                        getString(R.string.textoproblemaactulistaprincipal),getString(R.string.aceptar));
        }
    }
}

