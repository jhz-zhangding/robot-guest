package com.efrobot.guests.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 监听盖子状态Service
 */
public class GuestRobotService extends Service {

    public GuestRobotService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, START_STICKY, startId);
    }


}
