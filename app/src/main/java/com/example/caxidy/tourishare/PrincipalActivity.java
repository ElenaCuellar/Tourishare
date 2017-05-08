package com.example.caxidy.tourishare;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
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
        delegate.setContentView(R.layout.activity_acciones_user); //!!activity_principal ---> a lo mejor es este layout, pero tngo q Inflate el drawer pa q s vea
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
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        ip_server = sharedPref.getString("ipServer","192.168.1.101");

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

    public void llenarLista(){
        listaCiudades.clear();
        //Llenar la lista de ciudades
        new GetCiudadesAsyncTask().execute();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        /*adaptadorP = null;
        adaptadorP = new AdaptadorPrincipal(this,listaCiudades);
        adaptadorP.notifyDataSetChanged();
        setListAdapter(adaptadorP);*/
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
            //!!lista filtrando por ciudades favoritas, se puede reciclar o parametrizar la clase MostrarCiudad y etc
            Intent i = new Intent(this,CiudadesFavActivity.class);
            //!!pasar extras = id de usuario, etc , un objeto usuario entero a lo mejor
            i.putExtra("miId",miUsuario.getId());
            startActivity(i);

        }else if(id == R.id.itemamigos){
            //lista de amigos
            //!!lista filtrando por amigos, se pueden reciclar  o parametrizar las clases MostrarUsuario y EnviarMensaje

        }else if(id == R.id.itemMensajes){
            //bandeja de entrada
            //!!lista con los mensajes recibidos. Tener en cuentas las notas de Keep

        }else if(id == R.id.itemlogout){
            //cerrar sesion
            //!!mirar pdf
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            Bitmap bmResized = Bitmap.createScaledBitmap(bm, 120, 120, true);
            if (img.getDrawingCache() != null)
                img.destroyDrawingCache();
            img.setImageBitmap(bmResized);
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
            //Borramos las fotos, si las hubiere
            File miRuta = getExternalFilesDir(null);
            File archivos[] = miRuta.listFiles();

            if(archivos.length > 0) {
                for (int i = 0; i < archivos.length; i++) {
                    //si el archivo no es un directorio y es una imagen, se borra
                    if (archivos[i].isFile() && archivos[i].getName().contains(".jpg")) {
                        archivos[i].delete();
                    }
                }
            }

            try {
                downloadok = conexFtp.bajarArchivos(ip_server, PrincipalActivity.this);
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
            }
            else
                new MostrarMensaje(PrincipalActivity.this).mostrarMensaje(getString(R.string.tituloproblemaactulistaprincipal),
                        getString(R.string.textoproblemaactulistaprincipal),getString(R.string.aceptar));
        }
    }

}
