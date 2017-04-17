package com.example.caxidy.tourishare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button bsignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //para ocultar el header, el cual no queremos en esta actividad
        setContentView(R.layout.activity_main);

        bsignup = (Button) findViewById(R.id.bRegistrarse);

        bsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //abrir intent para registrar un nuevo usuario
                verActividadSignup();
            }
        });
    }

    protected void verActividadSignup(){
        Intent i = new Intent(this,SignupActivity.class);
        startActivity(i);
    }
}
