package com.efrobot.guest.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.efrobot.guest.bean.Location;
import com.efrobot.library.RobotManager;
import com.efrobot.library.task.NavigationManager;

import java.util.ArrayList;

/**
 * Created by zhaodekui on 2017/3/29.
 */
public class PlaceUtils {

    public static String GO_HOME = "com.efrobot.action.GO_TO_CHARGING";

    public static ArrayList<Location> query(Context mContext) {
        ArrayList<Location> locationList = new ArrayList<Location>();
        Uri uri = Uri.parse("content://com.efrobot.services.common/location");
        try {
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            while (cursor.moveToNext()) {

                String locationName = null;
                int location_name = cursor.getColumnIndex("location_name");
                if (location_name > 0) {
                    locationName = cursor.getString(location_name);
                }

                String locationX = null;
                int location_x = cursor.getColumnIndex("location_x");
                if (location_name > 0) {
                    locationX = cursor.getString(location_x);
                }

                String locationY = null;
                int location_y = cursor.getColumnIndex("location_y");
                if (location_y > 0) {
                    locationY = cursor.getString(location_y);
                }

                String locationType = null;
                int location_type = cursor.getColumnIndex("location_type");
                if (location_type > 0) {
                    locationType = cursor.getString(location_type);
                }

                String locationAngle = null;
                int location_angle = cursor.getColumnIndex("location_angle");
                if (location_angle > 0) {
                    locationAngle = cursor.getString(location_angle);
                }

                Location location = new Location(locationName, locationType, locationX, locationY, locationAngle);
                locationList.add(location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locationList;
    }


    //获取坐标
    public static Location getSelectLocation(Context mContext, String place) {
        ArrayList<Location> locationList = query(mContext);
        if (locationList != null && locationList.size() > 0) {
            for (int i = 0; i < locationList.size(); i++) {
                if (locationList.get(i).getLocation_name().equals(place)) {
                    return locationList.get(i);
                }
            }

        }
        Toast.makeText(mContext, "获取地点失败", Toast.LENGTH_SHORT).show();
        Log.i("AlarmUlService", "获取地点失败");
        return null;
    }

    //开始去
    public static void goGuestPlace(Context mContext, Location location, NavigationManager.OnNavigationStateChangeListener mOnNavigationStateChangeListener) {
        if (location != null) {
            Toast.makeText(mContext, "开始去" + location.getLocation_name(), Toast.LENGTH_SHORT).show();
            Log.i("AlarmUlService", "开始去" + location.getLocation_name());
            NavigationManager navigationManager = RobotManager.getInstance(mContext).getNavigationInstance();
            navigationManager.startNavigation(Float.parseFloat(location.getLocation_x()),
                    Float.parseFloat(location.getLocation_y()),
                    Float.parseFloat(location.getLocation_angle()),
                    mOnNavigationStateChangeListener);
        }
    }

    public static void goHome(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(GO_HOME);
        mContext.sendBroadcast(intent);
    }

}
