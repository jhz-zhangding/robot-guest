package com.efrobot.guest.bean;

import android.database.Cursor;

/**
 * Created by Administrator on 2017/3/3.
 */
public class SettingBean {
    public static String TABLE_NAME = "ultrasonic";
    public static String DISTANCE = "distance_value";

    private String distanceValue;

    private int id;

    public String getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(String distanceValue) {
        this.distanceValue = distanceValue;
    }

    public SettingBean(Cursor mCursor) {

        if (mCursor == null) {
            return;
        }
        id = mCursor.getInt(mCursor.getColumnIndexOrThrow("_id"));
        distanceValue = mCursor.getString(mCursor.getColumnIndexOrThrow(DISTANCE));
    }

    public SettingBean() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
