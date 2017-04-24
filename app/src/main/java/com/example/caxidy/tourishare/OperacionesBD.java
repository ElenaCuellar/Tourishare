package com.example.caxidy.tourishare;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Esta clase se encarga de realizar algunas consultas u otras operaciones a la BD del servidor
 */

public class OperacionesBD {

    ConexionHttpSelect conex;
    String consulta;
    JSONArray jsonArrResultado;

    public OperacionesBD(){
        conex = new ConexionHttpSelect();
        jsonArrResultado = new JSONArray();
    }

    public boolean comprobarUsuarioUnico(String linkConsulta, String username) {
        //SELECT Nombre FROM Usuarios;
        consulta="Select u.Nombre from usuarios u";

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Recorremos el jsonarray con los distintos nombres de usuario de la tabla Usuarios
            if (jsonArrResultado != null) {
                for (int i = 0; i < jsonArrResultado.length(); i++) {
                    JSONObject jsonObject = jsonArrResultado.getJSONObject(i);
                    //Si hay algun nombre de usuario en la BD igual al que queremos añadir, devuelve false
                    if (jsonObject.getString("Nombre").equals(username))
                        return false;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
