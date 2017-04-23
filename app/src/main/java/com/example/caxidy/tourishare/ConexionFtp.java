package com.example.caxidy.tourishare;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

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
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(15000);
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
}
