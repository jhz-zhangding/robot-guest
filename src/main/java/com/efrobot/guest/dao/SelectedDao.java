package com.efrobot.guest.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guest.db.DbHelper;
import com.efrobot.guest.setting.bean.SelectDirection;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/30.
 * 用户超声波设置
 */
public class SelectedDao {

    private final DbHelper dbOpenHelper;
    private SQLiteDatabase db;

    public SelectedDao(DbHelper dataBase) {
        this.dbOpenHelper = dataBase;
    }

    public void insert(final SelectDirection beanArrayList) {
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
                    values.put(SelectDirection.ULTRASONIC_ID, beanArrayList.getUltrasonicId());
                    values.put(SelectDirection.VALUE, beanArrayList.getValue());
                    values.put(SelectDirection.TYPE, beanArrayList.getType());
                    db.insert(SelectDirection.TABLE_NAME, null, values);

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


    public ArrayList<SelectDirection> queryAll() {

        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(SelectDirection.TABLE_NAME, null, null, null, null, null, null);
                    ArrayList<SelectDirection> list = new ArrayList<SelectDirection>();
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            SelectDirection mMembers = new SelectDirection(mCursor);
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
                    String existSql = "select * from " + SelectDirection.TABLE_NAME + " where " + SelectDirection.ULTRASONIC_ID + "=?";
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

    public void update(SelectDirection selectDirection) {
        ContentValues cv = new ContentValues();
        cv.put(SelectDirection.VALUE, selectDirection.getValue());
        cv.put(SelectDirection.TYPE, selectDirection.getType());
        db.update(SelectDirection.TABLE_NAME, cv, SelectDirection.ULTRASONIC_ID + "=?", new String[]{String.valueOf(selectDirection.getUltrasonicId())});
    }

    public ArrayList<SelectDirection> queryOneType(int type) {

        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(SelectDirection.TABLE_NAME, null, "type=? ", new String[]{type + ""}, null, null, null);
                    ArrayList<SelectDirection> list = new ArrayList<SelectDirection>();
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            SelectDirection mMembers = new SelectDirection(mCursor);
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

    public void delete(int id) {
        db.delete(SelectDirection.TABLE_NAME, "ultrasonicId = ?", new String[]{String.valueOf(id)});
    }

}
