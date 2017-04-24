package com.example.caxidy.tourishare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button bsignup, bentrar, bacercade, bprefsmain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //para ocultar el header, el cual no queremos en esta actividad
        setContentView(R.layout.activity_main);

        bsignup = (Button) findViewById(R.id.bRegistrarse);
        bentrar = (Button) findViewById(R.id.bEntrar);
        bacercade = (Button) findViewById(R.id.bAcercade);
        bprefsmain = (Button) findViewById(R.id.bPrefsMain);

        bsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //abrir intent para registrar un nuevo usuario
                verActividadSignup();
            }
        });

        bentrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //abrir intent para entrar en el programa
                verActividadLogin();
            }
        });

        bacercade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mostrar info de Acerca de
                new MostrarMensaje(MainActivity.this).mostrarMensaje(getString(R.string.acercade),
                        getString(R.string.textoacercade),getString(R.string.aceptar));
            }
        });

        bprefsmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //abrir preferencias para configurar la ip de servidor
                verPreferencias();
            }
        });
    }

    protected void verActividadSignup(){
        Intent i = new Intent(this,SignupActivity.class);
        startActivity(i);
    }

    protected void verActividadLogin(){
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
    }

    protected void verPreferencias(){
        Intent i = new Intent(this,Preferencias.class);
        startActivity(i);
    }
}
