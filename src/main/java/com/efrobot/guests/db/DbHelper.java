package com.efrobot.guests.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.guests.bean.FaceAndActionEntity;
import com.efrobot.guests.bean.ItemsContentBean;
import com.efrobot.guests.bean.UlDistanceBean;
import com.efrobot.guests.setting.bean.SelectDirection;
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
        //创建用户设置超声波距离表
        sqLiteDatabase.execSQL("create table if not exists " + UlDistanceBean.TABLE_NAME
                + " (_id integer primary key autoincrement," + UlDistanceBean.ULTRASONIC_ID + " text," + UlDistanceBean.ISOPEN + " integer," + UlDistanceBean.DISTANCE + " text)");
        //创建用户设置超声波开启表
        sqLiteDatabase.execSQL("create table if not exists " + SelectDirection.TABLE_NAME
                + " (" + SelectDirection.ULTRASONIC_ID + " integer," + SelectDirection.VALUE + " text," + SelectDirection.TYPE + " text)");

        try {
            TableUtils.createTable(connectionSource, FaceAndActionEntity.class);
            TableUtils.createTable(connectionSource, ItemsContentBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

    }

}
