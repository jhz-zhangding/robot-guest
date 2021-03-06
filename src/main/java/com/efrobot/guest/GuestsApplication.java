package com.efrobot.guest;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.efrobot.guest.bean.CustomActionBean;
import com.efrobot.guest.bean.UlPlaceBean;
import com.efrobot.guest.dao.ActionBaseDao;
import com.efrobot.guest.dao.ExchangeModeDao;
import com.efrobot.guest.dao.RemarkDao;
import com.efrobot.guest.dao.SettingDao;
import com.efrobot.guest.dao.UltrasonicDao;
import com.efrobot.guest.db.DbHelper;
import com.efrobot.guest.main.MainActivity;
import com.efrobot.guest.service.UltrasonicService;
import com.efrobot.guest.setting.SettingPresenter;
import com.efrobot.guest.utils.GuestDes3Util;
import com.efrobot.guest.utils.PreferencesUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    private void initData() {
        ArrayList<CustomActionBean> listBean = new ArrayList<CustomActionBean>();
        CustomActionBean bean = new CustomActionBean();
        bean.setFace("");
        bean.setHead("");
        bean.setWing("");
        // 进入常亮
        int lightType = 1;
        bean.setLight(lightType);
        listBean.add(bean);
        getActionDao().insertAction(listBean);

        ArrayList<CustomActionBean> listBean1 = new ArrayList<CustomActionBean>();
        CustomActionBean bean1 = new CustomActionBean();
        bean1.setFace("");
        bean1.setHead("");
        bean1.setWing("");
        // 退出关闭
        int lightTypeEnd = 0;
        bean1.setLight(lightTypeEnd);
        listBean1.add(bean1);
        getActionDao().insertEndAction(listBean1);

        //设置超声波
        UlPlaceBean ulPlaceBeen = new UlPlaceBean();
        ulPlaceBeen.setUltrasonicId(0);
        ulPlaceBeen.setIsOpenValue(1);
        ulPlaceBeen.setDistanceValue("100");
        getUltrasonicDao().insert(ulPlaceBeen);

        UlPlaceBean ulPlaceBeen1 = new UlPlaceBean();
        ulPlaceBeen1.setUltrasonicId(1);
        ulPlaceBeen1.setIsOpenValue(1);
        ulPlaceBeen1.setDistanceValue("100");
        getUltrasonicDao().insert(ulPlaceBeen1);

        UlPlaceBean ulPlaceBeen2 = new UlPlaceBean();
        ulPlaceBeen2.setUltrasonicId(2);
        ulPlaceBeen2.setIsOpenValue(1);
        ulPlaceBeen2.setDistanceValue("100");
        getUltrasonicDao().insert(ulPlaceBeen2);

        PreferencesUtils.putBoolean(getContext().getApplicationContext(), "FIRST_INIT_DATA", false);
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

    RemarkDao mDataDao;

    public RemarkDao getRemarkDao() {
        if (mDataDao == null) {
            mDataDao = new RemarkDao(getDataBase());
        }
        return mDataDao;
    }


    SettingDao mSettingDao;

    public SettingDao getSettingDao() {
        if (mSettingDao == null) {
            mSettingDao = new SettingDao(getDataBase());
        }
        return mSettingDao;
    }


    UltrasonicDao ultrasonicDao;

    public UltrasonicDao getUltrasonicDao() {
        if (ultrasonicDao == null) {
            ultrasonicDao = new UltrasonicDao(getDataBase());
        }
        return ultrasonicDao;
    }

    ActionBaseDao actionBaseDao;

    public ActionBaseDao getActionDao() {
        if (actionBaseDao == null)
            actionBaseDao = new ActionBaseDao(getDataBase());
        return actionBaseDao;
    }

    ExchangeModeDao exchangeModeDao;

    public ExchangeModeDao getModeDao() {
        if (exchangeModeDao == null)
            exchangeModeDao = new ExchangeModeDao(getDataBase());
        return exchangeModeDao;
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

            CharSequence timestamp = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
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
