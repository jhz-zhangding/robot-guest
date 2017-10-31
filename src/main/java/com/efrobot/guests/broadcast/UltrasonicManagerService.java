package com.efrobot.guests.broadcast;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.efrobot.guests.GuestsApplication;

/**
 * 需要2小时标定一次超声波
 *
 * */

public class UltrasonicManagerService extends Service {

    private GuestsApplication application;

    private Intent intent;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        application = GuestsApplication.from(this);



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
