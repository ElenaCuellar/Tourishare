package com.example.caxidy.tourishare;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class AdaptadorPrincipal extends BaseAdapter {

    private ArrayList<Ciudad> lista;
    private final Activity actividad;

    public AdaptadorPrincipal(Activity a, ArrayList<Ciudad> v){
        super();
        this.lista = v;
        this.actividad = a;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater ly = actividad.getLayoutInflater();
        View view = ly.inflate(R.layout.item_lista, null, true);

        //Llenamos los campos de la vista actual con los datos correspondientes
        TextView tNom= (TextView) view.findViewById(R.id.itemNombre);
        tNom.setText(lista.get(position).getNombre());

        ImageView fotoLista = (ImageView) view.findViewById(R.id.itemFoto);
        File archivoImg = new File(actividad.getExternalFilesDir(null) + "/" + lista.get(position).getUrlfoto());

        if (archivoImg.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(archivoImg.getAbsolutePath());
            Bitmap bmResized = Bitmap.createScaledBitmap(bm, 120, 120, true);
            if (fotoLista.getDrawingCache() != null)
                fotoLista.destroyDrawingCache();
            fotoLista.setImageBitmap(bmResized);
            fotoLista.setAdjustViewBounds(true);
        }

        return view;
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return lista.get(position).getId();
    }

}
