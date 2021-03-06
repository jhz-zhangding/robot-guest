package com.efrobot.guest.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guest.bean.AddCustomMode;
import com.efrobot.guest.db.DbHelper;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/3.
 */
public class ExchangeModeDao {

    private final DbHelper dbOpenHelper;
    private SQLiteDatabase db;

    public ExchangeModeDao(DbHelper dataBase) {
        this.dbOpenHelper = dataBase;
    }

    public void insert(final AddCustomMode beanArrayList) {
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
                    values.put(AddCustomMode.MUSIC, beanArrayList.getMusic());
                    values.put(AddCustomMode.MEDIA, beanArrayList.getMedia());
                    values.put(AddCustomMode.IMAGE, beanArrayList.getImage());
                    db.insert(AddCustomMode.TABLE_NAME, null, values);

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


    public ArrayList<AddCustomMode> queryAll() {

        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(AddCustomMode.TABLE_NAME, null, null, null, null, null, null);
                    ArrayList<AddCustomMode> list = new ArrayList<AddCustomMode>();
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            AddCustomMode mMembers = new AddCustomMode(mCursor);
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
    public boolean isExits(int id) {
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
                    String existSql = "select * from "+AddCustomMode.TABLE_NAME+" where " + "_id" + "=?";
                    mCursor = db.rawQuery(existSql, new String[]{id + ""});
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

    //删除全部
    public void delete() {
        db.delete(AddCustomMode.TABLE_NAME, null , null);
    }

}
