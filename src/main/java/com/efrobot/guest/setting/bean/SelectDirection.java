package com.efrobot.guest.setting.bean;

import android.database.Cursor;

/**
 * Created by zd on 2017/8/29.
 */
public class SelectDirection {

    public static String TABLE_NAME = "ultrasonic_selected_setting";

    public static String ULTRASONIC_ID = "ultrasonicId";
    public static String VALUE = "value";
    public static String TYPE = "type";

    private int ultrasonicId;

    private String value;

    private int type;

    private boolean isSelected;

    public SelectDirection() {
    }

    public SelectDirection(Cursor mCursor) {

        if (mCursor == null) {
            return;
        }
        ultrasonicId = mCursor.getInt(mCursor.getColumnIndexOrThrow(ULTRASONIC_ID));
        value = mCursor.getString(mCursor.getColumnIndexOrThrow(VALUE));
        type = mCursor.getInt(mCursor.getColumnIndexOrThrow(TYPE));
    }



    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getUltrasonicId() {
        return ultrasonicId;
    }

    public void setUltrasonicId(int ultrasonicId) {
        this.ultrasonicId = ultrasonicId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
