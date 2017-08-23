package com.efrobot.guest.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guest.bean.RemarkBean;
import com.efrobot.guest.db.DbHelper;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/3.
 */
public class RemarkDao {

    private final DbHelper dbOpenHelper;
    private SQLiteDatabase db;

    public RemarkDao(DbHelper dataBase) {
        this.dbOpenHelper = dataBase;
    }

    public void insert(final RemarkBean beanArrayList) {
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
                    values.put(RemarkBean.REMARK, beanArrayList.getRemarkValue());
                    db.insert(RemarkBean.TABLE_NAME, null, values);

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


    public ArrayList<RemarkBean> queryAll() {

        if (dbOpenHelper != null) {
            synchronized (dbOpenHelper) {
                db = dbOpenHelper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(RemarkBean.TABLE_NAME, null, null, null, null, null, null);
                    ArrayList<RemarkBean> list = new ArrayList<RemarkBean>();
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            RemarkBean mMembers = new RemarkBean(mCursor);
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
        cv.put(RemarkBean.REMARK, remark);
        db.update(RemarkBean.TABLE_NAME, cv, "_id = ?", new String[]{String.valueOf(id)});
    }

}
