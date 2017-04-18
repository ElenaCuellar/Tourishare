package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    //Constantes
    private static final int TU_FOTO = 1;

    //Variables
    Button bCrearUser;
    ImageView tuFoto;
    Uri fotoGaleria;
    EditText tuuser, tupass, tuciudad;
    Usuario usuario;

    //Variables que requieren del servidor o lo invocan
    private ProgressDialog pDialog;
    private JSONObject json;
    private int exito=0;
    private ConexionHttp conexion;
    private String ip_server;
    private String url_insert;
    InsertarUsuarioAsyncTask nuevoUserTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);

        bCrearUser = (Button) findViewById(R.id.bSignup);
        tuFoto = (ImageView) findViewById(R.id.tufoto);
        tuuser = (EditText) findViewById(R.id.txtuUser);
        tupass = (EditText) findViewById(R.id.txtupass);
        tuciudad = (EditText) findViewById(R.id.txtuciudad);

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        ip_server = sharedPref.getString("ipServer","192.168.1.101");

        url_insert = "http://" + ip_server + "/archivosphp/insert_user.php";

        conexion = new ConexionHttp();

        nuevoUserTask = new InsertarUsuarioAsyncTask();


        bCrearUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Si hemos seleccionado una foto y hemos escrito un nombre y una contraseña, subimos los datos al servidor
                if(fotoGaleria != null && !tuuser.getText().toString().equals("") && !tupass.getText().toString().equals("")){
                    //sube el registro a la BD y la foto a Filezilla
                    usuario = new Usuario(tuuser.getText().toString(),tupass.getText().toString(),
                            "!!!!!!!!!urlfoto - ruta + fecha actual con milisecs...",tuciudad.getText().toString());
                    insertarUsuario();
                    //!!subir foto a Filezilla
                    //!!desaparece este intent y nos logea con el usuario creado y accedemos al programa
                }
                else{
                    //Si no, muestra un mensaje avisandonos
                    mostrarDialog(getString(R.string.titulodiagsignup),getString(R.string.textodiagsignup));
                }
            }
        });

        tuFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escogerFoto();
            }
        });

    }

    protected void escogerFoto(){
        //Seleccionamos una foto de la galeria, que sera nuestra foto de perfil
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, TU_FOTO);
    }

    protected void insertarUsuario(){
        //ejecutar la tarea asincrona para agregar al nuevo usuario
        nuevoUserTask.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TU_FOTO && resultCode == RESULT_OK) {
            fotoGaleria = data.getData();
            Bitmap bm;
            try {
                //Ponemos la foto en el ImageView
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), fotoGaleria);
                Bitmap bmResized = Bitmap.createScaledBitmap(bm, 250, 250, true);
                if (tuFoto.getDrawingCache() != null)
                    tuFoto.destroyDrawingCache();
                tuFoto.setImageBitmap(bmResized);
                tuFoto.setAdjustViewBounds(true);
            } catch (IOException e) {}
        }
    }

    //metodo que muestra un dialog en la actividad actual
    public void mostrarDialog(String titulo,String mensaje){
        AlertDialog.Builder alertDialogBu = new AlertDialog.Builder(this);
        alertDialogBu.setTitle(titulo);
        alertDialogBu.setMessage(mensaje);
        alertDialogBu.setIcon(R.mipmap.ic_launcher);
        alertDialogBu.setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = alertDialogBu.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    //Tarea asincrona para insertar un nuevo usuario
    class InsertarUsuarioAsyncTask extends AsyncTask<Void, Void, Void> {

        String response = "";
        //Crear hashmap para mandar los parametros al servidor
        HashMap<String, String> postDataParams;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SignupActivity.this);
            pDialog.setMessage(getString(R.string.espere));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            postDataParams=new HashMap<String, String>();
            postDataParams.put("Nombre", usuario.getNombre());
            postDataParams.put("Password", usuario.getPass());
            postDataParams.put("UrlFoto", usuario.getUrlfoto());
            postDataParams.put("IdRango", Integer.toString(usuario.getIdRango()));
            postDataParams.put("ciudad", usuario.getCiudad());

            //Llamamos a ServerData() para almacenar el resultado en response
            response= conexion.ServerData(url_insert,postDataParams);

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
                Toast.makeText(getApplicationContext(), getString(R.string.insertexito), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
