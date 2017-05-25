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
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

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

public class MostrarCiudad extends AppCompatActivity implements OnMapReadyCallback {

    private static final int EDITAR_CIUDAD = 1;

    ImageView foto;
    TextView nombre;
    EditText descripcion;
    ToggleButton bSeguir;
    Button bBorrar, bEditar, bRest, bMonum, bMus, bTrans, bLug;
    OperacionesBD opBd;
    ConexionFtp conexFtp;
    private String ip_server;
    private String url_select;
    private String url_insert;
    private String url_delete;
    private long id;
    private ProgressDialog pDialog;
    Ciudad ciudad;
    private int idUsuario;
    ArrayList<Usuario> arrColaboradores;
    Spinner sp;
    ArrayAdapter<String> adp;
    ArrayList<String> nombresUsers;
    boolean nuevaFoto;
    Usuario usu;

    private double lat, longi;
    private SupportMapFragment mapaFragment;
    private GoogleMap gMap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mostar_ciudad);

        //id de la ciudad
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            id = extras.getLong("codigoCiu");
            idUsuario = extras.getInt("miUserId");
        }

        foto = (ImageView) findViewById(R.id.ciudadfoto);
        nombre = (TextView) findViewById(R.id.textonombreciudad);
        descripcion = (EditText) findViewById(R.id.txdescrpciudad);
        bSeguir = (ToggleButton) findViewById(R.id.bciudadseguir);
        bBorrar = (Button) findViewById(R.id.botonciudadBorrar);
        bEditar = (Button) findViewById(R.id.bciudadEditar);
        bRest = (Button) findViewById(R.id.botonciudadRest);
        bMonum = (Button) findViewById(R.id.botonciudadMonum);
        bMus = (Button) findViewById(R.id.botonciudadMuseos);
        bTrans = (Button) findViewById(R.id.botonciudadTransp);
        bLug = (Button) findViewById(R.id.botonciudadLugaresInter);
        sp = (Spinner) findViewById(R.id.spcolaboradores);

        nuevaFoto = false;

        opBd = new OperacionesBD();
        conexFtp = new ConexionFtp();

        bRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarLista(1);
            }
        });

        bMonum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarLista(2);
            }
        });

        bMus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarLista(3);
            }
        });

        bTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarLista(4);
            }
        });

        bLug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarLista(5);
            }
        });

        bSeguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //añadir o quitar de favoritos
                new SeguirCiudad(bSeguir.isChecked()).start();
            }
        });

        bBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Borrar los datos de esa ciudad
                new MostrarMensaje(MostrarCiudad.this).mostrarBorrar(getString(R.string.tituloborrarciudad),
                        getString(R.string.textoborrarciudad),getString(R.string.aceptar),
                        (int)id,"ciudades","IdCiudad",url_delete, url_select, ip_server, MostrarCiudad.this, true);
            }
        });

        bEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abrir Editar ciudad
                Intent i = new Intent(MostrarCiudad.this,EditarCiudad.class);
                i.putExtra("idUsua",idUsuario);
                i.putExtra("idCiu",(int)id);
                i.putExtra("editnombre",nombre.getText().toString());
                i.putExtra("editdescrp",descripcion.getText().toString());
                i.putExtra("editlat",lat);
                i.putExtra("editlongi",longi);
                i.putExtra("editurlfoto",ciudad.getUrlfoto());
                startActivityForResult(i,EDITAR_CIUDAD);
            }
        });

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Usuario seleccionado
                String itemSeleccionado = parent.getItemAtPosition(position).toString();

                if(!itemSeleccionado.equals(getString(R.string.colaboradores))) {
                    new SeleccionarColab(itemSeleccionado).execute();
                }
                sp.setSelection(0); //poner por defecto el item Colaboradores

            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        url_insert = "http://" + ip_server + "/archivosphp/insert_ciudadfav.php";

        url_delete = "http://" + ip_server + "/archivosphp/delete.php";

        new MuestraCiudadAsyncTask().execute();

    }

    private void mostrarLista(int tipoC){
        //Tipos de categorias: 1 - restaurante, 2 - Monumento, 3 -Museo, 4 - Transporte, 5 - Lugar de interes
        Intent intent = new Intent(this, ListaSubcategoria.class);
        intent.putExtra("tipoCat", tipoC);
        intent.putExtra("idCiu",(int)id);
        intent.putExtra("idUser",idUsuario);
        startActivity(intent);
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
                .title(ciudad.getNombre())
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

        if(requestCode == EDITAR_CIUDAD && resultCode == RESULT_OK) {
            nuevaFoto =true;
            new MuestraCiudadAsyncTask().execute();
        }
    }

    //metodo para mostrar el intent del colaborador
    public void mostrarColabSeleccionado(){
        Intent intent = new Intent(this,MostrarUsuario.class);
        intent.putExtra("idU",usu.getId());
        intent.putExtra("nombreU",usu.getNombre());
        intent.putExtra("idRangoU",usu.getIdRango());
        intent.putExtra("passU",usu.getPass());
        intent.putExtra("urlfotoU",usu.getUrlfoto());
        intent.putExtra("ciudadU",usu.getCiudad());
        intent.putExtra("miIdUser",idUsuario);

        startActivity(intent);
    }

    //Hilos para seguir o dejar de seguir una ciudad
    class SeguirCiudad extends Thread {

        boolean marcado;

        SeguirCiudad(boolean marcado){
            this.marcado = marcado;
        }

        public void run() {
            opBd.updateSigueCiudad(marcado,url_delete,url_insert,id,idUsuario);
        }
    }

    //Tarea asincrona para seleccionar un colaborador
    class SeleccionarColab extends AsyncTask<Void, Void, Void> {

        String colaborador;

        SeleccionarColab(String colaborador){
            this.colaborador = colaborador;
        }

        @Override
        protected Void doInBackground(Void... params) {

            //seleccionamos la id del usuario
            int idSeleccionado = opBd.mostrarColaborador(url_select,colaborador,(int)id);
            if(idSeleccionado != -1){
                //obtenemos dicho usuario
                usu = opBd.getUsuario(url_select,idSeleccionado);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(usu != null)
                mostrarColabSeleccionado();
        }
    }

    //Tarea asincrona para mostrar los datos de la ciudad
    class MuestraCiudadAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean sigueciudad = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MostrarCiudad.this);
            pDialog.setMessage(getString(R.string.esperemostrarciudad));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Si hemos actualizado los datos del registro, tenemos que actualizar tambien las fotos
            if(nuevaFoto) {

                //Nombres de las imagenes del alm. interno
                File miRuta = getExternalFilesDir(null);
                File archivos[] = miRuta.listFiles();
                ArrayList<String> imagenes = new ArrayList<>();

                if(archivos.length > 0) {
                    for (int i = 0; i < archivos.length; i++) {
                        //si el archivo no es un directorio y es una imagen, se añade
                        if (archivos[i].isFile() && archivos[i].getName().contains(".jpg")) {
                            imagenes.add(archivos[i].getName());
                        }
                    }
                }

                try {
                    conexFtp.bajarArchivos(ip_server, MostrarCiudad.this, imagenes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ciudad = opBd.getCiudad(url_select,id);

            //Activar o no activar el boton de "siguiendo"
            String miConsulta = "SELECT COUNT(*) AS total FROM ciudadesfav WHERE IdCiudad = " +
                    (int)id + " AND IdUsuario = " + idUsuario;
            sigueciudad = opBd.sigueItem(url_select,miConsulta);

            //Llenar el arraylist con los usuarios colaboradores, para ponerlo en el spinner
            arrColaboradores = new ArrayList<>();
            arrColaboradores = opBd.updateSpinnerColaboradores(url_select,(int)id);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(ciudad != null) {
                //Mostramos los datos
                File archivoImg = new File(getExternalFilesDir(null) + "/" + ciudad.getUrlfoto());

                if (archivoImg.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(archivoImg.getAbsolutePath());
                    //coger el ancho y alto para la imagen, dependiendo del tamaño de la pantalla
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int scaleToUse = 20;
                    int sizeBm = size.y * scaleToUse / 100;
                    Bitmap bmResized = Bitmap.createScaledBitmap(bm, sizeBm, sizeBm, true);
                    foto.setImageBitmap(bmResized);
                    foto.setAdjustViewBounds(true);
                }

                nombre.setText(ciudad.getNombre());
                descripcion.setText(ciudad.getDescripcion());

                lat = ciudad.getLatitud();
                longi = ciudad.getLongitud();

                //Fragmento con el mapa
                mapaFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mostrarcfragment);
                mapaFragment.getMapAsync(MostrarCiudad.this);

                //togglebutton de siguiendo
                if(sigueciudad)
                    bSeguir.setChecked(true);
                else
                    bSeguir.setChecked(false);

                if(arrColaboradores != null){
                    //sacar los nombres de los usuarios y ponerlos en el spinner
                    nombresUsers = new ArrayList<>();
                    //añadimos cabecera
                    nombresUsers.add(getString(R.string.colaboradores));
                    for(int i=0; i<arrColaboradores.size();i++)
                        nombresUsers.add(arrColaboradores.get(i).getNombre());

                    adp = new ArrayAdapter<String>(MostrarCiudad.this,android.R.layout.simple_spinner_item,nombresUsers);
                    adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp.setAdapter(adp);
                }
            }
        }
    }
}
