package com.example.caxidy.tourishare;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ConexionHttpUpdateDelete {
    private HttpURLConnection conn;
    public static final int CONNECTION_TIMEOUT = 15 * 1000;

    public JSONObject sendRequest(String link, HashMap<String, String> values) throws JSONException {
        JSONObject json = null;

        try {
            URL url = new URL(link);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(CONNECTION_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            if (values != null) {
                OutputStream os = conn.getOutputStream();
                OutputStreamWriter osWriter = new OutputStreamWriter(os,"UTF-8");
                BufferedWriter writer = new BufferedWriter(osWriter);
                writer.write(getPostData(values));
                writer.flush();
                writer.close();
                os.close();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                InputStreamReader isReader = new InputStreamReader(is,"UTF-8");
                BufferedReader reader = new BufferedReader(isReader);
                String result = "";
                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                is.close();
                result=sb.toString();

                try{
                    json = new JSONObject(result);
                    return json;
                } catch(JSONException e){
                    Log.e("ERROR => ", "Error convirtiendo los datos a JSON: " + e.toString());
                    e.printStackTrace();
                    return null;
                }
            }
        }
        catch (MalformedURLException e) {}
        catch (IOException e) {}

        return json;
    }

    public String getPostData(HashMap<String, String> values) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;

        for(Map.Entry<String, String> entry : values.entrySet()) {
            if (first)
                first = false;
            else
                builder.append("&");
            try {
                builder.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(entry.getValue(),"UTF-8"));
            }catch (UnsupportedEncodingException e) {}
        }
        return builder.toString();
    }
}
