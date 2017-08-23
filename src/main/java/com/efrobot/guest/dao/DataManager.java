package com.efrobot.guest.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guest.GuestsApplication;
import com.efrobot.guest.bean.FaceAndActionEntity;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.provider.GuestProvider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zd on 2017/8/18.
 */
public class DataManager {

    private static DataManager instance;

    private SQLiteDatabase db = null;

    /**
     * 项目中的内容
     */
    public static String CONTENT_TABLE = GuestProvider.RobotContentColumns.TABLE_NAME;

    /**
     * 动作表
     */
    private static String ACTION_TABLE = GuestProvider.RobotActionColumns.TABLE_NAME;

    public static Context mContext;


    public static DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager();
        }
        mContext = context;
        return instance;
    }

    /**
     * 动作和表情  1动作 2表情
     *
     * @param context
     * @param path
     */
    public void actionAndFace(Context context, String path, int type) {
        ArrayList<FaceAndActionEntity> beanList = new ArrayList<FaceAndActionEntity>();
        try {
            /**
             * 读取本地资源   <>读取assets的文件</>
             */
            InputStream in = context.getResources().getAssets().open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;


            while ((line = reader.readLine()) != null) {
                String item[] = line.split("##");
                int len = item.length;
                if (len > 2) {
                    beanList.add(new FaceAndActionEntity(item[0], item[1], item[2]));
                } else if (len > 1) {
                    beanList.add(new FaceAndActionEntity(item[0], item[1]));
                }
            }
            if (type == 1) {
                //插入动作
                deleteAction();
                insertAction(beanList);
            }
//            else if (type == 2) {
//                //插入表情
//                dao.insertFace(beanList);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除保存的动作数据
     */
    public void deleteAction() {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        db.delete(ACTION_TABLE, null, null);
        closDb(db);
    }

    /**
     * 插入动作
     *
     * @param beans 动作类
     */
    public void insertAction(ArrayList<FaceAndActionEntity> beans) {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        if (beans == null || beans.isEmpty())
            return;
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
                values.put(GuestProvider.RobotActionColumns.ACRIONNUM, beans.get(i).index);
                values.put(GuestProvider.RobotActionColumns.ACRIONNAME, beans.get(i).content);
                values.put(GuestProvider.RobotActionColumns.ACRIONTIME, beans.get(i).time);
                db.insert(GuestProvider.RobotActionColumns.TABLE_NAME, null, values);
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
            /**
             * 关闭数据库
             */
            closDb(db);
        }
    }

    /**
     * 查询所有的动作
     *
     * @return 返回动作数据集合
     */
    public ArrayList<FaceAndActionEntity> queryAllAction() {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        ArrayList<FaceAndActionEntity> beans = new ArrayList<FaceAndActionEntity>();
        Cursor c = null;
        try {
            c = db.query(ACTION_TABLE, null, null, null, null, null, null);
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    beans.add(new FaceAndActionEntity(c));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
            closDb(db);
        }
        return beans;
    }

    /**
     * 查询所插入项的内容
     *
     * @return 返回动作数据集合
     */
    public ArrayList<ItemsContentBean> queryAllContent() {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        ArrayList<ItemsContentBean> beans = new ArrayList<ItemsContentBean>();
        Cursor c = null;
        try {
            c = db.query(CONTENT_TABLE, null, null, null, null, null, null);
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    beans.add(new ItemsContentBean(c));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
            closDb(db);
        }
        return beans;
    }

    /**
     * 插入项的内容
     *
     * @param bean 内容实体类
     */
    public void insertContent(ItemsContentBean bean) {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        if (bean == null)
            return;
        ContentValues values = new ContentValues();
        values.put("itemNum", bean.getItemNum());
        values.put("sport", bean.getSport());
        values.put("face", bean.getFace());
        values.put("action", bean.getAction());
        values.put("light", bean.getLight());
        values.put("other", bean.getOther());
        values.put("media", bean.getMedia());
        values.put("time", bean.getTime());
        values.put("music", bean.getMusic());
        values.put("head", bean.getHead());
        values.put("wheel", bean.getWheel());
        values.put("wing", bean.getWing());
        values.put("openLightTime", bean.getOpenLightTime());
        values.put("flickerLightTime", bean.getFlickerLightTime());
        values.put("faceTime", bean.getFaceTime());
        values.put("actionSystemTime", bean.getActionSystemTime());
        values.put("maxTime", bean.getMaxTime());
        values.put("startAppAction", bean.getStartAppAction());
        values.put("startAppName", bean.getStartAppName());
        db.insert(CONTENT_TABLE, null, values);
        closDb(db);
    }

    /**
     * 修改某个项目的某一条内容
     *
     * @param bean
     */
    public void upateContent(ItemsContentBean bean) {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        if (bean == null)
            return;
        ContentValues values = new ContentValues();
        values.put("itemNum", bean.getItemNum());
        values.put("sport", bean.getSport());
        values.put("face", bean.getFace());
        values.put("action", bean.getAction());
        values.put("light", bean.getLight());
        values.put("other", bean.getOther());
        values.put("media", bean.getMedia());
        values.put("time", bean.getTime());
        values.put("music", bean.getMusic());
        values.put("head", bean.getHead());
        values.put("wheel", bean.getWheel());
        values.put("wing", bean.getWing());
        values.put("openLightTime", bean.getOpenLightTime());
        values.put("flickerLightTime", bean.getFlickerLightTime());
        values.put("faceTime", bean.getFaceTime());
        values.put("actionSystemTime", bean.getActionSystemTime());
        values.put("maxTime", bean.getMaxTime());
        values.put("startAppAction", bean.getStartAppAction());
        values.put("startAppName", bean.getStartAppName());
        db.update(CONTENT_TABLE, values, "_id=? ", new String[]{bean.getId() + ""});
    }

    public void updateItem(ItemsContentBean bean) {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        if (bean == null)
            return;
        ContentValues values = new ContentValues();
        values.put("maxTime", bean.getMaxTime());
        values.put("media", bean.getMedia());
        values.put("music", bean.getMusic());
        db.update(CONTENT_TABLE, values, "_id=? ", new String[]{bean.getId() + ""});
    }

    /**
     * 1:开始迎宾 2:结束迎宾
     */
    public ArrayList<ItemsContentBean> queryItem(int itemNum) {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        ArrayList<ItemsContentBean> beans = new ArrayList<ItemsContentBean>();
        Cursor c = null;
        try {
            c = db.query(CONTENT_TABLE, null, "itemNum=? ", new String[]{itemNum + ""}, null, null, null);
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    beans.add(new ItemsContentBean(c));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
            closDb(db);
        }
        return beans;
    }

    /**
     * 根据id删除某个项目下的某个内容
     *
     * @param id
     */
    public void deleteContentById(int id) {
        db = GuestsApplication.from(mContext).getDataBase().getWritableDatabase();
        db.delete(CONTENT_TABLE, "_id = ? ", new String[]{id + ""});
        closDb(db);
    }

    private void closDb(SQLiteDatabase db) {
//        db.close();
    }

}
