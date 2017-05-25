package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int SERVIDOR_CAMBIADO = 1;

    Button bsignup, bentrar, bacercade, bprefsmain;
    private ProgressDialog pDialog;
    private String ip_server;
    ConexionFtp conexFtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //para ocultar el header, el cual no queremos en esta actividad
        setContentView(R.layout.activity_main);

        bsignup = (Button) findViewById(R.id.bRegistrarse);
        bentrar = (Button) findViewById(R.id.bEntrar);
        bacercade = (Button) findViewById(R.id.bAcercade);
        bprefsmain = (Button) findViewById(R.id.bPrefsMain);

        conexFtp = new ConexionFtp();

        bsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //abrir intent para registrar un nuevo usuario
                verActividadSignup();
            }
        });

        bentrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //abrir intent para entrar en el programa
                verActividadLogin();
            }
        });

        bacercade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mostrar info de Acerca de
                new MostrarMensaje(MainActivity.this).mostrarMensaje(getString(R.string.acercade),
                        getString(R.string.textoacercade),getString(R.string.aceptar));
            }
        });

        bprefsmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //abrir preferencias para configurar la ip de servidor
                verPreferencias();
            }
        });

        //Descargamos las fotos del FTP de forma temporal
        descargaFotos();
    }

    protected void verActividadSignup(){
        Intent i = new Intent(this,SignupActivity.class);
        startActivity(i);
    }

    protected void verActividadLogin(){
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
    }

    protected void verPreferencias(){
        Intent i = new Intent(this,Preferencias.class);
        startActivityForResult(i,SERVIDOR_CAMBIADO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SERVIDOR_CAMBIADO && resultCode == RESULT_OK)
            //Actualizamos la descarga de fotos
            descargaFotos();
    }

    protected void descargaFotos(){

        //Actualizamos la ip del servidor
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");
        Toast.makeText(this,ip_server,Toast.LENGTH_SHORT).show();

        //Descargamos todas las fotos de forma temporal
        new GetFotosAsyncTask().execute();
    }

    //Tarea asincrona para hacer la consulta al servidor ftp
    class GetFotosAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean downloadok = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(getString(R.string.esperebajarfotos));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Nombres de las imagenes del alm. interno
            File miRuta = getExternalFilesDir(null);
            File archivos[] = miRuta.listFiles();
            ArrayList<String> imagenes = new ArrayList<>();

            if(archivos.length > 0) {
                for (int i = 0; i < archivos.length; i++) {
                    //si el archivo no es un directorio y es una imagen, se añade
                    if (archivos[i].isFile() && archivos[i].getName().contains(".jpg")) {
                        imagenes.add(archivos[i].getName());
                    }
                }
            }

            //Descargar la foto de Filezilla
            try {
                downloadok = conexFtp.bajarArchivos(ip_server, MainActivity.this, imagenes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(pDialog.isShowing())
                pDialog.dismiss();

            //Si las fotos no se han descargado correctamente...
            if(!downloadok)
                new MostrarMensaje(MainActivity.this).mostrarMensaje(getString(R.string.titulodatosiniciales),
                        getString(R.string.textodatosiniciales),getString(R.string.aceptar));
        }

    }
}
