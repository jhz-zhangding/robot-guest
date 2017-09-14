package com.efrobot.guests.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.efrobot.guests.service.GuestRobotService;
import com.efrobot.library.mvp.utils.L;

/**
 * Created by zd on 2017/9/14.
 */
public class ReceiveBootBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        L.e("ReceiveBootBroadcast", "ReceiveBootBroadcast:" + Intent.ACTION_BOOT_COMPLETED);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent intent1 = new Intent();
            ComponentName componentName = new ComponentName(context, GuestRobotService.class);
            intent1.setComponent(componentName);
            context.startService(intent1);
        }
    }
}
