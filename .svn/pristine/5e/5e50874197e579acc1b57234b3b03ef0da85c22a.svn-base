package com.efrobot.guest.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guest.bean.SettingBean;
import com.efrobot.guest.db.DbHelper;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/3.
 */
public class SettingDao {

    private final DbHelper dbOpenHelper;
    private SQLiteDatabase db;

    public SettingDao(DbHelper dataBase) {
        this.dbOpenHelper = dataBase;
    }

    public void insert(final SettingBean beanArrayList) {
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
                    values.put(SettingBean.DISTANCE, beanArrayList.getDistanceValue());
                    db.insert(SettingBean.TABLE_NAME, null, values);

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


    public ArrayList<SettingBean> queryAll() {

        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(SettingBean.TABLE_NAME, null, null, null, null, null, null);
                    ArrayList<SettingBean> list = new ArrayList<SettingBean>();
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            SettingBean mMembers = new SettingBean(mCursor);
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


    public void update(int id, String remark) {
        ContentValues cv = new ContentValues();
        cv.put(SettingBean.DISTANCE, remark);
        db.update(SettingBean.TABLE_NAME, cv, "_id = ?", new String[]{String.valueOf(id)});
    }

}
