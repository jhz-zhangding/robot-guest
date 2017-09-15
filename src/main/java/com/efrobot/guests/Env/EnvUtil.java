package com.efrobot.guests.Env;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhaodekui on 2017/3/31.
 */
public class EnvUtil {

    public static final int VERSION_CODE = 1;

    public static int VersionCode = 2;
    public static String VersionName = VersionCode + getCurrentTime();
    public static boolean IS_DEBUG = true;

    public final static String ULGST001 = "ULGST001";

    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yy:MM:dd HH:mm");
        return sdf.format(new Date());
    }

}
