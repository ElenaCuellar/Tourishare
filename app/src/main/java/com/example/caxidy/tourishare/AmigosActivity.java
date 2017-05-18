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
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AmigosActivity extends ListActivity{

    int idUser;
    AdaptadorAmigo adaptadorA;
    ArrayList<Usuario> listaAmigos;
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
        setContentView(R.layout.activity_listaamigos);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            idUser = extras.getInt("miId");
        }

        conexFtp = new ConexionFtp();

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        opDb = new OperacionesBD();

        //Creamos la lista de usuarios amigos
        listaAmigos = new ArrayList<>();
        llenarLista();

        listview = (ListView) findViewById(android.R.id.list);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapter, View view, int position, long arg)
            {
                long usu = listview.getAdapter().getItemId(position);
                //Abrir la actividad para mostrar el usuario
                abrirUsuario((int)usu);
            }
        });
    }

    public void abrirUsuario(int usu){
        Usuario amigo;
        boolean encontrado = false;
        int cont = 0;

        //Sacamos el amigo adecuado de la lista
        do {
            amigo = listaAmigos.get(cont);
            if(amigo.getId()==usu)
                encontrado = true;
            cont++;
        }while(!encontrado && cont < listaAmigos.size());

        //Se abre la actividad que muestra al usuario, pasandole el usuario amigo y nuestra id, para recuperar los datos en la nueva actividad
        Intent i = new Intent(this,MostrarUsuario.class);
        i.putExtra("idU",amigo.getId());
        i.putExtra("nombreU",amigo.getNombre());
        i.putExtra("passU",amigo.getPass());
        i.putExtra("urlfotoU",amigo.getUrlfoto());
        i.putExtra("idRangoU",amigo.getIdRango());
        i.putExtra("ciudadU",amigo.getCiudad());
        i.putExtra("miIdUser",idUser);
        startActivity(i);
    }

    public void llenarLista(){

        listaAmigos.clear();

        //Llenar la lista de amigos
        new GetAmigosAsyncTask().execute();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        llenarLista();
    }

    //Tarea asincrona para llenar la lista de amigos
    class GetAmigosAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean downloadok = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AmigosActivity.this);
            pDialog.setMessage(getString(R.string.esperellenarlistaprincipal));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //1-Actualizar las fotos (por si un usuario que tenemos de amigo ha borrado o midificado su perfil)
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
                downloadok = conexFtp.bajarArchivos(ip_server, AmigosActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //2-Llenar la lista de amigos
            listaAmigos = opDb.getAmigos(url_select, idUser);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(listaAmigos != null && downloadok) {
                adaptadorA = new AdaptadorAmigo(AmigosActivity.this, listaAmigos);
                adaptadorA.notifyDataSetChanged();
                setListAdapter(adaptadorA);
            }
            else
                new MostrarMensaje(AmigosActivity.this).mostrarMensaje(getString(R.string.tituloproblemaactulistaprincipal),
                        getString(R.string.textoproblemaactulistaprincipal),getString(R.string.aceptar));
        }
    }
}
