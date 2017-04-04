package com.example.caxidy.tourishare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //!!probar, no se si funciona
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //para ocultar el header, que no queremos en esta actividad
        setContentView(R.layout.activity_main);
    }
}
