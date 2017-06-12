package com.example.caxidy.tourishare;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
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
    Calendar calendario;
    String nomFoto; //nombre de la nueva foto de usuario
    OperacionesBD opBd;
    String nomUser; //nombre del nuevo user, para comprobar si esta disponible

    //Variables y constantes para pedir permisos de almacenamiento de imagenes
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] permisosAlmacenamiento = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Variables que requieren del servidor o lo invocan
    private ProgressDialog pDialog;
    private JSONObject json;
    private int exito=0;
    private ConexionHttpInsert conexion;
    private ConexionFtp conexionftp;
    private String ip_server;
    private String url_insert;
    private String url_select;
    private String url_ftp_upload, url_ftp_filepath;
    protected InsertarUsuarioAsyncTask nuevoUserTask;

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_insert = "http://" + ip_server + "/archivosphp/insert_user.php";

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        opBd = new OperacionesBD();

        conexion = new ConexionHttpInsert();

        conexionftp = new ConexionFtp();

        bCrearUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Si hemos seleccionado una foto y hemos escrito un nombre y una contraseña, subimos los datos al servidor
                if(fotoGaleria != null && !tuuser.getText().toString().equals("") && !tupass.getText().toString().equals("")){
                    //Sube el registro a la BD y la foto a Filezilla

                    nomUser = tuuser.getText().toString();

                    //Nombre de la foto (su nombre en galeria + la fecha actual + extension)
                    calendario = Calendar.getInstance();
                    nomFoto = "F" + calendario.get(Calendar.YEAR) + calendario.get(Calendar.MONTH) +
                            calendario.get(Calendar.DAY_OF_MONTH) + calendario.get(Calendar.HOUR_OF_DAY) +
                            calendario.get(Calendar.MINUTE) + calendario.get(Calendar.SECOND) +
                            calendario.get(Calendar.MILLISECOND) + "F" + fotoGaleria.getLastPathSegment() + ".jpg";

                    //urls para subir la foto al servidor Filezilla y para localizar la foto a subir de la galeria del dispositivo
                    url_ftp_upload = "archivosFilezilla/" + nomFoto;
                    url_ftp_filepath = getPathAbsolutoUri(getApplicationContext(), fotoGaleria);
                    System.out.println(nomFoto + " --- " + url_ftp_upload + " --- " + url_ftp_filepath);

                    usuario = new Usuario(tuuser.getText().toString(), tupass.getText().toString(),
                            nomFoto, tuciudad.getText().toString());

                    //Antes de insertar nada, verificamos los permisos de acceso a media, fotos... (necesario para versiones mayores a la 23)
                    boolean verificado = false;
                    while (!verificado) {
                        verificado = verificarPermisosAlmacenamiento(SignupActivity.this);
                    }
                    //insertamos...
                    insertarUsuario();
                }
                else{
                    //Si no, muestra un mensaje avisandonos
                    new MostrarMensaje(SignupActivity.this).mostrarMensaje(getString(R.string.titulodiagsignup),getString(R.string.textodiagsignup),
                            getString(R.string.aceptar));
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
        nuevoUserTask = new InsertarUsuarioAsyncTask();
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
            } catch (Exception e) {
                bm = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
                new MostrarMensaje(this).mostrarMensaje(getString(R.string.errorcargarfoto),getString(R.string.tituloerrorcargarfoto),
                        getString(R.string.aceptar));
            }

            //coger el ancho y alto para la imagen, dependiendo del tamaño de la pantalla
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            size.y = display.getHeight();
            int scaleToUse = 20;
            int sizeBm = size.y * scaleToUse / 100;
            Bitmap bmResized = Bitmap.createScaledBitmap(bm, sizeBm, sizeBm, true);

            if (tuFoto.getDrawingCache() != null)
                tuFoto.destroyDrawingCache();
            tuFoto.setImageBitmap(bmResized);
            tuFoto.setAdjustViewBounds(true);
        }
    }

    //metodo para obtener la ruta absoluta de la variable fotoGaleria
    public String getPathAbsolutoUri(Context contexto, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = contexto.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean verificarPermisosAlmacenamiento(Activity activity) {
        // Comprobamos si tenemos permisos de escritura
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //Si no tenemos permisos, le pedimos al usuario que los habilite
            ActivityCompat.requestPermissions(
                    activity,
                    permisosAlmacenamiento,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        return true;
    }

    public void focalizaNombre(){
        tuuser.setText("");
        tuuser.requestFocus();
    }

    //Tarea asincrona para insertar un nuevo usuario y subir su foto
    class InsertarUsuarioAsyncTask extends AsyncTask<Void, Void, Void> {

        MostrarMensaje msg;
        boolean mostrarMsg;
        String response = "";
        //Crear hashmaps para mandar los parametros al servidor http y ftp
        HashMap<String, String> postDataParams;
        HashMap<String, String> params;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mostrarMsg = false;
            msg = new MostrarMensaje(SignupActivity.this);

            pDialog = new ProgressDialog(SignupActivity.this);
            pDialog.setMessage(getString(R.string.espereNuevoUser));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if(opBd.comprobarUsuarioUnico(url_select,nomUser)) {
                //parametros del insert
                postDataParams = new HashMap<String, String>();
                postDataParams.put("Nombre", usuario.getNombre());
                postDataParams.put("Password", usuario.getPass());
                postDataParams.put("UrlFoto", usuario.getUrlfoto());
                postDataParams.put("IdRango", Integer.toString(usuario.getIdRango()));
                postDataParams.put("ciudad", usuario.getCiudad());

                //parametros del FTP
                params = new HashMap<String, String>();
                params.put("host", ip_server);
                params.put("uploadpath", url_ftp_upload);
                params.put("filepath", url_ftp_filepath);

                //Llamamos a serverData() para almacenar el resultado en response
                response = conexion.serverData(url_insert, postDataParams);

                //Llamamos a SubirDatos() para subir la foto a Filezilla Server
                conexionftp.SubirDatos(params);

                try {

                    json = new JSONObject(response);

                    //Obtenemos los valores del json
                    System.out.println(json.get("success"));
                    exito = json.getInt("success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                mostrarMsg = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            //Si muestra el mensaje: avisa de que el usuario ya existe
            if(mostrarMsg) {
                msg.mostrarMensaje(getString(R.string.titulodiaguserexiste), getString(R.string.useryaexiste),
                        getString(R.string.aceptar));
                focalizaNombre();
            }

            if(exito==1) {
                //El registro se ha insertado al completo y la imagen se ha subido al servidor FTP.
                //Volvemos a la pantalla anterior y se abre el activity de Login...
                msg.mostrarMensajeAbreIntent(LoginActivity.class, getString(R.string.titulodiagusercreado),
                        getString(R.string.textodiagusercreado), getString(R.string.aceptar));
            }
        }
    }

}
