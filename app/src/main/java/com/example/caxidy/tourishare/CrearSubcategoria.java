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
import android.widget.RatingBar;

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
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

public class CrearSubcategoria extends AppCompatActivity implements OnMapReadyCallback {

    private static final int TU_FOTO = 1;
    private static final int PLACE_AUTOCOMPLETE = 2;

    ImageView imgSubc;
    EditText txNombre, txDescr;
    Button bAceptar, bSearchM;
    RatingBar rB;
    Uri fotoGaleria;
    int idTipo, idS;
    Context contexto;
    OperacionesBD opBd;
    Calendar calendario;
    Subcategoria subcategoria;

    private JSONObject json;
    private int exito=0;
    private String ip_server;
    private String url_select;
    private String url_insert;
    private ProgressDialog pDialog;
    private ConexionHttpInsert conexion;
    private ConexionFtp conexionftp;
    private String url_ftp_upload, url_ftp_filepath;

    //Variables y constantes para pedir permisos de almacenamiento de imagenes
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] permisosAlmacenamiento = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Latitud y longitud que se guardan en el registro de subcategoria
    double lat, longi;

    private SupportMapFragment mapaFragment;
    private GoogleMap gMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_editar_subcategoria);

        Bundle extras = getIntent().getExtras();

        //tipo de categoria
        idTipo = extras.getInt("tipoCat");

        contexto = this;

        imgSubc = (ImageView) findViewById(R.id.edsubcatfoto);
        txNombre = (EditText) findViewById(R.id.edtxnombresubcat);
        txDescr = (EditText) findViewById(R.id.edtxdescrpsubcat);
        rB = (RatingBar) findViewById(R.id.edratingBarSubcat);
        bAceptar = (Button) findViewById(R.id.edbotonsubcatAceptar);
        bSearchM = (Button) findViewById(R.id.edbotonsubcatSearchM);

        opBd = new OperacionesBD();

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_insert = "http://" + ip_server + "/archivosphp/insert_subcategoria.php";

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        conexion = new ConexionHttpInsert();

        conexionftp = new ConexionFtp();

        //Fragmento con el mapa
        mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.edsubcfragment);
        mapaFragment.getMapAsync(this);

        imgSubc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escogerFoto();
            }
        });

        bAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Añadir registro y hacer el select de la id
                //Si hemos seleccionado una foto y hemos escrito un nombre, subimos los datos al servidor
                if(fotoGaleria != null && !txNombre.getText().toString().equals("")){
                    //Sube el registro a la BD y la foto a Filezilla

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

                    subcategoria = new Subcategoria(idTipo, txNombre.getText().toString(), txDescr.getText().toString(),
                            nomFoto, lat, longi, rB.getRating());

                    //Antes de insertar nada, verificamos los permisos de acceso a media, fotos... (necesario para versiones mayores a la 23)
                    boolean verificado = false;
                    while (!verificado) {
                        verificado = verificarPermisosAlmacenamiento(CrearSubcategoria.this);
                    }
                    //insertamos...
                    new AgregaSubcatAsyncTask().execute();
                }
                else{
                    //Si no, muestra un mensaje avisandonos
                    new MostrarMensaje(CrearSubcategoria.this).mostrarMensaje(getString(R.string.titulodiagsignup),getString(R.string.textodiaginsertsubcat),
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

    protected void escogerFoto(){
        //Seleccionamos una foto de la galeria para la ciudad
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, TU_FOTO);
    }

    protected void pasarId(int id){
        //pasamos la id a la ciudad
        Intent intent = new Intent();
        intent.putExtra("idSub",id);
        setResult(RESULT_OK,intent);
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

                if (imgSubc.getDrawingCache() != null)
                    imgSubc.destroyDrawingCache();
                imgSubc.setImageBitmap(bmResized);
                imgSubc.setAdjustViewBounds(true);
            } catch (IOException e) {}
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
    }

    //poner marcador
    private void ponerMarcador(Place pl){

        //borrar el marcador anterior, si lo hay
        gMap.clear();

        LatLng latlng = pl.getLatLng();

        lat = latlng.latitude;
        longi = latlng.longitude;

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lat, longi), 16));

        //poner la imagen en la posic correcta
        Bitmap bitmap = ((BitmapDrawable) imgSubc.getDrawable()).getBitmap();
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

    @Override
    public void onMapReady(GoogleMap mapa) {
        gMap = mapa;

        gMap.getUiSettings().setZoomControlsEnabled(true);

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(47.17, 27.5699), 16));

        gMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(47.17, 27.5699))
                .flat(true)); //Iasi, Romania

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    //Tarea asincrona para hacer el insert y luego la consulta de la id de subcategoria
    class AgregaSubcatAsyncTask extends AsyncTask<Void, Void, Void> {

        String response = "";
        //Crear hashmaps para mandar los parametros al servidor http y ftp
        HashMap<String, String> postDataParams;
        HashMap<String, String> params;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(CrearSubcategoria.this);
            pDialog.setMessage(getString(R.string.espereAddSubcat));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //parametros del insert
            postDataParams = new HashMap<String, String>();
            postDataParams.put("IdCategoria", Integer.toString(subcategoria.getIdCategoria()));
            postDataParams.put("Nombre", subcategoria.getNombre());
            postDataParams.put("Descripcion", subcategoria.getDescripcion());
            postDataParams.put("UrlFoto", subcategoria.getUrlfoto());
            postDataParams.put("Latitud", Double.toString(subcategoria.getLatitud()));
            postDataParams.put("Longitud", Double.toString(subcategoria.getLongitud()));
            postDataParams.put("Puntuacion", Double.toString(subcategoria.getPuntuacion()));

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

                //Guardamos el indice de la categoria para pasarlo al intent de ciudad
                if(exito == 1){
                    idS = opBd.getIdSubcategoria(url_select);

                    if (idS != -1)
                        pasarId(idS);
                }

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
                //El registro se ha insertado al completo y la imagen se ha subido al servidor FTP.
                if(idS == -1)
                    new MostrarMensaje(contexto).mostrarMensaje(getString(R.string.tituloidsubcatultima),
                            getString(R.string.textoidsubcatultima), getString(R.string.aceptar));
                else
                    finish();
            }
        }
    }
}
