package com.example.caxidy.tourishare;

import org.json.JSONArray;
import org.json.JSONException;
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

    public boolean nuevoColaborador(String linkConsulta,int idC, int idUsuario) throws JSONException {
        //SELECT COUNT(*) FROM colaboradores WHERE IdCiudad = idC AND IdUsuario = idUsuario

        consulta = "SELECT COUNT(*) AS total FROM colaboradores WHERE IdCiudad = " + idC + " AND IdUsuario = " + idUsuario;

        HashMap<String, String> params = new HashMap<>();
        params.put("ins_sql", consulta);
        jsonArrResultado = conex.sendRequest(linkConsulta, params);

        if (jsonArrResultado != null) {
            if (jsonArrResultado.getJSONObject(0).getInt("total") <= 0)
                return true;
        }

        return false;
    }

    public void agregarColab (String linkInsert, int idC, int idUsuario, boolean nuevo){
        //Si el colaborador no existe, lo agregamos
        //INSERT INTO colaboradores (IdCiudad, IdUsuario) VALUES (idC, idU);

        try {
            if(nuevo) {
                //Agregamos un nuevo colaborador
                HashMap<String, String> parametros = new HashMap<>();
                parametros.put("IdCiudad", Integer.toString(idC));
                parametros.put("IdUsuario", Integer.toString(idUsuario));
                conexInsert.serverData(linkInsert, parametros);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void sumarPuntosRango(String linkUp, int idUsuario){
        //UPDATE usuarios SET puntos = (puntos * 2) WHERE idUsuario = id

        consulta="UPDATE usuarios SET puntos = (puntos * 2) WHERE idUsuario = " + idUsuario;

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            conexUp.sendRequest(linkUp, parametros);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void cambiarRango(String linkUp, int idUsuario){
        //Novato <=16, Ocasional <= 256, Viajero <= 2048, Viajero experto > 2048
        //UPDATE usuarios SET idRango = CASE WHEN puntos <= 16 THEN 1 .... END
        //WHERE idUsuario = id

        consulta="UPDATE usuarios SET idRango = CASE WHEN puntos <= 16 THEN 1 WHEN (puntos > 16 AND puntos <= 256) THEN " +
                "2 WHEN (puntos > 256 AND puntos <= 2048) THEN 3 WHEN puntos > 2048 THEN 4 END WHERE idUsuario = " + idUsuario;

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            conexUp.sendRequest(linkUp, parametros);
        }catch (Exception e){
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

    public boolean sigueItem(String linkConsulta, String consult) {
        //comprueba si un usuario sigue a una ciudad, usuario, etc... (la tiene en favoritos) o no
        //SELECT COUNT(*) FROM tabla WHERE id1 = id1 AND id2 = id2;

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consult);
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

    public void borrarRegistroId(String linkConsultaDel, String tabla, String condicion, int id){
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

    public void borrarRegistro(String linkConsultaDel, String tabla, String condicion){
        //DELETE FROM tabla WHERE condicion;

        consulta="DELETE FROM " + tabla + " WHERE " + condicion;

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

    public boolean modificarCiudad(String linkConsultaUp, int id, Ciudad ciudad){
        //UPDATE ciudades SET .... WHERE IdCiudad = id

        consulta="UPDATE ciudades SET Nombre = '" + ciudad.getNombre() +"', Descripcion = '" +
                ciudad.getDescripcion() + "', UrlFoto = '"+ ciudad.getUrlfoto()+ "', Latitud = " +
                ciudad.getLatitud() + ", Longitud = " + ciudad.getLongitud() + " WHERE IdCiudad = " + id;

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            JSONObject json = conexUp.sendRequest(linkConsultaUp, parametros);

            if(json != null && json.getInt("success") != 0)
                return true;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public String getCabeceraSubcat(String linkConsulta, int idTipo){
        //SELECT Categoria FROM categorias WHERE IdCategoria = idTipo

        consulta = "SELECT Categoria FROM categorias WHERE IdCategoria = " + idTipo;

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                return jsonObject.getString("Categoria");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public ArrayList<Subcategoria> getListaSubcategoriaCiudad(String linkConsulta, int idC, int idTipo) {
        //SELECT * FROM items WHERE IdCiudad = idC AND IdCategoria = idTipo;
        consulta="SELECT * FROM items WHERE IdCiudad = " + idC + " AND IdCategoria = " + idTipo;

        ArrayList<Subcategoria> arrSubc = new ArrayList<>();

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Recorremos el jsonarray con las distintas subcats
            if (jsonArrResultado != null) {
                for (int i = 0; i < jsonArrResultado.length(); i++) {
                    JSONObject jsonObject = jsonArrResultado.getJSONObject(i);
                    Subcategoria s = new Subcategoria(jsonObject.getInt("IdItem"),jsonObject.getInt("IdCiudad"),
                            jsonObject.getInt("IdCategoria"), jsonObject.getString("Nombre"),
                            jsonObject.getString("Descripcion"),jsonObject.getString("UrlFoto"),
                            jsonObject.getDouble("Latitud"),jsonObject.getDouble("Longitud"),
                            jsonObject.getDouble("Puntuacion"));
                    arrSubc.add(i,s);
                }
                return arrSubc;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public Subcategoria getSubcategoria(String linkConsulta, int id) {
        //SELECT * FROM items WHERE IdItem = id;
        consulta="SELECT * FROM items WHERE IdItem = " + id;

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON
            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                Subcategoria s = new Subcategoria(jsonObject.getInt("IdItem"),jsonObject.getInt("IdCiudad"),
                        jsonObject.getInt("IdCategoria"), jsonObject.getString("Nombre"),
                        jsonObject.getString("Descripcion"),jsonObject.getString("UrlFoto"),
                        jsonObject.getDouble("Latitud"),jsonObject.getDouble("Longitud"),
                        jsonObject.getDouble("Puntuacion"));
                return s;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean modificarSubcategoria(String linkConsultaUp, int id, Subcategoria subcat){
        //UPDATE items SET .... WHERE IdItem = id

        consulta="UPDATE items SET IdCiudad = " + subcat.getIdCiudad() + ", IdCategoria = " + subcat.getIdCategoria() +
                ", Nombre = '" + subcat.getNombre() +"', Descripcion = '" +
                subcat.getDescripcion() + "', UrlFoto = '"+ subcat.getUrlfoto()+ "', Latitud = " +
                subcat.getLatitud() + ", Longitud = " + subcat.getLongitud() + ", Puntuacion = " + subcat.getPuntuacion() +
                " WHERE IdItem = " + id;

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            JSONObject json = conexUp.sendRequest(linkConsultaUp, parametros);

            if(json != null && json.getInt("success") != 0)
                return true;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public int mostrarColaborador (String linkConsulta, String nombreColab, int idCiudad){
        //SELECT IdUsuario FROM colaboradores WHERE IdUsuario IN (SELECT idUsuario FROM usuarios WHERE
        // Nombre = nombreColab) AND IdCiudad = idCiudad

        consulta="SELECT IdUsuario FROM colaboradores WHERE IdUsuario IN (SELECT idUsuario FROM usuarios WHERE Nombre = '" +
                nombreColab + "') AND IdCiudad = " + idCiudad;

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON
            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                return jsonObject.getInt("IdUsuario");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public Usuario getUsuario(String linkConsulta, int idUsuario){
        //SELECT * FROM usuarios WHERE idUsuario = idU;

        consulta = "SELECT * FROM usuarios WHERE idUsuario = " + idUsuario;

        try {
            //Realizar la consulta que devolvera un array de JSON
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            //Sacamos el objeto JSON
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

    public void updateSigueUsuario(boolean marcado, String linkConsultaDel, String linkConsultaInsert, int idAmigo, int idU) {
        //DELETE FROM amigos WHERE IdAmigo = idAmigo AND IdUsuario = idU;
        //INSERT INTO amigos (IdUsuario, IdAmigo) VALUES (idU, idAmigo);

        consulta="DELETE FROM amigos WHERE IdAmigo = " + idAmigo + " AND IdUsuario = " +idU;

        try {
            //Primero hacemos el delete, por si ya existia
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            conexUp.sendRequest(linkConsultaDel, parametros);

            if(marcado) {
                //Ahora hacemos el insert, en caso de que el boton de seguir estuviese marcado
                parametros.clear();
                parametros.put("IdUsuario", Integer.toString(idU));
                parametros.put("IdAmigo", Integer.toString(idAmigo));
                conexInsert.serverData(linkConsultaInsert, parametros);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getRango(String linkConsulta, int idR){
        //SELECT Rango from rangos WHERE idRango = idR

        consulta = "SELECT Rango FROM rangos WHERE idRango = " + idR;

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            jsonArrResultado = conex.sendRequest(linkConsulta, parametros);

            if (jsonArrResultado != null) {
                JSONObject jsonObject = jsonArrResultado.getJSONObject(0);
                return jsonObject.getString("Rango");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return "";
    }

    public String insertarMensaje(String linkInsert, HashMap<String, String> params){
        //Insertar un nuevo registro en la tabla de mensaje ---> insertar un nuevo mensaje
        try {
            return conexInsert.serverData(linkInsert, params);
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public boolean modificarUsuario(String linkConsultaUp, Usuario usuario){
        //UPDATE usuarios SET .... WHERE idUsuario = usuario.getId()

        consulta="UPDATE usuarios SET Nombre = '" + usuario.getNombre() + "', UrlFoto = '"+ usuario.getUrlfoto()+
                "', ciudad = '" + usuario.getCiudad() + "' WHERE idUsuario = " + usuario.getId();

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            JSONObject json = conexUp.sendRequest(linkConsultaUp, parametros);

            if(json != null && json.getInt("success") != 0)
                return true;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean modificarPass(String linkConsultaUp, String nuevaP, int idU){
        //UPDATE usuarios SET Password = 'nuevaP' WHERE idUsuario = id

        consulta="UPDATE usuarios SET Password = '" + nuevaP + "' WHERE idUsuario = " + idU ;

        try {
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("ins_sql", consulta);
            JSONObject json = conexUp.sendRequest(linkConsultaUp, parametros);

            if(json != null && json.getInt("success") != 0)
                return true;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
}
