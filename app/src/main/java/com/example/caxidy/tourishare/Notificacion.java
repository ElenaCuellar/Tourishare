package com.example.caxidy.tourishare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Notificacion {
    //Clase para usar notificaciones

    /**
     * Metodo lanzarNotificacion:
     * @param contexto El contexto desde donde se lanza la notificacion. Suele ser "this" el parametro
     * @param intent Sus parametros deben ser, normalmente, (this, activityQueSeLanzaConLaNotificacion.class)
     * @param titulo Titulo de la notificacion
     * @param texto Texto de la notificacion
     */
    public void lanzarNotificacion(Context contexto, Intent intent,String titulo, String texto){

        //!!ejemplo
        /*        Intent i = new Intent(this,MostrarMensaje.class);
        new Notificacion().lanzarNotificacion(this,i,"Hola","Esto es una prueba");*/

        //Actividad que se lanza al pulsar la notificacion
        PendingIntent pIntent = PendingIntent.getActivity(contexto, (int) System.currentTimeMillis(), intent, 0);

        //Builder de la notificacion. Se establecen las propiedades
        Notification noti = new NotificationCompat.Builder(contexto)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pIntent).build();

        //Se lanza la notificacion...
        NotificationManager notificationManager = (NotificationManager) contexto.getSystemService(NOTIFICATION_SERVICE);
        noti.flags |= Notification.FLAG_AUTO_CANCEL; //se esconde la notificacion tras seleccionarla
        notificationManager.notify(0, noti);
    }

}
