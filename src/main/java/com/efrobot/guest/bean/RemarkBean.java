package com.efrobot.guest.bean;

import android.database.Cursor;

/**
 * Created by Administrator on 2017/3/3.
 */
public class RemarkBean {

    public static String TABLE_NAME = "remark";
    public static String REMARK = "remark_value";

    private String remarkValue;

    private int id;

    public String getRemarkValue() {
        return remarkValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRemarkValue(String remarkValue) {
        this.remarkValue = remarkValue;
    }

    public RemarkBean(Cursor mCursor) {

        if (mCursor == null) {
            return;
        }
        id = mCursor.getInt(mCursor.getColumnIndexOrThrow("_id"));
        remarkValue = mCursor.getString(mCursor.getColumnIndexOrThrow(REMARK));
    }

    public RemarkBean() {
    }
}
