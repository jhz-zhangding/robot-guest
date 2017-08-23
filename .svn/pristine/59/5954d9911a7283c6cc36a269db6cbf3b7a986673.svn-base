package com.efrobot.guests.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/10.
 */
public class ActivityManager {
    private static ActivityManager instance;


    /**
     * 单例，返回一个实例
     *
     * @return
     */
    public static ActivityManager getInstance() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    List<Activity> activities;

    public void addActivity(Activity activity) {
        if (activities == null) {
            activities = new ArrayList<Activity>();
        }
        activities.add(activity);
    }

    public void finishActivity() {
        if (activities != null) {
            int size = activities.size();
            for (int i = 0; i < size; i++) {
                activities.get(i).finish();
            }
            activities.clear();
        }
    }

}
