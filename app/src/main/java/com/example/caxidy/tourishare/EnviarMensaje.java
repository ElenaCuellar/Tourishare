package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EnviarMensaje extends AppCompatActivity{

    protected int idEmisor, idReceptor;
    EditText txSubject, txBody;
    Button bEnviar, bSalir;
    OperacionesBD opBd;
    private String ip_server;
    private String url_insert;
    private ProgressDialog pDialog;
    String subject, body;
    private JSONObject json;
    private int exito=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_enviar_mensaje);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            //!!para parametrizar mas esta clase y poder usarla al enviar un mensaje desde la lista d amigos tambien, usar
            //!!las ids idEmisor e idReceptor con la misma key siempre
            //!!tambien tener en cuenta lo del mensaje en el onactivityresult de la actividad que llama a esta
            idEmisor = extras.getInt("idEmisor");
            idReceptor = extras.getInt("idReceptor");
        }

        txSubject = (EditText) findViewById(R.id.txSubjectenv);
        txBody = (EditText) findViewById(R.id.txBodyenv);
        bEnviar = (Button) findViewById(R.id.bEnviarMsg);
        bSalir = (Button) findViewById(R.id.bExitMsgenv);

        bEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject = txSubject.getText().toString();
                body = txBody.getText().toString();
                //Iniciar la tarea asincrona para insertar un nuevo registro de mensaje en la bd
                new EnviarMensajeAsyncTask().execute();
            }
        });

        bSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salir();
            }
        });

        opBd = new OperacionesBD();

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        ip_server = sharedPref.getString("ipServer","192.168.1.101");

        url_insert = "http://" + ip_server + "/archivosphp/insert_mensaje.php";
    }

    protected void salir(){
        finish();
    }

    class EnviarMensajeAsyncTask extends AsyncTask<Void, Void, Void> {

        String response = "";
        HashMap<String, String> postDataParams;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(EnviarMensaje.this);
            pDialog.setMessage(getString(R.string.espereenviarmensaje));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //parametros del insert
            postDataParams = new HashMap<String, String>();
            postDataParams.put("IdUsuario", Integer.toString(idReceptor));
            postDataParams.put("IdEmisor", Integer.toString(idEmisor));
            postDataParams.put("Cabecera", subject);
            postDataParams.put("Cuerpo", body);

            response = opBd.insertarMensaje(url_insert, postDataParams);

            try {
                json = new JSONObject(response);

                //Obtenemos los valores del json
                System.out.println(json.get("success"));
                exito = json.getInt("success");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(exito==1) {
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
    }
}
