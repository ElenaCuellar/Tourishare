package com.example.caxidy.tourishare;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

public class SignupActivity extends AppCompatActivity {

    //Constantes
    private static final int TU_FOTO = 1;

    //Variables
    Button bCrearUser;
    ImageView tuFoto;
    Uri fotoGaleria;
    EditText tuuser, tupass, tuciudad;

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

        bCrearUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Si hemos selecciona una foto y hemos escrito un nombre y una contraseña, subimos los datos al servidor
                if(fotoGaleria != null && !tuuser.getText().toString().equals("") && !tupass.getText().toString().equals("")){
                    /*!!sube el registro a la BD y la foto a Filezilla*/

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
}
