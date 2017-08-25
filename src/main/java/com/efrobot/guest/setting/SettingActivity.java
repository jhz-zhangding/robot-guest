package com.efrobot.guest.setting;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.efrobot.guest.R;
import com.efrobot.guest.action.AddBodyShowView;
import com.efrobot.guest.base.GuestsBaseActivity;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.bean.Location;
import com.efrobot.guest.bean.WeekBean;
import com.efrobot.guest.dao.DataManager;
import com.efrobot.guest.explain.ExplainActivity;
import com.efrobot.guest.service.UltrasonicService;
import com.efrobot.guest.utils.CustomHintDialog;
import com.efrobot.guest.utils.DatePickerUtils;
import com.efrobot.guest.utils.PreferencesUtils;
import com.efrobot.library.RobotManager;
import com.efrobot.library.RobotState;
import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.speechsdk.SpeechManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/3/2.
 */
public class SettingActivity extends GuestsBaseActivity<SettingPresenter> implements ISettingView, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static int MyUlNum = 6;
    private TextView ulDistanceText1, ulDistanceText2, ulDistanceText3,
            ulDistanceText4, ulDistanceText5, ulDistanceText6;

    //是否自动开始标定
    private CheckBox settingIsOpen;

    //定时任务设置
    private EditText timerStartEdit, timerEndEdit, timerPlace;
    private Button chooseStartDayBtn, chooseEndDayBtn, saveStartBtn;
    private TextView chooseStartTxt, chooseEndTxt;

    //用户设置距离
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;
    private EditText ulDistanceEdit1, ulDistanceEdit2, ulDistanceEdit3,
            ulDistanceEdit4, ulDistanceEdit5, ulDistanceEdit6;
    //迎宾语 结束语
    private ListView greetingListView;
    private ListView endListView;
    private ImageView addGreetBtn, addEndBtn;
    private ImageView delGreetBtn, delEndBtn;
    private ImageView startPlayMode, endPlayMode;
    private LinearLayout greetingShowListBtnLL, endShowListBtnLL;
    private ImageView greetingShowListBtn, endShowListBtn;

    //保存设置
    private TextView mCancle, mAffirm, mTestUlBtn;
    private ImageView mStartBtn;
    private final int WELCOME_REQUEST = 1;
    private final int END_REQUEST = 2;

    //交流模式
    private RadioGroup modeSettingRg;
    private RadioButton voiceMode, customMode;
    public static int selectedVoiceMode = 0;
    public static int selectedCustomMode = 1;
    private int selectedMode = 0;
    private TextView voiceTime;

    public static String SP_START_PLAY_MODE = "sp_start_play_mode";
    public static String SP_STOP_PLAY_MODE = "sp_stop_play_mode";

    //超声波测试数据
    private CheckBox[] myCheck = new CheckBox[]{checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6};
    private EditText[] myEdit = new EditText[]{ulDistanceEdit1, ulDistanceEdit2, ulDistanceEdit3, ulDistanceEdit4,
            ulDistanceEdit5, ulDistanceEdit6};

    private Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    int maskSwitch = RobotState.getInstance(getContext()).getMaskState();
                    L.i(TAG, "面罩maskSwitch:" + maskSwitch);
                    if (maskSwitch == 0) {
                        mServiceIntent = new Intent(getContext(), UltrasonicService.class);
                        getContext().getApplicationContext().startService(mServiceIntent);
                        showToast("开始迎宾");

                    }
                    break;
            }
        }
    };
    private int startMode;
    private int stopMode;

    @Override
    public BasePresenter createPresenter() {
        return new SettingPresenter(this);
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_setting;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onViewInit() {
        super.onViewInit();

        SpeechManager.getInstance().registerSpeechSDK(this, mISpeech);

        initViewId();

        greetingListView.setAdapter(((SettingPresenter) mPresenter).getGreetingAdapter(true));
        endListView.setAdapter(((SettingPresenter) mPresenter).getEndGreetingAdapter(true));

        updatePlayMode(false);
        updateStopMode(false);
        isShowStartSpreadBtn(false);
        isShowFinishSpreadBtn(false);

        int savedMode = PreferencesUtils.getInt(this.getApplicationContext(), "mMode", 0);
        selectedMode = savedMode;
        if (savedMode == SettingActivity.selectedVoiceMode) {
            voiceMode.setChecked(true);
        } else if (savedMode == SettingActivity.selectedCustomMode) {
            customMode.setChecked(true);
        }
        modeSettingRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.voice_communication_mode) {
                    selectedMode = selectedVoiceMode;
                } else if (i == R.id.user_custom_mode) {
                    selectedMode = selectedCustomMode;
                }
            }
        });

    }

    private void initViewId() {
        //传感器数据
        ulDistanceText1 = (TextView) findViewById(R.id.ul_place_test_edit1);
        ulDistanceText2 = (TextView) findViewById(R.id.ul_place_test_edit2);
        ulDistanceText3 = (TextView) findViewById(R.id.ul_place_test_edit3);
        ulDistanceText4 = (TextView) findViewById(R.id.ul_place_test_edit4);
        ulDistanceText5 = (TextView) findViewById(R.id.ul_place_test_edit5);
        ulDistanceText6 = (TextView) findViewById(R.id.ul_place_test_edit6);

        settingIsOpen = (CheckBox) findViewById(R.id.setting_is_open);

        //设置数据
        myCheck[0] = (CheckBox) findViewById(R.id.ul_place_ck1);
        myCheck[1] = (CheckBox) findViewById(R.id.ul_place_ck2);
        myCheck[2] = (CheckBox) findViewById(R.id.ul_place_ck3);
        myCheck[3] = (CheckBox) findViewById(R.id.ul_place_ck4);
        myCheck[4] = (CheckBox) findViewById(R.id.ul_place_ck5);
        myCheck[5] = (CheckBox) findViewById(R.id.ul_place_ck6);
        myEdit[0] = (EditText) findViewById(R.id.ul_place_edit1);
        myEdit[1] = (EditText) findViewById(R.id.ul_place_edit2);
        myEdit[2] = (EditText) findViewById(R.id.ul_place_edit3);
        myEdit[3] = (EditText) findViewById(R.id.ul_place_edit4);
        myEdit[4] = (EditText) findViewById(R.id.ul_place_edit5);
        myEdit[5] = (EditText) findViewById(R.id.ul_place_edit6);

        //定时任务
        timerStartEdit = (EditText) findViewById(R.id.timer_start_edit);
        timerEndEdit = (EditText) findViewById(R.id.timer_end_edit);
        timerPlace = (EditText) findViewById(R.id.timer_choose_place);
        chooseStartTxt = (TextView) findViewById(R.id.timer_start_select_day);
        chooseEndTxt = (TextView) findViewById(R.id.timer_end_select_day);


        chooseStartDayBtn = (Button) findViewById(R.id.timer_start_choose_days);
        chooseEndDayBtn = (Button) findViewById(R.id.timer_end_choose_days);
        saveStartBtn = (Button) findViewById(R.id.timer_save_start);

        //迎宾语设置
        greetingListView = (ListView) findViewById(R.id.greeting_set_lv);
        addGreetBtn = (ImageView) findViewById(R.id.greeting_add_im);
        delGreetBtn = (ImageView) findViewById(R.id.start_delete_img);
        startPlayMode = (ImageView) findViewById(R.id.greeting_play_mode);
        greetingShowListBtn = (ImageView) findViewById(R.id.greeting_show_list_img);
        greetingShowListBtnLL = (LinearLayout) findViewById(R.id.greeting_show_list_img_ll);

        endListView = (ListView) findViewById(R.id.end_set_lv);
        addEndBtn = (ImageView) findViewById(R.id.end_add_im);
        delEndBtn = (ImageView) findViewById(R.id.end_delete_img);
        endPlayMode = (ImageView) findViewById(R.id.end_play_mode);
        endShowListBtn = (ImageView) findViewById(R.id.end_show_list_img);
        endShowListBtnLL = (LinearLayout) findViewById(R.id.end_show_list_img_ll);

        //模式
        modeSettingRg = (RadioGroup) findViewById(R.id.mode_setting);
        voiceMode = (RadioButton) findViewById(R.id.voice_communication_mode);
        customMode = (RadioButton) findViewById(R.id.user_custom_mode);

        //按键
        mCancle = (TextView) findViewById(R.id.cancel);
        mAffirm = (TextView) findViewById(R.id.affirm);
        mStartBtn = (ImageView) findViewById(R.id.ultrasonic_open_btn);
        mTestUlBtn = (TextView) findViewById(R.id.ultrasonic_test_btn);
        mTestUlBtn.setVisibility(View.GONE);

        voiceTime = (TextView) findViewById(R.id.voice_time);
    }

    String data = "[\n" +
            "  {\n" +
            "    \"code\": \"1000\",\n" +
            "    \"ins\": \"开始迎宾\",\n" +
            "    \"packageName\": \"com.efrobot.guest\",\n" +
            "    \"reply\": \"好的\",\n" +
            "    \"type\": \"0\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"code\": \"1001\",\n" +
            "    \"ins\": \"关闭迎宾\",\n" +
            "    \"packageName\": \"com.efrobot.guest\",\n" +
            "    \"reply\": \"好的\",\n" +
            "    \"type\": \"0\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"code\": \"1001\",\n" +
            "    \"ins\": \"结束迎宾\",\n" +
            "    \"packageName\": \"com.efrobot.guest\",\n" +
            "    \"reply\": \"好的\",\n" +
            "    \"type\": \"0\"\n" +
            "  }\n" +
            "]";


    public void setData() {
        try {
            SpeechManager.getInstance().setData(this, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    SpeechManager.ISpeech mISpeech = new SpeechManager.ISpeech() {

        @Override
        public void onRegisterSuccess() {

            setData();
            Log.e(TAG, " onRegisterSuccess");
        }

        @Override
        public void onRegisterFail() {
            Log.e(TAG, " onRegisterFail");

        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    RobotManager.getInstance(SettingActivity.this).getWheelInstance().moveFront(100);
                    break;
            }
        }
    };

    private void stopAction() {
        Log.i(TAG, "关闭轮子");
        RobotManager.getInstance(this).getGroupInstance().stop();
        RobotManager.getInstance(this).getSpeechGroupManager().stop();
        RobotManager.getInstance(this).getDanceInstance().stop();
        RobotManager.getInstance(this).getGroupInstance().stop();
    }

    @Override
    protected void setOnListener() {
        super.setOnListener();
        for (int i = 0; i < myEdit.length; i++) {
            if (TextUtils.isEmpty(myEdit[i].getText())) {
                myEdit[i].setFocusableInTouchMode(false);
            } else {
                myEdit[i].setFocusableInTouchMode(true);
            }
            myCheck[i].setOnCheckedChangeListener(this);
        }
        mCancle.setOnClickListener(this);
        mAffirm.setOnClickListener(this);
        mStartBtn.setOnClickListener(this);
//        mTestUlBtn.setOnClickListener(this);

        findViewById(R.id.explain).setOnClickListener(this);
        findViewById(R.id.ultrasonic_init_btn).setOnClickListener(this);

        addGreetBtn.setOnClickListener(this);
        addEndBtn.setOnClickListener(this);
        delGreetBtn.setOnClickListener(this);
        delEndBtn.setOnClickListener(this);
        startPlayMode.setOnClickListener(this);
        endPlayMode.setOnClickListener(this);
        greetingShowListBtnLL.setOnClickListener(this);
        endShowListBtnLL.setOnClickListener(this);

        //定时任务
        chooseStartDayBtn.setOnClickListener(this);
        chooseEndDayBtn.setOnClickListener(this);
        timerStartEdit.setFocusable(false);
        timerEndEdit.setFocusable(false);
        timerPlace.setFocusable(false);
        DatePickerUtils.getInstance().setDataPickDialog(timerStartEdit, this);
        DatePickerUtils.getInstance().setDataPickDialog(timerEndEdit, this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                ((SettingPresenter) mPresenter).cancle();
                break;
            case R.id.affirm:
                ((SettingPresenter) mPresenter).affrim();
                break;
            case R.id.ultrasonic_open_btn: //开始迎宾
                ((SettingPresenter) mPresenter).affrim();
                showDialog("即将开始迎宾，请关闭面罩后开始迎宾");
                break;
            case R.id.ultrasonic_init_btn:
                //初始化超声波
                ((SettingPresenter) mPresenter).showDialog(getString(R.string.init_ultrasonic_hint));
                break;
            case R.id.timer_start_choose_days:
                String selectedDays = PreferencesUtils.getString(this, "selectedStartDays");
                setDialog(selectedDays, 0);
                break;
            case R.id.timer_end_choose_days:
                String selectedEndDays = PreferencesUtils.getString(this, "selectedEndDays");
                setDialog(selectedEndDays, 1);
                break;
            case R.id.explain:
                /***
                 * 打开使用说明页面
                 */
                startActivity(new Intent(this, ExplainActivity.class));
                break;
            case R.id.greeting_add_im:
                Intent intent = new Intent(this, AddBodyShowView.class);
                intent.putExtra("itemNum", 1);
                startActivityForResult(intent, WELCOME_REQUEST);
                break;
            case R.id.end_add_im:
                Intent intentEnd = new Intent(this, AddBodyShowView.class);
                intentEnd.putExtra("itemNum", 2);
                startActivityForResult(intentEnd, END_REQUEST);
                break;
            case R.id.start_delete_img:
                isShowStartSpreadBtn(true);
                ((SettingPresenter) mPresenter).getGreetingAdapter(false).setDelVisible(new GreetingAdapter.OnDeleteItemListener() {
                    @Override
                    public void existLessTwo(boolean isExistLessTwo) {
                        if(isExistLessTwo) {
                            greetingShowListBtn.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            case R.id.end_delete_img:
                isShowFinishSpreadBtn(true);
                ((SettingPresenter) mPresenter).getEndGreetingAdapter(false).setDelVisible(new GreetingAdapter.OnDeleteItemListener() {
                    @Override
                    public void existLessTwo(boolean isExistLessTwo) {
                        if(isExistLessTwo) {
                            endShowListBtn.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            case R.id.greeting_play_mode:
                updatePlayMode(true);
                break;
            case R.id.end_play_mode:
                updateStopMode(true);
                break;
            case R.id.greeting_show_list_img_ll:
                showListView(view);
                break;
            case R.id.end_show_list_img_ll:
                showListView(view);
                break;
        }
    }

    private void updatePlayMode(boolean isClick) {
        int mode = PreferencesUtils.getInt(this, SP_START_PLAY_MODE, 0);
        if (mode == 0) {
            if (isClick) {
                startPlayMode.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shuffle_play_pressed));
                PreferencesUtils.putInt(this, SP_START_PLAY_MODE, 1);
                showToast("随机播放");
            } else {
                startPlayMode.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.order_play_pressed));
            }
        } else if (mode == 1) {
            if (isClick) {
                startPlayMode.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.order_play_pressed));
                PreferencesUtils.putInt(this, SP_START_PLAY_MODE, 0);
                showToast("列表循环");
            } else {
                startPlayMode.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shuffle_play_pressed));
            }
        }


    }

    private void updateStopMode(boolean isClick) {
        int stopMode = PreferencesUtils.getInt(this, SP_STOP_PLAY_MODE, 0);
        if (stopMode == 0) {
            if (isClick) {
                endPlayMode.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shuffle_play_pressed));
                PreferencesUtils.putInt(this, SP_STOP_PLAY_MODE, 1);
                showToast("随机播放");
            } else {
                endPlayMode.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.order_play_pressed));
            }
        } else if (stopMode == 1) {
            if (isClick) {
                endPlayMode.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.order_play_pressed));
                PreferencesUtils.putInt(this, SP_STOP_PLAY_MODE, 0);
                showToast("列表循环");
            } else {
                endPlayMode.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shuffle_play_pressed));
            }
        }
    }

    private boolean isShowGreeting = false;
    private boolean isShowEnd = false;

    /**
     * isShow 是否展开 开始迎宾条目
     */
    private void isShowStartSpreadBtn(boolean isShowStart) {
        List<ItemsContentBean> startList = DataManager.getInstance(getContext()).queryItem(1);
        if (startList != null && startList.size() > 1) {
            greetingShowListBtn.setVisibility(View.VISIBLE);
            if (isShowStart) {
                //展开
                isShowGreeting = true;
                ((SettingPresenter) mPresenter).getGreetingAdapter(false).notifyDataSetChanged();
                greetingShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shrink));
            } else
                greetingShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.open));
        } else {
            ((SettingPresenter) mPresenter).getGreetingAdapter(true).notifyDataSetChanged();
            greetingShowListBtn.setVisibility(View.GONE);
        }

    }

    /**
     * isShow 是否展开 结束迎宾条目
     */
    private void isShowFinishSpreadBtn(boolean isShowFinish) {
        List<ItemsContentBean> endList = DataManager.getInstance(getContext()).queryItem(2);
        if (endList != null && endList.size() > 1) {
            endShowListBtn.setVisibility(View.VISIBLE);
            endShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shrink));
            if (isShowFinish) {
                isShowEnd = true;
                ((SettingPresenter) mPresenter).getEndGreetingAdapter(false).notifyDataSetChanged();
            } else
                endShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.open));
        } else {
            ((SettingPresenter) mPresenter).getEndGreetingAdapter(true).notifyDataSetChanged();
            endShowListBtn.setVisibility(View.GONE);
        }
    }

    private void showListView(View imageView) {
        if (imageView.equals(greetingShowListBtnLL)) {
            isShowGreeting = !isShowGreeting;
            if (isShowGreeting) {
                greetingShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shrink));
                ((SettingPresenter) mPresenter).getGreetingAdapter(false).notifyDataSetChanged();
            } else {
                greetingShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.open));
                ((SettingPresenter) mPresenter).getGreetingAdapter(true).notifyDataSetChanged();
            }
        } else if (imageView.equals(endShowListBtnLL)) {
            isShowEnd = !isShowEnd;
            if (isShowEnd) {
                endShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shrink));
                ((SettingPresenter) mPresenter).getEndGreetingAdapter(false).notifyDataSetChanged();
            } else {
                endShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.open));
                ((SettingPresenter) mPresenter).getEndGreetingAdapter(true).notifyDataSetChanged();
            }
        }
    }

    private void setDialog(String selectedDays, final int type) {
        if (TextUtils.isEmpty(selectedDays)) {
            selectedDays = "周一:周二:周三:周四:周五";
        }
        List<WeekBean> startWeekLists = initWeekData(selectedDays);
        DatePickerUtils.getInstance().setDayPickDialog(startWeekLists, this, new DatePickerUtils.OnDayCheckListener() {
            @Override
            public void onCheckListData(LinkedHashMap<String, Boolean> maps) {
                StringBuilder stringBuilder = new StringBuilder();
                for (LinkedHashMap.Entry<String, Boolean> map : maps.entrySet()) {
                    boolean ischeck = map.getValue();
                    if (ischeck) {
                        stringBuilder.append(map.getKey() + ":");
                    }
                }
                if (stringBuilder.toString().contains(":")) {
                    stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(":"));
                }

                if (type == 0) {
                    PreferencesUtils.putString(SettingActivity.this, "selectedStartDays", stringBuilder.toString());
                    chooseStartTxt.setText(stringBuilder.toString());
                    if (TextUtils.isEmpty(chooseStartTxt.getText())) {
                        chooseStartTxt.setText("周一:周二:周三:周四:周五");
                    }
                } else if (type == 1) {
                    PreferencesUtils.putString(SettingActivity.this, "selectedEndDays", stringBuilder.toString());
                    chooseEndTxt.setText(stringBuilder.toString());
                    if (TextUtils.isEmpty(chooseEndTxt.getText())) {
                        chooseEndTxt.setText("周一:周二:周三:周四:周五");
                    }
                }

            }
        });
    }

    private List<WeekBean> initWeekData(String selectedDays) {
        List<WeekBean> startWeekLists = new ArrayList<WeekBean>();

        String[] days = selectedDays.split(":");
        for (int i = 0; i < days.length; i++) {
            WeekBean weekBean = new WeekBean();
            weekBean.setDay(days[i]);
            weekBean.setCheck(true);
            startWeekLists.add(weekBean);
        }


        return startWeekLists;
    }

    @Override
    public int getIsOpenValue(int number) {
        return (myCheck[number].isChecked() ? 1 : 0);
    }

    @Override
    public String getOpenDistanceValue(int number) {
        if (myEdit[number].getText().toString().isEmpty()) {
            return "";
        }
        return myEdit[number].getText().toString();
    }

    @Override
    public void setIsOpenValue(int number, int isOpenValue) {
        myCheck[number].setChecked(isOpenValue == 1 ? true : false);
    }

    @Override
    public void setOpenDistanceValue(int number, String openDistanceValue) {
        myEdit[number].setEnabled(true);
        myEdit[number].setText(openDistanceValue);
    }

    @Override
    public int getExchangeMode() {
        return selectedMode;
    }

    @Override
    public void setVoiceTime(String time) {
        voiceTime.setText(time);
    }

    @Override
    public String getVoiceTime() {
        return voiceTime.getText().toString();
    }

    @Override
    public void setDistance0(String distance) {
        ulDistanceText1.setText(distance);
    }

    @Override
    public void setDistance1(String distance) {
        ulDistanceText2.setText(distance);
    }

    @Override
    public void setDistance7(String distance) {
        ulDistanceText3.setText(distance);
    }

    @Override
    public void setDistance8(String distance) {
        ulDistanceText4.setText(distance);
    }

    @Override
    public void setDistance9(String distance) {
        ulDistanceText5.setText(distance);
    }

    @Override
    public void setDistance10(String distance) {
        ulDistanceText6.setText(distance);
    }

    @Override
    public CheckBox getIsAutoOpen() {
        return settingIsOpen;
    }

    @Override
    public void setAutoOpen(boolean isAuto) {
        settingIsOpen.setChecked(isAuto);
    }

    @Override
    public void setStartTime(String startTime) {
        timerStartEdit.setText(startTime);
    }

    @Override
    public void setEndTime(String endTime) {
        timerEndEdit.setText(endTime);
    }

    @Override
    public void setTimerPlace(String guestPlace) {
        timerPlace.setText(guestPlace);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.ul_place_ck1:
                setCheckChange(compoundButton, 0);
                break;
            case R.id.ul_place_ck2:
                setCheckChange(compoundButton, 1);
                break;
            case R.id.ul_place_ck3:
                setCheckChange(compoundButton, 2);
                break;
            case R.id.ul_place_ck4:
                setCheckChange(compoundButton, 3);
                break;
            case R.id.ul_place_ck5:
                setCheckChange(compoundButton, 4);
                break;
            case R.id.ul_place_ck6:
                setCheckChange(compoundButton, 5);
                break;
        }
    }

    private void setCheckChange(CompoundButton compoundButton, int position) {
        if (compoundButton.isChecked()) {
            myEdit[position].setFocusableInTouchMode(true);
            myEdit[position].setFocusable(true);
            myEdit[position].requestFocus();
        } else {
            myEdit[position].setFocusableInTouchMode(false);
            myEdit[position].setFocusable(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.i(TAG, "SettingActivity onPause");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) { //从设置回来刷新
            isShowStartSpreadBtn(true);
        } else if (requestCode == 2 && resultCode == 1) {
            isShowFinishSpreadBtn(true);
        }
    }

    @Override
    protected void onDestroy() {
        L.i(TAG, "SettingActivity onDestroy");
        RobotManager.getInstance(getApplicationContext()).getControlInstance().setLightBeltBrightness(0);
        if (mServiceIntent != null) {
            getContext().getApplicationContext().stopService(mServiceIntent);
        }
//        if (lidBoardReceive != null) {
//            unregisterReceiver(lidBoardReceive);
//        }
        super.onDestroy();

    }

    /**
     * 任务地点
     */

    private AlertDialog placeDialog;

    private void initTimerPlaceData() {
        ArrayList<Location> locationList = query(getContext());
        if (locationList == null || locationList.size() == 0) {
            Toast.makeText(getContext(), "无迎宾地点", Toast.LENGTH_SHORT).show();
            return;
        }
        placeDialog = new AlertDialog.Builder(getContext()).create();
        placeDialog.setCancelable(true);
        placeDialog.show();
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.timer_place_dialog, null);
//        placeDialog.getWindow().setContentView(R.layout.timer_place_dialog);
        placeDialog.getWindow().setContentView(dialogView);
        ListView listView = (ListView) dialogView.findViewById(R.id.timer_place_list_view);

        PlaceAdapter adapter = new PlaceAdapter(getContext(), locationList);
        listView.setAdapter(adapter);


    }

    private class PlaceAdapter extends BaseAdapter {

        ArrayList<Location> locationList;
        Context context;

        private PlaceAdapter(Context context, ArrayList<Location> locationList) {
            this.locationList = locationList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return locationList.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            convertView = LayoutInflater.from(context).inflate(R.layout.timer_place_item, viewGroup, false);
            TextView placeText = (TextView) convertView.findViewById(R.id.timer_place_item_text);

            if (locationList != null && locationList.size() > 0) {
                placeText.setText(locationList.get(i).getLocation_name());
                placeText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        timerPlace.setText(locationList.get(i).getLocation_name());
                        if (placeDialog != null) {
                            placeDialog.dismiss();
                        }
                    }
                });
            }

            return convertView;
        }
    }

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

    /**
     * 提示框
     */
    private Intent mServiceIntent;
    private CustomHintDialog dialog;

    public void showDialog(String content) {
        alreadyClosed = false;
        dialog = new CustomHintDialog(getContext(), -1);
        dialog.setMessage(content);
        dialog.setCancelable(true);
        dialog.show();
        IntentFilter dynamic_filter = new IntentFilter();
        dynamic_filter.addAction(ROBOT_MASK_CHANGE);            //添加动态广播的Action
        getContext().registerReceiver(lidBoardReceive, dynamic_filter);
        dialog.setOnDismissListener(new CustomHintDialog.OnDismissListener() {
            @Override
            public void onDismiss() {
//                if(dialog != null && !dialog.isShowing()) {
//                    getContext().unregisterReceiver(lidBoardReceive);
//                }
            }
        });
    }

    public final static String ROBOT_MASK_CHANGE = "android.intent.action.MASK_CHANGED";
    public final static String KEYCODE_MASK_ONPROGRESS = "KEYCODE_MASK_ONPROGRESS"; //开闭状态
    public final static String KEYCODE_MASK_CLOSE = "KEYCODE_MASK_CLOSE"; //关闭面罩
    public final static String KEYCODE_MASK_OPEN = "KEYCODE_MASK_OPEN";  //打开面罩
    private boolean alreadyClosed = false;
    private BroadcastReceiver lidBoardReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ROBOT_MASK_CHANGE.equals(intent.getAction())) {
                boolean close = intent.getBooleanExtra(KEYCODE_MASK_CLOSE, false);
                boolean maskOnProgress = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
                boolean maskOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);
                L.i(TAG, "lidBoardReceive get data----" + "close----" + close + "  maskOnProgress----" + maskOnProgress + "  maskOpen----" + maskOpen);
                if (close) {
                    alreadyClosed = true;
                    try {
                        if (null != dialog) {
                            dialog.dismiss();
                        }
                        SpeechManager.getInstance().removeSpeechState(getContext().getApplicationContext(), 11);
                        L.i(TAG, "lidBoardReceive close----" + close + "   start service");
                        //开启迎宾
                        mServiceIntent = new Intent(getContext(), UltrasonicService.class);
                        getContext().getApplicationContext().startService(mServiceIntent);
                        UltrasonicService.IsOpenRepeatLight = false;
                        unregisterReceiver(lidBoardReceive);
                        L.i(TAG, "lidBoardReceive unregisterReceiver(this)");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };


}
