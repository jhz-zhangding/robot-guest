package com.efrobot.guests.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.efrobot.guests.Env.SpContans;
import com.efrobot.guests.service.GuestRobotService;
import com.efrobot.guests.service.UltrasonicService;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.PreferencesUtils;

/**
 * Created by zd on 2017/9/14.
 */
public class ReceiveMaskBroadcast extends BroadcastReceiver {

    /**
     * 监听盖子状态
     */
    public final String ROBOT_MASK_CHANGE = "android.intent.action.MASK_CHANGED";
    public final String KEYCODE_MASK_ONPROGRESS = "KEYCODE_MASK_ONPROGRESS"; //开闭状态
    public final String KEYCODE_MASK_CLOSE = "KEYCODE_MASK_CLOSE"; //关闭面罩
    public final String KEYCODE_MASK_OPEN = "KEYCODE_MASK_OPEN";  //打开面罩

    @Override
    public void onReceive(Context context, Intent intent) {
        L.i("ReceiveMaskBroadcast", "lidBoardReceive");
        if (ROBOT_MASK_CHANGE.equals(intent.getAction())) {
            boolean isOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);
            boolean isOpening = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
            boolean isClose = intent.getBooleanExtra(KEYCODE_MASK_CLOSE, false);

            if (isClose) {
                boolean isStartUpUltrasonic = PreferencesUtils.getBoolean(context, SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, false);
                if(isStartUpUltrasonic) {
                    Intent serviceIntent = new Intent(context, UltrasonicService.class);
                    context.startService(serviceIntent);
                }
            }

        }
    }
}
