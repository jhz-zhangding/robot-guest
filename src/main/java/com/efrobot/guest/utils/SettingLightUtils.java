package com.efrobot.guest.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.efrobot.library.RobotManager;

/**
 * Created by zhaodekui on 2017/3/17.
 */
public class SettingLightUtils  {

    private static SettingLightUtils utils;
    private Context context;
    private long firstIntoSystemTime, currentSystemTime;
    private long interval, duration;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            currentSystemTime = System.currentTimeMillis();;
            if(msg.what == 0) {
                RobotManager.getInstance(context).getControlInstance().setLightBeltBrightness(255);
                if(currentSystemTime - firstIntoSystemTime < duration) {
                    handler.sendEmptyMessageDelayed(1, interval);
                } else {
                    handler.sendEmptyMessage(1);
                }
            } else  if(msg.what == 1) {
                RobotManager.getInstance(context).getControlInstance().setLightBeltBrightness(0);
                if(currentSystemTime - firstIntoSystemTime < duration) {
                    handler.sendEmptyMessageDelayed(0, interval);
                }
            }
        }
    };

    public static SettingLightUtils getInstance() {
        if(utils == null) {
            utils = new SettingLightUtils();
        }
        return utils;
    }

    /**
     * 闪烁
     * interval 间隔
     * duration 多久
     * */
    private void setLightStyle(Context context, long interval, long duration) {
        this.context = context;
        this.interval = interval;
        this.duration = duration;
        handler.sendEmptyMessage(0);
        firstIntoSystemTime = System.currentTimeMillis();
    }

}
