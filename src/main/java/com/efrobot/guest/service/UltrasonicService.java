package com.efrobot.guest.service;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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

import com.efrobot.guest.Env.EnvUtil;
import com.efrobot.guest.GuestsApplication;
import com.efrobot.guest.R;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.bean.UlDistanceBean;
import com.efrobot.guest.dao.DataManager;
import com.efrobot.guest.dao.SelectedDao;
import com.efrobot.guest.dao.UltrasonicDao;
import com.efrobot.guest.setting.SettingActivity;
import com.efrobot.guest.setting.bean.SelectDirection;
import com.efrobot.guest.utils.ActivityManager;
import com.efrobot.guest.utils.BitmapUtils;
import com.efrobot.guest.utils.FileUtils;
import com.efrobot.guest.utils.MusicPlayer;
import com.efrobot.guest.utils.PreferencesUtils;
import com.efrobot.guest.utils.TtsUtils;
import com.efrobot.library.OnRobotStateChangeListener;
import com.efrobot.library.RobotManager;
import com.efrobot.library.RobotState;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.library.task.GroupManager;
import com.efrobot.library.task.NavigationManager;
import com.efrobot.library.task.SpeechGroupManager;
import com.efrobot.library.task.UltrasonicTaskManager;
import com.efrobot.speechsdk.SpeechManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 迎宾执行功能
 */

public class UltrasonicService extends Service implements RobotManager.OnGetUltrasonicCallBack,
        NavigationManager.OnNavigationStateChangeListener, RobotManager.OnWheelStateChangeListener,
        RobotManager.OnUltrasonicOccupyStatelistener, OnRobotStateChangeListener{

    private String CLOSE_TTS = "com.efrobot.speech.voice.ACTION_TTS";
    private static final String TAG = UltrasonicService.class.getSimpleName();

    public static boolean IsRunning = false;

    private boolean mIsExecute = false;
    private boolean isReceiveUltrasonic = false;

    //超声波数据设置
    private ArrayList<UlDistanceBean> ulDistanceBeen = null;

    private UltrasonicDao ultrasonicDao;
    private SelectedDao selectedDao;

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


    private List<SelectDirection> leftSelectDirections;
    private List<SelectDirection> rightSelectDirections;

    private List<Integer> leftSelectNum = new ArrayList<Integer>();
    private List<Integer> rightSelectNum = new ArrayList<Integer>();

    private List<ItemsContentBean> itemsLeftContents;
    private List<ItemsContentBean> itemsRightContents;
    private List<ItemsContentBean> itemsEndContents;
    private String light;
    private Dialog dialog;

    private MusicPlayer mediaPlayer;

    //0.27秒
    private long wordSpeed = 270;

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
        RobotManager.getInstance(UltrasonicService.this).registerUltrasonicOccupylistener(this);
        RobotManager.getInstance(UltrasonicService.this).registerHeadKeyStateChangeListener(this);


        GuestsApplication.from(this).setUltrasonicService(this);
        //注册盖子
        registerLiboard();
        //导航监听轮子变化
        RobotManager.getInstance(this).getNavigationInstance().registerOnNavigationStateChangeListener(this);
        RobotManager.getInstance(this).registerOnWheelStateChangeListener(this);
        ultrasonicDao = GuestsApplication.from(getApplicationContext()).getUltrasonicDao();
        selectedDao = GuestsApplication.from(getApplicationContext()).getSelectedDao();

        //超声波设置数据
        ulDistanceBeen = ultrasonicDao.queryAll();
        if (ulDistanceBeen == null || ulDistanceBeen.size() < 10) {
            showToast("迎宾距离不能为空");
            return super.onStartCommand(intent, START_STICKY, startId);
        }

        //超声波方向设置
        leftSelectDirections = selectedDao.queryOneType(1);
        rightSelectDirections = selectedDao.queryOneType(2);
        if (leftSelectDirections.size() == 0 && rightSelectDirections.size() == 0) {
            showToast("请设置迎宾方向");
            return super.onStartCommand(intent, START_STICKY, startId);
        } else {
            for (int i = 0; i < leftSelectDirections.size(); i++) {
                leftSelectNum.add(leftSelectDirections.get(i).getUltrasonicId());
            }
            for (int i = 0; i < rightSelectDirections.size(); i++) {
                rightSelectNum.add(rightSelectDirections.get(i).getUltrasonicId());
            }
        }

        initAllOpenData();

        // 1:开始迎宾 2:结束迎宾
        itemsLeftContents = DataManager.getInstance(UltrasonicService.this).queryItem(1);
        itemsRightContents = DataManager.getInstance(UltrasonicService.this).queryItem(2);
        itemsEndContents = DataManager.getInstance(UltrasonicService.this).queryItem(3);

        mediaPlayer = new MusicPlayer(null);

        if (itemsLeftContents == null || itemsLeftContents.size() == 0) {
            showToast("请设置欢迎语");
            return super.onStartCommand(intent, START_STICKY, startId);
        }
        //交流模式
        initGuestData();
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    public void initGuestData() {
        //播放模式
        startLeftPlayMode = PreferencesUtils.getInt(this, SettingActivity.SP_LEFT_PLAY_MODE, 0);
        startRightPlayMode = PreferencesUtils.getInt(this, SettingActivity.SP_RIGHT_PLAY_MODE, 0);
        stopPlayMode = PreferencesUtils.getInt(this, SettingActivity.SP_RIGHT_PLAY_MODE, 0);
        //交流时间
//        String voiceTime = PreferencesUtils.getString(this.getApplicationContext(), SettingPresenter.SP_VOICE_TIME);
//        if (voiceTime != null && !voiceTime.isEmpty()) {
//            waitTime = Integer.parseInt(voiceTime);
//        } else {
//            waitTime = 10;
//        }
        L.i(TAG, "waitTime=" + waitTime);

        //超声波设置数据
        ulDistanceBeen = ultrasonicDao.queryAll();
        if (ulDistanceBeen != null && ulDistanceBeen.size() > 0) {
            for (int i = 0; i < ulDistanceBeen.size(); i++) {
                flagsMap.put(ulDistanceBeen.get(i).getUltrasonicId(), false);
            }
            customUlData = new ArrayList<Integer>();
            for (int i = 0; i < ulDistanceBeen.size(); i++) {
                customUlData.add(ulDistanceBeen.get(i).getUltrasonicId());
            }
            customNumber = customUlData.size();
        }
        //每10秒发送移除睡眠
        han.sendEmptyMessageDelayed(START_TIMER_CLEAR_SLEEP, 1000);
        //是否有设置的探头信息
        if (getCustomUltrasonicData()) {
            try {
                SpeechManager.getInstance().closeSpeechDiscern(getApplicationContext());
//                boolean isAutoOpen = PreferencesUtils.getBoolean(getApplicationContext(), SettingPresenter.SP_IS_AUTO_OPEN);
//                if (isAutoOpen) {
//                    //开始标定模式
//                    TtsUtils.sendTts(getApplicationContext(), getString(R.string.init_ultrasonic_hint));
//                    mHandle.sendEmptyMessageDelayed(8, 3000);
//                } else
                {
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
                    mHandle.sendEmptyMessage(PLAY_MORE_ACTION);
                }
            }

        }
    };

    private final int PLAY_MORE_ACTION = 100;
    private final int LIGHT_END = 101;
    private final int LIGHT_OPEN = 102;
    private final int LIGHT_CLOSE = 103;
    private final int TTS_FINISH = 104;
    private final int LIGHT_ALWAYS_OPEN = 105;
    private boolean isCloseLight = true;

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
                    isLightFinish = true;
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
                    if(!isCloseLight) {
                        RobotManager.getInstance(UltrasonicService.this).getControlInstance().setLightBeltBrightness(240);
                        sendEmptyMessageDelayed(LIGHT_ALWAYS_OPEN, 800);
                    }
                    break;
                case TTS_FINISH:
                    if(isWelcomeTTsStart) {
                        ttsEnd();
                        isWelcomeTTsStart = false;
                    }
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
                        L.i(TAG, "numberNg = " + numberNg + "---myDistance = " + myDistance);
                        if (customUlData.contains(numberNg)) {
                            if (isConformDistance(valueNG, myDistance, numberNg)) {
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

                    flagsMap.put(ultrasonicId, true);
                    conformDistanceNum++;  //符合设定的距离就记忆次数
                    farDistanceNum = 0;
                    if (conformDistanceNum >= NUM_VALUE) {
                        conformDistanceNum = 0;
                        if (!mIsExecute) {
                            if (leftSelectNum.contains(ultrasonicId)) {
                                currentNeedPlay = START_LEFT_STRING;
                                L.i(TAG, "-----han----mIsExecute=" + mIsExecute);
                                mIsExecute = true;
                                isWelcomeTTsStart = true;
                                startPlay(currentNeedPlay);
                            } else if (rightSelectNum.contains(ultrasonicId)) {
                                currentNeedPlay = START_RIGHT_STRING;
                                L.i(TAG, "-----han----mIsExecute=" + mIsExecute);
                                mIsExecute = true;
                                isWelcomeTTsStart = true;
                                startPlay(currentNeedPlay);
                            }
                        }
                        return true;
                    }
                } else {
                    //记录距离
                    if (valueNG <= distance) {
                        flagsMap.put(ultrasonicId, true);
                    } else {
                        flagsMap.put(ultrasonicId, false);
                    }

                    farDistanceNum++;
                    L.i(TAG, "开始计时 farDistanceNum:" + farDistanceNum + "---isAllFaraway() = " + isAllFaraway() + "---mIsExecute = " + mIsExecute + "---isWelcomeTTsStart=" + isWelcomeTTsStart);
                    //全部检测
                    if (farDistanceNum >= NUM_FARVALUE * customNumber) {
                        //全部检测无人, 所有执行完毕播放结束语
                        if (mIsExecute) {
                            if (isTtsFinish == true && isFaceFinish == true && isLightFinish == true && isMusicFinish == true &&
                                    isPictureFinish == true && isActionFinish == true && isMediaFinish == true) {
                                L.i(TAG, "mIsExecute=" + mIsExecute);
                                if (mIsExecute) {
                                    startPlay(STOP_GUEST_STRING);
                                    han.removeMessages(0);
                                    if (mGroupTask != null) {
                                        mGroupTask.reset();
                                    }
                                    SpeechManager.getInstance().closeSpeechDiscern(getApplicationContext());
                                    isCloseLight = true;
                                    RobotManager.getInstance(getApplicationContext()).getControlInstance().setLightBeltBrightness(0);
                                }
                                mIsExecute = false;
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isAllPlayFinish() {
        boolean allFinish = false;
        L.e("isAllPlayFinish", "isTtsFinish = " + isTtsFinish + "--isFaceFinish = " + isFaceFinish + "--isLightFinish = " + isLightFinish + "--isMusicFinish = " + isMusicFinish + "--isPictureFinish = " + isPictureFinish +
                "--isActionFinish = " + isActionFinish + "--isMediaFinish = " + isMediaFinish);
        if (isTtsFinish == true && isFaceFinish == true && isLightFinish == true && isMusicFinish == true &&
                isPictureFinish == true && isActionFinish == true && isMediaFinish == true) {
            allFinish = true;
        }
        return allFinish;
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

    private boolean isFaceFinish, isTtsFinish, isLightFinish, isActionFinish, isMediaFinish, isMusicFinish, isPictureFinish;

    /**
     * type 0：开始迎宾 1：结束迎宾
     */
    private void startPlay(int type) {

        isTtsFinish = false;
        isFaceFinish = false;
        isLightFinish = false;
        isActionFinish = false;
        isMediaFinish = false;
        isMusicFinish = false;
        isPictureFinish = false;

        ItemsContentBean currentBean = null;
        if (type == START_LEFT_STRING) {
            if (itemsLeftContents != null && itemsLeftContents.size() > 0) {
                if (startLeftPlayMode == ORDER_PLAY) {
                    mStartLeftCurrentIndex++;
                    if (mStartLeftCurrentIndex > (itemsLeftContents.size() - 1)) {
                        mStartLeftCurrentIndex = 0;
                    }
                } else if (startLeftPlayMode == RANDOM_PLAY) {
                    mStartLeftCurrentIndex = (int) (Math.random() * (itemsLeftContents.size()));
                }
                currentBean = itemsLeftContents.get(mStartLeftCurrentIndex);
            }
        } else if (type == START_RIGHT_STRING) {
            if (itemsRightContents != null && itemsRightContents.size() > 0) {
                if (startRightPlayMode == ORDER_PLAY) {
                    mStartRightCurrentIndex++;
                    if (mStartRightCurrentIndex > (itemsRightContents.size() - 1)) {
                        mStartRightCurrentIndex = 0;
                    }
                } else if (startRightPlayMode == RANDOM_PLAY) {
                    mStartRightCurrentIndex = (int) (Math.random() * (itemsRightContents.size()));
                }
                currentBean = itemsRightContents.get(mStartRightCurrentIndex);
            }
        } else if (type == STOP_GUEST_STRING) {
            if (itemsEndContents != null && itemsEndContents.size() > 0) {
                if (stopPlayMode == ORDER_PLAY) {
                    mEndCurrentIndex++;
                    if (mEndCurrentIndex > (itemsEndContents.size() - 1)) {
                        mEndCurrentIndex = 0;
                    }
                } else if (stopPlayMode == RANDOM_PLAY) {
                    mEndCurrentIndex = (int) (Math.random() * (itemsEndContents.size()));
                }
                currentBean = itemsEndContents.get(mEndCurrentIndex);
            }
        }


        if (currentBean != null) {
            L.e("startPlay", "currentBean = " + currentBean.toString());
            //迎宾语和表情
            if (!TextUtils.isEmpty(currentBean.getOther())) {
                closeTTs();
                if (!TextUtils.isEmpty(currentBean.getFace())) {
                    TtsUtils.sendTts(getApplicationContext(), currentBean.getOther() + "@#;" + currentBean.getFace());
                } else
                    TtsUtils.sendTts(getApplicationContext(), currentBean.getOther());

                int ttsLength = currentBean.getOther().length();
                long ttsTime = ttsLength * wordSpeed;
                showTip("ttsLength=" + ttsLength + "- - ttsTime=" + ttsTime);
                mHandle.sendEmptyMessageDelayed(TTS_FINISH, ttsTime);

                SpeechManager.getInstance().openSpeechDiscern(getApplicationContext());
            } else {
                isTtsFinish = true;
                isFaceFinish = true;
            }

            //多动作播放
            executeAction(currentBean);

            //灯带
            executeLight(currentBean);

            if (!TextUtils.isEmpty(currentBean.getMedia())) {

                if (currentBean.getMedia().toLowerCase().endsWith(".mp4")) {
                    //播放视频
                    playAudio(currentBean.getMedia());
                    isPictureFinish = true;
                } else {
                    //播放图片
                    //正在播放图片
                    isPlayPicture = true;
                    if (!TextUtils.isEmpty(currentBean.getMedia())) {
                        File mFile = new File(currentBean.getMedia());
                        if (mFile.exists()) {
                            dialog = new Dialog(this, R.style.Dialog_Fullscreen);
                            View currentView = LayoutInflater.from(UltrasonicService.this).inflate(R.layout.ul_picture_dialog, null);
                            ImageView adPlayerPic = (ImageView) currentView.findViewById(R.id.ul_picture_img);
                            playAdPicture(adPlayerPic, mFile);
                            dialog.setContentView(currentView);
                            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                            dialog.show();
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
        }
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
                if (dialog != null) {
                    isPictureFinish = true;
                    dialog.dismiss();
                    openSpeechDiscern();
                }
            }
        }, time);
    }

    public void ttsEnd() {
        showTip("说话结束");
        isTtsFinish = true;
        isFaceFinish = true;
        openSpeechDiscern();
    }

    private void openSpeechDiscern() {
        if (isAllPlayFinish()) {
            showTip("开启语音识别");
            SpeechManager.getInstance().openSpeechDiscern(getApplicationContext());
            TtsUtils.sendTts(getApplicationContext(), "");
            isCloseLight = false;
            mHandle.sendEmptyMessage(LIGHT_ALWAYS_OPEN);
        }
    }

    /**
     * 播放音乐
     *
     * @param music 音乐路径
     */
    private void playMusic(String music) {
        //判断文件是否存在
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        assert mediaPlayer != null;
        mediaPlayer.playUrl(music, new MusicPlayer.OnMusicCompletionListener() {
            @Override
            public void onCompletion(boolean isPlaySuccess) {
                showTip("执行播放音乐结束");
                isMusicFinish = true;
                openSpeechDiscern();
            }

            @Override
            public void onPrepare(int Duration) {

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

            isLightFinish = false;

            if (light.equals("0")) {
                time = currentBean.getOpenLightTime();
            } else if (light.equals("2")) {
                time = currentBean.getFlickerLightTime();
            }

            if (mHandle != null) {
                if (TextUtils.isEmpty(time)) {
                    isLightFinish = true;
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
        //data[4] = (byte) 0x1F; 打开全部
        //data[5] = (byte) 0xFF;
        //开启后8秒左右收到回调
        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getApplication())).openUltrasonicFeedback(EnvUtil.ULGST001, (byte4 & 0xFFFF << 8) | (byte5 & 0xFFFF));
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
                if (customUlData.get(i) < 9) {
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


    /**
     * byte[5]
     */
    byte mByte1 = (byte) 0x01; // 探头1---索引0
    byte mByte2 = (byte) 0x02; // 探头2---
    byte mByte3 = (byte) 0x04; // 探头3---
    byte mByte7 = (byte) 0x40; // 探头7---
    byte mByte8 = (byte) 0x80; // 探头8---
    /**
     * byte[4]
     */
    byte mByte9 = (byte) 0x01; // 探头9---
    byte mByte10 = (byte) 0x02; // 探头10---
    byte mByte11 = (byte) 0x04; // 探头11---
    byte mByte12 = (byte) 0x08; // 探头12---
    byte mByte13 = (byte) 0x10; // 探头13---

    Map<Integer, Byte> ultrasonicOpenMap = null;

    /**
     * 设置要打开的的探头字节
     * position 探头的下标
     */
    private void initAllOpenData() {
        if (ultrasonicOpenMap == null) {
            ultrasonicOpenMap = new HashMap<Integer, Byte>();
            ultrasonicOpenMap.put(0, mByte1);
            ultrasonicOpenMap.put(1, mByte2);
            ultrasonicOpenMap.put(2, mByte3);
            ultrasonicOpenMap.put(6, mByte7);
            ultrasonicOpenMap.put(7, mByte8);

            ultrasonicOpenMap.put(8, mByte9);
            ultrasonicOpenMap.put(9, mByte10);
            ultrasonicOpenMap.put(10, mByte11);
            ultrasonicOpenMap.put(11, mByte12);
            ultrasonicOpenMap.put(12, mByte13);
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
            mHandle.sendEmptyMessageDelayed(7, 5000);
        }
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
                    closeEveryOne();
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
//        closeUltrasonic(this);// 关闭超声波，暂不关
        RobotManager.getInstance(this).unRegisterOnGetUltrasonicCallBack();
        //TODO 新策略
        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getApplication())).closeUltrasonicFeedback(EnvUtil.ULGST001);
//        RobotManager.getInstance(this).unRegisterOnGetInfraredCallBack();
        RobotManager.getInstance(this).getNavigationInstance().unRegisterOnNavigationStateChangeListener(this);
        RobotManager.getInstance(this).unRegisterOnWheelStateChangeListener();

        if (mediaPlayer != null)
            mediaPlayer.stop();

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
            mHandle.removeMessages(LIGHT_CLOSE);
            mHandle.removeMessages(LIGHT_END);
            mHandle.removeMessages(LIGHT_OPEN);
            mHandle.removeMessages(TTS_FINISH);
        }
        closeRepeatLight();
        mHandle = null;
    }

    @Override
    public void onUltrasonicOccupyState(String sceneCode, int isAvailable) {
        L.e(TAG, "sceneCode = " + sceneCode + "---isAvailable = " + isAvailable);
    }

    @Override
    public void onRobotSateChange(int robotStateIndex, int newState) {
        if(robotStateIndex == RobotState.ROBOT_STATE_INDEX_HEAD_KEY) {

        }
    }
}
