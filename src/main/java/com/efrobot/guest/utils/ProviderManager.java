package com.efrobot.guest.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/2/18.
 */
public class ProviderManager {

    private Context context;

    private static ProviderManager manager;

    private ProviderManager(Context context) {
        this.context = context;
    }

    public synchronized static ProviderManager getInstance(Context context) {
        if (manager == null) {
            manager = new ProviderManager(context);
        }
        return manager;
    }

    Uri faceUri = Uri.parse("content://com.efrobot.speech.qa/speechface");

    Uri actionUri = Uri.parse("content://com.efrobot.speech.qa/speechaction");

    /**
     * 查询所有的表情
     *
     * @return
     */
    public HashMap<String, String> queryAllFace() {


        //根据alarmId  查询出表情
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(faceUri, null, null, null, null);
            if (cursor != null) {
                HashMap<String, String> entities = new HashMap<String, String>();
                while (cursor.moveToNext()) {
                    String num = cursor.getString(cursor.getColumnIndexOrThrow("faceNum"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("faceName"));
                    entities.put(num, name);
                }
                return entities;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    /**
     * 查询所有的动作
     *
     * @return
     */
    public HashMap<String, String> queryAllAction() {


        //根据alarmId  查询出此定时找你数据
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(actionUri, null, null, null, null);
            if (cursor != null) {
                HashMap<String, String> entities = new HashMap<String, String>();
                while (cursor.moveToNext()) {
                    String num = cursor.getString(cursor.getColumnIndexOrThrow("actionNum"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("actionName"));
                    entities.put(num, name);
                }
                return entities;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    /**
     * 读取表情数据
     */
    HashMap<String, String> mFaceList;
    public HashMap<String, String> readFaceData() {
        if (mFaceList == null || mFaceList.isEmpty())
            mFaceList = queryAllFace();
        return mFaceList;
    }


}
