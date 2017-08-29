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
public class SettingActivity extends GuestsBaseActivity<SettingPresenter> implements ISettingView, View.OnClickListener {

    private List<GreetingAdapter> adapterList;

    public static int MyUlNum = 6;
    private TextView ulDistanceText1, ulDistanceText2, ulDistanceText3, ulDistanceText7, ulDistanceText8;
    private TextView ulDistanceText9, ulDistanceText10, ulDistanceText11, ulDistanceText12, ulDistanceText13;

    //用户设置距离
    private EditText ulDistanceEdit1, ulDistanceEdit2, ulDistanceEdit3,
            ulDistanceEdit7, ulDistanceEdit8, ulDistanceEdit9,
            ulDistanceEdit10, ulDistanceEdit11, ulDistanceEdit12, ulDistanceEdit13;

    //迎宾语 结束语
    private ListView leftListView;
    private ListView rightListView;
    private ListView finishListView;

    //播放模式
    private ImageView leftPlayModeImg;
    private ImageView rightPlayModeImg;
    private ImageView finishPlayModeImg;

    private ImageView addRightGreetBtn, addLeftGreetBtn, addFinishGreetBtn;
    private ImageView delRightGreetBtn, delLeftGreetBtn, delFinishGreetBtn;

    //展开
    private LinearLayout leftShowListBtnLL, rightShowListBtnLL, finishShowListBtnLL;
    private ImageView leftShowListBtn, rightShowListBtn, finishShowListBtn;

    //选择超声波
    private TextView leftUltrasonicTv,rightUltrasonicTv;
    private ImageView leftUltrasonicBtn, rightUltrasonicBtn;

    //保存设置
    private TextView mCancel, mAffirm;
    private ImageView mStartBtn;
    private final int LEFT_REQUEST_CODE = 1;
    private final int RIGHT_REQUEST_CODE = 2;
    private final int FINISH_REQUEST_CODE = 3;

    public static String SP_START_PLAY_MODE = "sp_start_play_mode";
    public static String SP_STOP_PLAY_MODE = "sp_stop_play_mode";

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

        adapterList = new ArrayList<GreetingAdapter>();
        adapterList.add(((SettingPresenter) mPresenter).getLeftGreetingAdapter());
        adapterList.add(((SettingPresenter) mPresenter).getRightGreetingAdapter());
        adapterList.add(((SettingPresenter) mPresenter).getFinishGreetingAdapter());

        adapterList.get(0).isShowAllData(1, false);
        adapterList.get(1).isShowAllData(2, false);
        adapterList.get(2).isShowAllData(3, false);

        leftListView.setAdapter(adapterList.get(0));
        rightListView.setAdapter(adapterList.get(1));
        finishListView.setAdapter(adapterList.get(2));

        updateExpandView();
        updatePlayModeView(leftPlayModeImg, false);
        updatePlayModeView(rightPlayModeImg, false);
        updatePlayModeView(finishPlayModeImg, false);

    }

    private void initViewId() {
        //传感器数据
        ulDistanceText1 = (TextView) findViewById(R.id.ul_place_test_edit1);
        ulDistanceText2 = (TextView) findViewById(R.id.ul_place_test_edit2);
        ulDistanceText3 = (TextView) findViewById(R.id.ul_place_test_edit3);
        ulDistanceText7 = (TextView) findViewById(R.id.ul_place_test_edit7);
        ulDistanceText8 = (TextView) findViewById(R.id.ul_place_test_edit8);

        ulDistanceText9 = (TextView) findViewById(R.id.ul_place_test_edit9);
        ulDistanceText10 = (TextView) findViewById(R.id.ul_place_test_edit10);
        ulDistanceText11 = (TextView) findViewById(R.id.ul_place_test_edit11);
        ulDistanceText12 = (TextView) findViewById(R.id.ul_place_test_edit12);
        ulDistanceText13 = (TextView) findViewById(R.id.ul_place_test_edit13);

        //设置数据
//        ulDistanceEdit1 = (EditText) findViewById(R.id.ul_place_edit1);
//        ulDistanceEdit2 = (EditText) findViewById(R.id.ul_place_edit2);
//        ulDistanceEdit3 = (EditText) findViewById(R.id.ul_place_edit3);
//        ulDistanceEdit7 = (EditText) findViewById(R.id.ul_place_edit7);
//        ulDistanceEdit8 = (EditText) findViewById(R.id.ul_place_edit8);
//        ulDistanceEdit9 = (EditText) findViewById(R.id.ul_place_edit9);
//        ulDistanceEdit10 = (EditText) findViewById(R.id.ul_place_edit10);
//        ulDistanceEdit11 = (EditText) findViewById(R.id.ul_place_edit11);
//        ulDistanceEdit12 = (EditText) findViewById(R.id.ul_place_edit12);
//        ulDistanceEdit13 = (EditText) findViewById(R.id.ul_place_edit13);

        //迎宾语设置
        leftListView = (ListView) findViewById(R.id.left_greeting_set_lv);
        rightListView = (ListView) findViewById(R.id.right_greeting_set_lv);
        finishListView = (ListView) findViewById(R.id.finish_greeting_set_lv);

        //播放模式
        leftPlayModeImg = (ImageView) findViewById(R.id.left_greeting_play_mode);
        rightPlayModeImg = (ImageView) findViewById(R.id.right_greeting_play_mode);
        finishPlayModeImg = (ImageView) findViewById(R.id.finish_greeting_play_mode);

        //添加
        addLeftGreetBtn = (ImageView) findViewById(R.id.left_greeting_add_im);
        addRightGreetBtn = (ImageView) findViewById(R.id.right_greeting_add_im);
        addFinishGreetBtn = (ImageView) findViewById(R.id.finish_greeting_add_im);

        //删除
        delLeftGreetBtn = (ImageView) findViewById(R.id.left_greeting_delete_img);
        delRightGreetBtn = (ImageView) findViewById(R.id.right_greeting_delete_img);
        delFinishGreetBtn = (ImageView) findViewById(R.id.finish_greeting_delete_img);

        leftShowListBtnLL = (LinearLayout) findViewById(R.id.left_greeting_show_list_img_ll);
        rightShowListBtnLL = (LinearLayout) findViewById(R.id.right_greeting_show_list_img_ll);
        finishShowListBtnLL = (LinearLayout) findViewById(R.id.finish_greeting_show_list_img_ll);
        leftShowListBtn = (ImageView) findViewById(R.id.left_greeting_show_list_img);
        rightShowListBtn = (ImageView) findViewById(R.id.right_greeting_show_list_img);
        finishShowListBtn = (ImageView) findViewById(R.id.finish_greeting_show_list_img);

        //超声波设置
        leftUltrasonicTv = (TextView) findViewById(R.id.ultrasonic_left_set_tv);
        rightUltrasonicTv = (TextView) findViewById(R.id.ultrasonic_right_set_tv);
        leftUltrasonicBtn = (ImageView) findViewById(R.id.ultrasonic_left_set_img);
        rightUltrasonicBtn = (ImageView) findViewById(R.id.ultrasonic_right_set_img);

        //按键
        mCancel = (TextView) findViewById(R.id.cancel);
        mAffirm = (TextView) findViewById(R.id.affirm);
        mStartBtn = (ImageView) findViewById(R.id.ultrasonic_open_btn);
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
        mCancel.setOnClickListener(this);
        mAffirm.setOnClickListener(this);
        mStartBtn.setOnClickListener(this);

        findViewById(R.id.explain).setOnClickListener(this);
        findViewById(R.id.ultrasonic_init_btn).setOnClickListener(this);

        leftPlayModeImg.setOnClickListener(this);
        rightPlayModeImg.setOnClickListener(this);
        finishPlayModeImg.setOnClickListener(this);
        addLeftGreetBtn.setOnClickListener(this);
        addRightGreetBtn.setOnClickListener(this);
        addFinishGreetBtn.setOnClickListener(this);
        delLeftGreetBtn.setOnClickListener(this);
        delRightGreetBtn.setOnClickListener(this);
        delFinishGreetBtn.setOnClickListener(this);
        leftShowListBtnLL.setOnClickListener(this);
        rightShowListBtnLL.setOnClickListener(this);
        finishShowListBtnLL.setOnClickListener(this);
        leftUltrasonicBtn.setOnClickListener(this);
        rightUltrasonicBtn.setOnClickListener(this);
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
            case R.id.explain:
                /***
                 * 打开使用说明页面
                 */
                startActivity(new Intent(this, ExplainActivity.class));
                break;
            case R.id.left_greeting_add_im:
                Intent leftIntent = new Intent(this, AddBodyShowView.class);
                leftIntent.putExtra("itemNum", 1);
                startActivityForResult(leftIntent, LEFT_REQUEST_CODE);
                break;
            case R.id.right_greeting_add_im:
                Intent rightIntentEnd = new Intent(this, AddBodyShowView.class);
                rightIntentEnd.putExtra("itemNum", 2);
                startActivityForResult(rightIntentEnd, RIGHT_REQUEST_CODE);
                break;
            case R.id.finish_greeting_add_im:
                Intent finishIntent = new Intent(this, AddBodyShowView.class);
                finishIntent.putExtra("itemNum", 3);
                startActivityForResult(finishIntent, FINISH_REQUEST_CODE);
                break;
            case R.id.left_greeting_delete_img:
                ((SettingPresenter) mPresenter).getLeftGreetingAdapter().setDelVisible(new GreetingAdapter.OnDeleteItemListener() {
                    @Override
                    public void existLessTwo(boolean isExistLessTwo) {
                        if (isExistLessTwo) {
                            leftShowListBtnLL.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            case R.id.right_greeting_delete_img:
                ((SettingPresenter) mPresenter).getRightGreetingAdapter().setDelVisible(new GreetingAdapter.OnDeleteItemListener() {
                    @Override
                    public void existLessTwo(boolean isExistLessTwo) {
                        if (isExistLessTwo) {
                            rightShowListBtnLL.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            case R.id.finish_greeting_delete_img:
                ((SettingPresenter) mPresenter).getFinishGreetingAdapter().setDelVisible(new GreetingAdapter.OnDeleteItemListener() {
                    @Override
                    public void existLessTwo(boolean isExistLessTwo) {
                        if (isExistLessTwo) {
                            finishShowListBtnLL.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            case R.id.left_greeting_play_mode:
                updatePlayModeView(view, true);
                break;
            case R.id.right_greeting_play_mode:
                updatePlayModeView(view, true);
                break;
            case R.id.finish_greeting_play_mode:
                updatePlayModeView(view, true);
                break;
            case R.id.left_greeting_show_list_img_ll:
                // 展开和收起
                expandListView(1);
                break;
            case R.id.right_greeting_show_list_img_ll:
                // 展开和收起
                expandListView(2);
                break;
            case R.id.finish_greeting_show_list_img_ll:
                // 展开和收起
                expandListView(3);
                break;
            case R.id.ultrasonic_left_set_img:

                break;
            case R.id.ultrasonic_right_set_img:

                break;
        }
    }

    private void updatePlayModeView(View imageView, boolean isClick) {
        int playMode = -1;
        if (imageView.equals(leftPlayModeImg)) {
            playMode = PreferencesUtils.getInt(this, SP_STOP_PLAY_MODE, 0);
        } else if (imageView.equals(rightPlayModeImg)) {
            playMode = PreferencesUtils.getInt(this, SP_STOP_PLAY_MODE, 0);
        } else if (imageView.equals(finishPlayModeImg)) {

        }
        if (playMode != -1) {
            if (isClick) {
                imageView.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shuffle_play_pressed));
                PreferencesUtils.putInt(this, SP_STOP_PLAY_MODE, 1);
                showToast("随机播放");
            } else {
                imageView.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.order_play_pressed));
            }
        }
    }

    /**
     * 开始迎宾条目
     */
    private void updateExpandView() {
        List<ItemsContentBean> leftList = DataManager.getInstance(getContext()).queryItem(1);
        int mLeftListSize = adapterList.get(0).getSourceDataSize();
        if (mLeftListSize > 1) {
            leftShowListBtnLL.setVisibility(View.VISIBLE);
            leftShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shrink));
        } else {
            if (leftList != null && leftList.size() > 1) {
                leftShowListBtnLL.setVisibility(View.VISIBLE);
                leftShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.open));
            } else
                leftShowListBtnLL.setVisibility(View.GONE);
        }


        List<ItemsContentBean> rightList = DataManager.getInstance(getContext()).queryItem(2);
        int mRightListSize = adapterList.get(1).getSourceDataSize();
        if (mRightListSize > 1) {
            rightShowListBtnLL.setVisibility(View.VISIBLE);
            rightShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shrink));
        } else {
            if (rightList != null && rightList.size() > 1) {
                rightShowListBtnLL.setVisibility(View.VISIBLE);
                rightShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.open));
            } else
                rightShowListBtnLL.setVisibility(View.GONE);
        }


        List<ItemsContentBean> finishList = DataManager.getInstance(getContext()).queryItem(3);
        int mFinishListSize = adapterList.get(2).getSourceDataSize();
        if (mFinishListSize > 1) {
            finishShowListBtnLL.setVisibility(View.VISIBLE);
            finishShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shrink));
        } else {
            if (finishList != null && finishList.size() > 1) {
                finishShowListBtnLL.setVisibility(View.VISIBLE);
                finishShowListBtn.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.open));
            } else
                finishShowListBtnLL.setVisibility(View.GONE);
        }

    }


    /**
     * 是否展开条目
     * type 类型
     */
    private void expandListView(int type) {
        List<ItemsContentBean> startList = DataManager.getInstance(getContext()).queryItem(type);
        int currentListSize = adapterList.get(type - 1).getSourceDataSize();
        if (startList != null) {
            if (startList.size() > 1) {
                if (startList.size() > currentListSize) {
                    //展开
                    adapterList.get(type - 1).isShowAllData(type, true);
                } else if (startList.size() == currentListSize) {
                    //收起
                    adapterList.get(type - 1).isShowAllData(type, false);
                }
            }
            updateExpandView();
        }
    }

    @Override
    public void setDistance1(String distance) {
        ulDistanceText1.setText(distance);
    }

    @Override
    public void setDistance2(String distance) {
        ulDistanceText2.setText(distance);
    }

    @Override
    public void setDistance3(String distance) {
        ulDistanceText3.setText(distance);
    }

    @Override
    public void setDistance7(String distance) {
        ulDistanceText7.setText(distance);
    }

    @Override
    public void setDistance8(String distance) {
        ulDistanceText8.setText(distance);
    }

    @Override
    public void setDistance9(String distance) {
        ulDistanceText9.setText(distance);
    }

    @Override
    public void setDistance10(String distance) {
        ulDistanceText10.setText(distance);
    }

    @Override
    public void setDistance11(String distance) {
        ulDistanceText11.setText(distance);
    }

    @Override
    public void setDistance12(String distance) {
        ulDistanceText12.setText(distance);
    }

    @Override
    public void setDistance13(String distance) {
        ulDistanceText13.setText(distance);
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.i(TAG, "SettingActivity onPause");
    }


    private void showLeftUltrasonicDialog() {

    }

    private void showRightUltrasonicDialog() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LEFT_REQUEST_CODE && resultCode == 1) { //从设置回来刷新
            adapterList.get(0).isShowAllData(1, true);
        } else if (requestCode == RIGHT_REQUEST_CODE && resultCode == 1) {
            adapterList.get(1).isShowAllData(2, true);
        } else if (requestCode == FINISH_REQUEST_CODE && resultCode == 1) {
            adapterList.get(2).isShowAllData(3, true);
        }

        updateExpandView();
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
