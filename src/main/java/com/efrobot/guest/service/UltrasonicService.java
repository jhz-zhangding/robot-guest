package com.efrobot.guest.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.efrobot.guest.GuestsApplication;
import com.efrobot.guest.R;
import com.efrobot.guest.bean.Action_Wheel;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.bean.UlPlaceBean;
import com.efrobot.guest.dao.DataManager;
import com.efrobot.guest.dao.UltrasonicDao;
import com.efrobot.guest.setting.SettingActivity;
import com.efrobot.guest.setting.SettingPresenter;
import com.efrobot.guest.utils.ActivityManager;
import com.efrobot.guest.utils.PreferencesUtils;
import com.efrobot.guest.utils.TtsUtils;
import com.efrobot.library.RobotManager;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.library.task.GroupManager;
import com.efrobot.library.task.NavigationManager;
import com.efrobot.library.task.SpeechGroupManager;
import com.efrobot.speechsdk.SpeechManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 迎宾执行功能
 */

public class UltrasonicService extends Service implements RobotManager.OnGetUltrasonicCallBack,
        NavigationManager.OnNavigationStateChangeListener, RobotManager.OnWheelStateChangeListener {

    private String CLOSE_TTS = "com.efrobot.speech.voice.ACTION_TTS";
    private static final String TAG = UltrasonicService.class.getSimpleName();

    public static boolean IsRunning = false;

    private boolean mIsExecute = false;
    private boolean isReceiveUltrasonic = false;
    private String ttsValue = "";

    //超声波数据设置
    private ArrayList<UlPlaceBean> ulPlaceBeans = null;

    private UltrasonicDao mUltrasonic;

    private int conformDistanceNum = 0;  //进入检测范围次数
    private int farDistanceNum = 0;  //离开检测范围次数
    private int NUM_VALUE = 1; //默认进入次数
    private int NUM_FARVALUE = 4; //默认离开次数

    public static boolean IsOpenRepeatLight = true; //灯光开关
    private Map<Integer, Boolean> flagsMap = new HashMap<Integer, Boolean>();
    private int customNumber = 1;
    private List<Integer> customUlData = null;
    private byte byte5 = (byte) 0x00;
    private byte byte4 = (byte) 0x00;

    private GroupManager groupManager;

    private long duration = 1000;

    //离开检测时间
    private int waitTime = 10;

    public static boolean isWelcomeTTsStart = false;

    private SpeechGroupManager mGroupTask;

    private List<ItemsContentBean> itemsStartContents;
    private List<ItemsContentBean> itemsEndContents;

    @Override
    public void onCreate() {
        super.onCreate();
        L.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i(TAG, "onStartCommand");
        IsRunning = true;
        groupManager = RobotManager.getInstance(this.getApplicationContext()).getGroupInstance();
        mGroupTask = SpeechGroupManager.getInstance(RobotManager.getInstance(getApplicationContext()));
        //注册盖子
        registerLiboard();
        //导航监听轮子变化
        RobotManager.getInstance(this).getNavigationInstance().registerOnNavigationStateChangeListener(this);
        RobotManager.getInstance(this).registerOnWheelStateChangeListener(this);
        mUltrasonic = GuestsApplication.from(getApplicationContext()).getUltrasonicDao();
        // 1:开始迎宾 2:结束迎宾
        itemsStartContents = DataManager.getInstance(UltrasonicService.this).queryItem(1);
        itemsEndContents = DataManager.getInstance(UltrasonicService.this).queryItem(2);

        if (itemsStartContents == null || itemsStartContents.size() == 0) {
            showToast("请设置欢迎语");
            return super.onStartCommand(intent, START_STICKY, startId);
        }
        //交流模式
        initGuestData();
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    public void initGuestData() {
        //播放模式
        startPlayMode = PreferencesUtils.getInt(this, SettingActivity.SP_START_PLAY_MODE, 1);
        stopPlayMode = PreferencesUtils.getInt(this, SettingActivity.SP_STOP_PLAY_MODE, 1);
        //交流时间
        String voiceTime = PreferencesUtils.getString(this.getApplicationContext(), SettingPresenter.SP_VOICE_TIME);
        if (voiceTime != null && !voiceTime.isEmpty()) {
            waitTime = Integer.parseInt(voiceTime);
        } else {
            waitTime = 10;
        }
        L.i(TAG, "waitTime=" + waitTime);

        //超声波设置数据
        ulPlaceBeans = mUltrasonic.queryAll();
        if (ulPlaceBeans != null && ulPlaceBeans.size() > 0) {
            for (int i = 0; i < ulPlaceBeans.size(); i++) {
                flagsMap.put(ulPlaceBeans.get(i).getUltrasonicId(), false);
            }
            customUlData = new ArrayList<Integer>();
            for (int i = 0; i < ulPlaceBeans.size(); i++) {
                if (ulPlaceBeans.get(i).getIsOpenValue() == 1) {
                    customUlData.add(ulPlaceBeans.get(i).getUltrasonicId());
                }
            }
            customNumber = customUlData.size();
        }
        //每10秒发送移除睡眠
        han.sendEmptyMessageDelayed(START_TIMER_CLEAR_SLEEP, 1000);
        //是否有设置的探头信息
        if (getCustomUltrasonicData()) {
            try {
                SpeechManager.getInstance().closeSpeechDiscern(getApplicationContext());
                boolean isAutoOpen = PreferencesUtils.getBoolean(getApplicationContext(), SettingPresenter.SP_IS_AUTO_OPEN);
                if (isAutoOpen) {
                    //开始标定模式
                    TtsUtils.sendTts(getApplicationContext(), getString(R.string.init_ultrasonic_hint));
                    mHandle.sendEmptyMessageDelayed(8, 3000);
                } else {
                    //直接开启超声波迎宾
                    L.i(TAG, "开始迎宾");
                    mHandle.sendEmptyMessageDelayed(0, 5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showToast("请设置超声波距离");
        }

    }

    /**
     * 执行脚本监听
     */
    GroupManager.OnGroupTaskExecuteListener groupTaskListener = new GroupManager.OnGroupTaskExecuteListener() {
        @Override
        public void onStart() {
            //开始执行脚本动作

        }

        @Override
        public void onStop() {
            //脚本动作结束

        }
    };


    private boolean isReceiveData = false;

    Handler mHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            L.i(TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case 0:
                    if (!isReceiveData) {
                        L.i(TAG, "Resend open UltrasonicData");
                        //发送探头信息
                        RobotManager.getInstance(getApplicationContext()).registerOnGetUltrasonicCallBack(UltrasonicService.this);
                        sendUserUltrasonic();
                    }
                    break;
                case 1: // 打开常亮灯光
                    L.i(TAG, "Open Light");
                    openRepeatLight();
                    break;
                case 2: //
                    L.i(TAG, "flicker Light");
                    if (IsOpenRepeatLight) {
                        RobotManager.getInstance(getApplicationContext()).getControlInstance().setLightBeltBrightness(0);
                        mHandle.sendEmptyMessageDelayed(6, duration);
                    }
                    break;
                case 6: //
                    if (IsOpenRepeatLight) {
                        RobotManager.getInstance(getApplicationContext()).getControlInstance().setLightBeltBrightness(255);
                        mHandle.sendEmptyMessageDelayed(2, duration);
                    }
                    break;
                case 7:
                    //打开红外
                    sendInfrared();
                    break;
                case 8:
                    RobotManager.getInstance(getApplicationContext()).registerOnGetUltrasonicCallBack(UltrasonicService.this);
                    //超声波初始化
                    sendTestUltrasonic(false);

                    break;
            }
        }
    };

    @Override
    public void onGetUltrasonic(byte[] data) {
        try {
            isReceiveData = true;
            isReceiveUltrasonic = true;
            mHandle.removeMessages(0);
            if (isUltraData(data)) {
//                L.i(TAG, "---------------data--" + Arrays.toString(data));
                byte[] bytes = new byte[customUlData.size() * 4];
                System.arraycopy(data, 5, bytes, 0, customUlData.size() * 4);
                for (int i = 0; i < bytes.length; i++) {
                    if ((i - 3) % 4 == 0) {
                        int valueNG = (bytes[i] & 255) | ((bytes[i - 1] & 255) << 8); // 距离
                        int numberNg = (bytes[i - 2] & 255) | ((bytes[i - 3] & 255) << 8); // 返回的探头编号 0-12


                        int myDistance = getDistanceFromPosition(numberNg); //设置的探头距离
                        int number = getPositionFromNumber(numberNg); //获取自己设置的探头编号
                        L.i(TAG, "numberNg = " + numberNg + "---myDistance = " + myDistance);
                        if (customUlData.contains(getPositionFromNumber(numberNg))) {
                            if (isConformDistance(valueNG, myDistance, number)) {
                                break;
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isTimer = false;

    /**
     * valueNG 探测到的数据
     * distance 用户阈值
     * number 探头编号
     */
    private boolean isConformDistance(int valueNG, int distance, int number) {
        try {
            L.i(TAG, number + "号-----valueNG-" + valueNG + "-----distance-" + distance + "-----numValue----" + NUM_VALUE + "---numFarValue---" + NUM_FARVALUE + "检测到远离次数:" + farDistanceNum);

            if (distance != -1) {

                if (valueNG <= distance) {
                    //跟随动作
                    if (number == 0 || number == 4) {
                        setHeadAction(ORIGIN_HEAD);
                    } else if (number == 1 || number == 5) {
                        setHeadAction(RIGHT_HEAD);
                    } else if (number == 2 || number == 3) {
                        setHeadAction(LEFT_HEAD);
                    }

                    flagsMap.put(number, true);
                    conformDistanceNum++;  //符合设定的距离就记忆次数
                    farDistanceNum = 0;
                    if (conformDistanceNum >= NUM_VALUE) {
                        conformDistanceNum = 0;

                        isTimer = false;
                        han.removeMessages(0);
                        time = 0;

                        if (!mIsExecute) {
                            L.i(TAG, "-----han----mIsExecute=" + mIsExecute);
                            //播放欢迎语  并开启语音识别
                            mIsExecute = true;
                            //执行动作等
                            isWelcomeTTsStart = true;
                            SpeechManager.getInstance().openSpeechDiscern(getApplicationContext());
                            startPlay(START_GUEST_STRING);
                            L.i(TAG, "-------openSpeechDiscern");
                        }
                        return true;
                    }

                } else {
                    //记录距离
                    if (valueNG <= distance) {
                        flagsMap.put(number, true);
                    } else {
                        flagsMap.put(number, false);
                    }


                    farDistanceNum++;
                    L.i(TAG, "开始计时 farDistanceNum:" + farDistanceNum + "---isAllFaraway() = " + isAllFaraway() + "---isTimer = " + isTimer + "---mIsExecute = " + mIsExecute + "---isWelcomeTTsStart=" + isWelcomeTTsStart);
                    //全部检测
                    if (farDistanceNum >= NUM_FARVALUE * customNumber) {
                        //全部检测无人, 开始计时
                        if (!isTimer && mIsExecute && !isWelcomeTTsStart) {
                            isTimer = true;
                            L.i(TAG, "mIsExecute=" + mIsExecute);
                            han.sendEmptyMessageDelayed(START_TIMER_GUEST, 1000);
                        }

                        //离开n秒 检测机制
                        if (time < waitTime) {
                            // 检测到有人
                            if (!isAllFaraway() && mIsExecute) {
                                L.i(TAG, "计时中检测有人:" + isAllFaraway());
                                //需要重新开始计时
                                time = 0;
                                return false;
                            }
                        } else {
                            if (mIsExecute) {
                                startPlay(STOP_GUEST_STRING);
                                han.removeMessages(0);
                                isTimer = false;
                                if (mGroupTask != null) {
                                    mGroupTask.reset();
                                }
                                SpeechManager.getInstance().closeSpeechDiscern(getApplicationContext());
                            }
                            mIsExecute = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private String START_GUEST_STRING = "开始迎宾";
    private String STOP_GUEST_STRING = "结束迎宾";
    private int startPlayMode = 0;
    private int stopPlayMode = 0;
    private int ORDER_PLAY = 0;
    private int RANDOM_PLAY = 1;
    private int mStartCurrentIndex = -1;
    private int mEndCurrentIndex = -1;

    /**
     * type 0：开始迎宾 1：结束迎宾
     */
    private void startPlay(String typeStr) {
        ItemsContentBean itemsContentBean = null;
        if (START_GUEST_STRING.equals(typeStr)) {
            if (startPlayMode == ORDER_PLAY) {
                mStartCurrentIndex++;
                if (mStartCurrentIndex > (itemsStartContents.size() - 1)) {
                    mStartCurrentIndex = 0;
                }
            } else if (startPlayMode == RANDOM_PLAY) {
                mStartCurrentIndex = (int) (Math.random() * ((itemsStartContents.size() - 1)));
            }
            itemsContentBean = itemsStartContents.get(mStartCurrentIndex);
        } else if (STOP_GUEST_STRING.equals(typeStr)) {
            if (stopPlayMode == ORDER_PLAY) {
                mEndCurrentIndex++;
                if (mEndCurrentIndex > (itemsEndContents.size() - 1)) {
                    mEndCurrentIndex = 0;
                }
            } else if (stopPlayMode == RANDOM_PLAY) {
                mEndCurrentIndex = (int) (Math.random() * ((itemsEndContents.size() - 1)));
            }
            itemsContentBean = itemsEndContents.get(mEndCurrentIndex);
        }


        if (!TextUtils.isEmpty(itemsContentBean.getOther())) {
            closeTTs();
            TtsUtils.sendTts(getApplicationContext(), itemsContentBean.getOther());
        }

        if (!TextUtils.isEmpty(itemsContentBean.getLight())) {

        }

        if (!TextUtils.isEmpty(itemsContentBean.getFace())) {

        }

        if (!TextUtils.isEmpty(itemsContentBean.getAction())) {

        }
    }

    private final int START_TIMER_GUEST = 0;
    private final int START_TIMER_CLEAR_SLEEP = 1;
    private int time;
    private Handler han = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_TIMER_GUEST:
                    time++;
                    han.sendEmptyMessageDelayed(START_TIMER_GUEST, 1000);
                    L.i(TAG, "开始计时迎宾time = " + time);
                    break;
                case START_TIMER_CLEAR_SLEEP:
                    try {
                        //正在迎宾
                        SpeechManager.getInstance().removeSpeechState(UltrasonicService.this, 13);
                        SpeechManager.getInstance().removeSpeechState(UltrasonicService.this, 11);
                        if (!mIsExecute) {
                            SpeechManager.getInstance().closeSpeechDiscern(getApplicationContext());
                        }
                        han.sendEmptyMessageDelayed(START_TIMER_CLEAR_SLEEP, 5000);
                        L.i(TAG, "-----removeSpeechState++-");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    //     是否全部超声波都检测不到目标 判断：1次并左右摆动一次
    private boolean isAllFaraway() {
        if (flagsMap != null && flagsMap.size() > 0) {
            for (Integer key : flagsMap.keySet()) {
                if (flagsMap.get(key)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isUltraData(byte[] data) {
        boolean bool = false;
        int cmdStart = (data[0] & 255) << 8 | data[1] & 255;
        int cmdOrder = (data[2] & 255) << 8 | data[3] & 255;
        if (cmdStart == 0xC03) {
            if (cmdOrder == 0x0502) {
                //超声波反馈
                bool = true;
            } else if (cmdOrder == 0x0602) {
                //超声波初始化
                int result = (data[4] & 255);
                L.i(TAG, "超声波=" + result);
                if (result == 0) {
                    //成功
                    TtsUtils.sendTts(getApplicationContext(), "标定成功");
                    showToast("超声波标定成功 \t\tTime: " + getCurrentTime());
                    isReceiveUltrasonic = false;
                    sendUserUltrasonic();
                } else {
//                    失败
                    TtsUtils.sendTts(getApplicationContext(), "标定失败");
                    showToast("超声波标定失败 \t\tTime: " + getCurrentTime());
                }
            }
        }
        return bool;
    }

    private boolean dataTypePerson(byte[] data) {
        int start = (data[0] & 255) << 8 | data[1] & 255;
        if (start == 0x0c03) {
            int order = (data[2] & 255) << 8 | data[3] & 255;
            if (order == 0x0506) {
                return true;
            }
        }
        return false;
    }

    private void reSend() {
        mHandle.sendEmptyMessageDelayed(0, 5000);
    }

    /**
     * 关闭超声波检测
     *
     * @param context 上下文
     */
    public void closeUltrasonic(Context context) {
        L.i(TAG, "Send data to close ultrasonic");
        byte[] data = new byte[12];
        data[0] = (byte) 0x0c;
        data[1] = (byte) 0x03;
        data[2] = (byte) 0x06;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0x00;
        data[5] = (byte) 0x00;
        data[6] = (byte) 0x00;
        data[7] = (byte) 0x00;

        RobotManager.getInstance(context).getCustomTaskInstance().sendByteData(data);

    }

    //打开用户定义的超声波
    private void sendUserUltrasonic() {

        L.i(TAG, "Send user custom data to open ultrasonic");
        byte[] data = new byte[12];
        data[0] = (byte) 0x0c;
        data[1] = (byte) 0x03;
        data[2] = (byte) 0x06;
        data[3] = (byte) 0x03;
        data[4] = byte4;//        data[4] = (byte) 0x1F; 打开全部
        data[5] = byte5;//        data[5] = (byte) 0xFF;
        data[6] = (byte) 0x00;
        data[7] = (byte) 7;
        //开启后8秒左右收到回调
        RobotManager.getInstance(getApplicationContext()).getCustomTaskInstance().sendByteData(data);
        //TODO 新策略
//        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getApplication())).openUltrasonicFeedback(byte4 << 8 | byte5);
        if (!isReceiveUltrasonic) { //是否接受到超声波检测信息
            reSend();
        }

    }

    //超声波初始化
    public void sendTestUltrasonic(boolean isWriteFlash) {

        byte[] data = new byte[11];
        data[0] = (byte) 0x0c;
        data[1] = (byte) 0x03;
        data[2] = (byte) 0x06;
        data[3] = (byte) 0x02;
        data[4] = (byte) 0x1F;
        data[5] = (byte) 0xFF;
        if (isWriteFlash)
            data[6] = (byte) 0x01;
        else
            data[6] = (byte) 0x00;
        RobotManager.getInstance(getApplicationContext()).getCustomTaskInstance().sendByteData(data);
    }

    public String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return (df.format(new Date()));     // new Date()为获取当前系统时间
    }

    //常亮
    private void openRepeatLight() {
        RobotManager.getInstance(getApplicationContext()).getControlInstance().setLightBeltBrightness(255);
        L.i(TAG, "openRepeatLight常亮");
        if (IsOpenRepeatLight) {
            mHandle.sendEmptyMessageDelayed(1, 500);
        }
    }

    //闪烁
    private void openFlickerLight(long duration) {
        this.duration = duration;
        RobotManager.getInstance(getApplicationContext()).getControlInstance().setLightBeltBrightness(255);
        if (IsOpenRepeatLight) {
            mHandle.sendEmptyMessageDelayed(2, duration);
        }
    }

    //关闭
    private void closeRepeatLight() {
        IsOpenRepeatLight = false;
        mHandle.removeMessages(1);
        RobotManager.getInstance(getApplicationContext()).getControlInstance().setLightBeltBrightness(0);
    }

    /**
     * ulId: 0～5对应0、1、7、8、9、10
     * 通过用户设置的No得到探头的数据
     */
    private boolean getCustomUltrasonicData() { // 是否有选择的探头

        List<Byte> byteList5 = new ArrayList<Byte>(); // byte5 前8个
        List<Byte> byteList4 = new ArrayList<Byte>(); // byte4 后5个
        if (customUlData != null && customUlData.size() > 0) {
            for (int i = 0; i < customUlData.size(); i++) {
                if (customUlData.get(i) <= 2) {
                    byteList5.add(getDataFromPosition(customUlData.get(i)));
                }
                if (customUlData.get(i) >= 3) {
                    byteList4.add(getDataFromPosition(customUlData.get(i)));
                }
            }

            for (int i = 0; i < byteList5.size(); i++) {
                byte currentByte5 = byteList5.get(i);
                byte5 |= currentByte5;
            }
            for (int i = 0; i < byteList4.size(); i++) {
                byte currentByte4 = byteList4.get(i);
                byte4 |= currentByte4;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * position 前面6个探头 1～6
     */
    private int getDistanceFromPosition(int numberNg) {
        if (ulPlaceBeans != null && ulPlaceBeans.size() > 0) {
            int position = getPositionFromNumber(numberNg);
            int isSelected = ulPlaceBeans.get(position).getIsOpenValue();
            String distanceValue = ulPlaceBeans.get(position).getDistanceValue();
            //是否选中该探头
            if (isSelected == 1 && !distanceValue.isEmpty()) {
                return Integer.parseInt(distanceValue) * 10;
            }
        }
        return -1;
    }


    /**
     * position 前面6个探头 1～6
     */
    /**
     * byte[5]
     */
    byte mByte1 = (byte) 0x01; // 探头1---0
    byte mByte2 = (byte) 0x02; // 探头2---1
    byte mByte8 = (byte) 0x80; // 探头8---2
    /**
     * byte[4]
     */
    byte mByte9 = (byte) 0x01; // 探头9---3
    byte mByte10 = (byte) 0x02; // 探头10---4
    byte mByte11 = (byte) 0x04; // 探头11---5

    private byte getDataFromPosition(int position) {

        switch (position) {
            case 0:
                return mByte1;
            case 1:
                return mByte2;
            case 2:
                return mByte8;
            case 3:
                return mByte9;
            case 4:
                return mByte10;
            case 5:
                return mByte11;
        }
        return (byte) 0x00;
    }

    //获取对应的pos
    private int getPositionFromNumber(int numberNg) {
        switch (numberNg) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 7:
                return 2;
            case 8:
                return 3;
            case 9:
                return 4;
            case 10:
                return 5;
        }
        return 1;
    }

    private MediaPlayer player;

    private void playAudio(String path) {
        // 从文件系统播放
        player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    player.start();
                    player.setLooping(true);
                }
            });
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isGetInfrared = false;

    private void sendInfrared() {
        byte[] data = new byte[9];
        data[0] = (byte) 0x0c;
        data[1] = (byte) 0x04;
        data[2] = (byte) 0x06;
        data[3] = (byte) 0x02;
        data[4] = (byte) 0x01;
//        L.i(TAG, "startInfrared=" + Arrays.toString(data));
        RobotManager.getInstance(getApplicationContext()).getCustomTaskInstance().sendByteData(data);
        if (!isGetInfrared) {
            mHandle.sendEmptyMessageDelayed(7, 5000);
        }
    }

    /**
     * 创建wheel的json
     */
    private String wheelJson = "";

    private boolean cerateWheelJson(List<Action_Wheel> list_Wheel) {
        Log.e("zhang", "size==" + list_Wheel.size());
        if (list_Wheel.isEmpty())
            return true;
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject2 = new JSONObject();
        JSONObject jsonObject = null;
        JSONObject tmpObj = null;
        int count = list_Wheel.size();
        try {
            for (int i = 0; i < count; i++) {
                tmpObj = new JSONObject();
                jsonObject = new JSONObject();


                if (list_Wheel.get(i).getDirection().equals("无")) {
                    tmpObj.put("direction", "stop");
                } else if (list_Wheel.get(i).getDirection().equals("前进")) {
                    tmpObj.put("direction", "front");
                } else if (list_Wheel.get(i).getDirection().equals("后退")) {
                    tmpObj.put("direction", "back");
                } else if (list_Wheel.get(i).getDirection().equals("左转")) {
                    tmpObj.put("direction", "left");
                } else if (list_Wheel.get(i).getDirection().equals("右转")) {
                    tmpObj.put("direction", "right");
                }


                tmpObj.put("directionspinner", list_Wheel.get(i).getDirection());

                if (!list_Wheel.get(i).getAngle().equals("无")) {
                    tmpObj.put("angle", list_Wheel.get(i).getAngle());
                }
                if (!list_Wheel.get(i).getSpeed().equals("无")) {
                    tmpObj.put("speed", list_Wheel.get(i).getSpeed());
                }
                jsonObject.put("wheel", tmpObj);
                if (!list_Wheel.get(i).getNext_action_time().equals("无")) {
                    jsonObject.put("next_action_time", list_Wheel.get(i).getNext_action_time());
                } else {
                    wheelJson = "";
                    showToast("轮子动作时间不能为空");
                    return false;
                }
                jsonArray.put(jsonObject);
            }
            if (jsonArray.length() > 0) {
                jsonObject2.put("actions", jsonArray);
                wheelJson = jsonObject2.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //退出播放自定义
    private void stopCustomMode() {
        ActivityManager.getInstance().finishActivity();
        if (null != player) {
            player.reset();
            player.stop();
        }
    }

    //导航监听
    @Override
    public void onNavigationStart() {
        //停止动作
        RobotManager.getInstance(getApplicationContext()).getNavigationInstance().stop();
    }

    @Override
    public void onNavigationPause() {

    }

    @Override
    public void onNavigationContinue() {

    }

    @Override
    public void onNavigationStop() {

    }

    @Override
    public void onNavigationSuccess() {

    }

    @Override
    public void onNavigationFail(int reason) {

    }

    //双轮监听
    @Override
    public void onWheelMoving(int part) {
        RobotManager.getInstance(getApplicationContext()).getWheelInstance().stop();
        L.i(TAG, "轮子启动");
        RobotManager.getInstance(getApplicationContext()).getGroupInstance().stop();
        RobotManager.getInstance(getApplicationContext()).getSpeechGroupManager().stop();
        RobotManager.getInstance(getApplicationContext()).getDanceInstance().stop();
    }

    @Override
    public void onWheelStop(int part) {

    }

    @Override
    public void onError(int part, int errorCode) {

    }

    private void registerLiboard() {
        IntentFilter dynamic_filter = new IntentFilter();
        dynamic_filter.addAction(ROBOT_MASK_CHANGE);            //添加动态广播的Action
        registerReceiver(lidBoardReceive, dynamic_filter);
    }

    /**
     * 监听盖子状态
     */
    public final static String ROBOT_MASK_CHANGE = "android.intent.action.MASK_CHANGED";
    public final static String KEYCODE_MASK_ONPROGRESS = "KEYCODE_MASK_ONPROGRESS"; //开闭状态
    public final static String KEYCODE_MASK_CLOSE = "KEYCODE_MASK_CLOSE"; //关闭面罩
    public final static String KEYCODE_MASK_OPEN = "KEYCODE_MASK_OPEN";  //打开面罩
    private BroadcastReceiver lidBoardReceive = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ROBOT_MASK_CHANGE.equals(intent.getAction())) {
                boolean isOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);
                boolean isOpening = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
                if (isOpen || isOpening) {
                    closeAllCallBack();
                    stopSelf();
                    unregisterReceiver(lidBoardReceive);
                }

            }
        }
    };


    /**
     * 关闭TTS
     */
    private void closeTTs() {
        Intent intent = new Intent(CLOSE_TTS);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("modelType", "stopTTS");
        intent.putExtra("data", simpleMapToJsonStr(map));
        L.i(TAG, "data = " + simpleMapToJsonStr(map));
        sendBroadcast(intent);
    }

    public String simpleMapToJsonStr(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "null";
        }
        String jsonStr = "{";
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            jsonStr += "\"" + key + "\":\"" + map.get(key) + "\",";
        }
        jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
        jsonStr += "}";
        return jsonStr;
    }

    String leftHeadJson = "{\n" +
            "  \"actions\": [\n" +
            "    {\n" +
            "      \"head\": {\n" +
            "        \"angle\": 75,\n" +
            "        \"directionspinner\": \"左转\",\n" +
            "        \"direction\": \"move\"\n" +
            "      },\n" +
            "      \"next_action_time\": \"3000\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    String rightHeadJson = "{\n" +
            "  \"actions\": [\n" +
            "    {\n" +
            "      \"head\": {\n" +
            "        \"angle\": 165,\n" +
            "        \"directionspinner\": \"右转\",\n" +
            "        \"direction\": \"move\"\n" +
            "      },\n" +
            "      \"next_action_time\": \"3000\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    String originHeadJson = "{\n" +
            "  \"actions\": [\n" +
            "    {\n" +
            "      \"head\": {\n" +
            "        \"angle\": 120,\n" +
            "        \"directionspinner\": \"归位\",\n" +
            "        \"direction\": \"move\"\n" +
            "      },\n" +
            "      \"next_action_time\": \"1000\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private final int RIGHT_HEAD = 0;
    private final int LEFT_HEAD = 1;
    private final int ORIGIN_HEAD = 2;

    private int lastHead = -1;

    private void setHeadAction(int direc) {
        switch (direc) {
            case RIGHT_HEAD:
                if (direc != lastHead) {
                    groupManager.execute(rightHeadJson);
                }
                break;
            case LEFT_HEAD:
                if (direc != lastHead) {
                    groupManager.execute(leftHeadJson);
                }
                break;
            case ORIGIN_HEAD:
                if (direc != lastHead) {
                    groupManager.execute(originHeadJson);
                }
                break;
        }
        lastHead = direc;
    }

    private void showToast(String content) {
        RobotToastUtil.getInstance(getApplicationContext()).showToast(content);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeAllCallBack();
    }

    private void closeAllCallBack() {
        IsRunning = false;
        L.i(TAG, "onDestroy");
//        closeUltrasonic(this);// 关闭超声波，暂不关
        RobotManager.getInstance(this).unRegisterOnGetUltrasonicCallBack();
        //TODO 新策略
//        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getApplication())).closeUltrasonicFeedback();
//        RobotManager.getInstance(this).unRegisterOnGetInfraredCallBack();
        RobotManager.getInstance(this).getNavigationInstance().unRegisterOnNavigationStateChangeListener(this);
        RobotManager.getInstance(this).unRegisterOnWheelStateChangeListener();
        closeRepeatLight();

        if (han != null) {
            han.removeMessages(START_TIMER_GUEST);
            han.removeMessages(START_TIMER_CLEAR_SLEEP);
        }
        if (mHandle != null) {
            mHandle.removeMessages(0);
            mHandle.removeMessages(1);
            mHandle.removeMessages(2);
            mHandle.removeMessages(6);
            mHandle.removeMessages(7);
            mHandle.removeMessages(8);
        }
    }

}
