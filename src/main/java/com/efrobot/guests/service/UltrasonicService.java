package com.efrobot.guests.service;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.guests.Env.EnvUtil;
import com.efrobot.guests.Env.SpContans;
import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.R;
import com.efrobot.guests.bean.ItemsContentBean;
import com.efrobot.guests.bean.UlDistanceBean;
import com.efrobot.guests.dao.DataManager;
import com.efrobot.guests.dao.SelectedDao;
import com.efrobot.guests.dao.UltrasonicDao;
import com.efrobot.guests.setting.SettingActivity;
import com.efrobot.guests.setting.bean.SelectDirection;
import com.efrobot.guests.utils.BitmapUtils;
import com.efrobot.guests.utils.CustomHintDialog;
import com.efrobot.guests.utils.DatePickerUtils;
import com.efrobot.guests.utils.FileUtils;
import com.efrobot.guests.utils.MusicPlayer;
import com.efrobot.guests.utils.PreferencesUtils;
import com.efrobot.guests.utils.TtsUtils;
import com.efrobot.library.OnRobotStateChangeListener;
import com.efrobot.library.RobotManager;
import com.efrobot.library.RobotState;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.library.task.GroupManager;
import com.efrobot.library.task.NavigationManager;
import com.efrobot.speechsdk.SpeechManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 迎宾执行功能
 */

public class UltrasonicService extends Service implements RobotManager.OnGetUltrasonicCallBack,
        NavigationManager.OnNavigationStateChangeListener, RobotManager.OnWheelStateChangeListener, OnRobotStateChangeListener, CheckTimeUtils.OnCheckTimeListener
//        , RobotManager.OnUltrasonicOccupyStatelistener
// OnRobotStateChangeListener
{

    private static final String TAG = UltrasonicService.class.getSimpleName();

    public static boolean IsRunning = false;

    private boolean mIsExecute = false;
    private boolean isReceiveUltrasonic = false;
    /**
     * 是否标定结束
     */
    private boolean isInitFinish = true;

    //超声波数据设置
    private ArrayList<UlDistanceBean> ulDistanceBeen = null;

    private UltrasonicDao ultrasonicDao;
    private SelectedDao selectedDao;

    private int farDistanceNum = 0;  //离开检测范围次数
    private int NUM_VALUE = 1; //默认进入次数
    private int NUM_FARVALUE = 4; //默认离开次数
    private int waitTime = 3;//离开检测时间

    public static boolean IsOpenRepeatLight = true; //灯光开关
    private Map<Integer, Boolean> flagsMap = new HashMap<>();
    private int customNumber = 1;
    private List<Integer> customUlData = null;
    private byte byte5 = (byte) 0x00;
    private byte byte4 = (byte) 0x00;

    private GroupManager groupManager;

    public static boolean isWelcomeTTsStart = false;

    private List<SelectDirection> leftSelectDirections;
    private List<SelectDirection> rightSelectDirections;

    private List<Integer> leftSelectNum = new ArrayList<>();
    private List<Integer> rightSelectNum = new ArrayList<>();

    private List<ItemsContentBean> itemsLeftContents;
    private List<ItemsContentBean> itemsRightContents;
    private List<ItemsContentBean> itemsEndContents;
    private String light;
    private Dialog pictureDialog;

    private MusicPlayer musicPlayer;

    //语音说话 0.27秒
    private final long wordSpeed = 270;

    private Calendar mCalendar;

    private boolean isNeedOpenSpeech;

    private boolean isAutoSettingDistance = false;

    private boolean isOpenDataTestDialog = false;

    /**
     * 20分钟未迎宾标定一次
     */
    private final long calibraitonTime = 20 * 60 * 1000;

    private final int CALIBRATION_20_MIN = 21;

    /**
     * 10分钟超声波数据一直不变，自动标定一次
     */
    private final long ultrasonicDataExectionTime = 10 * 60 * 1000;

    private final int DATA_EXECTION_10_MIN = 22;

    private CheckTimeUtils checkTimeUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        L.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i(TAG, "onStartCommand");
        if (!TtsUtils.isCanUseGuest(this)) {
            return -1;
        }
        isNeedOpenSpeech = PreferencesUtils.getBoolean(this, SpContans.AdvanceContans.SP_GUEST_NEED_SPEECH, true);
        isAutoSettingDistance = PreferencesUtils.getBoolean(this, SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, true);
        isOpenDataTestDialog = PreferencesUtils.getBoolean(this, SpContans.SP_OPEN_TEST_DIALOG, false);

        GuestsApplication.from(this).setUltrasonicService(this);
        checkTimeUtils = new CheckTimeUtils();
        groupManager = RobotManager.getInstance(this.getApplicationContext()).getGroupInstance();
        IsRunning = true;
        mCalendar = Calendar.getInstance();
//        RobotManager.getInstance(this).registerUltrasonicOccupylistener(this);
        initListener();

        initAllOpenData();

        initGuestData();

        playLaunchSound();

        return super.onStartCommand(intent, START_STICKY, startId);
    }

    private void initGuestData() {
        /*********是否禁止轮子*********/
//        isOpenWheel = PreferencesUtils.getBoolean(this, SpContans.AdvanceContans.SP_GUEST_OPEN_WHEEL, true);
//        if (!isOpenWheel) {
//            WheelActionUtils.getInstance(this).rememberRobotWheel();
//            WheelActionUtils.getInstance(this).closeWheelAction();
//        }

        musicPlayer = new MusicPlayer(null);
        /**开始迎宾item**/
        itemsLeftContents = DataManager.getInstance(UltrasonicService.this).queryItem(1);
        itemsRightContents = DataManager.getInstance(UltrasonicService.this).queryItem(2);
        itemsEndContents = DataManager.getInstance(UltrasonicService.this).queryItem(3);
        if (itemsLeftContents == null || itemsLeftContents.size() == 0) {
            showToast("请设置欢迎语");
            return;
        }

        //超声波方向数据
        selectedDao = GuestsApplication.from(getApplicationContext()).getSelectedDao();
        int currentType = PreferencesUtils.getInt(this, EnvUtil.SP_CURRENT_TYPE, 2);
        leftSelectDirections = selectedDao.queryOneType(1);
        rightSelectDirections = selectedDao.queryOneType(2);
        if (leftSelectDirections.size() == 0 && rightSelectDirections.size() == 0) {
            showToast("请设置迎宾方向");
            return;
        } else {
            if (currentType == 1) {
                for (int i = 0; i < leftSelectDirections.size(); i++) {
                    leftSelectNum.add(leftSelectDirections.get(i).getUltrasonicId());
                }
            } else if (currentType == 2) {
                for (int i = 0; i < leftSelectDirections.size(); i++) {
                    leftSelectNum.add(leftSelectDirections.get(i).getUltrasonicId());
                }
                for (int i = 0; i < rightSelectDirections.size(); i++) {
                    rightSelectNum.add(rightSelectDirections.get(i).getUltrasonicId());
                }
            }
        }

        /*********超声波设置数据******/
        ultrasonicDao = GuestsApplication.from(getApplicationContext()).getUltrasonicDao();
        customUlData = new ArrayList<Integer>();
        ulDistanceBeen = ultrasonicDao.queryAll();
        for (int i = ulDistanceBeen.size() - 1; i >= 0; i--) {
            if (TextUtils.isEmpty(ulDistanceBeen.get(i).getDistanceValue())) {
                ulDistanceBeen.remove(i);
            } else {
                flagsMap.put(ulDistanceBeen.get(i).getUltrasonicId(), false);
                customUlData.add(ulDistanceBeen.get(i).getUltrasonicId());
            }
        }
        initDistanceMap(ulDistanceBeen);
        customNumber = customUlData.size();

        /*********是否自动标定*********/
        Boolean isAutoInitUl = PreferencesUtils.getBoolean(this, SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION, true);

        /*********迎宾延迟时间*********/
        waitTime = PreferencesUtils.getInt(this, SpContans.AdvanceContans.SP_GUEST_DELAY_TIME, 3);

        /*********播放模式*************/
        startLeftPlayMode = PreferencesUtils.getInt(this, SettingActivity.SP_LEFT_PLAY_MODE, 0);
        startRightPlayMode = PreferencesUtils.getInt(this, SettingActivity.SP_RIGHT_PLAY_MODE, 0);
        stopPlayMode = PreferencesUtils.getInt(this, SettingActivity.SP_RIGHT_PLAY_MODE, 0);


        //每10秒发送移除睡眠
        han.sendEmptyMessageDelayed(START_TIMER_CLEAR_SLEEP, 3000);
        //是否有设置的探头信息
        if (getCustomUltrasonicData()) {
            try {
                SpeechManager.getInstance().closeSpeechDiscern(getApplicationContext());
                {
                    //是否先初始化超声波
                    L.i(TAG, "开始迎宾 isAutoInitUl = " + isAutoInitUl);
                    if (isAutoInitUl) {
                        isInitFinish = false;
                        mHandle.sendEmptyMessageDelayed(INIT_ULTRASONIC, 5000);
                    } else {
                        mHandle.sendEmptyMessageDelayed(OPEN_USER_ULTRASONIC, 5000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showToast("请设置超声波距离");
        }

    }

    private void playLaunchSound() {
        musicPlayer.playResource(this, R.raw.launch, new MusicPlayer.OnMusicCompletionListener() {
            @Override
            public void onCompletion(boolean isPlaySuccess) {

            }

            @Override
            public void onPrepare(int mDuration) {

            }
        });
    }

    private void playStartSound() {
        musicPlayer.playResource(this, R.raw.start, new MusicPlayer.OnMusicCompletionListener() {
            @Override
            public void onCompletion(boolean isPlaySuccess) {

            }

            @Override
            public void onPrepare(int mDuration) {

            }
        });
    }

    private void initListener() {
        RobotManager.getInstance(this).registerHeadKeyStateChangeListener(this);
        RobotManager.getInstance(this).getNavigationInstance().registerOnNavigationStateChangeListener(this);
        RobotManager.getInstance(this).registerOnWheelStateChangeListener(this);
        //注册面罩广播
        registerLiboard();
    }

    /**
     * 执行脚本监听
     */
    GroupManager.OnGroupTaskExecuteListener groupTaskListener = new GroupManager.OnGroupTaskExecuteListener() {
        @Override
        public void onStart() {
            //开始执行脚本动作
            currentCount++;
        }

        @Override
        public void onStop() {
            showTip("执行动作结束");
            if (actionList.size() > 0) {
                if (currentCount > actionList.size() - 1) {
                    isActionFinish = true;
                    openSpeechDiscern();
                } else {
                    L.i("执行动作", "currentCount = " + currentCount);
                    if (mHandle != null)
                        mHandle.sendEmptyMessage(PLAY_MORE_ACTION);
                }
            }

        }
    };

    private final int OPEN_USER_ULTRASONIC = 0;
    private final int OPEN_LIGHT_ALWAYS = 1;
    private final int OPEN_INFRARED = 7;
    private final int INIT_ULTRASONIC = 8;

    private final int PLAY_MORE_ACTION = 100;
    private final int LIGHT_END = 101;
    private final int LIGHT_OPEN = 102;
    private final int LIGHT_CLOSE = 103;
    private final int TTS_FINISH = 104;
    private final int LIGHT_ALWAYS_OPEN = 105;
    public final int VIDEO_FINISH = 106;
    public final int MUSIC_NEED_SAY = 107;
    private boolean isCloseLight = true;

    public Handler mHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            L.i(TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case OPEN_USER_ULTRASONIC:
                    if (!isReceiveUltrasonic) {
                        L.i(TAG, "Resend open UltrasonicData");
                        //发送探头信息
                        RobotManager.getInstance(getApplicationContext()).registerOnGetUltrasonicCallBack(UltrasonicService.this);
                        sendUserUltrasonic();
                    }
                    break;
                case OPEN_LIGHT_ALWAYS: // 打开常亮灯光
                    L.i(TAG, "Open Light");
                    openRepeatLight();
                    break;
                case OPEN_INFRARED:
                    //打开红外
                    sendInfrared();
                    break;
                case INIT_ULTRASONIC:
                    TtsUtils.sendTts(UltrasonicService.this, "请您不要靠近ROBOT_NICKNAME_VALUE，正在初始化超声波数据");
                    RobotManager.getInstance(getApplicationContext()).registerOnGetUltrasonicCallBack(UltrasonicService.this);
                    //超声波初始化
                    sendTestUltrasonic(false);
                    break;
                case PLAY_MORE_ACTION:
                    checkAction(actionList.get(currentCount));
                    break;
                case LIGHT_OPEN:
                    //开 灯带
                    L.e("LIGHT_TAG", "灯带开---------------");
                    RobotManager.getInstance(UltrasonicService.this).getControlInstance().setLightBeltBrightness(240);
                    if (light.equals("2")) {
                        sendEmptyMessageDelayed(LIGHT_CLOSE, 250);
                    }
                    break;
                case LIGHT_END:
                    showTip("执行灯带结束");
                    light = "-1";
                    removeMessages(LIGHT_OPEN);
                    removeMessages(LIGHT_CLOSE);
                    RobotManager.getInstance(UltrasonicService.this).getControlInstance().setLightBeltBrightness(0);
                    openSpeechDiscern();
                    break;
                case LIGHT_CLOSE:
                    //关 灯带
                    L.e("LIGHT_TAG", "灯带关---------------");
                    RobotManager.getInstance(UltrasonicService.this).getControlInstance().setLightBeltBrightness(0);
                    if (light.equals("2")) {
                        sendEmptyMessageDelayed(LIGHT_OPEN, 250);
                    }
                    break;
                case LIGHT_ALWAYS_OPEN:
                    if (!isCloseLight) {
                        RobotManager.getInstance(UltrasonicService.this).getControlInstance().setLightBeltBrightness(240);
                        sendEmptyMessageDelayed(LIGHT_ALWAYS_OPEN, 800);
                    }
                    break;
                case TTS_FINISH:
                    if (isWelcomeTTsStart) {
                        ttsEnd();
                        isWelcomeTTsStart = false;
                    }
                    break;
                case VIDEO_FINISH:
                    isMediaFinish = true;
                    openSpeechDiscern();
                    break;
                case MUSIC_NEED_SAY:
                    TtsUtils.sendTts(UltrasonicService.this, " ");
                    break;
                case 1110:
                    int id = msg.arg1;
                    int distance = msg.arg2;
                    showDataDialog(id, distance);
                    break;
                case CALIBRATION_20_MIN:
                    TtsUtils.sendTts(UltrasonicService.this, "为了保证迎宾的正常使用，ROBOT_NICKNAME_VALUE自检开始");
                    RobotManager.getInstance(getApplicationContext()).registerOnGetUltrasonicCallBack(UltrasonicService.this);
                    //超声波初始化
                    sendTestUltrasonic(false);
                    break;
            }
        }
    };


    private Dialog dataDialog;
    private TextView addDataText1, addDataText2, addDataText3, addDataText4, addDataText5;
    private TextView addUserText1, addUserText2, addUserText3, addUserText4, addUserText5;

    private void showDataDialog(int numberId, int distance) {
        if (dataDialog == null) {
            dataDialog = new Dialog(this, R.style.Dialog_Fullscreen);
            View currentView = LayoutInflater.from(UltrasonicService.this).inflate(R.layout.ul_text_dialog, null);
            addDataText1 = (TextView) currentView.findViewById(R.id.ul_text_img1);
            addDataText2 = (TextView) currentView.findViewById(R.id.ul_text_img2);
            addDataText3 = (TextView) currentView.findViewById(R.id.ul_text_img3);
            addDataText4 = (TextView) currentView.findViewById(R.id.ul_text_img4);
            addDataText5 = (TextView) currentView.findViewById(R.id.ul_text_img5);

            addUserText1 = (TextView) currentView.findViewById(R.id.ul_text_user1);
            addUserText2 = (TextView) currentView.findViewById(R.id.ul_text_user2);
            addUserText3 = (TextView) currentView.findViewById(R.id.ul_text_user3);
            addUserText4 = (TextView) currentView.findViewById(R.id.ul_text_user4);
            addUserText5 = (TextView) currentView.findViewById(R.id.ul_text_user5);
            dataDialog.setContentView(currentView);
            dataDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }

        if (map != null) {
            if (TextUtils.isEmpty(addUserText1.getText())) {
                for (Map.Entry mMap : map.entrySet()) {
                    Integer mapId = (Integer) mMap.getKey();
                    Integer mapDistance = (Integer) mMap.getValue();
                    switch (mapId) {
                        case 2:
                            addUserText1.setText((mapDistance / 10) + "");
                            break;
                        case 1:
                            addUserText2.setText((mapDistance / 10) + "");
                            break;
                        case 0:
                            addUserText3.setText((mapDistance / 10) + "");
                            break;
                        case 7:
                            addUserText4.setText((mapDistance / 10) + "");
                            break;
                        case 6:
                            addUserText5.setText((mapDistance / 10) + "");
                            break;
                    }
                }
            }
        }


        switch (numberId) {
            case 2:
                setTextMessage(numberId, distance, addDataText1);
                break;
            case 1:
                setTextMessage(numberId, distance, addDataText2);
                break;
            case 0:
                setTextMessage(numberId, distance, addDataText3);
                break;
            case 7:
                setTextMessage(numberId, distance, addDataText4);
                break;
            case 6:
                setTextMessage(numberId, distance, addDataText5);
                break;
        }
        dataDialog.show();
    }

    private void setTextMessage(int numberId, int distance, TextView textView) {
        if (map.containsKey(numberId)) {
            int mapDistance = map.get(numberId);
            if (distance <= mapDistance) {
                textView.setTextColor(Color.RED);
            } else {
                textView.setTextColor(Color.WHITE);
            }
            textView.setText("    " + (distance / 10));
        }
    }

    //测试用 是否已经动态设置好数据
    private Boolean isSettingFinish = false;

    private List<Integer> integerList1, integerList2, integerList3, integerList4, integerList5;

    private int settingNum = 0;

    private void checkData() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

            }
        }, ultrasonicDataExectionTime);
    }

    @Override
    public void onGetUltrasonic(byte[] data) {
        try {
            isReceiveUltrasonic = true;
            mHandle.removeMessages(OPEN_USER_ULTRASONIC);
            if (isUltraData(data) && isInitFinish) {
//                L.i(TAG, "---------------data--" + Arrays.toString(data));
                byte[] bytes = new byte[customUlData.size() * 4];
                System.arraycopy(data, 5, bytes, 0, customUlData.size() * 4);

                if (isAutoSettingDistance) {
                    if (settingNum < 10) {
                        settingNum++;
                        if (integerList1 == null) {
                            integerList1 = new ArrayList<>();
                            integerList2 = new ArrayList<>();
                            integerList3 = new ArrayList<>();
                            integerList4 = new ArrayList<>();
                            integerList5 = new ArrayList<>();
                        }
                        for (int i = 0; i < bytes.length; i++) {
                            if ((i - 3) % 4 == 0) {
                                int valueNG = (bytes[i] & 255) | ((bytes[i - 1] & 255) << 8); // 距离
                                int numberNg = (bytes[i - 2] & 255) | ((bytes[i - 3] & 255) << 8); // 返回的探头编号 0-12

                                switch (numberNg) {
                                    case 0:
                                        integerList1.add(valueNG);
                                        break;
                                    case 1:
                                        integerList2.add(valueNG);
                                        break;
                                    case 2:
                                        integerList3.add(valueNG);
                                        break;
                                    case 6:
                                        integerList4.add(valueNG);
                                        break;
                                    case 7:
                                        integerList5.add(valueNG);
                                        break;
                                }
//                            initDistanceMap(numberNg, valueNG);
                                L.e(TAG, "设置数据完毕");
                            }
                        }
                        return;
                    }

                    if (!isSettingFinish) {
                        isSettingFinish = true;
                        initDistanceMap();
                        TtsUtils.sendTts(getApplicationContext(), "初始化成功，开始迎宾");
                    }


                    if (isOpenDataTestDialog) {
                        for (int i = 0; i < bytes.length; i++) {
                            if ((i - 3) % 4 == 0) {
                                int valueNG = (bytes[i] & 255) | ((bytes[i - 1] & 255) << 8); // 距离
                                int numberNg = (bytes[i - 2] & 255) | ((bytes[i - 3] & 255) << 8); // 返回的探头编号 0-12
                                Message message = mHandle.obtainMessage();
                                message.what = 1110;
                                message.arg1 = numberNg;
                                message.arg2 = valueNG;
                                mHandle.sendMessage(message);
                            }
                        }
                    }

                }

                for (int i = 0; i < bytes.length; i++) {
                    if ((i - 3) % 4 == 0) {
                        int valueNG = (bytes[i] & 255) | ((bytes[i - 1] & 255) << 8); // 距离
                        int numberNg = (bytes[i - 2] & 255) | ((bytes[i - 3] & 255) << 8); // 返回的探头编号 0-12
//                        int myDistance = getDistanceFromPosition(numberNg); //设置的探头距离
                        if (map.containsKey(numberNg)) {
                            if (getDataMap().containsKey(numberNg)) {
                                if (valueNG == getDataMap().get(numberNg)) {
                                    checkTimeUtils.gennerateTimeTask(numberNg);
                                } else {
                                    checkTimeUtils.clearThisTask(numberNg);
                                }
                                getDataMap().put(numberNg, valueNG);
                            }
                            int myDistance = map.get(numberNg); //设置的探头距离
                            L.i(TAG, "numberNg = " + numberNg + "---myDistance = " + myDistance);
                            if (customUlData.contains(numberNg)) {
                                if (isConformDistance(valueNG, myDistance, numberNg)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, Integer> dataMap = null;

    private Map<Integer, Integer> getDataMap() {
        if (dataMap == null) {
            dataMap.put(0, -1);
            dataMap.put(1, -1);
            dataMap.put(2, -1);
            dataMap.put(6, -1);
            dataMap.put(7, -1);
        }
        return dataMap;
    }


    private int currentNeedPlay = 0;

    /**
     * valueNG 探测到的数据
     * distance 用户阈值
     * ultrasonicId 探头编号
     */
    private boolean isConformDistance(int valueNG, int distance, int ultrasonicId) {
        try {
            L.i(TAG, ultrasonicId + "号-----valueNG-" + valueNG + "-----distance-" + distance + "-----numValue----" + NUM_VALUE + "---numFarValue---" + NUM_FARVALUE + "检测到远离次数:" + farDistanceNum);

            if (distance != -1) {

                if (valueNG <= distance) {
                    //跟随动作
                    if (ultrasonicId == 0 || ultrasonicId == 9) {
                        setHeadAction(ORIGIN_HEAD);
                    } else if (ultrasonicId == 1 || ultrasonicId == 10) {
                        setHeadAction(RIGHT_HEAD);
                    } else if (ultrasonicId == 7 || ultrasonicId == 8) {
                        setHeadAction(LEFT_HEAD);
                    } else if (ultrasonicId == 2 || ultrasonicId == 12) {
                        setHeadAction(TOO_RIGHT_HEAD);
                    } else if (ultrasonicId == 6 || ultrasonicId == 11) {
                        setHeadAction(TOO_LEFT_HEAD);
                    }

                    if (mHandle.hasMessages(CALIBRATION_20_MIN)) {
                        mHandle.removeMessages(CALIBRATION_20_MIN);
                    }

                    if (isTimingCount) {
                        removeTimerCount();
                    }

                    flagsMap.put(ultrasonicId, true);
                    farDistanceNum = 0;
                    if (!mIsExecute) {
                        if (leftSelectNum.contains(ultrasonicId)) {
                            L.i(TAG, "-----han----mIsExecute=" + mIsExecute);
                            mIsExecute = true;
                            startPlay(START_LEFT_STRING);
                        } else if (rightSelectNum.contains(ultrasonicId)) {
                            L.i(TAG, "-----han----mIsExecute=" + mIsExecute);
                            mIsExecute = true;
                            startPlay(START_RIGHT_STRING);
                        }
                    }
                    return true;
                } else {
                    //记录距离
                    if (valueNG <= distance) {
                        flagsMap.put(ultrasonicId, true);
                    } else {
                        flagsMap.put(ultrasonicId, false);
                    }

                    //全部检测
                    if (mIsExecute) {
                        farDistanceNum++;
                        L.i(TAG, "开始计时 farDistanceNum:" + farDistanceNum + "---isAllFaraway() = " + isAllFaraway());
                        //全部探头检测无人, 并且所有执行完毕开始计时
                        if (farDistanceNum >= NUM_FARVALUE * customNumber && isAllFaraway() && isAllPlayFinish()) {
                            //发送计时
                            if (!isTimingCount) {
                                sendTimerCount();
                            }
                            if (isTimingCount && timingCount > waitTime) {
                                L.i(TAG, "迎宾结束");
//                                startPlay(STOP_GUEST_STRING);
                                han.removeMessages(0);
                                SpeechManager.getInstance().closeSpeechDiscern(getApplicationContext());
                                closeAlwaysLight();
                                mIsExecute = false;
                                removeTimerCount();
                                if (groupManager != null) {
                                    groupManager.reset();
                                }
                            }
                            lastHead = -1;
                        }
                    }

                    //未检测到人就开始计时
                    if (!mHandle.hasMessages(CALIBRATION_20_MIN)) {
                        mHandle.sendEmptyMessageDelayed(CALIBRATION_20_MIN, calibraitonTime);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 开始发送计时
     */
    public void sendTimerCount() {
        timingCount = 0;
        isTimingCount = true;
        han.removeMessages(START_TIMER_GUEST);
        han.sendEmptyMessage(START_TIMER_GUEST);
    }

    /**
     * 移除计时
     */
    public void removeTimerCount() {
        han.removeMessages(START_TIMER_GUEST);
        timingCount = 0;
        isTimingCount = false;
    }

    private boolean isAllPlayFinish() {
//        L.e("isAllPlayFinish", "isTtsFinish = " + isTtsFinish + "--isFaceFinish = " + isFaceFinish + "--isMusicFinish = " + isMusicFinish + "--isPictureFinish = " + isPictureFinish +
//                "--isActionFinish = " + isActionFinish + "--isMediaFinish = " + isMediaFinish);
        return isTtsFinish == true && isFaceFinish == true && isMusicFinish == true &&
                isPictureFinish == true && isActionFinish == true && isMediaFinish == true;
    }

    private final int START_LEFT_STRING = 1;
    private final int START_RIGHT_STRING = 2;
    private final int STOP_GUEST_STRING = 3;
    private int startLeftPlayMode = 0;
    private int startRightPlayMode = 0;
    private int stopPlayMode = 0;
    private int ORDER_PLAY = 0;
    private int RANDOM_PLAY = 1;
    private int mStartLeftCurrentIndex = -1;
    private int mStartRightCurrentIndex = -1;
    private int mEndCurrentIndex = -1;

    public boolean isPlayPicture = false;

    private boolean isFaceFinish, isTtsFinish, isActionFinish, isMediaFinish, isMusicFinish, isPictureFinish;

    /**
     * type 0：开始迎宾 1：结束迎宾
     */
    private void startPlay(int type) {
        openAlwaysLight();

        currentNeedPlay = type;
        isWelcomeTTsStart = true;

        isTtsFinish = false;
        isFaceFinish = false;
        isActionFinish = false;
        isMediaFinish = false;
        isMusicFinish = false;
        isPictureFinish = false;

        ItemsContentBean currentBean = getItemsContentBean(type);

        if (currentBean != null) {
            L.e("startPlay", "currentBean = " + currentBean.toString());
            //迎宾语和表情
            if (!TextUtils.isEmpty(currentBean.getOther())) {
//                TtsUtils.closeTTs(UltrasonicService.this);
                if (!TextUtils.isEmpty(currentBean.getFace())) {
                    TtsUtils.sendTts(getApplicationContext(), currentBean.getOther() + "@#;" + currentBean.getFace());
                } else
                    TtsUtils.sendTts(getApplicationContext(), currentBean.getOther());

                int ttsLength = currentBean.getOther().length();
                long ttsTime = ttsLength * wordSpeed;
                showTip("ttsLength=" + ttsLength + "- - ttsTime=" + ttsTime);
                if (mHandle != null)
                    mHandle.sendEmptyMessageDelayed(TTS_FINISH, ttsTime);
            } else {
                isTtsFinish = true;
                isFaceFinish = true;
            }

            //多动作播放
            executeAction(currentBean);

            //灯带
//            executeLight(currentBean);

            if (!TextUtils.isEmpty(currentBean.getMedia())) {

                if (currentBean.getMedia().toLowerCase().endsWith(".mp4")) {
                    //播放视频
                    GuestsApplication.from(this).playGuestVideoByPath(currentBean.getMedia());
                    isPictureFinish = true;
                } else {
                    //播放图片
                    isPlayPicture = true;
                    if (!TextUtils.isEmpty(currentBean.getMedia())) {
                        File mFile = new File(currentBean.getMedia());
                        if (mFile.exists()) {
                            pictureDialog = new Dialog(this, R.style.Dialog_Fullscreen);
                            View currentView = LayoutInflater.from(UltrasonicService.this).inflate(R.layout.ul_picture_dialog, null);
                            ImageView adPlayerPic = (ImageView) currentView.findViewById(R.id.ul_picture_img);
                            playAdPicture(adPlayerPic, mFile);
                            pictureDialog.setContentView(currentView);
                            pictureDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                            pictureDialog.show();
                        }
                    }
                    isMediaFinish = true;
                }
            } else {
                isPictureFinish = true;
                isMediaFinish = true;
            }

            //播放音乐
            if (!TextUtils.isEmpty(currentBean.getMusic())) {
                playMusic(currentBean.getMusic());
            } else isMusicFinish = true;
        } else {
            if (groupManager != null) {
                groupManager.reset();
            }
        }
    }

    private boolean isCanPlayItem(String guestTime) {
        if (!TextUtils.isEmpty(guestTime) && guestTime.contains("@#")) {
            String[] times = guestTime.split("@#");
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            int currentHour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int currentMinutes = mCalendar.get(Calendar.MINUTE);
            String currentTime = (currentHour + ":" + currentMinutes);

            boolean isThanStart = DatePickerUtils.getInstance().timeCompareMax(currentTime, times[0]);
            boolean isThanEnd = DatePickerUtils.getInstance().timeCompareMax(times[1], currentTime);

            if (isThanStart && isThanEnd) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    private ItemsContentBean getItemsContentBean(int type) {
        int mStartCurrentIndex = -1;
        int currentPlayMode = 0;
        List<ItemsContentBean> itemsContents = null;

        if (type == START_LEFT_STRING) {
            mStartCurrentIndex = mStartLeftCurrentIndex;
            itemsContents = itemsLeftContents;
            currentPlayMode = startLeftPlayMode;

        } else if (type == START_RIGHT_STRING) {
            mStartCurrentIndex = mStartRightCurrentIndex;
            itemsContents = itemsRightContents;
            currentPlayMode = startRightPlayMode;

        } else if (type == STOP_GUEST_STRING) {
            mStartCurrentIndex = mEndCurrentIndex;
            itemsContents = itemsEndContents;
            currentPlayMode = stopPlayMode;

        }

        ItemsContentBean currentBean = null;
        List<ItemsContentBean> tempItemsContentBean = new ArrayList<>();

        if (itemsContents != null && itemsContents.size() > 0) {
            /** 符合时间的优先*/
            for (int i = 0; i < itemsContents.size(); i++) {
                String guestTime = itemsContents.get(i).getStartGuestTimePart();
                if (isCanPlayItem(guestTime)) {
                    tempItemsContentBean.add(itemsContents.get(i));
                }
            }


            /** 无符合时间，选择未设置时间*/
            if (tempItemsContentBean.size() == 0) {
                for (int i = 0; i < itemsContents.size(); i++) {
                    String guestTime = itemsContents.get(i).getStartGuestTimePart();
                    if (TextUtils.isEmpty(guestTime)) {
                        tempItemsContentBean.add(itemsContents.get(i));
                    }
                }
            }

            if (tempItemsContentBean.size() == 0) {
                return null;
            }

            if (currentPlayMode == ORDER_PLAY) {
                mStartCurrentIndex++;
                if (mStartCurrentIndex > (tempItemsContentBean.size() - 1)) {
                    mStartCurrentIndex = 0;
                }
            } else if (currentPlayMode == RANDOM_PLAY) {
                mStartCurrentIndex = (int) (Math.random() * (tempItemsContentBean.size()));
            }
            currentBean = tempItemsContentBean.get(mStartCurrentIndex);

            if (type == START_LEFT_STRING) {
                mStartLeftCurrentIndex = mStartCurrentIndex;
            } else if (type == START_RIGHT_STRING) {
                mStartRightCurrentIndex = mStartCurrentIndex;
            } else if (type == STOP_GUEST_STRING) {
                mEndCurrentIndex = mStartCurrentIndex;
            }

        }

        return currentBean;
    }

    /**
     * 执行动作
     *
     * @param currentBean 当前Bean
     */
    private List<Integer> actionList;
    private int currentCount;

    private void executeAction(ItemsContentBean currentBean) {
        actionList = new ArrayList<Integer>();
        String actionStr = currentBean.getAction();
        //自带动作
        if (!TextUtils.isEmpty(actionStr)) {
            if (actionStr.contains("、")) {

                String[] faceArr = actionStr.split("、");
                int faceCount = faceArr.length;
                for (int i = 0; i < faceCount; i++) {
                    if (!TextUtils.isEmpty(faceArr[i].trim())) {
                        actionList.add(Integer.parseInt(faceArr[i]));
                        showTip("faceStr--faceArr->" + faceCount + "     " + faceArr[i]);
                    }
                }
            } else {
                actionList.add(Integer.parseInt(actionStr));
            }
        }

        if (actionList != null && actionList.size() > 0) {
            currentCount = 0;
            checkAction(actionList.get(currentCount));
        } else
            isActionFinish = true;
    }

    /**
     * 检测动作
     *
     * @param action
     * @return
     */
    private void checkAction(int action) {
        L.i("执行动作", "action = " + action);
        if (action != -1) {
            //执行脚本
            String sport = FileUtils.getActionFile(this, "action/action_" + action);
            groupManager.execute(sport, groupTaskListener);
            L.i("执行动作", "actionJson = " + sport);
        }

    }

    /**
     * 播放图片
     *
     * @param view, mFile
     * @return
     */
    public void playAdPicture(ImageView view, File mFile) {
        try {
            if (mFile != null && mFile.exists()) {
                Bitmap bitmap = view.getDrawingCache();
                if (bitmap != null)
                    bitmap.recycle();
                Log.d("TAG", "playAdPicture: ...................");
                Bitmap mp = BitmapUtils.getimage(mFile.getPath());
                view.setImageBitmap(mp);
                view.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlayPicture(long time) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                showTip("图片结束");
                if (pictureDialog != null) {
                    isPictureFinish = true;
                    pictureDialog.dismiss();
                    openSpeechDiscern();
                }
            }
        }, time);
    }

    public void ttsEnd() {
        showTip("说话结束, 需要结束图片");
        isTtsFinish = true;
        isFaceFinish = true;
        openSpeechDiscern();
        if (!isPictureFinish) {
            stopPlayPicture(3000);
        }
    }

    private void openSpeechDiscern() {
        if (isAllPlayFinish()) {

            if (currentNeedPlay == START_LEFT_STRING || currentNeedPlay == START_RIGHT_STRING) {
                if (isNeedOpenSpeech) {
                    showTip("开启语音识别");
                    SpeechManager.getInstance().openSpeechDiscern(getApplicationContext());
                    TtsUtils.getInstance().sendOpenSpeechBroadcast(this);
                }
            } else if (currentNeedPlay == STOP_GUEST_STRING) {
                if (groupManager != null) {
                    groupManager.reset();
                }
            }
        }
    }

    /**
     * 开启常亮灯带
     **/
    private void openAlwaysLight() {
        isCloseLight = false;
        if (mHandle != null)
            mHandle.sendEmptyMessage(LIGHT_ALWAYS_OPEN);
    }

    /**
     * 关闭常亮灯带
     **/
    private void closeAlwaysLight() {
        isCloseLight = true;
        RobotManager.getInstance(getApplicationContext()).getControlInstance().setLightBeltBrightness(0);
    }

    /**
     * 播放音乐
     *
     * @param music 音乐路径
     */
    public boolean musicNeedSay = false;

    private void playMusic(String music) {
        //判断文件是否存在
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        assert musicPlayer != null;
        musicPlayer.playUrl(music, new MusicPlayer.OnMusicCompletionListener() {
            @Override
            public void onCompletion(boolean isPlaySuccess) {
                showTip("执行播放音乐结束");
                isMusicFinish = true;
                musicNeedSay = false;
                mHandle.removeMessages(MUSIC_NEED_SAY);
                openSpeechDiscern();
            }

            @Override
            public void onPrepare(int Duration) {
                // TODO需要循環发送说说话表情
                musicNeedSay = true;
                mHandle.sendEmptyMessage(MUSIC_NEED_SAY);
            }
        });

    }

    /**
     * 执行灯带
     *
     * @param currentBean
     * @return null
     */
    private void executeLight(ItemsContentBean currentBean) {
        light = currentBean.getLight();
        if (TextUtils.isEmpty(light)) {
            light = "1";
        }

        if (!light.equals("1")) {
            /***
             * 0开  或者  2闪烁
             */
            String time = "0";

            if (light.equals("0")) {
                time = currentBean.getOpenLightTime();
            } else if (light.equals("2")) {
                time = currentBean.getFlickerLightTime();
            }

            if (mHandle != null) {
                if (TextUtils.isEmpty(time)) {
                    /***
                     * 关
                     */
                    if (mHandle != null)
                        mHandle.sendEmptyMessage(LIGHT_END);
                } else {
                    mHandle.sendEmptyMessage(LIGHT_OPEN);
                    /***
                     * 关闭
                     */
                    mHandle.sendEmptyMessageDelayed(LIGHT_END, (long) (Float.parseFloat(time) * 1000));
                }

            }
        } else {
            /***
             * 关
             */
            if (mHandle != null)
                mHandle.sendEmptyMessage(LIGHT_END);
        }
    }

    private void showTip(String s) {
        L.e("迎宾", s);
    }

    private final int START_TIMER_GUEST = 0;
    private final int START_TIMER_CLEAR_SLEEP = 1;
    /**
     * time 计时
     */
    private int timingCount = 0;
    /**
     * 是否正在计时
     */
    private boolean isTimingCount = false;

    private Handler han = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_TIMER_GUEST:
                    timingCount++;
                    han.sendEmptyMessageDelayed(START_TIMER_GUEST, 1000);
                    L.i(TAG, "开始计时 timingCount = " + timingCount + "-- waitTime = " + waitTime);
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
//                    TtsUtils.sendTts(getApplicationContext(), "标定成功");
                    playStartSound();
                    showToast("超声波标定成功 \t\tTime: " + getCurrentTime());
                    isReceiveUltrasonic = false;
                    isInitFinish = true;
                    mHandle.sendEmptyMessageDelayed(OPEN_USER_ULTRASONIC, 1200);
                } else {
//                    失败
                    TtsUtils.sendTts(getApplicationContext(), "标定失败");
                    showToast("超声波标定失败 \t\tTime: " + getCurrentTime());
                }
            }
        }
        L.i(TAG, "超声波:bool= " + bool);
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
        mHandle.sendEmptyMessageDelayed(OPEN_USER_ULTRASONIC, 5000);
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

    /**
     * 打开用户定义的超声波
     */
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
//        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getApplication())).openUltrasonicFeedback(EnvUtil.ULGST001, byte4 << 8 | byte5);
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
            if (mHandle != null)
                mHandle.sendEmptyMessageDelayed(OPEN_LIGHT_ALWAYS, 500);
        }
    }

    //关闭
    private void closeRepeatLight() {
        IsOpenRepeatLight = false;
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
                if (customUlData.get(i) < 8) {
                    byteList5.add(ultrasonicOpenMap.get(customUlData.get(i)));
                } else {
                    byteList4.add(ultrasonicOpenMap.get(customUlData.get(i)));
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
        if (ulDistanceBeen != null && ulDistanceBeen.size() > 0) {
            for (int i = 0; i < ulDistanceBeen.size(); i++) {
                if (numberNg == ulDistanceBeen.get(i).getUltrasonicId()) {
                    if (!ulDistanceBeen.get(i).getDistanceValue().isEmpty()) {
                        return Integer.parseInt(ulDistanceBeen.get(i).getDistanceValue()) * 10;
                    }
                }
            }
        }
        return -1;
    }

    private Map<Integer, Integer> map;


    private void initDistanceMap(List<UlDistanceBean> distanceBeen) {
        if (map == null) {
            map = new HashMap<>();
            map.put(0, 2000);
            map.put(1, 2000);
            map.put(2, 2000);
            map.put(6, 2000);
            map.put(7, 2000);
        }

        if (distanceBeen != null) {
            for (int i = 0; i < distanceBeen.size(); i++) {
                UlDistanceBean ul = distanceBeen.get(i);
                if (map.containsKey(ul.getUltrasonicId())) {
                    int distance = Integer.parseInt(ul.getDistanceValue());
                    map.put(ul.getUltrasonicId(), distance);
                    L.e(TAG, "initDistanceMap :" + "探头ID = " + ul.getUltrasonicId() + "; 检测距离 = " + distance);
                }
            }
        }

    }

    private void initDistanceMap() {
        if (map == null) {
            map = new HashMap<>();
            map.put(0, 1500);
            map.put(1, 1500);
            map.put(2, 1500);
            map.put(6, 1500);
            map.put(7, 1500);
        }

        if (integerList1.size() > 0) {
            Collections.sort(integerList1);
            setDistanceMapData(0, integerList1);
        }

        if (integerList2.size() > 0) {
            Collections.sort(integerList2);
            setDistanceMapData(1, integerList2);
        }

        if (integerList3.size() > 0) {
            Collections.sort(integerList3);
            setDistanceMapData(2, integerList3);
        }

        if (integerList4.size() > 0) {
            Collections.sort(integerList4);
            setDistanceMapData(6, integerList4);
        }

        if (integerList5.size() > 0) {
            Collections.sort(integerList5);
            setDistanceMapData(7, integerList5);
        }
    }


    private int minDistanceValue = 100;

    private void setDistanceMapData(int number, List<Integer> integerList) {
        int valueNg = 0;
        for (int i = 0; i < integerList.size(); i++) {
            int inter = integerList.get(i);
            if (inter > minDistanceValue) {
                valueNg = integerList.get(i);
                break;
            }
        }

        L.e(TAG, "setDistanceMapData :" + "探头ID = " + number + "; 检测距离 = " + valueNg);
        if (valueNg > 340 && valueNg <= 1500) {
            map.put(number, valueNg - 200);
        } else if (valueNg > 1500 && valueNg < 8190) {
            map.put(number, 1500);
        } else {
//            map.put(number, 1500);
        }
    }

    Map<Integer, Byte> ultrasonicOpenMap = null;

    /**
     * 设置要打开的的探头字节
     * position 探头的下标
     */
    private void initAllOpenData() {
        if (ultrasonicOpenMap == null) {
            ultrasonicOpenMap = new HashMap<>();
            //byte[5]
            ultrasonicOpenMap.put(0, (byte) 0x01);
            ultrasonicOpenMap.put(1, (byte) 0x02);
            ultrasonicOpenMap.put(2, (byte) 0x04);
            ultrasonicOpenMap.put(6, (byte) 0x40);
            ultrasonicOpenMap.put(7, (byte) 0x80);

            ultrasonicOpenMap.put(8, (byte) 0x01);
            ultrasonicOpenMap.put(9, (byte) 0x02);
            ultrasonicOpenMap.put(10, (byte) 0x04);
            ultrasonicOpenMap.put(11, (byte) 0x08);
            ultrasonicOpenMap.put(12, (byte) 0x10);
        }
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
//                    player.start();
//                    player.setLooping(true);
                    showTip("播放视频结束");
                    isMediaFinish = true;
                    openSpeechDiscern();
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
            mHandle.sendEmptyMessageDelayed(OPEN_INFRARED, 5000);
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
        L.i(TAG, "轮子启动");
        RobotManager.getInstance(getApplicationContext()).getWheelInstance().stop();
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
                    closeEveryOne();
                    stopSelf();
                    unregisterReceiver(lidBoardReceive);
                }

            }
        }
    };

    String leftHeadJson = "{\n" +
            "  \"actions\": [\n" +
            "    {\n" +
            "      \"head\": {\n" +
            "        \"angle\": 75,\n" +
            "        \"directionspinner\": \"左转\",\n" +
            "        \"direction\": \"move\"\n" +
            "      },\n" +
            "      \"next_action_time\": \"1000\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    String tooLeftHeadJson = "{\n" +
            "  \"actions\": [\n" +
            "    {\n" +
            "      \"head\": {\n" +
            "        \"angle\": 45,\n" +
            "        \"directionspinner\": \"左转\",\n" +
            "        \"direction\": \"move\"\n" +
            "      },\n" +
            "      \"next_action_time\": \"1000\"\n" +
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
            "      \"next_action_time\": \"1000\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    String tooRightHeadJson = "{\n" +
            "  \"actions\": [\n" +
            "    {\n" +
            "      \"head\": {\n" +
            "        \"angle\": 195,\n" +
            "        \"directionspinner\": \"右转\",\n" +
            "        \"direction\": \"move\"\n" +
            "      },\n" +
            "      \"next_action_time\": \"1000\"\n" +
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
    private final int TOO_RIGHT_HEAD = 0;
    private final int RIGHT_HEAD = 1;
    private final int ORIGIN_HEAD = 2;
    private final int LEFT_HEAD = 3;
    private final int TOO_LEFT_HEAD = 4;

    private int lastHead = -1;

    private void setHeadAction(int direc) {
        switch (direc) {
            case TOO_RIGHT_HEAD:
                if (direc != lastHead) {
                    groupManager.execute(tooRightHeadJson);
                }
                break;
            case RIGHT_HEAD:
                if (direc != lastHead) {
                    groupManager.execute(rightHeadJson);
                }
                break;
            case ORIGIN_HEAD:
                if (direc != lastHead) {
                    groupManager.execute(originHeadJson);
                }
                break;
            case LEFT_HEAD:
                if (direc != lastHead) {
                    groupManager.execute(leftHeadJson);
                }
                break;
            case TOO_LEFT_HEAD:
                if (direc != lastHead) {
                    groupManager.execute(tooLeftHeadJson);
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
        closeEveryOne();
    }

    private void closeEveryOne() {
        IsRunning = false;
        L.i(TAG, "onDestroy");
        RobotManager.getInstance(this).unRegisterOnGetUltrasonicCallBack();
        //TODO 新策略
//        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getApplication())).closeUltrasonicFeedback(EnvUtil.ULGST001);
        RobotManager.getInstance(this).getNavigationInstance().unRegisterOnNavigationStateChangeListener(this);
        RobotManager.getInstance(this).unRegisterOnWheelStateChangeListener();

        //音乐说话表情停止
        musicNeedSay = false;

        if (musicPlayer != null)
            musicPlayer.stop();

        //图片停止
        if (pictureDialog != null) {
            pictureDialog.dismiss();
        }

        GuestsApplication.from(this).dismissGuestVideo();


        if (han != null) {
            han.removeCallbacksAndMessages(null);
        }
        if (mHandle != null) {
            mHandle.removeCallbacksAndMessages(null);
        }
        closeRepeatLight();
        mHandle = null;

        if (dataDialog != null && dataDialog.isShowing()) {
            dataDialog.dismiss();
        }

        PreferencesUtils.putBoolean(this, SpContans.SP_OPEN_TEST_DIALOG, false);
    }

    @Override
    public void onRobotSateChange(int robotStateIndex, int newState) {
        if (robotStateIndex == RobotState.ROBOT_STATE_INDEX_HEAD_KEY) {
            if (newState == 2) {
                if (mHandle != null)
                    mHandle.removeMessages(TTS_FINISH);

                SpeechManager.getInstance().openSpeechDiscern(getApplicationContext());
                TtsUtils.getInstance().sendOpenSpeechBroadcast(this);

                if (mIsExecute) {
                    openAlwaysLight();
                }

                if (!isTtsFinish) {
                    isTtsFinish = true;
                    isFaceFinish = true;
                }

                if (!isPictureFinish)
                    stopPlayPicture(0);

                if (!isActionFinish) {
                    isActionFinish = true;
                }

                if (!isMusicFinish) {
                    if (musicPlayer != null)
                        musicPlayer.stop();
                    isMusicFinish = true;

                    musicNeedSay = false;
                    if (mHandle != null)
                        mHandle.removeMessages(MUSIC_NEED_SAY);
                }

                if (!isMediaFinish) {
                    isMediaFinish = true;
                    GuestsApplication.from(this).dismissGuestVideo();
                }


            }
        }
    }

    /**
     * 提示框
     */
    CustomHintDialog hitDialog;

    public void showCanUserDialog(String content) {
        hitDialog = new CustomHintDialog(UltrasonicService.this, -1);
        hitDialog.setTitle("提示");
        hitDialog.setMessage(content);
        hitDialog.setCancelable(false);
        hitDialog.setSubmitButton("确定", new CustomHintDialog.IButtonOnClickLister() {
            @Override
            public void onClickLister() {
                stopSelf();
            }
        });
        hitDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        hitDialog.show();
    }

    @Override
    public void timeFinish() {

    }

//    @Override
//    public void onUltrasonicOccupyState(String sceneCode, int isAvailable) {
//        L.e(TAG, "sceneCode = " + sceneCode + "---isAvailable = " + isAvailable);
//        if (isAvailable == 0) {
//            showCanUserDialog("超声波被占用暂不可用");
//        }
//    }

}
