package com.efrobot.guest.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guest.bean.UlPlaceBean;
import com.efrobot.guest.db.DbHelper;

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

    public void insert(final UlPlaceBean beanArrayList) {
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
                    values.put(UlPlaceBean.ISOPEN, beanArrayList.getIsOpenValue());
                    values.put(UlPlaceBean.ULTRASONIC_ID, beanArrayList.getUltrasonicId());
                    values.put(UlPlaceBean.DISTANCE, beanArrayList.getDistanceValue());
                    db.insert(UlPlaceBean.TABLE_NAME, null, values);

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


    public ArrayList<UlPlaceBean> queryAll() {

        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(UlPlaceBean.TABLE_NAME, null, null, null, null, null, null);
                    ArrayList<UlPlaceBean> list = new ArrayList<UlPlaceBean>();
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            UlPlaceBean mMembers = new UlPlaceBean(mCursor);
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
        boolean result = false ;
        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    String existSql = "select * from "+UlPlaceBean.TABLE_NAME+" where " + UlPlaceBean.ULTRASONIC_ID + "=?";
                    mCursor = db.rawQuery(existSql, new String[]{ulId + ""});
                    result = null != mCursor && mCursor.moveToFirst() ;
                    db.setTransactionSuccessful();
                }finally {
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
        ContentValues cv = new ContentValues();
        cv.put(UlPlaceBean.ISOPEN, isOpen);
        cv.put(UlPlaceBean.DISTANCE, openDistance);
        db.update(UlPlaceBean.TABLE_NAME, cv, UlPlaceBean.ULTRASONIC_ID + "=?", new String[]{String.valueOf(ulId)});
    }

    public void delete(int id) {
        db.delete(UlPlaceBean.TABLE_NAME, "UlPlaceBean.ULTRASONIC_ID = ?", new String[]{String.valueOf(id)});
    }

}
