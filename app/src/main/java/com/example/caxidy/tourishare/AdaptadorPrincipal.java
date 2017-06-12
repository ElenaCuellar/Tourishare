package com.example.caxidy.tourishare;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

        //Contenedor de la imagen del item de lista
        ViewHolder holder = new ViewHolder();

        //Llenamos los campos de la vista actual con los datos correspondientes
        TextView tNom= (TextView) view.findViewById(R.id.itemNombre);
        tNom.setText(lista.get(position).getNombre());

        holder.position = position;
        holder.fotoLista = (ImageView) view.findViewById(R.id.itemFoto);
        //Iniciamos una tarea asincrona que coge el bitmap del almacenamiento interno
        new ImagenAdpTask(position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

        return view;
    }

    private class ImagenAdpTask extends AsyncTask<Void,Void,Bitmap> {
        private int mPosition;
        private ViewHolder mHolder;

        public ImagenAdpTask(int position, ViewHolder holder) {
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(Void... arg0) {
            //Descargar bitmap
            File archivoImg;
            Bitmap bm;

            try {
                archivoImg = new File(actividad.getExternalFilesDir(null) + "/" + lista.get(mPosition).getUrlfoto());
            }catch(Exception e){
                archivoImg = null;
            }

            if (archivoImg != null && archivoImg.exists())
                bm = BitmapFactory.decodeFile(archivoImg.getAbsolutePath());
            else
                bm = BitmapFactory.decodeResource(actividad.getResources(), R.mipmap.ic_launcher);

            Bitmap bmResized = Bitmap.createScaledBitmap(bm, 120, 120, true);

            return bmResized;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //si el item esta en la misma posicion y no es nulo, ponemos la imagen (para evitar errores al actualizar la lista)
            if (mHolder.position == mPosition && bitmap != null) {
                if (mHolder.fotoLista.getDrawingCache() != null)
                    mHolder.fotoLista.destroyDrawingCache();
                mHolder.fotoLista.setImageBitmap(bitmap);
                mHolder.fotoLista.setAdjustViewBounds(true);
            }
        }
    }

    private static class ViewHolder {
        public ImageView fotoLista;
        public int position;
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
