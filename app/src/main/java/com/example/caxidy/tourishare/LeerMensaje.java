package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LeerMensaje extends AppCompatActivity{

    private static final int ENVIAR_MENSAJE = 1;

    TextView txSubject, txSender;
    EditText txBody;
    Button bResponder, bEliminar, bSalir;
    OperacionesBD opBd;
    private String ip_server;
    private String url_select;
    private String url_delete;
    private ProgressDialog pDialog;
    Mensaje msg;
    String nombreEmisor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_leer_mensaje);

        //Datos del mensaje
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            msg = new Mensaje(extras.getInt("idM"),extras.getInt("miIdUser"),
                    extras.getInt("idEmisorM"),extras.getString("cabeceraM"), extras.getString("cuerpoM"));
        }

        txSubject = (TextView) findViewById(R.id.txSubject);
        txSender = (TextView) findViewById(R.id.txFrom);
        txBody = (EditText) findViewById(R.id.txBody);
        bResponder = (Button) findViewById(R.id.bEnvMsg);
        bEliminar = (Button) findViewById(R.id.bDeleteMsg);
        bSalir = (Button) findViewById(R.id.bExitMsg);

        opBd = new OperacionesBD();

        bResponder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                responderMsg();
            }
        });

        bEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Borrar el mensaje
                new MostrarMensaje(LeerMensaje.this).mostrarBorrarMensaje(getString(R.string.tituloborrarmensaje),
                        getString(R.string.textoborrarmensaje),getString(R.string.aceptar),
                        msg.getIdMensaje(),"mensajes","IdMensaje",url_delete, url_select, ip_server, LeerMensaje.this);
            }
        });

        bSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        url_delete = "http://" + ip_server + "/archivosphp/delete.php";

        new MuestraMensajeAsyncTask().execute();

    }

    protected void responderMsg(){
        Intent intent = new Intent(this,EnviarMensaje.class);
        intent.putExtra("idEmisor",msg.getIdUsuario());
        intent.putExtra("idReceptor",msg.getIdEmisor());
        startActivityForResult(intent,ENVIAR_MENSAJE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ENVIAR_MENSAJE && resultCode == RESULT_OK){
            new MostrarMensaje(this).mostrarMensaje(getString(R.string.tituloenviarmensaje),
                    getString(R.string.textoenviarmensaje),getString(R.string.aceptar));
        }
    }

    //Tarea asincrona para mostrar los datos del usuario
    class MuestraMensajeAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(LeerMensaje.this);
            pDialog.setMessage(getString(R.string.espereleermensaje));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Sacar el nombre del emisor
            nombreEmisor = opBd.getEmisor(url_select,msg.getIdEmisor());

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(msg != null) {
                //Mostramos los datos
                txSender.setText(nombreEmisor);
                txSubject.setText(msg.getCabecera());
                txBody.setText(msg.getCuerpo());
            }
        }
    }
}
