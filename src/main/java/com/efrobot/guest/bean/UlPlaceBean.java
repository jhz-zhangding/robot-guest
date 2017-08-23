package com.efrobot.guest.bean;

import android.database.Cursor;

/**
 * Created by zd on 2017/3/13.
 */
public class UlPlaceBean {

    public static String TABLE_NAME = "ultrasonic_setting";
    public static String ULTRASONIC_ID = "ultrasonic_id";
    public static String ISOPEN = "isOpen_value";
    public static String DISTANCE = "distance_value";


    private int id;

    private int ultrasonicId;
    private int isOpenValue;
    private String distanceValue;

    public UlPlaceBean() {

    }

    public UlPlaceBean(Cursor mCursor) {

        if (mCursor == null) {
            return;
        }
        id = mCursor.getInt(mCursor.getColumnIndexOrThrow("_id"));
        ultrasonicId = mCursor.getInt(mCursor.getColumnIndexOrThrow(ULTRASONIC_ID));
        isOpenValue = mCursor.getInt(mCursor.getColumnIndexOrThrow(ISOPEN));
        distanceValue = mCursor.getString(mCursor.getColumnIndexOrThrow(DISTANCE));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUltrasonicId() {
        return ultrasonicId;
    }

    public void setUltrasonicId(int ultrasonicId) {
        this.ultrasonicId = ultrasonicId;
    }

    public int getIsOpenValue() {
        return isOpenValue;
    }

    public void setIsOpenValue(int isOpenValue) {
        this.isOpenValue = isOpenValue;
    }

    public String getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(String distanceValue) {
        this.distanceValue = distanceValue;
    }
}
