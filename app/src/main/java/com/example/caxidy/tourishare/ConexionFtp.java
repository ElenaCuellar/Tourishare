package com.example.caxidy.tourishare;

import android.app.Activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.net.ftp.*;

public class ConexionFtp {

    private static final int BUFFER_SIZE = 4096;
    URL url;
    String urlFtp;

    public void SubirDatos(HashMap<String, String> params) {

        urlFtp = "ftp://%s:%s@%s/%s";

        //Formamos la url correcta de nuestro FTP --> ftp://user:password@host:port/path
        urlFtp = String.format(urlFtp, "tourishare", "root", params.get("host"), params.get("uploadpath"));
        System.out.println(urlFtp);

        try {
            url = new URL(urlFtp);
            URLConnection conn = url.openConnection(); //Si hay problemas para conectar usar Proxy.NO_PROXY
            conn.setReadTimeout(300000);
            conn.setConnectTimeout(300000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            //Nos conectamos al servidor FTP y abrimos su tuberia de escritura
            OutputStream outputStream = conn.getOutputStream();
            //Abrimos la tuberia de lectura en nuestro dispositivo
            FileInputStream inputStream = new FileInputStream(params.get("filepath"));

            //Leemos y escribimos la informacion (subimos el archivo al servidor)
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                System.out.println(bytesRead + " bytes");
            }

            inputStream.close();
            outputStream.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Boolean bajarDatos (HashMap<String, String> params, Activity act) throws IOException {
        //Metodo para descargar una única imagen del servidor FTP

        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.setConnectTimeout(300000);
            ftp.connect(params.get("host"));

            ftp.login("tourishare", "root");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            //Creamos un archivo de imagen temporal en la carpeta del proyecto, para poder acceder a la imagen y ponerla en un ImageView
            OutputStream outputStream = null;
            boolean success = false;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(
                        new File(act.getExternalFilesDir(null), params.get("temporalpath"))));
                success = ftp.retrieveFile(params.get("downloadpath"), outputStream);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }

            return success;
        } finally {
            if (ftp != null) {
                ftp.logout();
                ftp.disconnect();
            }
        }
    }

    public Boolean bajarTodosLosArchivos (String host, Activity act) throws IOException {

        FTPClient ftp = null;
        OutputStream outputStream = null;
        boolean success = false;
        int totalArchivos = 0;
        int sizeArchivos = 0;

        try {
            ftp = new FTPClient();
            ftp.setConnectTimeout(15000);
            ftp.connect(host);

            ftp.login("tourishare", "root");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            //Obtenemos la lista de archivos
            FTPFile[] archivos = ftp.listFiles("archivosFilezilla");

            //Vamos descargando cada uno de los archivos en el directorio del proyecto
            if(archivos.length > 0) {
                for (int i = 0; i < archivos.length; i++){
                    if(archivos[i].isFile() && archivos[i].getName().contains(".jpg")){
                        sizeArchivos++;
                        try {
                            outputStream = new BufferedOutputStream(new FileOutputStream(
                                    new File(act.getExternalFilesDir(null), archivos[i].getName())));
                            success = ftp.retrieveFile("archivosFilezilla/" + archivos[i].getName(), outputStream);
                            outputStream.flush();
                            outputStream.close();
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                        if(success)
                            totalArchivos++;
                    }
                }
            }

            //Si se han descargado todos los archivos satisfactoriamente...
            if(totalArchivos == sizeArchivos)
                return true;
            else
                return false;
        } finally {
            if (ftp != null) {
                ftp.logout();
                ftp.disconnect();
            }
        }
    }

    public Boolean bajarArchivos (String host, Activity act, ArrayList<String> imagenes)  throws IOException {

        FTPClient ftp = null;
        OutputStream outputStream = null;
        boolean success = false;
        int totalArchivos = 0;
        int sizeArchivos = 0;

        try {
            ftp = new FTPClient();
            ftp.setConnectTimeout(15000);
            ftp.connect(host);

            ftp.login("tourishare", "root");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            //Obtenemos la lista de archivos
            FTPFile[] archivos = ftp.listFiles("archivosFilezilla");

            //Vamos descargando cada uno de los archivos en el directorio del proyecto
            if(archivos.length > 0) {
                for (int i = 0; i < archivos.length; i++){
                    if(archivos[i].isFile() && archivos[i].getName().contains(".jpg")){
                        //si el archivo no esta descargado, lo descargamos
                        if(!archivoExiste(archivos[i].getName(),imagenes)) {
                            sizeArchivos++;
                            try {
                                outputStream = new BufferedOutputStream(new FileOutputStream(
                                        new File(act.getExternalFilesDir(null), archivos[i].getName())));
                                success = ftp.retrieveFile("archivosFilezilla/" + archivos[i].getName(), outputStream);
                                outputStream.flush();
                                outputStream.close();
                            } finally {
                                if (outputStream != null) {
                                    outputStream.close();
                                }
                            }
                            if (success)
                                totalArchivos++;
                        }
                        else{
                            //si ya tenemos los archivos en el alm. interno pero no estan en el FTP, los borramos
                            archivosBorrados(archivos,imagenes,act);
                        }
                    }
                }
            }

            //Si se han descargado todos los archivos satisfactoriamente...
            if(totalArchivos == sizeArchivos)
                return true;
            else
                return false;
        } finally {
            if (ftp != null) {
                ftp.logout();
                ftp.disconnect();
            }
        }
    }

    public boolean archivoExiste (String archivo, ArrayList<String> archs){
        //Comprueba si el archivo a bajar del FTP ya existe en el archivo interno, para ver si es necesario bajarlo o no
        for(int i=0;i < archs.size(); i++){
            if(archs.get(i).equals(archivo))
                return true;
        }

        return false;
    }

    public void archivosBorrados (FTPFile[] archivos, ArrayList<String> archs, Activity act){
        //Borra los archivos del alm. interno que ya no existan en el FTP
        boolean borrado;
        for(int i=0;i < archs.size(); i++){
            borrado=true;
            for(int j=0; j < archivos.length; j++){
                if(archs.get(i).equals(archivos[j].getName()))
                    borrado=false;
            }

            if(borrado)
                new File(act.getExternalFilesDir(null),archs.get(i)).delete();
        }
    }

    public boolean borrarArchivo(HashMap<String, String> params) throws IOException{
        //Metodo para borrar una única imagen del servidor FTP

        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.setConnectTimeout(1500000);
            ftp.connect(params.get("host"));

            ftp.login("tourishare", "root");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            return ftp.deleteFile("archivosFilezilla" + "/" + params.get("nombreFoto"));

        } finally {
            if (ftp != null) {
                ftp.logout();
                ftp.disconnect();
            }
        }
    }
}
