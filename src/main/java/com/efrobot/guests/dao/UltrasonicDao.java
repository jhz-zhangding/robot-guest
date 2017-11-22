package com.efrobot.guests.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guests.bean.UlDistanceBean;
import com.efrobot.guests.db.DbHelper;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/3.
 * 用户超声波设置
 */
public class UltrasonicDao {

    private final DbHelper dbOpenHelper;
    private SQLiteDatabase db;

    public UltrasonicDao(DbHelper dataBase) {
        this.dbOpenHelper = dataBase;
    }

    public void insert(final UlDistanceBean beanArrayList) {
        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                try {
                    /**
                     * 插入问题
                     */
                    ContentValues values = new ContentValues();
                    values.put(UlDistanceBean.ISOPEN, beanArrayList.getIsOpenValue());
                    values.put(UlDistanceBean.ULTRASONIC_ID, beanArrayList.getUltrasonicId());
                    values.put(UlDistanceBean.DISTANCE, beanArrayList.getDistanceValue());
                    db.insert(UlDistanceBean.TABLE_NAME, null, values);

                    /**
                     * 设置批量插入成功
                     */
                    db.setTransactionSuccessful();
                } finally {
                    /**
                     * 结束事务
                     */
                    db.endTransaction();
                }
            }
        }
    }


    public ArrayList<UlDistanceBean> queryAll() {

        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(UlDistanceBean.TABLE_NAME, null, null, null, null, null, null);
                    ArrayList<UlDistanceBean> list = new ArrayList<UlDistanceBean>();
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            UlDistanceBean mMembers = new UlDistanceBean(mCursor);
                            list.add(mMembers);
                        }
                    }

                    /**
                     * 设置批量插入成功
                     */
                    db.setTransactionSuccessful();
                    return list;
                } finally {
                    if (mCursor != null) {
                        mCursor.close();
                    }
                    /**
                     * 结束事务
                     */
                    db.endTransaction();
                }
            }

        }
        return null;
    }

    //判断是否已经存在该数据
    public boolean isExits(int ulId) {
        boolean result = false;
        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    String existSql = "select * from " + UlDistanceBean.TABLE_NAME + " where " + UlDistanceBean.ULTRASONIC_ID + "=?";
                    mCursor = db.rawQuery(existSql, new String[]{ulId + ""});
                    result = null != mCursor && mCursor.moveToFirst();
                    db.setTransactionSuccessful();
                } finally {
                    /**
                     * 结束事务
                     */
                    if (mCursor != null) {
                        mCursor.close();
                    }
                    db.endTransaction();
                }
            }
        }

        return result;
    }

    public void update(int isOpen, int ulId, String openDistance) {
        if (dbOpenHelper != null) db = dbOpenHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(UlDistanceBean.ISOPEN, isOpen);
        cv.put(UlDistanceBean.DISTANCE, openDistance);
        db.update(UlDistanceBean.TABLE_NAME, cv, UlDistanceBean.ULTRASONIC_ID + "=?", new String[]{String.valueOf(ulId)});
    }

    public void delete(int id) {
        if (dbOpenHelper != null) db = dbOpenHelper.getWritableDatabase();
        db.delete(UlDistanceBean.TABLE_NAME, UlDistanceBean.ULTRASONIC_ID + "=?", new String[]{String.valueOf(id)});
    }

}
