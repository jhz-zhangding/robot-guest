package com.efrobot.guest.bean;

import android.database.Cursor;

/**
 * Created by zhaodekui on 2017/3/28.
 */
public class AddCustomMode {

    public static String TABLE_NAME = "exchange_mode";

    public static String MUSIC = "music_value";
    public static String MEDIA = "media_value";
    public static String IMAGE = "image_value";

    private String music;
    private String media;
    private String image;

    private int id;

    public AddCustomMode(Cursor mCursor) {

        if (mCursor == null) {
            return;
        }
        id = mCursor.getInt(mCursor.getColumnIndexOrThrow("_id"));
        music = mCursor.getString(mCursor.getColumnIndexOrThrow(MUSIC));
        media = mCursor.getString(mCursor.getColumnIndexOrThrow(MEDIA));
        image = mCursor.getString(mCursor.getColumnIndexOrThrow(IMAGE));
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public AddCustomMode() {

    }
}
