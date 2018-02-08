package com.efrobot.guests.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 检测数据
 * Created by zd on 2018/2/8.
 */
public class CheckTimeUtils {

    private Map<Integer, Timer> map = new HashMap<>();

    private final long CHECK_MINUTE = 5 * 60 * 1000;

    private OnCheckTimeListener onCheckTimeListener;

    public void setOnCheckTimeListener(OnCheckTimeListener onCheckTimeListener) {
        this.onCheckTimeListener = onCheckTimeListener;
    }

    //加入任务
    public void gennerateTimeTask(int tag) {
        if (!map.containsKey(tag)) {
            map.put(tag, new Timer());
            map.get(tag).schedule(new TimerTask() {
                @Override
                public void run() {
                    if (onCheckTimeListener != null) {
                        onCheckTimeListener.timeFinish();
                    }
                    cancel();
                }
            }, CHECK_MINUTE);
        }
    }

    public void clearThisTask(int tag) {
        if (map.containsKey(tag)) {
            map.get(tag).cancel();
        }
    }

    interface OnCheckTimeListener {
        void timeFinish();
    }

}
