package com.efrobot.guests.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.efrobot.guests.Env.SpContans;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.PreferencesUtils;

/**
 * 监听盖子状态Service
 */
public class GuestRobotService extends Service {

    private static final String TAG = UltrasonicService.class.getSimpleName();

    public GuestRobotService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i(TAG, "GuestRobotService onStartCommand");
        //注册面罩广播
        registerLiboard();
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    /**
     * 监听盖子状态
     */
    public final String ROBOT_MASK_CHANGE = "android.intent.action.MASK_CHANGED";
    public final String KEYCODE_MASK_ONPROGRESS = "KEYCODE_MASK_ONPROGRESS"; //开闭状态
    public final String KEYCODE_MASK_CLOSE = "KEYCODE_MASK_CLOSE"; //关闭面罩
    public final String KEYCODE_MASK_OPEN = "KEYCODE_MASK_OPEN";  //打开面罩
    private BroadcastReceiver lidBoardReceive = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ROBOT_MASK_CHANGE.equals(intent.getAction())) {
                boolean isOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);
                boolean isOpening = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
                boolean isClose = intent.getBooleanExtra(KEYCODE_MASK_CLOSE, false);

                if (isClose) {
                    boolean isStartUpUltrasonic = PreferencesUtils.getBoolean(context, SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, false);
                    if(isStartUpUltrasonic) {
                        Intent serviceIntent = new Intent(context, UltrasonicService.class);
                        startService(serviceIntent);
                    }
                }

            }
        }
    };

    private void registerLiboard() {
        IntentFilter dynamic_filter = new IntentFilter();
        dynamic_filter.addAction(ROBOT_MASK_CHANGE);            //添加动态广播的Action
        registerReceiver(lidBoardReceive, dynamic_filter);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
