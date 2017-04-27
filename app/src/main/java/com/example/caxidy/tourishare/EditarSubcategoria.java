package com.example.caxidy.tourishare;

import android.*;
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

import java.io.IOException;

public class EditarSubcategoria extends AppCompatActivity implements OnMapReadyCallback {

    private static final int TU_FOTO = 1;
    private static final int PLACE_AUTOCOMPLETE = 2;

    ImageView imgSubc;
    EditText txNombre, txDescr;
    Button bAceptar, bSearchM;
    RatingBar rB;
    Uri fotoGaleria;

    //Latitud y longitud que se guardan en el registro de subcategoria
    double lat, longi;

    private SupportMapFragment mapaFragment;
    private GoogleMap gMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_editar_subcategoria);

        imgSubc = (ImageView) findViewById(R.id.edsubcatfoto);
        txNombre = (EditText) findViewById(R.id.edtxnombresubcat);
        txDescr = (EditText) findViewById(R.id.edtxdescrpsubcat);
        rB = (RatingBar) findViewById(R.id.edratingBarSubcat);
        bAceptar = (Button) findViewById(R.id.edbotonsubcatAceptar);
        bSearchM = (Button) findViewById(R.id.edbotonsubcatSearchM);

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
                //!!se añade el registro de subcat, sin id de ciudad aun, y se pasa la id del registro añadido a la actividad
                //!!de ciudad, para añadirlo al array de subcats
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
                Bitmap bmResized = Bitmap.createScaledBitmap(bm, 250, 250, true);
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
}
