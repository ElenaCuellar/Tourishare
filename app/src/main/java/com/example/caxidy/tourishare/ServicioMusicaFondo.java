package com.example.caxidy.tourishare;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Servicio que activa la musica de fondo de la aplicacion
 */

public class ServicioMusicaFondo extends Service implements MediaPlayer.OnCompletionListener {

    MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mediaPlayer = MediaPlayer.create(this,R.raw.chopinwaltzminor);
        mediaPlayer.setLooping(true); //activar el loop de la cancion
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Si no esta activo ya, comenzar reproduccion
        if(!mediaPlayer.isPlaying())
            mediaPlayer.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Parar reproduccion de musica
        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();

        mediaPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {}
}
