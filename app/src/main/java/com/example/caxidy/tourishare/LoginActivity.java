package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity{

    Button bEntrar;
    EditText txUser, txPass;
    OperacionesBD opBd;
    Usuario miUser;
    String miNom, miPass;
    private String ip_server;
    private String url_select;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        bEntrar = (Button) findViewById(R.id.bLogin);
        txUser = (EditText) findViewById(R.id.txUser);
        txPass = (EditText) findViewById(R.id.txPass);

        opBd = new OperacionesBD();

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        ip_server = sharedPref.getString("ipServer","192.168.1.101");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        bEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                miNom = txUser.getText().toString();
                miPass = txPass.getText().toString();

                new LoginAsyncTask().execute();
            }
        });
    }

    public void lanzarActividadPrincipal(){
        Intent intent = new Intent(this,PrincipalActivity.class);
        intent.putExtra("miNombre",miUser.getNombre());
        intent.putExtra("miPass",miUser.getPass());
        intent.putExtra("miFoto",miUser.getUrlfoto());
        intent.putExtra("miCiudad",miUser.getCiudad());
        startActivity(intent);
    }

    //Tarea asincrona para hacer la consulta del Login
    class LoginAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage(getString(R.string.espereLogin));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            miUser = opBd.comprobarLogin(url_select,miNom,miPass);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(miUser != null){
                //Se abre la actividad principal, con la lista de ciudades, pasando los datos de nuestro usuario-perfil
                lanzarActividadPrincipal();
            }
            else
                new MostrarMensaje(LoginActivity.this).mostrarMensaje(getString(R.string.titulodiagEntrar),
                        getString(R.string.textodiagEntrar),getString(R.string.aceptar));
        }
    }
}
