package com.efrobot.guest.bean;

import android.database.Cursor;

/**
 * Created by zhaodekui on 2017/3/20.
 */
public class CustomActionBean {

    public static String TABLE_NAME = "content";
    public static String TABLE_END_NAME = "end_content";
    public static String HEAD = "head_value";
    public static String WHEEL = "wheel_value";
    public static String WING = "wing_value";
    public static String FACE = "face_value";
    public static String LIGHT_TYPE = "light_type_value";
    public static String LIGHT_DURATION = "light_duration_value";
    public static String ACTION = "action_value";

    private String head;
    private String wheel;
    private String wing;
    private String face;
    private int lightType;
    private String lightDuration;
    private String action;

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CustomActionBean(Cursor mCursor) {

        if (mCursor == null) {
            return;
        }
        id = mCursor.getInt(mCursor.getColumnIndexOrThrow("_id"));
        head = mCursor.getString(mCursor.getColumnIndexOrThrow(HEAD));
        wheel = mCursor.getString(mCursor.getColumnIndexOrThrow(WHEEL));
        wing = mCursor.getString(mCursor.getColumnIndexOrThrow(WING));
        face = mCursor.getString(mCursor.getColumnIndexOrThrow(FACE));
        lightType = mCursor.getInt(mCursor.getColumnIndexOrThrow(LIGHT_TYPE));
        lightDuration = mCursor.getString(mCursor.getColumnIndexOrThrow(LIGHT_DURATION));
        action = mCursor.getString(mCursor.getColumnIndexOrThrow(ACTION));
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getWheel() {
        return wheel;
    }

    public void setWheel(String wheel) {
        this.wheel = wheel;
    }

    public String getWing() {
        return wing;
    }

    public void setWing(String wing) {
        this.wing = wing;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public int getLight() {
        return lightType;
    }

    public void setLight(int light) {
        this.lightType = light;
    }

    public String getLightDuration() {
        return lightDuration;
    }

    public void setLightDuration(String lightDuration) {
        this.lightDuration = lightDuration;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getLightType() {
        return lightType;
    }

    public void setLightType(int lightType) {
        this.lightType = lightType;
    }

    public CustomActionBean() {
    }

}
