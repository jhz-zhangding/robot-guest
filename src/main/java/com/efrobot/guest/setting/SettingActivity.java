package com.efrobot.guest.setting;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.efrobot.guest.GuestsApplication;
import com.efrobot.guest.R;
import com.efrobot.guest.action.AddBodyShowView;
import com.efrobot.guest.base.GuestsBaseActivity;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.bean.UlDistanceBean;
import com.efrobot.guest.dao.DataManager;
import com.efrobot.guest.dao.SelectedDao;
import com.efrobot.guest.dao.UltrasonicDao;
import com.efrobot.guest.explain.ExplainActivity;
import com.efrobot.guest.service.UltrasonicService;
import com.efrobot.guest.setting.bean.SelectDirection;
import com.efrobot.guest.utils.CustomHintDialog;
import com.efrobot.guest.utils.PreferencesUtils;
import com.efrobot.guest.utils.ui.DisplayParamsUtil;
import com.efrobot.library.RobotManager;
import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.speechsdk.SpeechManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/2.
 */
public class SettingActivity extends GuestsBaseActivity<SettingPresenter> implements ISettingView, View.OnClickListener {

    public final int START_GUEST = 0;
    public final int INIT_GUEST_USER_DATA = 1;
    public final int UPDATE_LEFT_SELECTED = 100;
    public final int UPDATE_RIGHT_SELECTED = 101;

    private SelectedDao selectedDao;

    private List<GreetingAdapter> adapterList;

    private TextView ulDistanceText1, ulDistanceText2, ulDistanceText3, ulDistanceText7, ulDistanceText8;
    private TextView ulDistanceText9, ulDistanceText10, ulDistanceText11, ulDistanceText12, ulDistanceText13;

    //用户设置距离
    private EditText ulDistanceEdit0, ulDistanceEdit1, ulDistanceEdit2, ulDistanceEdit6,
            ulDistanceEdit7, ulDistanceEdit8, ulDistanceEdit9,
            ulDistanceEdit10, ulDistanceEdit11, ulDistanceEdit12;

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
    private TextView leftUltrasonicTv, rightUltrasonicTv;
    private ImageView leftUltrasonicBtn, rightUltrasonicBtn;

    //保存设置
    private TextView mCancel, mAffirm, mSetting;
    private ImageView mStartBtn;
    private final int LEFT_REQUEST_CODE = 1;
    private final int RIGHT_REQUEST_CODE = 2;
    private final int FINISH_REQUEST_CODE = 3;

    public static String SP_LEFT_PLAY_MODE = "sp_left_play_mode";
    public static String SP_RIGHT_PLAY_MODE = "sp_right_play_mode";
    public static String SP_FINISH_PLAY_MODE = "sp_finish_play_mode";

    public static int GUEST_ORDER_PLAY_MODE = 0;
    public static int GUEST_LOOP_PLAY_MODE = 1;

    private Map<Integer, EditText> ultrasonicMap = new HashMap<Integer, EditText>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
//                case START_GUEST:
//                    int maskSwitch = RobotState.getInstance(getContext()).getMaskState();
//                    L.i(TAG, "面罩maskSwitch:" + maskSwitch);
//                    if (maskSwitch == 0) {
//                        mServiceIntent = new Intent(getContext(), UltrasonicService.class);
//                        getContext().getApplicationContext().startService(mServiceIntent);
//                        showToast("开始迎宾");
//
//                    }
//                    break;
                case INIT_GUEST_USER_DATA:
                    initUserSetting();
                    break;
                case UPDATE_LEFT_SELECTED:
                    SelectDirection selectDirection = (SelectDirection) msg.obj;
                    addChildLinearLayout(titleLeftContainer, selectDirection);
                    break;
                case UPDATE_RIGHT_SELECTED:
                    SelectDirection selectDirection1 = (SelectDirection) msg.obj;
                    addChildLinearLayout(titleRightContainer, selectDirection1);
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
        selectedDao = GuestsApplication.from(this).getSelectedDao();

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

        updateDirectionView(leftUltrasonicTv, rightUltrasonicTv);

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
        mSetting = (TextView) findViewById(R.id.ultrasonic_setting_btn);
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

    @Override
    protected void setOnListener() {
        super.setOnListener();
        mCancel.setOnClickListener(this);
        mAffirm.setOnClickListener(this);
        mSetting.setOnClickListener(this);
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
                ((SettingPresenter) mPresenter).cancel();
                break;
            case R.id.affirm:
                ((SettingPresenter) mPresenter).affirm();
                break;
            case R.id.ultrasonic_open_btn: //开始迎宾
                boolean isCanAffirm = ((SettingPresenter) mPresenter).affirm();
                if (isCanAffirm) {
                    showDialog("即将开始迎宾，请关闭面罩后开始迎宾");
                }
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
            case R.id.ultrasonic_setting_btn:
                showUltrasonicSettingDialog();
                break;
            case R.id.left_greeting_add_im:
                Intent leftIntent = new Intent(this, AddBodyShowView.class);
                leftIntent.putExtra("itemNum", LEFT_REQUEST_CODE);
                startActivityForResult(leftIntent, LEFT_REQUEST_CODE);
                break;
            case R.id.right_greeting_add_im:
                Intent rightIntentEnd = new Intent(this, AddBodyShowView.class);
                rightIntentEnd.putExtra("itemNum", RIGHT_REQUEST_CODE);
                startActivityForResult(rightIntentEnd, RIGHT_REQUEST_CODE);
                break;
            case R.id.finish_greeting_add_im:
                Intent finishIntent = new Intent(this, AddBodyShowView.class);
                finishIntent.putExtra("itemNum", FINISH_REQUEST_CODE);
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
                showSelectUltrasonicDialog(1);
                break;
            case R.id.ultrasonic_right_set_img:
                showSelectUltrasonicDialog(2);
                break;
        }
    }


    private Dialog ultrasonicSettingDialog;

    private void showUltrasonicSettingDialog() {
        if (ultrasonicSettingDialog == null) {
            ultrasonicSettingDialog = new Dialog(this, R.style.NewSettingDialog);
            View currentView = LayoutInflater.from(this).inflate(R.layout.layout_ultrasonic_select_dialog, null);
            ulDistanceEdit0 = (EditText) currentView.findViewById(R.id.ul_place_edit0);
            ultrasonicMap.put(0, ulDistanceEdit0);
            ulDistanceEdit1 = (EditText) currentView.findViewById(R.id.ul_place_edit1);
            ultrasonicMap.put(1, ulDistanceEdit1);
            ulDistanceEdit2 = (EditText) currentView.findViewById(R.id.ul_place_edit2);
            ultrasonicMap.put(2, ulDistanceEdit2);
            ulDistanceEdit6 = (EditText) currentView.findViewById(R.id.ul_place_edit6);
            ultrasonicMap.put(6, ulDistanceEdit6);
            ulDistanceEdit7 = (EditText) currentView.findViewById(R.id.ul_place_edit7);
            ultrasonicMap.put(7, ulDistanceEdit7);
            ulDistanceEdit8 = (EditText) currentView.findViewById(R.id.ul_place_edit8);
            ultrasonicMap.put(8, ulDistanceEdit8);
            ulDistanceEdit9 = (EditText) currentView.findViewById(R.id.ul_place_edit9);
            ultrasonicMap.put(9, ulDistanceEdit9);
            ulDistanceEdit10 = (EditText) currentView.findViewById(R.id.ul_place_edit10);
            ultrasonicMap.put(10, ulDistanceEdit10);
            ulDistanceEdit11 = (EditText) currentView.findViewById(R.id.ul_place_edit11);
            ultrasonicMap.put(11, ulDistanceEdit11);
            ulDistanceEdit12 = (EditText) currentView.findViewById(R.id.ul_place_edit12);
            ultrasonicMap.put(12, ulDistanceEdit12);
            currentView.findViewById(R.id.ultrasonic_setting_affirm_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveUserSetting();
                    getUpdateData();
                    ultrasonicSettingDialog.dismiss();
                }
            });
            ultrasonicSettingDialog.setContentView(currentView);
            Window dialogWindow = ultrasonicSettingDialog.getWindow();
            dialogWindow.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = DisplayParamsUtil.dipToPixel(this, 1100);
            lp.height = DisplayParamsUtil.dipToPixel(this, 400);

            dialogWindow.setAttributes(lp);
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        mHandler.sendEmptyMessage(INIT_GUEST_USER_DATA);
        ultrasonicSettingDialog.show();

    }

    /**
     * 用户设置距离
     */
    private void saveUserSetting() {
        UltrasonicDao ultrasonicDao = GuestsApplication.from(this).getUltrasonicDao();
        for (Map.Entry entry : ultrasonicMap.entrySet()) {
            L.e("saveUserSetting", "setUltrasonicId=" + (Integer) entry.getKey() + "- - -setDistanceValue=" + ((EditText) entry.getValue()).getText().toString());
            UlDistanceBean ulDistanceBean = new UlDistanceBean();
            int ultrasonicId = (Integer) entry.getKey();
            String distanceValue = ((EditText) entry.getValue()).getText().toString();
            ulDistanceBean.setUltrasonicId(ultrasonicId);
            ulDistanceBean.setDistanceValue(distanceValue);
            if (ultrasonicDao.isExits(ultrasonicId))
                ultrasonicDao.update(0, ultrasonicId, distanceValue);
            else
                ultrasonicDao.insert(ulDistanceBean);
        }
    }

    /**
     * 用户设置距离
     */
    private void initUserSetting() {
        UltrasonicDao ultrasonicDao = GuestsApplication.from(this).getUltrasonicDao();
        ArrayList<UlDistanceBean> lists = ultrasonicDao.queryAll();

        if (lists != null) {
            for (int i = 0; i < lists.size(); i++) {
                UlDistanceBean ulDistanceBean = lists.get(i);
                ultrasonicMap.get(ulDistanceBean.getUltrasonicId()).setText(ulDistanceBean.getDistanceValue());
            }
        }
    }

    private Dialog selectUltrasonicDialog;
    private GridView selectGridView;
    private SelectDirecAdapter selectDirecAdapter;
    private LinearLayout titleLeftContainer, titleRightContainer;
    private int currentDialogType = 1;

    private void showSelectUltrasonicDialog(int type) {
        currentDialogType = type;
        if (selectUltrasonicDialog == null) {
            selectUltrasonicDialog = new Dialog(this, R.style.NewSettingDialog);
            View currentView = LayoutInflater.from(this).inflate(R.layout.select_derc_ultrasonic_dialog, null);
            selectGridView = (GridView) currentView.findViewById(R.id.selected_dec_gv);
            titleLeftContainer = (LinearLayout) currentView.findViewById(R.id.selected_dec_left_container);
            titleRightContainer = (LinearLayout) currentView.findViewById(R.id.selected_dec_right_container);

            selectDirecAdapter = new SelectDirecAdapter(this);
            selectDirecAdapter.setSourceData(getUpdateData());
            selectDirecAdapter.setOnSelectedItem(new SelectDirecAdapter.OnSelectedItem() {
                @Override
                public void onSelect(SelectDirection selectDirection) {
                    if (currentDialogType == 1) {
                        addChildLinearLayout(titleLeftContainer, selectDirection);
                        tempLeftSelectDirections.add(selectDirection);
                        updateSelectedTv(tempLeftSelectDirections, leftUltrasonicTv);
                    } else if (currentDialogType == 2) {
                        addChildLinearLayout(titleRightContainer, selectDirection);
                        tempRightSelectDirections.add(selectDirection);
                        updateSelectedTv(tempRightSelectDirections, rightUltrasonicTv);
                    }
                    if (!selectedDao.isExits(selectDirection.getUltrasonicId())) {
                        selectedDao.insert(selectDirection);
                    }
                }
            });
            selectGridView.setAdapter(selectDirecAdapter);

            selectUltrasonicDialog.setContentView(currentView);
            Window dialogWindow = selectUltrasonicDialog.getWindow();
            dialogWindow.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = DisplayParamsUtil.dipToPixel(this, 800);
            lp.height = DisplayParamsUtil.dipToPixel(this, 300);

            dialogWindow.setAttributes(lp);
        }
        if (type == 1) {
            titleLeftContainer.setVisibility(View.VISIBLE);
            titleRightContainer.setVisibility(View.GONE);
        } else if (type == 2) {
            titleLeftContainer.setVisibility(View.GONE);
            titleRightContainer.setVisibility(View.VISIBLE);
        }
        selectUltrasonicDialog.show();

    }

    private List<SelectDirection> tempLeftSelectDirections = new ArrayList<SelectDirection>();
    private List<SelectDirection> tempRightSelectDirections = new ArrayList<SelectDirection>();

    private void addChildLinearLayout(final LinearLayout parentView, final SelectDirection selectDirection) {
        if (currentDialogType == 1) {
            selectDirection.setType(1);
        } else if (currentDialogType == 2) {
            selectDirection.setType(2);
        }

        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView = new TextView(this);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(DisplayParamsUtil.spToPixel(this, 26));
        textView.setText(selectDirection.getValue());
        linearLayout.addView(textView);

        ImageView imageView = new ImageView(this);
        imageView.setBackground(this.getResources().getDrawable(R.mipmap.del));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除

                if (parentView != null) {
                    parentView.removeView(linearLayout);
                    if (currentDialogType == 1) {
                        for (int i = 0; i < tempLeftSelectDirections.size(); i++) {
                            if (tempLeftSelectDirections.get(i).getUltrasonicId() == selectDirection.getUltrasonicId()) {
                                tempLeftSelectDirections.remove(i);
                                break;
                            }
                        }
                        updateSelectedTv(tempLeftSelectDirections, leftUltrasonicTv);

                    } else if (currentDialogType == 2) {
                        for (int i = 0; i < tempRightSelectDirections.size(); i++) {
                            if (tempRightSelectDirections.get(i).getUltrasonicId() == selectDirection.getUltrasonicId()) {
                                tempRightSelectDirections.remove(i);
                                break;
                            }
                        }
                        updateSelectedTv(tempRightSelectDirections, rightUltrasonicTv);
                    }
                    if (selectDirecAdapter != null) {
                        selectDirecAdapter.resetTextView(selectDirection);
                    }
                    if (selectedDao.isExits(selectDirection.getUltrasonicId())) {
                        selectedDao.delete(selectDirection.getUltrasonicId());
                    }
                }
            }
        });
        //添加
        linearLayout.addView(imageView);
        if (parentView != null) {
            parentView.addView(linearLayout);
        }

    }


    private void updateSelectedTv(List<SelectDirection> tempSelectDirections, TextView textView) {
        StringBuffer sb = new StringBuffer();
        if (tempSelectDirections.size() > 0) {
            if (tempSelectDirections.size() > 1) {
                for (int i = 0; i < tempSelectDirections.size(); i++) {
                    sb.append(tempSelectDirections.get(i).getValue()).append("/");
                }
            } else {
                sb.append(tempSelectDirections.get(0).getValue());
            }
            if (sb.toString().contains("/")) {
                sb.toString().substring(0, sb.toString().lastIndexOf("/"));
            }
            textView.setText(sb.toString());
        } else {
            textView.setText("");
        }
    }

    private void updateDirectionSelectedView() {
        List<UlDistanceBean> ulDistanceBeen = GuestsApplication.from(this).getUltrasonicDao().queryAll();
        int ul_id = -1;
        for (int i = 0; i < ulDistanceBeen.size(); i++) {
            if(TextUtils.isEmpty(ulDistanceBeen.get(i).getDistanceValue())) {
                ul_id = ulDistanceBeen.get(i).getUltrasonicId();
            }

            for (int j = tempLeftSelectDirections.size() - 1; j > 0; j--) {
                if (tempLeftSelectDirections.get(j).getUltrasonicId() == ul_id) {
                    tempLeftSelectDirections.remove(j);
                }
            }

            for (int j = tempRightSelectDirections.size() - 1; j > 0; j--) {
                if (tempRightSelectDirections.get(j).getUltrasonicId() == ul_id) {
                    tempRightSelectDirections.remove(j);
                }
            }
        }
    }


    private LinkedHashMap<Integer, String> direcMap;

    private List<SelectDirection> getUpdateData() {
        if (direcMap == null) {
            direcMap = new LinkedHashMap<Integer, String>();
            direcMap.put(2, "左1");
            direcMap.put(1, "左3");
            direcMap.put(0, "中1");
            direcMap.put(7, "右3");
            direcMap.put(6, "右1");

            direcMap.put(12, "左2");
            direcMap.put(10, "左4");
            direcMap.put(9, "中2");
            direcMap.put(8, "右4");
            direcMap.put(11, "右2");
        }
        return updateDirectionButton();
    }

    private List<SelectDirection> updateDirectionButton() {
        if (titleLeftContainer != null) {
            titleLeftContainer.removeAllViews();
        }
        if (titleRightContainer != null) {
            titleRightContainer.removeAllViews();
        }

        updateDirectionSelectedView();

        List<SelectDirection> data = new ArrayList<SelectDirection>();
        for (Map.Entry entry : direcMap.entrySet()) {
            int ultrasonicId = (Integer) entry.getKey();
            String value = (String) entry.getValue();
            SelectDirection selectDirection = new SelectDirection();
            selectDirection.setUltrasonicId(ultrasonicId);
            selectDirection.setValue(value);
            for (int i = 0; i < tempLeftSelectDirections.size(); i++) {
                if (tempLeftSelectDirections.get(i).getUltrasonicId() == ultrasonicId) {
                    selectDirection.setSelected(true);
                    Message message = new Message();
                    message.what = UPDATE_LEFT_SELECTED;
                    message.obj = selectDirection;
                    mHandler.sendMessage(message);
                }
            }
            for (int i = 0; i < tempRightSelectDirections.size(); i++) {
                if (tempRightSelectDirections.get(i).getUltrasonicId() == ultrasonicId) {
                    selectDirection.setSelected(true);
                    Message message = new Message();
                    message.what = UPDATE_RIGHT_SELECTED;
                    message.obj = selectDirection;
                    mHandler.sendMessage(message);
                }
            }
            data.add(selectDirection);
        }
        return data;
    }

    private void updateDirectionView(TextView leftView, TextView rightView) {
        ArrayList<SelectDirection> leftDirectionLists = selectedDao.queryOneType(1);
        ArrayList<SelectDirection> rightDirectionLists = selectedDao.queryOneType(2);
        tempLeftSelectDirections = leftDirectionLists;
        tempRightSelectDirections = rightDirectionLists;
        updateSelectedTv(tempLeftSelectDirections, leftView);
        updateSelectedTv(tempRightSelectDirections, rightView);
    }


    private void updatePlayModeView(View imageView, boolean isClick) {
        int playMode = -1;
        String needSaveSp = "";
        if (imageView.equals(leftPlayModeImg)) {
            playMode = PreferencesUtils.getInt(this, SP_LEFT_PLAY_MODE, GUEST_ORDER_PLAY_MODE);
            needSaveSp = SP_LEFT_PLAY_MODE;
        } else if (imageView.equals(rightPlayModeImg)) {
            playMode = PreferencesUtils.getInt(this, SP_RIGHT_PLAY_MODE, GUEST_ORDER_PLAY_MODE);
            needSaveSp = SP_RIGHT_PLAY_MODE;
        } else if (imageView.equals(finishPlayModeImg)) {
            playMode = PreferencesUtils.getInt(this, SP_FINISH_PLAY_MODE, GUEST_ORDER_PLAY_MODE);
            needSaveSp = SP_FINISH_PLAY_MODE;
        }
        if (playMode != -1) {
            if (playMode == GUEST_ORDER_PLAY_MODE) {
                imageView.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.order_play_pressed));
            } else if (playMode == GUEST_LOOP_PLAY_MODE) {
                imageView.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shuffle_play_pressed));
            }
        }

        if (isClick) {
            if (playMode == GUEST_ORDER_PLAY_MODE) {
                imageView.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.shuffle_play_pressed));
                PreferencesUtils.putInt(this, needSaveSp, GUEST_LOOP_PLAY_MODE);
                showToast("随机播放");
            } else if (playMode == GUEST_LOOP_PLAY_MODE) {
                imageView.setBackgroundDrawable(this.getResources().getDrawable(R.mipmap.order_play_pressed));
                PreferencesUtils.putInt(this, needSaveSp, GUEST_ORDER_PLAY_MODE);
                showToast("顺序播放");
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
