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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
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

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class EditarCiudad extends AppCompatActivity implements OnMapReadyCallback {

    private static final int TU_FOTO = 1;
    private static final int PLACE_AUTOCOMPLETE = 2;
    private static final int SUBCATEGORIA_AGREGADA = 3;


    ImageView imgCiudad;
    EditText txNombre, txDescr;
    Button bRest, bMonum, bMus, bTrans, bLug, bAceptar, bSearchM;
    Uri fotoGaleria;
    ArrayList<Integer> idsSubcats = new ArrayList<>();
    OperacionesBD opBd;
    Calendar calendario;
    Ciudad ciudad;
    int idC, idUsuario;
    boolean nuevafoto = false;

    private String ip_server;
    private String url_update;
    private String url_select;
    private String url_insert_colab;
    private ProgressDialog pDialog;
    private ConexionFtp conexionftp;
    private String url_ftp_upload, url_ftp_filepath;

    //Variables y constantes para pedir permisos de almacenamiento de imagenes
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] permisosAlmacenamiento = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Latitud y longitud que se guardan en el registro de ciudad
    double lat, longi;

    private SupportMapFragment mapaFragment;
    private GoogleMap gMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_editar_ciudad);

        imgCiudad = (ImageView) findViewById(R.id.edciudadfoto);
        txNombre = (EditText) findViewById(R.id.edtxnombreciudad);
        txDescr = (EditText) findViewById(R.id.edtxdescrpciudad);
        bRest = (Button) findViewById(R.id.edbotonciudadRest);
        bMonum = (Button) findViewById(R.id.edbotonciudadMonum);
        bMus = (Button) findViewById(R.id.edbotonciudadMuseos);
        bTrans = (Button) findViewById(R.id.edbotonciudadTransp);
        bLug = (Button) findViewById(R.id.edbotonciudadLugaresInter);
        bAceptar = (Button) findViewById(R.id.edbotonciudadAceptar);
        bSearchM = (Button) findViewById(R.id.edbotonciudadSearchmapa);

        //Ponemos los datos por defecto
        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            idUsuario = extras.getInt("idUsua");
            idC = extras.getInt("idCiu");

            //Ponemos la foto
            File archivoImg = new File(getExternalFilesDir(null) + "/" + extras.getString("editurlfoto"));

            if (archivoImg.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(archivoImg.getAbsolutePath());
                //coger el ancho y alto para la imagen, dependiendo del tamaño de la pantalla
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int scaleToUse = 20;
                int sizeBm = size.y * scaleToUse / 100;
                Bitmap bmResized = Bitmap.createScaledBitmap(bm, sizeBm, sizeBm, true);
                imgCiudad.setImageBitmap(bmResized);
                imgCiudad.setAdjustViewBounds(true);
            }

            //Ponemos el nombre y la descripcion
            txNombre.setText(extras.getString("editnombre"));
            txDescr.setText(extras.getString("editdescrp"));

            //Ponemos la latitud y longitud en el mapa
            lat = extras.getDouble("editlat");
            longi = extras.getDouble("editlongi");

        }

        opBd = new OperacionesBD();

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_update = "http://" + ip_server + "/archivosphp/update.php";

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        url_insert_colab = "http://" + ip_server + "/archivosphp/insert_nuevaciudadcolab.php";

        conexionftp = new ConexionFtp();

        //Fragmento con el mapa
        mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.edcfragment);
        mapaFragment.getMapAsync(this);

        imgCiudad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escogerFoto();
            }
        });

        bRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirNuevaSubcat(1);
            }
        });

        bMonum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirNuevaSubcat(2);
            }
        });

        bMus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirNuevaSubcat(3);
            }
        });

        bTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirNuevaSubcat(4);
            }
        });

        bLug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirNuevaSubcat(5);
            }
        });

        bAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Actualizar registro de ciudad y actualizar la id de ciudad de las subcategorias añadidas
                if(!txNombre.getText().toString().equals("")){
                    //Modifica el registro en la BD y la foto en Filezilla
                    if(fotoGaleria != null) {
                        //Nombre de la foto (su nombre en galeria + la fecha actual + extension)
                        calendario = Calendar.getInstance();
                        String nomFoto = "F" + calendario.get(Calendar.YEAR) + calendario.get(Calendar.MONTH) +
                                calendario.get(Calendar.DAY_OF_MONTH) + calendario.get(Calendar.HOUR_OF_DAY) +
                                calendario.get(Calendar.MINUTE) + calendario.get(Calendar.SECOND) +
                                calendario.get(Calendar.MILLISECOND) + "F" + fotoGaleria.getLastPathSegment() + ".jpg";

                        //urls para subir la foto al servidor Filezilla y para localizar la foto a subir de la galeria del dispositivo
                        url_ftp_upload = "archivosFilezilla/" + nomFoto;
                        url_ftp_filepath = getPathAbsolutoUri(getApplicationContext(), fotoGaleria);
                        System.out.println(nomFoto + " --- " + url_ftp_upload + " --- " + url_ftp_filepath);

                        ciudad = new Ciudad(txNombre.getText().toString(), txDescr.getText().toString(),
                                nomFoto, lat, longi);
                        nuevafoto=true;

                        //Antes de insertar nada, verificamos los permisos de acceso a media, fotos... (necesario para versiones mayores a la 23)
                        boolean verificado = false;
                        while (!verificado) {
                            verificado = verificarPermisosAlmacenamiento(EditarCiudad.this);
                        }
                    }
                    else{
                        ciudad = new Ciudad(txNombre.getText().toString(), txDescr.getText().toString(), lat, longi);
                    }
                    //modificamos...
                    new ModificaCiudadAsyncTask().execute();
                }
                else{
                    //Si no, muestra un mensaje avisandonos
                    new MostrarMensaje(EditarCiudad.this).mostrarMensaje(getString(R.string.titulononombre),getString(R.string.textononombre),
                            getString(R.string.aceptar));
                }
            }
        });

        bSearchM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Aparece el intent para la busqueda de Google
                mostrarBusquedaGoogle();
            }
        });
    }

    protected void escogerFoto() {
        //Seleccionamos una foto de la galeria para la ciudad
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

    protected void mostrarBusquedaGoogle(){
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {

        } catch (GooglePlayServicesNotAvailableException e) {
        }
    }

    protected void abrirNuevaSubcat(int tipoC) {
        //Tipos de categorias: 1 - restaurante, 2 - Monumento, 3 -Museo, 4 - Transporte, 5 - Lugar de interes
        Intent intent = new Intent(this, CrearSubcategoria.class);
        intent.putExtra("tipoCat", tipoC);
        startActivityForResult(intent, SUBCATEGORIA_AGREGADA);
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
                display.getSize(size);
                int scaleToUse = 20;
                int sizeBm = size.y * scaleToUse / 100;
                Bitmap bmResized = Bitmap.createScaledBitmap(bm, sizeBm, sizeBm, true);

                if (imgCiudad.getDrawingCache() != null)
                    imgCiudad.destroyDrawingCache();
                imgCiudad.setImageBitmap(bmResized);
                imgCiudad.setAdjustViewBounds(true);
            } catch (IOException e) {
            }
        }
        else if(requestCode == PLACE_AUTOCOMPLETE && resultCode == RESULT_OK){
            //Guardamos el sitio y lo situamos con un marcador en el mapa - borramos el marcador anterior, si lo hay
            Place place = PlaceAutocomplete.getPlace(this, data);
            ponerMarcador(place);
        }
        else if(requestCode == PLACE_AUTOCOMPLETE && resultCode == PlaceAutocomplete.RESULT_ERROR){
            Status status = PlaceAutocomplete.getStatus(this, data);
            new MostrarMensaje(this).mostrarMensaje(getString(R.string.error),
                    getString(R.string.error) + ": " + status.getStatusMessage() ,getString(R.string.aceptar));
        }
        else if(requestCode == SUBCATEGORIA_AGREGADA && resultCode == RESULT_OK){
            idsSubcats.add(data.getExtras().getInt("idSub"));

            new MostrarMensaje(EditarCiudad.this).mostrarMensaje(getString(R.string.titulodiagsubcatcreada),
                    getString(R.string.textodiagsubcatcreada), getString(R.string.aceptar));
        }
    }

    //poner marcador en la ciudad
    private void ponerMarcador(Place pl){

        //borrar el marcador anterior, si lo hay
        gMap.clear();

        LatLng latlng = pl.getLatLng();

        lat = latlng.latitude;
        longi = latlng.longitude;

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lat, longi), 16));

        //poner la imagen en la posic de la ciudad
        Bitmap bitmap = ((BitmapDrawable) imgCiudad.getDrawable()).getBitmap();
        //coger el ancho y alto para la imagen, dependiendo del tamaño de la pantalla
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int scaleToUse = 8;
        int sizeBm = size.y * scaleToUse / 100;
        Bitmap bmResized = Bitmap.createScaledBitmap(ponerBordeImg(bitmap,15), sizeBm, sizeBm, true);

        gMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bmResized))
                .anchor(0.0f, 1.0f)
                .position(new LatLng(lat, longi))
                .title(pl.getName().toString())
                .snippet(pl.getAddress().toString())
                .flat(true));
    }

    private Bitmap ponerBordeImg(Bitmap bm, int borderSize){
        Bitmap bmpWithBorder = Bitmap.createBitmap(bm.getWidth() + borderSize * 2, bm.getHeight() + borderSize * 2,
                bm.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bm, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    //Acciones del mapa
    @Override
    public void onMapReady(GoogleMap mapa) {
        //Config. mapa y situacion por defecto
        gMap = mapa;

        //Le ponemos zoom
        gMap.getUiSettings().setZoomControlsEnabled(true);

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lat, longi), 16));

        //poner la imagen en la posic de la ciudad
        Bitmap bitmap = ((BitmapDrawable) imgCiudad.getDrawable()).getBitmap();
        //coger el ancho y alto para la imagen, dependiendo del tamaño de la pantalla
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int scaleToUse = 8;
        int sizeBm = size.y * scaleToUse / 100;
        Bitmap bmResized = Bitmap.createScaledBitmap(ponerBordeImg(bitmap,15), sizeBm, sizeBm, true);

        gMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bmResized))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(lat, longi))
                .flat(true)); //Iasi, Romania

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    //Tarea asincrona para hacer el update y luego la consulta de la id de subcategoria
    class ModificaCiudadAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean actualizado = false;
        int resUpdate = 0;
        //Crear hashmap para mandar los parametros al servidor ftp
        HashMap<String, String> params;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(EditarCiudad.this);
            pDialog.setMessage(getString(R.string.espereactualizandociudad));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if(nuevafoto) {
                //Borrar la foto anterior
                HashMap<String, String> prms = new HashMap<>();
                prms.put("host", ip_server);
                prms.put("nombreFoto", opBd.getNombreFoto(url_select, "ciudades", "IdCiudad", idC));

                try {
                    conexionftp.borrarArchivo(prms);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //parametros del FTP
                params = new HashMap<String, String>();
                params.put("host", ip_server);
                params.put("uploadpath", url_ftp_upload);
                params.put("filepath", url_ftp_filepath);

                //Llamamos a SubirDatos() para subir la foto a Filezilla Server
                conexionftp.SubirDatos(params);

                //Update del registro
                actualizado = opBd.modificarCiudad(url_update, idC, ciudad);
            }
            else
                actualizado = opBd.modificarCiudadSinFoto(url_update,idC,ciudad);

            //Hacemos un UPDATE de los idCiudad de las subcategorias
            if(actualizado){
                try{
                    if(idsSubcats.size() > 0) {
                        //actualizamos el campo idCiudad de las subcats
                        resUpdate = opBd.updateIdCiudadSubcategorias(url_update, idC, idsSubcats);
                    }
                    else
                        resUpdate = 1;

                    //comprobamos que el colaborador no ha sido agregado anteriormente
                    boolean nuevo = opBd.nuevoColaborador(url_select,idC,idUsuario);

                    //añadimos el colaborador
                    opBd.agregarColab(url_insert_colab,idC,idUsuario,nuevo);

                    //sumamos puntos al colaborador, para el rango
                    opBd.sumarPuntosRango(url_update,idUsuario);
                    opBd.cambiarRango(url_update,idUsuario);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(resUpdate != 1)
                new MostrarMensaje(EditarCiudad.this).mostrarMensaje(getString(R.string.tituloidciudadsubcats),
                        getString(R.string.textoidciudadsubcats), getString(R.string.aceptar));
            else {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
