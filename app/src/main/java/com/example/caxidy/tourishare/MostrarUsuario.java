package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;

public class MostrarUsuario extends AppCompatActivity {

    private static final int ENVIAR_MENSAJE = 1;

    ImageView foto;
    TextView nombre;
    EditText ciudad, rango;
    ToggleButton bSeguir;
    Button bMensaje;
    OperacionesBD opBd;
    ConexionFtp conexFtp;
    private String ip_server;
    private String url_select;
    private String url_delete;
    private String url_insert;
    private int idTuUser;
    private ProgressDialog pDialog;
    Usuario usu;
    String nombreRango;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mostrar_usuario);

        //Datos del usuario
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            usu = new Usuario(extras.getInt("idU"),extras.getString("nombreU"),extras.getString("passU"),
                    extras.getString("urlfotoU"),extras.getInt("idRangoU"),extras.getString("ciudadU"));
            idTuUser = extras.getInt("miIdUser");
        }

        foto = (ImageView) findViewById(R.id.usuariofoto);
        nombre = (TextView) findViewById(R.id.textonombreusuario);
        ciudad = (EditText) findViewById(R.id.txusuariociudad);
        rango = (EditText) findViewById(R.id.txusuariorango);
        bSeguir = (ToggleButton) findViewById(R.id.busuarioseguir);
        bMensaje = (Button) findViewById(R.id.busuariomensaje);

        opBd = new OperacionesBD();
        conexFtp = new ConexionFtp();

        bSeguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //añadir o quitar de favoritos
                new SeguirUsuario(bSeguir.isChecked()).start();

                habilitarBotonMensajes(bSeguir.isChecked());
            }
        });

        bMensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Enviar un mensaje a ese usuario (si es tu amigo)
                enviarMensaje();
            }
        });

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        url_insert = "http://" + ip_server + "/archivosphp/insert_amigo.php";

        url_delete = "http://" + ip_server + "/archivosphp/delete.php";

        new MuestraUsuarioAsyncTask().execute();

    }

    protected void habilitarBotonMensajes(boolean pulsado){
        //Habilitamos o deshabilitamos el boton de los mensajes, depende si seguimos o no al usuario
        if(pulsado)
            bMensaje.setEnabled(true);
        else
            bMensaje.setEnabled(false);
    }

    protected void enviarMensaje(){
        Intent i = new Intent(this,EnviarMensaje.class);
        i.putExtra("idEmisor",idTuUser);
        i.putExtra("idReceptor",usu.getId());
        startActivityForResult(i,ENVIAR_MENSAJE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ENVIAR_MENSAJE && resultCode == RESULT_OK){
            new MostrarMensaje(this).mostrarMensaje(getString(R.string.tituloenviarmensaje),
                    getString(R.string.textoenviarmensaje),getString(R.string.aceptar));
        }
    }

    //Hilos para seguir o dejar de seguir a un usuario
    class SeguirUsuario extends Thread {

        boolean marcado;

        SeguirUsuario(boolean marcado){
            this.marcado = marcado;
        }

        public void run() {
            opBd.updateSigueUsuario(marcado,url_delete,url_insert,usu.getId(),idTuUser);
        }
    }

    //Tarea asincrona para mostrar los datos del usuario
    class MuestraUsuarioAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean sigueuser = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MostrarUsuario.this);
            pDialog.setMessage(getString(R.string.esperemostrarusuario));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Activar o no activar el boton de "siguiendo"
            String miConsulta = "SELECT COUNT(*) AS total FROM amigos WHERE IdAmigo = " + usu.getId() +
             " AND IdUsuario = " + idTuUser;
            sigueuser = opBd.sigueItem(url_select,miConsulta);

            //Sacar el nombre del rango
            nombreRango = opBd.getRango(url_select,usu.getIdRango());

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(usu != null) {
                //Mostramos los datos
                File archivoImg = new File(getExternalFilesDir(null) + "/" + usu.getUrlfoto());

                if (archivoImg.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(archivoImg.getAbsolutePath());
                    //coger el ancho y alto para la imagen, dependiendo del tamaño de la pantalla
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int scaleToUse = 20;
                    int sizeBm = size.y * scaleToUse / 100;
                    Bitmap bmResized = Bitmap.createScaledBitmap(bm, sizeBm, sizeBm, true);
                    foto.setImageBitmap(bmResized);
                    foto.setAdjustViewBounds(true);
                }

                nombre.setText(usu.getNombre());
                ciudad.setText(usu.getCiudad());
                rango.setText(nombreRango);

                //togglebutton de siguiendo
                if(sigueuser)
                    bSeguir.setChecked(true);
                else
                    bSeguir.setChecked(false);

                habilitarBotonMensajes(bSeguir.isChecked());

                //Si somos nosotros mismos no podemos agregarnos como amigos o enviarnos un mensaje
                if(usu.getId()==idTuUser) {
                    bSeguir.setEnabled(false);
                    bSeguir.setVisibility(View.INVISIBLE);
                    bMensaje.setEnabled(false);
                    bMensaje.setVisibility(View.INVISIBLE);
                }

            }
        }
    }

}
