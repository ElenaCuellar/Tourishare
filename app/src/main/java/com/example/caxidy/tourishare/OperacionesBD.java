package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Esta clase se encarga de realizar algunas consultas u otras operaciones a la BD del servidor
 */

public class OperacionesBD {

    public ProgressDialog pD;
    ConexionHttpSelect conex;
    ConsultaAsyncTask consultaAsyncTask;
    Context contexto;
    String urlConsulta, mensajePBar, consulta;
    JSONArray jsonArrResultado;
    boolean consultaFin;

    public OperacionesBD(Context cntx){
        contexto = cntx;
        conex = new ConexionHttpSelect();
        consultaAsyncTask = new ConsultaAsyncTask();
        jsonArrResultado = new JSONArray();
    }

    public boolean comprobarUsuarioUnico(String linkConsulta, String msgPBar, String username) {
        //SELECT Nombre FROM Usuarios;

        urlConsulta=linkConsulta;
        mensajePBar=msgPBar;
        consulta="Select u.Nombre from usuarios u";
        consultaFin = false;

        consultaAsyncTask.execute();
        while(!consultaFin){}
        //Recorremos el jsonarray con los distintos nombres de usuario de la tabla Usuarios
        if(jsonArrResultado != null) {
            for (int i = 0; i < jsonArrResultado.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArrResultado.getJSONObject(i);
                    //Si hay algun nombre de usuario en la BD igual al que queremos añadir, devuelve false
                    if (jsonObject.getString("Nombre").equals(username)) ;
                    return false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    //Tarea asincrona de consulta a la BD
    class ConsultaAsyncTask extends AsyncTask<String, String, JSONArray> {

        @Override
        protected void onPreExecute() {

            /*pD = new ProgressDialog(contexto);
            pD.setMessage(mensajePBar);
            pD.setIndeterminate(false);
            pD.setCancelable(true);
            pD.show();*/
        }

        @Override
        protected JSONArray doInBackground(String... args) {
            try {
                //Realizar la consulta que devolvera un array de JSON
                JSONArray jsonArray;
                HashMap<String, String> parametros = new HashMap<>();
                parametros.put("ins_sql", consulta);
                jsonArray = conex.sendRequest(urlConsulta,
                        parametros);
                if (jsonArray != null) {
                    return jsonArray;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONArray json) {
            /*if (pD != null && pD.isShowing()) {
                pD.dismiss();
            }*/
            System.out.println("POST EXECUTEEEEEEEEEE");
            if(json!=null) {
                jsonArrResultado = json;
                consultaFin = true;
            }else {
                consultaFin = true;
                Toast.makeText(contexto, contexto.getString(R.string.arraynulo), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
