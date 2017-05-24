package com.example.caxidy.tourishare;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PrincipalActivity extends ListActivity implements AppCompatCallback, NavigationView.OnNavigationItemSelectedListener{

    private static final int CIUDAD_NUEVA = 1;
    private static final int MI_PERFIL = 2;

    private AppCompatDelegate delegate;

    Usuario miUsuario;
    AdaptadorPrincipal adaptadorP;
    ArrayList<Ciudad> listaCiudades;
    ListView listview;
    private String ip_server;
    private String url_select;
    private ProgressDialog pDialog;
    private OperacionesBD opDb;
    private ConexionFtp conexFtp;
    private NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        delegate = AppCompatDelegate.create(this,this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_acciones_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navOpen, R.string.navClos);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        conexFtp = new ConexionFtp();

        //Creamos el objeto usuario con los datos de nuestro perfil
        Bundle datosUser = getIntent().getExtras();
        miUsuario = new Usuario(datosUser.getInt("miIdUser"),datosUser.getString("miNombre"),datosUser.getString("miPass"),
                datosUser.getString("miFoto"),datosUser.getInt("miIdRango"),datosUser.getString("miCiudad"));

        //Ponemos los datos del usuario en la cabecera del navigation drawer
        setDatosCabeceraDrawer(navigationView);

        //Recuperar la IP de las preferencias
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ip_server = sharedPref.getString("ipServer","192.168.1.131");

        url_select = "http://" + ip_server + "/archivosphp/consulta.php";

        opDb = new OperacionesBD();

        //Creamos la lista de ciudades
        listaCiudades = new ArrayList<>();
        llenarLista();

        listview = (ListView) findViewById(android.R.id.list);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapter, View view, int position, long arg)
            {
                long ciu = listview.getAdapter().getItemId(position);
                //Abrir la actividad para mostrar la ciudad
                abrirCiudad(ciu);
            }
        });

    }

    protected void setDatosCabeceraDrawer(NavigationView navigationView){
        View hView =  navigationView.getHeaderView(0);
        ImageView navF = (ImageView)hView.findViewById(R.id.navFoto);
        setImagen(navF);
        TextView navuser = (TextView)hView.findViewById(R.id.navUser);
        navuser.setText(miUsuario.getNombre());
        TextView navc = (TextView)hView.findViewById(R.id.navCiudad);
        navc.setText(miUsuario.getCiudad());
    }

    public void abrirCiudad(long ciu){
        //Se abre la actividad que muestra la ciudad, pasandole la id de la ciudad, para recuperar los datos en la nueva actividad
        Intent i = new Intent(this,MostrarCiudad.class);
        i.putExtra("codigoCiu",ciu);
        i.putExtra("miUserId",miUsuario.getId());
        startActivity(i);
    }

    //Compara el numero de ciudades actual con el anterior, para lanzar una notificacion en caso de haber nuevas ciudades
    public void comprobarListaCiudades(){
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        int numCiudades = prefs.getInt("numeroCiudades", 0);
        int numCiudadesActual = listview.getAdapter().getCount();

        //Si antes habia menos ciudades que ahora...hay nuevas ciudades
        if (numCiudades < numCiudadesActual)
            new Notificacion().lanzarNotificacion(this,getString(R.string.notiftituloCiudad),getString(R.string.notiftextoCiudad));

        //Guardamos el nuevo numero de ciudades
        prefs.edit().putInt("numeroCiudades",numCiudadesActual).apply();
    }

    public void llenarLista(){
        listaCiudades.clear();
        //Llenar la lista de ciudades
        new GetCiudadesAsyncTask().execute();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        llenarLista();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuactu) {
            //recargar la lista de ciudades
            llenarLista();
        }

        else if (id == R.id.menunueva){
            //Añadir nueva ciudad
            Intent i = new Intent(this,CrearCiudad.class);
            i.putExtra("idUsua",miUsuario.getId());
            startActivityForResult(i, CIUDAD_NUEVA);
        }

        else if (id == R.id.menuprefs){
            Intent i = new Intent(this,Preferencias.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {}

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {}

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Opciones del drawer
        int id = item.getItemId();

        if(id == R.id.itemPerfil){
            //ver o editar nuestros datos
            Intent i = new Intent(this,MiPerfil.class);
            i.putExtra("miId",miUsuario.getId());
            i.putExtra("miNombre",miUsuario.getNombre());
            i.putExtra("miPass",miUsuario.getPass());
            i.putExtra("miFoto",miUsuario.getUrlfoto());
            i.putExtra("miIdRango",miUsuario.getIdRango());
            i.putExtra("miCiudad",miUsuario.getCiudad());
            startActivityForResult(i,MI_PERFIL);

        }else if(id == R.id.itemCiudades){
            //lista de ciudades favoritas
            Intent i = new Intent(this,CiudadesFavActivity.class);
            i.putExtra("miId",miUsuario.getId());
            startActivity(i);

        }else if(id == R.id.itemamigos){
            //lista de amigos
            Intent i = new Intent(this,AmigosActivity.class);
            i.putExtra("miId",miUsuario.getId());
            startActivity(i);

        }else if(id == R.id.itemMensajes){
            //bandeja de entrada
            Intent i = new Intent(this,BandejaEntrada.class);
            i.putExtra("miId",miUsuario.getId());
            startActivity(i);

        }else if(id == R.id.itemlogout){
            //cerrar sesion
            new MostrarMensaje(this).mensajeMainIntent(PrincipalActivity.this,getString(R.string.titulocerrarsesion),
                    getString(R.string.textocerrarsesion),getString(R.string.aceptar),true);
        }else if(id == R.id.itemCancion){
            //Reproducir o parar la cancion de fondo
            if(item.getTitle().equals(getString(R.string.opcancion))){

                //arrancamos el servicio
                startService(new Intent(this,ServicioMusicaFondo.class));
                //cambiamos la apariencia de la opcion
                item.setIcon(android.R.drawable.ic_lock_silent_mode);
                item.setTitle(R.string.opcancionoff);

            }else if(item.getTitle().equals(getString(R.string.opcancionoff))){

                //paramos el servicio
                stopService(new Intent(this,ServicioMusicaFondo.class));
                item.setIcon(android.R.drawable.ic_lock_silent_mode_off);
                item.setTitle(R.string.opcancion);

            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(this,ServicioMusicaFondo.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CIUDAD_NUEVA && resultCode == RESULT_OK){
            new MostrarMensaje(this).mostrarMensaje(getString(R.string.titulociudadagregada),
                    getString(R.string.textociudadagregada),getString(R.string.aceptar));
        }
        else if(requestCode == MI_PERFIL && resultCode == RESULT_OK){
            //Refrescar los datos de usuario
            Bundle extras = data.getExtras();
            miUsuario.setNombre(extras.getString("miNombre"));
            miUsuario.setPass(extras.getString("miPass"));
            miUsuario.setUrlfoto(extras.getString("miFoto"));
            miUsuario.setIdRango(extras.getInt("miIdRango"));
            miUsuario.setCiudad(extras.getString("miCiudad"));
        }
    }

    public void setImagen(ImageView img){
        //Poner la imagen de usuario en el Navigation Drawer

        File archivoImg = new File(getExternalFilesDir(null) + "/" + miUsuario.getUrlfoto());

        if (archivoImg.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(archivoImg.getAbsolutePath());
            //coger el ancho y alto para la imagen, dependiendo del tamaño de la pantalla
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int scaleToUse = 12;
            int sizeBm = size.y * scaleToUse / 100;
            Bitmap bmResized = Bitmap.createScaledBitmap(bm, sizeBm, sizeBm, true);

            //Redondear la foto de perfil
            Bitmap output = Bitmap.createBitmap(bmResized.getWidth(),
                    bmResized.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bmResized.getWidth(), bmResized.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(bmResized.getWidth() / 2, bmResized.getHeight() / 2,
                    bmResized.getWidth() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bmResized, rect, rect, paint);

            //Poner la foto en el imageView
            if (img.getDrawingCache() != null)
                img.destroyDrawingCache();
            img.setImageBitmap(output);
            img.setAdjustViewBounds(true);
        }
    }

    //Tarea asincrona para llenar la lista de ciudades
    class GetCiudadesAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean downloadok = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(PrincipalActivity.this);
            pDialog.setMessage(getString(R.string.esperellenarlistaprincipal));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //1-Actualizar las fotos
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
                downloadok = conexFtp.bajarArchivos2(ip_server, PrincipalActivity.this, imagenes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //2-Llenar la lista de ciudades
            listaCiudades = opDb.getCiudades(url_select);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(listaCiudades != null && downloadok) {
                adaptadorP = new AdaptadorPrincipal(PrincipalActivity.this, listaCiudades);
                adaptadorP.notifyDataSetChanged();
                setListAdapter(adaptadorP);
                setDatosCabeceraDrawer(navigationView);

                //Lanzar notificacion si hay nuevas ciudades
                comprobarListaCiudades();
            }
            else
                new MostrarMensaje(PrincipalActivity.this).mostrarMensaje(getString(R.string.tituloproblemaactulistaprincipal),
                        getString(R.string.textoproblemaactulistaprincipal),getString(R.string.aceptar));
        }
    }

}
