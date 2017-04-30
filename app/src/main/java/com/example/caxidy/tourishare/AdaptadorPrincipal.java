package com.example.caxidy.tourishare;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AdaptadorPrincipal extends BaseAdapter {

    private ArrayList<Ciudad> lista;
    private final Activity actividad;
    private ConexionFtp conexFtp;
    private String url_download_foto;
    private String ip_server;
    private ImageView fotoLista;

    public AdaptadorPrincipal(Activity a, ArrayList<Ciudad> v, String ip){
        super();
        this.lista = v;
        this.actividad = a;
        ip_server = ip;
        conexFtp = new ConexionFtp();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater ly = actividad.getLayoutInflater();
        View view = ly.inflate(R.layout.item_lista, null, true);

        //Llenamos los campos de la vista actual con los datos correspondientes
        TextView tNom= (TextView) view.findViewById(R.id.itemNombre);
        tNom.setText(lista.get(position).getNombre());
        fotoLista = (ImageView) view.findViewById(R.id.itemFoto);
        url_download_foto = "archivosFilezilla/" + lista.get(position).getUrlfoto();

        new GetFotoAsyncTask().execute();

        //!!hace falta una pausa porque no da tiempo a coger las fotos.
        //!!tambien: scrollbar ajustada en la lista segun items???, igual es mejor quitar las fotos de las listas, aunq
        //!!lo del ftp es util para recuperar fotos en otras actividades

        return view;
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return lista.get(position).getId();
    }

    //Tarea asincrona para hacer la consulta al servidor ftp
    class GetFotoAsyncTask extends AsyncTask<Void, Void, Void> {

        HashMap<String, String> params;
        boolean downloadok;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //parametros del FTP
            params = new HashMap<String, String>();
            params.put("host", ip_server);
            params.put("downloadpath", url_download_foto);

            //Descargar la foto de Filezilla
            try {
                downloadok = conexFtp.bajarDatos(params, actividad);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //Si la foto se ha descargado correctamente, ponerla en el imageview como bitmap (y borrarla de la carpeta del proyecto)
            if(downloadok){
                File archivoImg = new File(actividad.getExternalFilesDir(null) + "/temporal.jpg");
                if (archivoImg.exists()) {
                    fotoLista.setImageBitmap(BitmapFactory.decodeFile(archivoImg.getAbsolutePath()));
                    fotoLista.setAdjustViewBounds(true);
                    archivoImg.delete();
                }
            }
        }
    }
}
