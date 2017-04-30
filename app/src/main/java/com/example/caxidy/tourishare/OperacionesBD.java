package com.example.caxidy.tourishare;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Esta clase se encarga de realizar algunas consultas u otras operaciones a la BD del servidor
 */

public class OperacionesBD {

    ConexionHttpSelect conex;
    ConexionHttpUpdateDelete conexUp;
    String consulta;
    JSONArray jsonArrResultado;
    JSONObject jsonOb;

    public OperacionesBD(){
        conex = new ConexionHttpSelect();
        conexUp = new ConexionHttpUpdateDelete();
        jsonArrResultado = new JSONArray();
        jsonOb = new JSONObject();

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

    public Usuario comprobarLogin(String linkConsulta, String username, String pass) {
        //SELECT * FROM usuarios WHERE Nombre = username AND Password = pass;
        consulta="SELECT * FROM usuarios WHERE Nombre = '" + username + "' AND Password = '" + pass + "'";

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON y pasamos sus datos a un objeto Usuario
            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                Usuario us = new Usuario(jsonObject.getString("Nombre"),jsonObject.getString("Password"),
                        jsonObject.getString("UrlFoto"),jsonObject.getString("ciudad"));
                return us;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public int getIdSubcategoria(String linkConsulta) {
        //SELECT IdItem FROM items WHERE IdItem = (SELECT MAX(IdItem) FROM items);
        consulta="SELECT IdItem FROM items WHERE IdItem = (SELECT MAX(IdItem) FROM items)";

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON y sacamos la id
            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                return jsonObject.getInt("IdItem");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public int getIdCiudad(String linkConsulta) {
        //SELECT IdCiudad FROM ciudades WHERE IdCiudad = (SELECT MAX(IdCiudad) FROM ciudades);
        consulta="SELECT IdCiudad FROM ciudades WHERE IdCiudad = (SELECT MAX(IdCiudad) FROM ciudades)";

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON y sacamos la id
            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                return jsonObject.getInt("IdCiudad");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public int updateIdCiudadSubcategorias(String linkConsulta, int idC, ArrayList<Integer> idsS) {
        //UPDATE items SET IdCiudad = idC WHERE IdItem = idsS(0) OR .....
        //Formar la consulta...
        consulta="UPDATE items SET IdCiudad = " + idC + " WHERE ";

        for(int i=0; i<idsS.size();i++){
            if(i==idsS.size()-1)
                consulta += "IdItem = " + idsS.get(i);
            else
                consulta += "IdItem = " + idsS.get(i) + " OR ";
        }

        System.out.println(consulta);

        try {
            //Realizar el update que devolvera un json
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonOb = conexUp.sendRequest(linkConsulta, parametros);

            //Si el update ha ido bien, debe devolver 1
            if (jsonOb != null) {
                return jsonOb.getInt("success"); //debe devolver 1
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Ciudad> getCiudades(String linkConsulta) {
        //SELECT * FROM ciudades;
        consulta="SELECT * FROM ciudades";

        ArrayList<Ciudad> arrCiudad = new ArrayList<>();

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Recorremos el jsonarray con las distintas ciudades
            if (jsonArrResultado != null) {
                for (int i = 0; i < jsonArrResultado.length(); i++) {
                    JSONObject jsonObject = jsonArrResultado.getJSONObject(i);
                    Ciudad c = new Ciudad(jsonObject.getInt("IdCiudad"),jsonObject.getString("Nombre"),
                            jsonObject.getString("Descripcion"),jsonObject.getString("UrlFoto"),
                            jsonObject.getDouble("Latitud"),jsonObject.getDouble("Longitud"));
                    arrCiudad.add(i,c);
                }
                return arrCiudad;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
