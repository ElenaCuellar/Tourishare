package com.example.caxidy.tourishare;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
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

        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.setConnectTimeout(300000);
            ftp.connect(params.get("host"));

            ftp.login("tourishare", "root");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            OutputStream outputStream = null;
            boolean success = false;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(
                        new File(act.getExternalFilesDir(null), "temporal.jpg")));
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
}
