package com.efrobot.guest.Env;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhaodekui on 2017/3/31.
 */
public class EnvUtil {

    public static int VersionCode = 2;
    public static String VersionName = VersionCode + getCurrentTime();
    public static boolean IS_DEBUG = true;

    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yy:MM:dd HH:mm");
        return sdf.format(new Date());
    }

}
