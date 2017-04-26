package com.example.caxidy.tourishare;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

    ImageView imgSubc;
    EditText txNombre, txDescr;
    Button bAceptar;
    RatingBar rB;
    Uri fotoGaleria;

    private SupportMapFragment mapa;

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

        //Fragmento con el mapa
        mapa = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.edsubcfragment);
        mapa.getMapAsync(this);

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

    }

    protected void escogerFoto(){
        //Seleccionamos una foto de la galeria para la ciudad
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, TU_FOTO);
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
    }

    @Override
    public void onMapReady(GoogleMap mapa) {
        //!!este codigo es de ejemplo, hay q hacer otra cosa
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(47.17, 27.5699), 16));
        mapa.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(47.17, 27.5699))); //Iasi, Romania
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
        mapa.setMyLocationEnabled(true);
    }
}
