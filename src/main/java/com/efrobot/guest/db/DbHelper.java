package com.efrobot.guest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guest.bean.AddCustomMode;
import com.efrobot.guest.bean.CustomActionBean;
import com.efrobot.guest.bean.FaceAndActionEntity;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.bean.RemarkBean;
import com.efrobot.guest.bean.SettingBean;
import com.efrobot.guest.bean.UlPlaceBean;
import com.efrobot.library.mvp.utils.L;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Administrator on 2017/3/3.
 */
public class DbHelper extends OrmLiteSqliteOpenHelper {
    public final String TAG = this.getClass().getSimpleName();
    private Context context;
    private static int version = 1;
    private static String DB_NAME = "GUESTS";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, version);
        this.context = context;
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        sqLiteDatabase.execSQL("create table if not exists " + RemarkBean.TABLE_NAME
                + " (_id integer primary key autoincrement," + RemarkBean.REMARK + " text)");

        sqLiteDatabase.execSQL("create table if not exists " + SettingBean.TABLE_NAME
                + " (_id integer primary key autoincrement," + SettingBean.DISTANCE + " text)");

        //创建用户设置超声波表
        sqLiteDatabase.execSQL("create table if not exists " + UlPlaceBean.TABLE_NAME
                + " (_id integer primary key autoincrement," + UlPlaceBean.ULTRASONIC_ID + " text," + UlPlaceBean.ISOPEN + " integer," + UlPlaceBean.DISTANCE + " text)");
        //创建动作表
        sqLiteDatabase.execSQL(getCreatContentSql());
        //结束动作表
        sqLiteDatabase.execSQL(getCreatEndActionSql());
        //创建交流模式信息表
        sqLiteDatabase.execSQL(getCreatExchangeModeSql());

        try {
            TableUtils.createTable(connectionSource, FaceAndActionEntity.class);
            TableUtils.createTable(connectionSource, ItemsContentBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        if (version == 2) {
            try {
                TableUtils.createTable(connectionSource, FaceAndActionEntity.class);
                TableUtils.createTable(connectionSource, ItemsContentBean.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建动作内容表语句
     *
     * @return
     */
    public static String getCreatContentSql() {
        return "CREATE TABLE IF NOT EXISTS " + CustomActionBean.TABLE_NAME +
                "(_id  integer primary key autoincrement," +
                CustomActionBean.HEAD + " text," +
                CustomActionBean.WHEEL + " text," +
                CustomActionBean.WING + " text," +
                CustomActionBean.FACE + " text," +
                CustomActionBean.LIGHT_TYPE + " integer," +
                CustomActionBean.LIGHT_DURATION + " text," +
                CustomActionBean.ACTION + " text)";
    }

    /**
     * 结束动作内容表语句
     *
     * @return
     */
    public static String getCreatEndActionSql() {
        return "CREATE TABLE IF NOT EXISTS " + CustomActionBean.TABLE_END_NAME +
                "(_id  integer primary key autoincrement," +
                CustomActionBean.HEAD + " text," +
                CustomActionBean.WHEEL + " text," +
                CustomActionBean.WING + " text," +
                CustomActionBean.FACE + " text," +
                CustomActionBean.LIGHT_TYPE + " integer," +
                CustomActionBean.LIGHT_DURATION + " text," +
                CustomActionBean.ACTION + " text)";
    }

    /**
     * 结束动作内容表语句
     *
     * @return
     */
    public static String getCreatExchangeModeSql() {
        return "CREATE TABLE IF NOT EXISTS " + AddCustomMode.TABLE_NAME +
                "(_id  integer primary key autoincrement," +
                AddCustomMode.MUSIC + " text," +
                AddCustomMode.MEDIA + " text," +
                AddCustomMode.IMAGE + " text)";
    }

}
