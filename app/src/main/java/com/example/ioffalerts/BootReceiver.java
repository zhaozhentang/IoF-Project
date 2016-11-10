package com.example.ioffalerts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Zhen on 02/11/2016.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Log.i("Service Stop","Need to debug ");
        context.startService(new Intent(context,NotificationService.class));;
    }
}
