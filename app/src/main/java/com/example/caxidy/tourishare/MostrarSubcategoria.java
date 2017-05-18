package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MostrarSubcategoria extends AppCompatActivity implements OnMapReadyCallback {

    private static final int EDITAR_SUBCAT = 1;

    ImageView foto;
    TextView nombre;
    EditText descripcion;
    Button bBorrar, bEditar;
    RatingBar rBar;
    OperacionesBD opBd;
    ConexionFtp conexFtp;
    private String ip_server;
    private String url_select, url_delete;
    private long id;
    private ProgressDialog pDialog;
    Subcategoria subcategoria;
    private int idUsuario, idCiudad, idTipo;
    boolean nuevaFoto;

    private double lat, longi;
    private SupportMapFragment mapaFragment;
    private GoogleMap gMap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mostrar_subcategoria);

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            id = extras.getLong("codigoSubc");
            idUsuario = extras.getInt("idUser");
            idCiudad = extras.getInt("idCiu");
            idTipo = extras.getInt("idTipo");
        }

        foto = (ImageView) findViewById(R.id.subcatfoto);
        nombre = (TextView) findViewById(R.id.textonombresubcat);
        descripcion = (EditText) findViewById(R.id.txdescrpsubcat);
        bBorrar = (Button) findViewById(R.id.bsubcateliminar);
        bEditar = (Button) findViewById(R.id.bsubcateditar);
        rBar = (RatingBar) findViewById(R.id.ratingbSubcat);

        nuevaFoto = false;

        opBd = new OperacionesBD();
        conexFtp = new ConexionFtp();

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        url_delete = "http://" + ip_server + "/archivosphp/delete.php";

        bBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Borrar los datos de esa subcat
                new MostrarMensaje(MostrarSubcategoria.this).mostrarBorrar(getString(R.string.tituloborrarsubcat),
                        getString(R.string.textoborrarsubcat),getString(R.string.aceptar),
                        (int)id,"items","IdItem",url_delete, url_select, ip_server, MostrarSubcategoria.this, false);
            }
        });

        bEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abrir Editar subcategoria
                Intent i = new Intent(MostrarSubcategoria.this,EditarSubcategoria.class);
                i.putExtra("idSubcat",(int)id);
                i.putExtra("idUsua",idUsuario);
                i.putExtra("idTipo",idTipo);
                i.putExtra("idCiu", idCiudad);
                i.putExtra("editnombre",nombre.getText().toString());
                i.putExtra("editdescrp",descripcion.getText().toString());
                i.putExtra("editlat",lat);
                i.putExtra("editlongi",longi);
                i.putExtra("editurlfoto",subcategoria.getUrlfoto());
                i.putExtra("editrating",subcategoria.getPuntuacion());
                startActivityForResult(i,EDITAR_SUBCAT);
            }
        });

        new MuestraSubcategoriaAsyncTask().execute();

    }

    @Override
    public void onMapReady(GoogleMap mapa) {
        //Config. mapa y situacion por defecto
        gMap = mapa;

        //Le ponemos zoom
        gMap.getUiSettings().setZoomControlsEnabled(true);

        //poner nuestra latitud y longitud
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lat, longi), 16));

        //poner la imagen en la posic de la ciudad
        Bitmap bitmap = ((BitmapDrawable) foto.getDrawable()).getBitmap();
        Bitmap bmResized = Bitmap.createScaledBitmap(ponerBordeImg(bitmap,15), 120, 120, true);

        gMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bmResized))
                .anchor(0.0f, 1.0f)
                .position(new LatLng(lat, longi))
                .title(subcategoria.getNombre())
                .flat(true));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == EDITAR_SUBCAT && resultCode == RESULT_OK) {
            nuevaFoto =true;
            new MuestraSubcategoriaAsyncTask().execute();
        }
    }

    //Tarea asincrona para mostrar los datos de la subcategoria
    class MuestraSubcategoriaAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MostrarSubcategoria.this);
            pDialog.setMessage(getString(R.string.esperemostrarsubcat));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Si hemos actualizado los datos del registro, tenemos que actualizar tambien las fotos
            if(nuevaFoto) {
                try {
                    conexFtp.bajarArchivos(ip_server, MostrarSubcategoria.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            subcategoria = opBd.getSubcategoria(url_select,(int)id);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(subcategoria != null) {
                //Mostramos los datos
                File archivoImg = new File(getExternalFilesDir(null) + "/" + subcategoria.getUrlfoto());

                if (archivoImg.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(archivoImg.getAbsolutePath());
                    Bitmap bmResized = Bitmap.createScaledBitmap(bm, 250, 250, true);
                    foto.setImageBitmap(bmResized);
                    foto.setAdjustViewBounds(true);
                }

                nombre.setText(subcategoria.getNombre());
                descripcion.setText(subcategoria.getDescripcion());
                rBar.setRating((float)subcategoria.getPuntuacion());

                lat = subcategoria.getLatitud();
                longi = subcategoria.getLongitud();

                //Fragmento con el mapa
                mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mostrarcfragment);
                mapaFragment.getMapAsync(MostrarSubcategoria.this);

            }
        }
    }
}
