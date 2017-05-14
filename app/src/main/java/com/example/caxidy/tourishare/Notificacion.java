package com.example.caxidy.tourishare;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Notificacion {
    //Clase para usar notificaciones

    public void lanzarNotificacion(Context contexto,String titulo, String texto){

        //Builder de la notificacion. Se establecen las propiedades
        Notification noti = new NotificationCompat.Builder(contexto)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setSmallIcon(R.drawable.icono_notifs)
                .setOngoing(false)
                .setAutoCancel(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_MAX).build();

        //Se lanza la notificacion...
        NotificationManager notificationManager = (NotificationManager) contexto.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, noti);
    }
}
