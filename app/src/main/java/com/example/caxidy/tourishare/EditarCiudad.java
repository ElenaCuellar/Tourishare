package com.example.caxidy.tourishare;

import android.Manifest;
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

    ImageView imgCiudad;
    EditText txNombre, txDescr;
    Button bRest, bMonum, bMus, bTrans, bLug, bAceptar;
    Uri fotoGaleria;

    private SupportMapFragment mapa;

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

        //Fragmento con el mapa
        mapa = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.edcfragment);
        mapa.getMapAsync(this);

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

    }

    protected void escogerFoto() {
        //Seleccionamos una foto de la galeria para la ciudad
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, TU_FOTO);
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
    }

    //Acciones del mapa
    @Override
    public void onMapReady(GoogleMap mapa) {
        //!!este codigo es de ejemplo, hay q hacer otra cosa
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(47.17, 27.5699), 16));
        mapa.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(47.17, 27.5699))); //Iasi, Romania
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
