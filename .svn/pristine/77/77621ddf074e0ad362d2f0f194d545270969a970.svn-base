package com.efrobot.guest.bean;

import android.database.Cursor;

import com.j256.ormlite.field.DatabaseField;

/**
 * 表情和动作Bean
 * Created by Administrator on 2016/2/18.
 */
public class FaceAndActionEntity {
    @DatabaseField(generatedId = true, columnName = "_id")
    public int id;
    @DatabaseField(columnName = "actionNum")
    public String index;//id
    @DatabaseField(columnName = "actionName")
    public String content;//内容
    @DatabaseField(columnName = "actionTime")
    public String time;//时间

    FaceAndActionEntity(){
        super();
    }
    public FaceAndActionEntity(String index, String content) {
        this.index = index;
        this.content = content;
    }

    public FaceAndActionEntity(String index, String content, String time) {
        this.index = index;
        this.content = content;
        this.time = time;
    }

    public FaceAndActionEntity(Cursor c) {
        this.index = c.getString(c.getColumnIndexOrThrow("actionNum"));
        this.content = c.getString(c.getColumnIndexOrThrow("actionName"));
        this.time = c.getString(c.getColumnIndexOrThrow("actionTime"));
    }

}
