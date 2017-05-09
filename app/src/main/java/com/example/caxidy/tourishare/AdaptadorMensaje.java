package com.example.caxidy.tourishare;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
;
import java.util.ArrayList;

public class AdaptadorMensaje extends BaseAdapter {

    private ArrayList<Mensaje> lista;
    private final Activity actividad;

    public AdaptadorMensaje(Activity a, ArrayList<Mensaje> v){
        super();
        this.lista = v;
        this.actividad = a;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater ly = actividad.getLayoutInflater();
        View view = ly.inflate(R.layout.mensaje_lista, null, true);

        //Llenamos los campos de la vista actual con los datos correspondientes
        TextView tAsunto = (TextView) view.findViewById(R.id.itemmsgAsunto);
        tAsunto.setText(lista.get(position).getCabecera());

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
        return lista.get(position).getIdMensaje();
    }
}
