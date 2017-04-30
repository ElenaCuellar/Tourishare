package com.example.caxidy.tourishare;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;

public class PrincipalActivity extends ListActivity implements AppCompatCallback, NavigationView.OnNavigationItemSelectedListener{

    private AppCompatDelegate delegate;

    Usuario miUsuario;
    AdaptadorPrincipal adaptadorP;
    ArrayList<Ciudad> listaCiudades;
    ListView listview;
    private String ip_server;
    private String url_select;
    private ProgressDialog pDialog;
    private OperacionesBD opDb;

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Creamos el objeto usuario con los datos de nuestro perfil
        Bundle datosUser = getIntent().getExtras();
        miUsuario = new Usuario(datosUser.getString("miNombre"),datosUser.getString("miPass"),
                datosUser.getString("miFoto"),datosUser.getString("miCiudad"));

        //Ponemos los datos del usuario en la cabecera del navigation drawer
        View hView =  navigationView.getHeaderView(0);
        ImageView navF = (ImageView)hView.findViewById(R.id.navFoto);
        setImagen(navF);
        TextView navuser = (TextView)hView.findViewById(R.id.navUser);
        navuser.setText(miUsuario.getNombre());
        TextView navc = (TextView)hView.findViewById(R.id.navCiudad);
        navc.setText(miUsuario.getCiudad());

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

    public void abrirCiudad(long ciu){
        //Se abre la actividad que muestra la ciudad, pasandole la id de la ciudad, para recuperar los datos en la nueva actividad
        /*Intent i = new Intent(this,Modificacion.class); //!!
        i.putExtra("codigoCiu",ciu);
        startActivity(i);*/
    }

    public void llenarLista(){
        listaCiudades.clear();
        //Llenar la lista de ciudades
        new GetCiudadesAsyncTask().execute();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        adaptadorP = null;
        adaptadorP = new AdaptadorPrincipal(this,listaCiudades, ip_server);
        adaptadorP.notifyDataSetChanged();
        setListAdapter(adaptadorP);
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
            //!!recargar la lista de ciudades. Volver a sacar todos los datos del servidor - SELECT * FROM Ciudades
        }

        else if (id == R.id.menunueva){
            //Añadir nueva ciudad
            Intent i = new Intent(this,EditarCiudad.class);
            startActivity(i);
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

        }else if(id == R.id.itemCiudades){

        }else if(id == R.id.itemamigos){

        }else if(id == R.id.itemMensajes){

        }else if(id == R.id.itemlogout){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //!!para recargar la lista de ciudades con la nueva ciudad, tras añadirla
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setImagen(ImageView img){
        //!!Este metodo hay que cambiarlo para que coja la imagen del usuario del servidor ftp, la pase a bitmap, la redimensione
        //!!y la ponga en el ImageView
        Drawable dr;
        dr = getResources().getDrawable(R.mipmap.ic_launcher);
        //Bitmap bmResized = Bitmap.createScaledBitmap(bm, 250, 250, true);
        if (img.getDrawingCache() != null)
            img.destroyDrawingCache();
        img.setImageDrawable(dr);
        img.setAdjustViewBounds(true);
    }

    //Tarea asincrona para llenar la lista de ciudades
    class GetCiudadesAsyncTask extends AsyncTask<Void, Void, Void> {

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

            listaCiudades = opDb.getCiudades(url_select);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            if(listaCiudades != null) {
                adaptadorP = new AdaptadorPrincipal(PrincipalActivity.this, listaCiudades, ip_server);
                adaptadorP.notifyDataSetChanged();
                setListAdapter(adaptadorP);
            }
            else
                new MostrarMensaje(PrincipalActivity.this).mostrarMensaje(getString(R.string.tituloproblemaactulistaprincipal),
                        getString(R.string.textoproblemaactulistaprincipal),getString(R.string.aceptar));
        }
    }

}
