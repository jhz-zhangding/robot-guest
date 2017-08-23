package com.efrobot.guest.speech;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.efrobot.guest.service.UltrasonicService;
import com.efrobot.guest.setting.SettingPresenter;
import com.efrobot.guest.utils.PlaceUtils;
import com.efrobot.guest.utils.PreferencesUtils;
import com.efrobot.guest.utils.TtsUtils;
import com.efrobot.library.RobotState;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.task.NavigationManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by hp on 2017/4/13.
 */
public class AlarmUlService extends Service {

    public static String TAG = AlarmUlService.class.getSimpleName();

    private static AlarmManager startAm, endAm;
    private static PendingIntent startPi, endPi;

    //24小时
    private long Repeat_Time = 1000 * 60 * 60 * 24;
    public static String guestPlace;
    public static Context mContext;
    public static Intent mServiceIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i(TAG, "onStartCommand");
        mContext = this;

        return super.onStartCommand(intent, START_STICKY, startId);
    }

    @Override
    public void onDestroy() {
        L.i(TAG, "onDestroy");
        super.onDestroy();
    }

    private void setCandleTime(String startTime, String endTime) {
        //获取当前毫秒值
        if (startTime != null && !TextUtils.isEmpty(startTime)) {
            long systemTime = System.currentTimeMillis();

            //获取上面设置的如：08点25分的毫秒值
            //开始时间
            Calendar mStartCalendar = getmCalendarFromTime(startTime);
            long selectStartTime = mStartCalendar.getTimeInMillis();

            // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
            if (endTime != null && !TextUtils.isEmpty(endTime)) {
                Calendar mEndCalendar = getmCalendarFromTime(endTime);
                long selectEndTime = mEndCalendar.getTimeInMillis();
                if (systemTime > selectStartTime && systemTime < selectEndTime) {
                    handler.sendEmptyMessageDelayed(1, 3000);
                    mStartCalendar.add(Calendar.DAY_OF_MONTH, 1);
                } else if (systemTime > selectEndTime) {
                    mStartCalendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            } else if (systemTime > selectStartTime) {
                mStartCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒 E");
            L.i(TAG, "时间:" + simpleDateFormat.format(date));

            //AlarmReceiver.class为广播接受者
            Intent intent = new Intent(AlarmUlService.this, AlarmStartReceiver.class);
            startPi = PendingIntent.getBroadcast(AlarmUlService.this, 0, intent, 0);
            //得到AlarmManager实例
            startAm = (AlarmManager) getSystemService(ALARM_SERVICE);
            //重复闹钟
            startAm.setRepeating(AlarmManager.RTC_WAKEUP, mStartCalendar.getTimeInMillis(), Repeat_Time, startPi);
        }

        if (endTime != null && !TextUtils.isEmpty(endTime)) {
            //获取上面设置的如：18点25分的毫秒值
            //结束时间
            Calendar mEndCalendar = getmCalendarFromTime(endTime);
            long selectEndTime = mEndCalendar.getTimeInMillis();

            Intent intentEnd = new Intent(AlarmUlService.this, AlarmEndReceiver.class);
            endPi = PendingIntent.getBroadcast(AlarmUlService.this, 0, intentEnd, 0);
            //得到AlarmManager实例
            endAm = (AlarmManager) getSystemService(ALARM_SERVICE);
            //重复闹钟
            endAm.setRepeating(AlarmManager.RTC_WAKEUP, mEndCalendar.getTimeInMillis(), Repeat_Time, endPi);
        }

    }

    //params 小时:分钟
    private Calendar getmCalendarFromTime(String mTime) {
        //开始时间
        String[] st = mTime.split(":");
        int stHour = Integer.parseInt(st[0]);
        int stMinute = Integer.parseInt(st[1]);
        L.i(TAG, "stHour = " + stHour + "-----stMinute = " + stMinute);

        Calendar mCalendar = Calendar.getInstance();

        //是设置日历的时间，主要是让日历的年月日和当前同步
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        //设置在几点提醒  设置的为13点
        mCalendar.set(Calendar.HOUR_OF_DAY, stHour);
        //设置在几分提醒  设置的为25分
        mCalendar.set(Calendar.MINUTE, stMinute);
        //下面这两个看字面意思也知道
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        return mCalendar;
    }

    //接受到开始任务广播
    public static class AlarmStartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            L.i(TAG, "接收到定时开始任务");
            //开始去指定地点并迎宾
            TtsUtils.sendTts(context.getApplicationContext(), "开始导航");
            handler.sendEmptyMessageDelayed(1, 3000);
        }
    }

    //接受到任务广播
    public static class AlarmEndReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            L.i(TAG, "接收到定时结束任务");
            if (mServiceIntent != null) {
                L.i(TAG, "结束迎宾任务");
                mContext.getApplicationContext().stopService(mServiceIntent);
            }
            //回充电桩
            PlaceUtils.goHome(mContext);
        }
    }

    //取消定时功能
    public static void stopAlarm() {
        //取消警报
        if (startAm != null) {
            if (startPi != null) {
                startAm.cancel(startPi);
            }
        }

        if (endAm != null) {
            if (endPi != null) {
                endAm.cancel(endPi);
            }
        }
        L.i(TAG, "结束定时任务");

        if (mServiceIntent != null) {
            L.i(TAG, "结束迎宾任务");
            mContext.getApplicationContext().stopService(mServiceIntent);
        }
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    startGuest();
                    break;
            }

        }
    };

    ////开始去指定地点并迎宾
    private static void startGuest() {
        // 获取面罩信息 0:关闭
        int maskSwitch = RobotState.getInstance(mContext).getMaskState();
        L.i(TAG, "面罩maskSwitch:" + maskSwitch);
        if (maskSwitch == 0) {

            if (!TextUtils.isEmpty(guestPlace)) {
                PlaceUtils.goGuestPlace(mContext, PlaceUtils.getSelectLocation(mContext, guestPlace), new NavigationManager.OnNavigationStateChangeListener() {
                    @Override
                    public void onNavigationStart() {
                        L.i(TAG, "onNavigationStart");
                    }

                    @Override
                    public void onNavigationPause() {
                        L.i(TAG, "onNavigationPause");
                    }

                    @Override
                    public void onNavigationContinue() {
                        L.i(TAG, "onNavigationContinue");
                    }

                    @Override
                    public void onNavigationStop() {
                        L.i(TAG, "onNavigationStop");
                    }

                    @Override
                    public void onNavigationSuccess() {
                        L.i(TAG, "onNavigationSuccess");
                        if (!UltrasonicService.IsRunning) {
                            mServiceIntent = new Intent(mContext, UltrasonicService.class);
                            mContext.getApplicationContext().startService(mServiceIntent);
                        }

                    }

                    @Override
                    public void onNavigationFail(int reason) {
                        TtsUtils.sendTts(mContext.getApplicationContext(), "导航失败" + reason);
                        L.i(TAG, "onNavigationFail" + reason);
                    }
                });
            } else {
                L.i(TAG, "guestPlace = null");
            }
        } else {
            Toast.makeText(mContext, "请关闭面罩再试", Toast.LENGTH_SHORT).show();
        }
    }

}
