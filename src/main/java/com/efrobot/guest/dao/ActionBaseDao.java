package com.efrobot.guest.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guest.bean.CustomActionBean;
import com.efrobot.guest.db.DbHelper;

import java.util.ArrayList;

/**
 * Created by zhaodekui on 2017/3/17.
 */
public class ActionBaseDao {

    private DbHelper helper;
    private SQLiteDatabase db;


    public ActionBaseDao(DbHelper dataBase) {
        this.helper = dataBase;
    }

    /**
     * 插入动作
     *
     * @param beans 动作类
     */
    public void insertAction(ArrayList<CustomActionBean> beans) {
        if (beans == null || beans.isEmpty())
            return;
        db = helper.getWritableDatabase();
        /**
         * 开启事务
         */
        db.beginTransaction();
        try {
            int len = beans.size();
            /**
             * 插入动作
             */
            for (int i = 0; i < len; i++) {
                ContentValues values = new ContentValues();
                values.put(CustomActionBean.HEAD, beans.get(i).getHead());
                values.put(CustomActionBean.WHEEL, beans.get(i).getWheel());
                values.put(CustomActionBean.WING, beans.get(i).getWing());
                values.put(CustomActionBean.FACE, beans.get(i).getFace());
                values.put(CustomActionBean.LIGHT_TYPE, beans.get(i).getLight());
                values.put(CustomActionBean.LIGHT_DURATION, beans.get(i).getLightDuration());
                values.put(CustomActionBean.ACTION, beans.get(i).getAction());
                db.insert(CustomActionBean.TABLE_NAME, null, values);
            }

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

    // 结束界面插入
    public void insertEndAction(ArrayList<CustomActionBean> beans) {
        if (beans == null || beans.isEmpty())
            return;
        db = helper.getWritableDatabase();
        /**
         * 开启事务
         */
        db.beginTransaction();
        try {
            int len = beans.size();
            /**
             * 插入动作
             */
            for (int i = 0; i < len; i++) {
                ContentValues values = new ContentValues();
                values.put(CustomActionBean.HEAD, beans.get(i).getHead());
                values.put(CustomActionBean.WHEEL, beans.get(i).getWheel());
                values.put(CustomActionBean.WING, beans.get(i).getWing());
                values.put(CustomActionBean.FACE, beans.get(i).getFace());
                values.put(CustomActionBean.LIGHT_TYPE, beans.get(i).getLight());
                values.put(CustomActionBean.LIGHT_DURATION, beans.get(i).getLightDuration());
                values.put(CustomActionBean.ACTION, beans.get(i).getAction());
                db.insert(CustomActionBean.TABLE_END_NAME, null, values);
            }

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

    /**
     * 查询开始迎宾所有的动作
     *
     * @return 返回动作数据集合
     */
    public ArrayList<CustomActionBean> queryAllAction() {
        ArrayList<CustomActionBean> list = new ArrayList<CustomActionBean>();
        if (helper != null) {
            synchronized (helper) {
                db = helper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(CustomActionBean.TABLE_NAME, null, null, null, null, null, null);
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            CustomActionBean mMembers = new CustomActionBean(mCursor);
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
        return list;
    }

    /**
     * 查询结束设置所有的动作
     *
     * @return 返回动作数据集合
     */
    public ArrayList<CustomActionBean> queryAllEndAction() {
        ArrayList<CustomActionBean> list = new ArrayList<CustomActionBean>();
        if (helper != null) {
            synchronized (helper) {
                db = helper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    mCursor = db.query(CustomActionBean.TABLE_END_NAME, null, null, null, null, null, null);
                    if (mCursor != null) {

                        while (mCursor.moveToNext()) {
                            CustomActionBean mMembers = new CustomActionBean(mCursor);
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
        return list;
    }


    //判断是否已经存在该数据
    public boolean isExits(String type, String content) {
        boolean result = false;
        if (helper != null) {
            synchronized (helper) {
                db = helper.getWritableDatabase();
                /**
                 * 开启事务
                 */
                db.beginTransaction();
                Cursor mCursor = null;
                try {
                    String existSql = "select * from " + CustomActionBean.TABLE_NAME + " where " + type + "=?";
                    mCursor = db.rawQuery(existSql, new String[]{content});
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

    public void delete(String tableName) {
        if (helper != null) {
            db = helper.getWritableDatabase();
            db.delete(tableName, null, null);
        }
    }


}
