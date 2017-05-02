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
    ConexionHttpInsert conexInsert;
    String consulta;
    JSONArray jsonArrResultado;
    JSONObject jsonOb;

    public OperacionesBD(){
        conex = new ConexionHttpSelect();
        conexUp = new ConexionHttpUpdateDelete();
        conexInsert = new ConexionHttpInsert();
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
                Usuario us = new Usuario(jsonObject.getInt("idUsuario"),
                        jsonObject.getString("Nombre"),jsonObject.getString("Password"),
                        jsonObject.getString("UrlFoto"),jsonObject.getInt("idRango"), jsonObject.getString("ciudad"));
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

    public void agregarColab (String linkInsert, int idC, int idUsuario){
        //INSERT INTO colaboradores (IdCiudad, IdUsuario) VALUES (idC, idU);

        try {
            //Agregamos un nuevo colaborador
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("IdCiudad", Integer.toString(idC));
            parametros.put("IdUsuario", Integer.toString(idUsuario));
            conexInsert.serverData(linkInsert, parametros);

        }catch(Exception e){
            e.printStackTrace();
        }

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

    public Ciudad getCiudad(String linkConsulta, long id) {
        //SELECT * FROM ciudades WHERE IdCiudad = id;
        consulta="SELECT * FROM ciudades WHERE IdCiudad = " + (int)id;

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON
            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                Ciudad c = new Ciudad(jsonObject.getInt("IdCiudad"),
                        jsonObject.getString("Nombre"),jsonObject.getString("Descripcion"),
                        jsonObject.getString("UrlFoto"),jsonObject.getDouble("Latitud"),jsonObject.getDouble("Longitud"));
                return c;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean sigueCiudad(String linkConsulta, long idC, int id) {
        //comprueba si un usuario sigue a una ciudad (la tiene en favoritos) o no
        //SELECT COUNT(*) FROM ciudadesfav WHERE IdCiudad = idC AND IdUsuario = id;
        consulta="SELECT COUNT(*) AS total FROM ciudadesfav WHERE IdCiudad = " + (int)idC + " AND IdUsuario = " + id;

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON
            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                if(jsonObject.getInt("total") > 0)
                    return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void updateSigueCiudad(boolean marcado, String linkConsultaDel, String linkConsultaInsert, long idC, int idU) {
        //DELETE FROM ciudadesfav WHERE IdCiudad = idC AND IdUsuario = idU;
        //INSERT INTO ciudadesfav (IdCiudad, IdUsuario) VALUES (idC, idU);

        consulta="DELETE FROM ciudadesfav WHERE IdCiudad = " + idC + " AND IdUsuario = " +idU;

        try {
            //Primero hacemos el delete, por si ya existia
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            conexUp.sendRequest(linkConsultaDel, parametros);

            if(marcado) {
                //Ahora hacemos el insert, en caso de que el boton de seguir estuviese marcado
                parametros.clear();
                parametros.put("IdCiudad", Long.toString(idC));
                parametros.put("IdUsuario", Integer.toString(idU));
                conexInsert.serverData(linkConsultaInsert, parametros);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Usuario> updateSpinnerColaboradores(String linkConsulta, int id){
        //SELECT * FROM usuarios WHERE IdUsuario IN (SELECT IdUsuario FROM colaboradores WHERE IdCiudad = id)

        consulta = "SELECT * FROM usuarios WHERE IdUsuario IN (SELECT IdUsuario FROM colaboradores WHERE IdCiudad = " + id + ")";

        ArrayList<Usuario> arrUsers = new ArrayList<>();

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Recorremos el jsonarray con los distintos usuarios
            if (jsonArrResultado != null) {
                for (int i = 0; i < jsonArrResultado.length(); i++) {
                    JSONObject jsonObject = jsonArrResultado.getJSONObject(i);
                    Usuario u = new Usuario(jsonObject.getInt("idUsuario"),jsonObject.getString("Nombre"),
                            jsonObject.getString("Password"),jsonObject.getString("UrlFoto"),
                            jsonObject.getInt("idRango"),jsonObject.getString("ciudad"));
                    arrUsers.add(u);
                }
                return arrUsers;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String getNombreFoto(String linkConsulta, String tabla, String condicion, int id) {
        //SELECT UrlFoto FROM tabla WHERE condicion = id;
        consulta="SELECT UrlFoto FROM " + tabla + " WHERE " + condicion + " = " + id;

        try {
            //Realizar la consulta
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON y sacamos la id
            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                return jsonObject.getString("UrlFoto");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public void borrarRegistro(String linkConsultaDel, String tabla, String condicion, int id){
        //DELETE FROM tabla WHERE condicion = id;

        consulta="DELETE FROM " + tabla + " WHERE " + condicion + " = " + id;

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            conexUp.sendRequest(linkConsultaDel, parametros);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getIdsSubcategorias(String linkConsulta, int idC){
        //SELECT IdItem FROM items WHERE IdCiudad = idC;

        consulta = "SELECT IdItem FROM items WHERE IdCiudad = " + idC;

        ArrayList<Integer> arrIds = new ArrayList<>();

        try {
            //Realizar la consulta
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON y sacamos la id
            if (jsonArrResultado != null) {
                for (int i = 0; i < jsonArrResultado.length(); i++) {
                    JSONObject jsonObject = jsonArrResultado.getJSONObject(i);
                    arrIds.add(jsonObject.getInt("IdItem"));
                }
                return arrIds;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;

    }

}
