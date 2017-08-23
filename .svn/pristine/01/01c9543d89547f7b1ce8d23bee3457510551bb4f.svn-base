package com.efrobot.guests.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by zhaodekui on 2017/3/9.
 */
public class MyLidBoardReceive extends BroadcastReceiver {
    public final static String ROBOT_MASK_CHANGE = "android.intent.action.MASK_CHANGED";
    public final static String KEYCODE_MASK_ONPROGRESS = "KEYCODE_MASK_ONPROGRESS";
    public final static String KEYCODE_MASK_CLOSE = "KEYCODE_MASK_CLOSE";
    public final static String KEYCODE_MASK_OPEN = "KEYCODE_MASK_OPEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        String actonReceive = intent.getAction();
        if(ROBOT_MASK_CHANGE.equals(actonReceive)) {
            boolean maskOnProgress = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
            boolean maskOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);
            if(maskOnProgress || maskOpen) {
                //判断service是否活着

            }
        }
    }
}
