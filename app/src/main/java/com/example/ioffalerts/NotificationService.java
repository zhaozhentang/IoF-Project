package com.example.ioffalerts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

/**
 * Created by Zhen on 02/11/2016.
 */

public class NotificationService extends Service {

    private MobileServiceTable<ToDoItem> mchecktable;
    private static int before=0;
    private static int after=0;
    private static boolean ignore1st = false;
    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    private Timer mTimer;

    @Override
    public void onCreate(){
        super.onCreate();
        mTimer = new Timer();
        mTimer.schedule(timerTask,2000, 2 * 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        try{}catch(Exception e){e.printStackTrace();}
        return super.onStartCommand(intent,flags,startId);
    }

    TimerTask timerTask = new TimerTask(){
        @Override
        public void run(){
            Log.e("Log","Running");
            try{
                after=0;
                mchecktable=ToDoActivity.mToDoTable;
                final List<ToDoItem> results = refreshItemsFromMobileServiceTable();
                after= results.size();
                if (after>before && after>-1){

                    if(ignore1st==false){
                        ignore1st=true;
                    }else{
                        notifying(results.get(0).getDeviceName(),results.get(0).getText());
                    }

                    before=after;

                }
            }catch(Exception e){e.printStackTrace();}

        }
    };

    public void onDestroy(){
        try{
            mTimer.cancel();
            timerTask.cancel();
        }catch(Exception e){e.printStackTrace();}
        Intent intent = new Intent("com.example.sample");
        intent.putExtra("yourvalue","torestore");
        sendBroadcast(intent);
    }
    private int notificationid =0;


    public void notifying(String title,String message){


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message);
        //Vibration
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        //LED
        mBuilder.setLights(Color.RED, 10000, 10000);

        //Ton
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(uri);

        Intent resultIntent = new Intent(this, ToDoActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        mNotificationManager.notify(notificationid, mBuilder.build());
        notificationid+=1;
    }

    public void checktable(){
        int before,after;

    }

    private List<ToDoItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {

        return mchecktable.where().field("complete").orderBy("createdAt", QueryOrder.Descending).
                eq(val(false)).execute().get();
    }


}
