package com.efrobot.guests;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.efrobot.guests.bean.UlDistanceBean;
import com.efrobot.guests.dao.SelectedDao;
import com.efrobot.guests.dao.UltrasonicDao;
import com.efrobot.guests.db.DbHelper;
import com.efrobot.guests.main.MainActivity;
import com.efrobot.guests.player.MediaPlayDialog;
import com.efrobot.guests.service.UltrasonicService;
import com.efrobot.guests.utils.GuestDes3Util;
import com.efrobot.guests.utils.PreferencesUtils;
import com.efrobot.library.mvp.utils.L;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/2.
 */
public class GuestsApplication extends Application {
    private DbHelper mDbHelper;

    private String TAG = getClass().getSimpleName();
    private String PACKGE_NAME = "com.efrobot.guest";
    private String mRogotSnNumber;

    private boolean isPrintCrashLog = true;

    public UltrasonicService ultrasonicService;

    public MediaPlayDialog mediaPlayDialog;

    @Override
    public void onCreate() {
        if (isPrintCrashLog) {
            Thread.setDefaultUncaughtExceptionHandler(new MythouCrashHandler(this));
        }
        super.onCreate();
        L.i(TAG, "into application");

        //设置迎宾默认数据
        if (PreferencesUtils.getBoolean(getContext().getApplicationContext(), "FIRST_INIT_DATA", true)) {
            initData();
        }

        String[] propertys = {"ro.boot.serialno", "ro.serialno"};
        for (String key : propertys) {
//          String v = android.os.SystemProperties.get(key);
            mRogotSnNumber = getAndroidOsSystemProperties(key);
            Log.e("", "get " + key + " : " + mRogotSnNumber);
        }
        if (!mRogotSnNumber.isEmpty()) {
            String encryptNumber = null;
            try {
                encryptNumber = GuestDes3Util.encode(mRogotSnNumber);
                L.e("3Des加密", "SN号码：" + mRogotSnNumber + "     3Des_password = " + encryptNumber + "end");
                if (!encryptNumber.isEmpty()) {
                    PreferencesUtils.putString(this, MainActivity.SP_GUEST_PASSWORD, encryptNumber.trim());
                } else {
                    Toast.makeText(getContext(), "加密失败", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Map<Integer, String> ultrasonicMaps = new HashMap<Integer, String>();
    private void initData() {
        //设置默认开启的超声波
        ultrasonicMaps.put(0, "100");
        ultrasonicMaps.put(1, "100");
        ultrasonicMaps.put(2, "100");
        ultrasonicMaps.put(6, "100");
        ultrasonicMaps.put(7, "100");

        ultrasonicMaps.put(8, "");
        ultrasonicMaps.put(9, "");
        ultrasonicMaps.put(10, "");
        ultrasonicMaps.put(11, "");
        ultrasonicMaps.put(12, "");
        saveUserSetting(ultrasonicMaps);

        PreferencesUtils.putBoolean(getContext().getApplicationContext(), "FIRST_INIT_DATA", false);
    }

    /**
     * 保存用户设置距离
     */
    private void saveUserSetting(Map<Integer, String> ultrasonicMap) {
        UltrasonicDao ultrasonicDao = GuestsApplication.from(this).getUltrasonicDao();
        for (Map.Entry entry : ultrasonicMap.entrySet()) {
            UlDistanceBean ulDistanceBean = new UlDistanceBean();
            int ultrasonicId = (Integer) entry.getKey();
            String distanceValue = (String) entry.getValue();
            ulDistanceBean.setUltrasonicId(ultrasonicId);
            ulDistanceBean.setDistanceValue(distanceValue);
            if (ultrasonicDao.isExits(ultrasonicId))
                ultrasonicDao.update(0, ultrasonicId, distanceValue);
            else
                ultrasonicDao.insert(ulDistanceBean);
        }
    }

    public GuestsApplication getContext() {
        return this;
    }


    /**
     * 初始化
     *
     * @param context Application
     * @return Application
     */
    public static GuestsApplication from(Context context) {
        if (context != null)
            return (GuestsApplication) context.getApplicationContext();
        else return null;
    }

    public void setUltrasonicService(UltrasonicService ultrasonicService) {
        this.ultrasonicService = ultrasonicService;
    }

    /**
     * 获取数据库操作类
     *
     * @return 数据库操作类
     */

    public synchronized DbHelper getDataBase() {
        if (mDbHelper == null)
            mDbHelper = new DbHelper(getApplicationContext());
        return mDbHelper;
    }

    UltrasonicDao ultrasonicDao;

    public UltrasonicDao getUltrasonicDao() {
        if (ultrasonicDao == null) {
            ultrasonicDao = new UltrasonicDao(getDataBase());
        }
        return ultrasonicDao;
    }

    SelectedDao selectedDao;

    public SelectedDao getSelectedDao() {
        if (selectedDao == null)
            selectedDao = new SelectedDao(getDataBase());
        return selectedDao;
    }


    /***
     * 播放DIY视频
     */
    public void playGuestVideoByPath(String path) {

        if (!TextUtils.isEmpty(path)) {
            mediaPlayDialog = new MediaPlayDialog(getContext());
            mediaPlayDialog.setFilePath(path);
            mediaPlayDialog.show();
        }

    }

    public void dismissGuestVideo() {
        if (mediaPlayDialog != null) {
            mediaPlayDialog.dismiss();
            mediaPlayDialog = null;
        }

    }


    static Method systemProperties_get = null;

    static String getAndroidOsSystemProperties(String key) {
        String ret;
        try {
            systemProperties_get = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
            if ((ret = (String) systemProperties_get.invoke(null, key)) != null)
                return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return "";
    }

    /**
     * 程序崩溃本地记录工具类
     */
    /**
     * 记录Bug
     */
    String logpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/efrobot/guests";

    class MythouCrashHandler implements Thread.UncaughtExceptionHandler {
        private Thread.UncaughtExceptionHandler defaultUEH;
        private Context context;

        //构造函数，获取默认的处理方法
        public MythouCrashHandler(Context context) {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            this.context = context;
        }

        // 这个接口用来处理我们的异常信息
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            //获取跟踪的栈信息，除了系统栈信息，还把手机型号、系统版本、编译版本的唯一标示
            StackTraceElement[] trace = ex.getStackTrace();
            StackTraceElement[] trace2 = new StackTraceElement[trace.length + 3];
            System.arraycopy(trace, 0, trace2, 0, trace.length);
            trace2[trace.length] = new StackTraceElement("Android", "MODEL",
                    android.os.Build.MODEL, -1);
            trace2[trace.length + 1] = new StackTraceElement("Android", "VERSION",
                    android.os.Build.VERSION.RELEASE, -1);
            trace2[trace.length + 2] = new StackTraceElement("Android",
                    "FINGERPRINT", android.os.Build.FINGERPRINT, -1);
            //追加信息，因为后面会回调默认的处理方法
            ex.setStackTrace(trace2);
            ex.printStackTrace(printWriter);
            //把上面获取的堆栈信息转为字符串，打印出来
            String stacktrace = result.toString();
            printWriter.close();
            // 这里把刚才异常堆栈信息写入SD卡的Log日志里面
            String path;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                path = logpath + "/log";
            else
                path = context.getFilesDir().toString();

            File file = new File(path);
            if (!file.exists())
                file.mkdirs();
            writeLog(stacktrace, path + "/mythou");
            // 下面两个只执行在前面一行的代码
            defaultUEH.uncaughtException(thread, ex);
        }

        //写入Log信息的方法，写入到SD卡里面
        private void writeLog(String log, String name) {

            CharSequence timestamp = DateFormat.format("yyyyMMdd_kkmmss",
                    System.currentTimeMillis());
            String filename = name + "_" + timestamp + ".log";

            File file = new File(filename);
            if (!file.exists()) {
                File pa = file.getParentFile();
                if (!pa.exists()) {
                    pa.mkdirs();
                }

            }


            try {
                FileOutputStream stream = new FileOutputStream(filename);
                OutputStreamWriter output = new OutputStreamWriter(stream);
                BufferedWriter bw = new BufferedWriter(output);
                //写入相关Log到文件
                bw.write(log);
                bw.newLine();
                bw.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
