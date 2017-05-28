package com.example.caxidy.tourishare;

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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

public class MiPerfil extends AppCompatActivity {

    private static final int TU_FOTO = 1;

    Usuario usu;
    ImageView foto;
    EditText nombre, ciudad;
    TextView rango;
    Button bConfirmar, bPass, bBorrar, bSalir;
    String nombreRango;
    Calendar calendario;
    boolean nuevaFoto = false;
    OperacionesBD opBd;
    ConexionFtp conexFtp;
    private String ip_server, url_select, url_delete, url_update;
    ProgressDialog pDialog;
    Uri fotoGaleria;
    private String url_ftp_upload, url_ftp_filepath;

    //Variables y constantes para pedir permisos de almacenamiento de imagenes
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] permisosAlmacenamiento = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_miperfil);

        //Datos del usuario
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            usu = new Usuario(extras.getInt("miId"),extras.getString("miNombre"),extras.getString("miPass"),
                    extras.getString("miFoto"),extras.getInt("miIdRango"),extras.getString("miCiudad"));
        }

        foto = (ImageView) findViewById(R.id.tuperfilFoto);
        nombre = (EditText) findViewById(R.id.tuperfilNombre);
        ciudad = (EditText) findViewById(R.id.tuperfilCiudad);
        rango = (TextView) findViewById(R.id.tuperfilRango);
        bConfirmar = (Button) findViewById(R.id.tuperfilConfirmar);
        bPass = (Button) findViewById(R.id.tuperfilPass);
        bBorrar = (Button) findViewById(R.id.tuperfilBorrar);
        bSalir = (Button) findViewById(R.id.tuperfilSalir);

        opBd = new OperacionesBD();
        conexFtp = new ConexionFtp();

        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escogerFoto();
            }
        });

        bConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!nombre.getText().toString().equals("")){
                    //Si hemos cambiado la foto...
                    if(fotoGaleria != null) {
                        calendario = Calendar.getInstance();
                        String nomFoto = "F" + calendario.get(Calendar.YEAR) + calendario.get(Calendar.MONTH) +
                                calendario.get(Calendar.DAY_OF_MONTH) + calendario.get(Calendar.HOUR_OF_DAY) +
                                calendario.get(Calendar.MINUTE) + calendario.get(Calendar.SECOND) +
                                calendario.get(Calendar.MILLISECOND) + "F" + fotoGaleria.getLastPathSegment() + ".jpg";

                        //urls para subir la foto al servidor Filezilla y para localizar la foto a subir de la galeria del dispositivo
                        url_ftp_upload = "archivosFilezilla/" + nomFoto;
                        url_ftp_filepath = getPathAbsolutoUri(getApplicationContext(), fotoGaleria);
                        System.out.println(nomFoto + " --- " + url_ftp_upload + " --- " + url_ftp_filepath);

                        usu.setUrlfoto(nomFoto);
                        nuevaFoto = true;

                        //Antes de insertar nada, verificamos los permisos de acceso a media, fotos... (necesario para versiones mayores a la 23)
                        boolean verificado = false;
                        while (!verificado) {
                            verificado = verificarPermisosAlmacenamiento(MiPerfil.this);
                        }
                    }

                    usu.setNombre(nombre.getText().toString());
                    usu.setCiudad(ciudad.getText().toString());

                    //actualizamos el perfil
                    new ActualizarPerfilAsyncTask().execute();
                }
                else{
                    new MostrarMensaje(MiPerfil.this).mostrarMensaje(getString(R.string.titulononombre),getString(R.string.textononombre),
                            getString(R.string.aceptar));
                }
            }
        });

        bPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogPass();
            }
        });

        bBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Borrar el usuario
                new MostrarMensaje(MiPerfil.this).mostrarBorrarPerfil(getString(R.string.tituloborrarperfil),
                        getString(R.string.textoborrarperfil),getString(R.string.aceptar),
                        usu.getId(),"usuarios","idUsuario",url_delete, url_select, ip_server, MiPerfil.this);
            }
        });

        bSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salir();
            }
        });

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        url_update = "http://" + ip_server + "/archivosphp/update.php";

        url_delete = "http://" + ip_server + "/archivosphp/delete.php";

        new MuestraPerfilAsyncTask().execute();
    }

    protected void mostrarDialogPass(){
        new MostrarMensaje(this).cambiarPass(getString(R.string.titulocambiarpass),
                getString(R.string.aceptar),url_update,usu.getId());
    }

    protected void salir(){
        finish();
    }

    protected void escogerFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, TU_FOTO);
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
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TU_FOTO && resultCode == RESULT_OK) {
            fotoGaleria = data.getData();
            Bitmap bm;
            try {
                //Ponemos la foto en el ImageView
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), fotoGaleria);
                //coger el ancho y alto para la imagen, dependiendo del tamaño de la pantalla
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                size.y = display.getHeight();
                int scaleToUse = 20;
                int sizeBm = size.y * scaleToUse / 100;
                Bitmap bmResized = Bitmap.createScaledBitmap(bm, sizeBm, sizeBm, true);

                if (foto.getDrawingCache() != null)
                    foto.destroyDrawingCache();
                foto.setImageBitmap(bmResized);
                foto.setAdjustViewBounds(true);
            } catch (IOException e) {
            }
        }
    }

    //Tarea asincrona para actualizar el perfil
    class ActualizarPerfilAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean actualizado = false;
        //Crear hashmap para mandar los parametros al servidor ftp
        HashMap<String, String> params;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MiPerfil.this);
            pDialog.setMessage(getString(R.string.espereactualizandoperfil));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if(nuevaFoto) {
                //Borrar la foto anterior
                HashMap<String, String> prms = new HashMap<>();
                prms.put("host", ip_server);
                prms.put("nombreFoto", opBd.getNombreFoto(url_select, "usuarios", "idUsuario", usu.getId()));

                try {
                    conexFtp.borrarArchivo(prms);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //parametros del FTP
                params = new HashMap<String, String>();
                params.put("host", ip_server);
                params.put("uploadpath", url_ftp_upload);
                params.put("filepath", url_ftp_filepath);

                //Llamamos a SubirDatos() para subir la foto a Filezilla Server
                conexFtp.SubirDatos(params);
            }

            //Update del registro
            actualizado = opBd.modificarUsuario(url_update,usu);

            //Actualizamos el usuario
            usu = opBd.getUsuario(url_select,usu.getId());

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(!actualizado)
                new MostrarMensaje(MiPerfil.this).mostrarMensaje(getString(R.string.tituloerroractuperfil),
                        getString(R.string.textoerroractuperfil), getString(R.string.aceptar));
            else {
                Intent i = new Intent();
                i.putExtra("miNombre",usu.getNombre());
                i.putExtra("miPass",usu.getPass());
                i.putExtra("miFoto",usu.getUrlfoto());
                i.putExtra("miIdRango",usu.getIdRango());
                i.putExtra("miCiudad",usu.getCiudad());
                setResult(RESULT_OK,i);
                finish();
            }
        }
    }

    //Tarea asincrona para mostrar los datos del usuario
    class MuestraPerfilAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MiPerfil.this);
            pDialog.setMessage(getString(R.string.esperemostrarperfil));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

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
                    size.y = display.getHeight();
                    int scaleToUse = 20;
                    int sizeBm = size.y * scaleToUse / 100;
                    Bitmap bmResized = Bitmap.createScaledBitmap(bm, sizeBm, sizeBm, true);
                    foto.setImageBitmap(bmResized);
                    foto.setAdjustViewBounds(true);
                }

                nombre.setText(usu.getNombre());
                ciudad.setText(usu.getCiudad());
                rango.setText(nombreRango);

            }
        }
    }
}
