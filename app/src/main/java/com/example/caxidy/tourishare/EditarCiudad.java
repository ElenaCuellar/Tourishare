package com.example.caxidy.tourishare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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

import java.io.IOException;

public class EditarCiudad extends AppCompatActivity implements OnMapReadyCallback {

    private static final int TU_FOTO = 1;
    private static final int PLACE_AUTOCOMPLETE = 2;


    ImageView imgCiudad;
    EditText txNombre, txDescr;
    Button bRest, bMonum, bMus, bTrans, bLug, bAceptar, bSearchM;
    Uri fotoGaleria;

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
                //!!se añade el registro de ciudad, se coge un array con los ids de subcategorias añadidas y se les añade la id
                //!!de la ciudad - UPDATE
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
        Intent intent = new Intent(this, EditarSubcategoria.class);
        intent.putExtra("tipoCat", tipoC);
        startActivity(intent);
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
        Bitmap bmResized = Bitmap.createScaledBitmap(ponerBordeImg(bitmap,15), 120, 120, true);

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
                new LatLng(47.17, 27.5699), 16));

        gMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(47.17, 27.5699))
                .flat(true)); //Iasi, Romania

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }
}
