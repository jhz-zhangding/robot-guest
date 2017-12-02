package com.efrobot.guests.action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.efrobot.guests.R;
import com.efrobot.guests.base.GuestsBaseActivity;
import com.efrobot.guests.bean.ItemsContentBean;
import com.efrobot.guests.utils.DatePickerUtils;
import com.efrobot.library.mvp.utils.L;
import com.umeng.analytics.MobclickAgent;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2016/2/18.
 */
public class AddBodyShowView extends GuestsBaseActivity<AddBodyShowPresenter> implements IAddBodyShowView, View.OnClickListener, AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener {
    private TextView msaybtn;
    private String mSayType;

    private TextView totalTime;
    private TextView mLightbtn, add_title;
    private int size;
    //用来区分，视频，图片，音频
//    private String selectType;
//    private static int videotag = -1;
//    private static int music_img_tag = -1;

    /***
     * 是否选择了图片
     */
    public boolean isSelectedPicture;
    /***
     * 是否选择了音乐
     */
    public boolean isSelectedMusic;
    /***
     * 是否选择了视频
     */
    public boolean isSelectedVideo;

    private double sayTime = 0;
    private double lightTime = 0;
    private double fileTime = 0;
    private double actionTime = 0;
    private double faceTime = 0;
    private int itemNum;

    @Override
    public AddBodyShowPresenter createPresenter() {
        return new AddBodyShowPresenter(this);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_add;
    }

    /**
     * 表情、动作
     */
    private TextView mFace, mAction;
    /**
     * 显示表情和动作的GridView
     */
    private GridView mGridView;
    /**
     * 保存按钮
     */
    private Button mSaveBtn;
    /**
     * 内容 文本框
     */
    private EditText mEditText;
    /**
     * 语音时长
     */
    private TextView tvSayTime;

    /**
     * 语音预设词条
     */
    private TagFlowLayout addFlowLayout;

    //    private ListView prepareSayLv;
//    private TtsContentAdapter ttsContentAdapter;


    private LinearLayout relSay;
    /**
     * 表情和动作的Layout
     */
    private View faceActionView;
    /**
     * 自定义View
     */
    private View addMediaView;


    /**
     * 自定义动作View
     */
    private View addCustom;
    /**
     * 动作表情
     */
    private TextView mfaceActionvalue;
    /**
     * 表情动作的RecyclerView
     */
    private RecyclerView tvAnctionsAndFaces;
    private LinearLayoutManager linearLayoutManager;
    /**
     * 动作表情View的标题
     */
    private TextView mTitle;
    /**
     * 灯带的 关、开、闪烁
     */
    private RadioButton mClose, mOpen, mFlicker;
    /**
     * 灯带group
     */
    private RadioGroup mRadioGroup;
    /**
     * 标记灯带的checked
     * 1关、0常亮、2闪烁
     */
    private String light = "1";
    /**
     * 创建动作脚本按钮
     */
    private TextView mCreateScript;

    /**
     * 返回按钮
     */

    private TextView mbackView;

    /**
     * 添加图片或视频
     */
    private TextView tvAddMedia;
    /**
     * 上传文件
     */
    private TextView videoPull, musicPull, imagePull;
    /**
     * 显示选择的图片或视频
     */
    private ImageView ivPhoto;
    /**
     * 选择的图片，视频，音频的名称
     */
    private TextView imageName, videoName, musicName;
    /**
     * 删除选择的图片，视频，音频
     */
    private ImageView deleteImage, deleteVideo, deleteMusic;
    /**
     * 灯带选择视图
     */
    private RelativeLayout relBelt;

    /**
     * 常亮和闪烁的灯带时长选择
     */
    private Spinner openSpinner, flickerSpinner;

    ArrayList<TextView> viewList = new ArrayList<TextView>();
    //定义一个过滤器；
    private IntentFilter intentFilter;

    //定义一个广播监听器；
    private ResourceBroadcastReceiver resourcereceiver;

    /**
     * 迎宾触发时间段设置
     */
    private View tvTimeView;
    private TextView tvTimeSpace;
    private TextView etTimeStart, etTimeEnd;

    public String[] ttsWelcome1 = new String[]{
            "客人来了，欢迎欢迎##1",
            "您好，我是迎宾小胖，想和我聊天请摸摸我的头哦##1",
            "尊贵的来宾，您好，很高兴见到您，我是迎宾小胖，欢迎您来到xxx##1",
            "是有人来了吗，您好，能和我这个可爱的机器人聊聊天吗##1",
            "贵客光临，蓬荜生辉，您来了，里面请哦##1",
            "小胖提醒您，进门记得打卡哦##1",
            "您好，您想了解一下xx产品吗，小胖这就给你细细介绍哦,xx##1",
            "客人您好，您是想购买xx吗，左转直行50米就是xx商品区哦##1"

    };

    public String[] ttsWelcome2 = new String[]{
            "您好，您这是要走了吗，小胖期待您的下次光临##1",
            "亲，别急着走啊，和小胖聊聊天呗##1",
            "您要走了吗，您下次什么时候再来呢，小胖会想你的##1",
            "下班啦，小胖提醒您，出门前记得关灯关电源关窗户哦##1",
            "下班啦，小胖提醒您明天是个重要的日子哦，一定要记得xxx##1"

    };

    public String[] ttsEnd = new String[]{
            "下次再见##1",
            "您慢走##1"
    };

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onViewInit() {
        super.onViewInit();

        Intent intent = getIntent();

        itemNum = 0;
        if (intent.hasExtra("itemNum")) {
            itemNum = intent.getIntExtra("itemNum", -1);
        } else if (intent.hasExtra("content")) {
            ItemsContentBean bean = (ItemsContentBean) intent.getSerializableExtra("content");
            if (bean != null) {
                itemNum = bean.getItemNum();
            }
        }
        if (itemNum == 3)
            mSayType = "结束语";
        else {
            mSayType = "迎宾语";
        }

        msaybtn = (TextView) findViewById(R.id.say_value_btn);
        msaybtn.setText(mSayType);

        relSay = (LinearLayout) findViewById(R.id.relSay);
        mLightbtn = (TextView) findViewById(R.id.light_value_btn);
        mFace = (TextView) findViewById(R.id.add_face_btn);
        mAction = (TextView) findViewById(R.id.add_action_btn);
        mCreateScript = (TextView) findViewById(R.id.add_create_script);
        tvAddMedia = (TextView) findViewById(R.id.tvAddMedia);
        tvTimeSpace = (TextView) findViewById(R.id.tvTimeSpace);
        add_title = (TextView) findViewById(R.id.add_title);
        viewList.add(msaybtn);
//        viewList.add(mLightbtn);
        viewList.add(mFace);
        viewList.add(mAction);
        viewList.add(mCreateScript);
        viewList.add(tvAddMedia);
        viewList.add(tvTimeSpace);

        size = viewList.size();

        tvAnctionsAndFaces = (RecyclerView) findViewById(R.id.tvFaces);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        tvAnctionsAndFaces.setLayoutManager(linearLayoutManager);

        mGridView = (GridView) findViewById(R.id.add_grid_view);
        faceActionView = (View) findViewById(R.id.face_action);
        addMediaView = (View) findViewById(R.id.addMeida);
//        addCustom = (View) findViewById(R.id.custom_action);
        tvTimeView = (View) findViewById(R.id.addTimeSpace);

        mSaveBtn = (Button) findViewById(R.id.add_save_btn);

        mEditText = (EditText) findViewById(R.id.add_edit_text);
        tvSayTime = (TextView) findViewById(R.id.tvSayTime);
//        prepareSayLv = (ListView) findViewById(R.id.add_prepare_say_words_lv);
        addFlowLayout = (TagFlowLayout) findViewById(R.id.add_flow_layout);

        videoPull = (TextView) findViewById(R.id.videoPull);
        musicPull = (TextView) findViewById(R.id.musicPull);
        imagePull = (TextView) findViewById(R.id.imagePull);

        videoName = (TextView) findViewById(R.id.videoName);
        imageName = (TextView) findViewById(R.id.imageName);
        musicName = (TextView) findViewById(R.id.musicName);

        deleteImage = (ImageView) findViewById(R.id.deleteImage);
        deleteMusic = (ImageView) findViewById(R.id.deleteMusic);
        deleteVideo = (ImageView) findViewById(R.id.deleteVideo);
        totalTime = (TextView) findViewById(R.id.totalTime);


        InputFilter[] filters = {new NameLengthFilter(Integer.MAX_VALUE)};
        mEditText.setFilters(filters);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float time = (float) (mEditText.getText().length() * 0.27);
                tvSayTime.setText("语音时长：" + time + "秒");
                if (!isSelectedMusic && !isSelectedVideo) {
                    msaybtn.setText(mSayType + "(" + time + "'')");
                    sayTime = (double) time;
                }

                getMaxTime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mfaceActionvalue = (TextView) findViewById(R.id.face_action_value);
        mTitle = (TextView) findViewById(R.id.face_action_title);
        mRadioGroup = (RadioGroup) findViewById(R.id.add_light_group);
        relBelt = (RelativeLayout) findViewById(R.id.relBelt);
        mClose = (RadioButton) findViewById(R.id.add_light_close);
        mOpen = (RadioButton) findViewById(R.id.add_light_open);
        mFlicker = (RadioButton) findViewById(R.id.add_light_flicker);
        openSpinner = (Spinner) findViewById(R.id.open_light_spin);
        flickerSpinner = (Spinner) findViewById(R.id.flicker_light_spin);
        openSpinner.setEnabled(false);
        flickerSpinner.setEnabled(false);
        mbackView = (TextView) findViewById(R.id.add_back);

        etTimeStart = (TextView) findViewById(R.id.add_start_time_space);
        etTimeEnd = (TextView) findViewById(R.id.add_end_time_space);

        /**
         * 隐藏键盘
         */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //文件管理数据接受广播
        intentFilter = new IntentFilter();
        //添加过滤的Action值；
        intentFilter.addAction("efrobot.robot.resoure");

        //实例化广播监听器；
        resourcereceiver = new ResourceBroadcastReceiver();

        //将广播监听器和过滤器注册在一起；
        registerReceiver(resourcereceiver, intentFilter);
        openSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!"无".equals(openSpinner.getSelectedItem().toString())) {
                    mLightbtn.setText("灯带" + "(" + openSpinner.getSelectedItem().toString() + "'')");
                    lightTime = Double.parseDouble(openSpinner.getSelectedItem().toString());
                    getMaxTime();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        flickerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!"无".equals(flickerSpinner.getSelectedItem().toString())) {
                    mLightbtn.setText("灯带" + "(" + flickerSpinner.getSelectedItem().toString() + "'')");
                    lightTime = Double.parseDouble(flickerSpinner.getSelectedItem().toString());
                    getMaxTime();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /** 初始化预设词条*/
        initTtsAdapterData();
    }

    String[] tts = null;

    private List<TtsBean> getPreList(int type) {
        if (type == 1) {
            tts = ttsWelcome1;
        } else if (type == 2) {
            tts = ttsWelcome2;
        } else if (type == 3) {
            tts = ttsEnd;
        }


        List<TtsBean> ttsBeans = new ArrayList<TtsBean>();
        if (tts != null) {
            for (int i = 0; i < tts.length; i++) {
                String ttsStr = tts[i];
                String[] mTtsStr = ttsStr.split("##");
                TtsBean ttsBean = new TtsBean();
                ttsBean.setmSay(mTtsStr[0]);
                ttsBean.setType(Integer.parseInt(mTtsStr[1]));
                if (mTtsStr.length > 2) {
                    ttsBean.setTimeSpace(mTtsStr[2]);
                }
                ttsBeans.add(ttsBean);
            }
        }
        return ttsBeans;
    }

    /***
     * 解决软键盘弹出时任务栏不隐藏和单击输入框以外区域输入法不隐藏的bug
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

                if (android.os.Build.VERSION.SDK_INT >= 19) {
                    uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
                } else {
                    uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                }
                getWindow().getDecorView().setSystemUiVisibility(uiFlags);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override
    public void setLight(String light) {
        this.light = light;
        if (light.equals("0")) {
            mOpen.setChecked(true);
            flickerSpinner.setEnabled(false);
            openSpinner.setEnabled(true);
            mLightbtn.setText("灯带" + "(" + openSpinner.getSelectedItem().toString() + "'')");
            if (!"无".equals(openSpinner.getSelectedItem().toString())) {
                lightTime = Double.parseDouble(openSpinner.getSelectedItem().toString());
            }
            getMaxTime();
        } else if (light.equals("1")) {
            mClose.setChecked(true);
            mFlicker.setChecked(false);
            mLightbtn.setText("灯带");
            flickerSpinner.setEnabled(false);
            openSpinner.setEnabled(false);
        } else if (light.equals("2")) {
            mFlicker.setChecked(true);
            flickerSpinner.setEnabled(true);
            openSpinner.setEnabled(false);
            mLightbtn.setText("灯带" + "(" + flickerSpinner.getSelectedItem().toString() + "'')");
            if (!"无".equals(openSpinner.getSelectedItem().toString())) {
                lightTime = Double.parseDouble(flickerSpinner.getSelectedItem().toString());
            }
            getMaxTime();
        }
    }

    /**
     * 设置灯带常亮时长和闪烁时长
     */
    @Override
    public void setOpenAndFlickerTime(String open, String flicker) {
        Resources res = getContext().getResources();
        final String[] light = res.getStringArray(R.array.light);
        for (int i = 0; i < light.length; i++) {
            if (light[i].equals(open)) {
                openSpinner.setSelection(i);
            }
            if (light[i].equals(flicker)) {
                flickerSpinner.setSelection(i);
            }
        }
    }

    @Override
    public Spinner getOpenSpinner() {
        return openSpinner;
    }

    @Override
    public Spinner getFlickerSpinner() {
        return flickerSpinner;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.e("=========>>>>onDestroy", "onDestroy");
        if (resourcereceiver != null)
            getContext().unregisterReceiver(resourcereceiver);
    }

    @Override
    public void setFirstViewShow() {
        updateView(msaybtn);
    }

    /**
     * 自定义数据设置
     *
     * @param musicPath
     * @param mediaPath
     */
    @Override
    public void setMedia2(String musicPath, String mediaPath) {
        L.e("==============>>>", "设置媒体显示=============》》》》》》》》》》》》》》》");
        int dosmusic = -1;
        int dosmedia = -1;

        isSelectedPicture = false;
        isSelectedMusic = false;
        isSelectedVideo = false;

        if (!TextUtils.isEmpty(musicPath)) {
            dosmusic = musicPath.lastIndexOf("/");
        }
        if (!TextUtils.isEmpty(mediaPath)) {
            dosmedia = mediaPath.lastIndexOf("/");
        }

        /***
         * 选了音频
         */
        if (dosmusic != -1) {
            String musicname = musicPath.substring(dosmusic + 1, musicPath.length());
            musicName.setText(musicname);
            deleteMusic.setVisibility(View.VISIBLE);
            isSelectedMusic = true;
        }
        /***
         * 判断是否选择了视频或者图片
         */
        if (dosmedia != -1) {
            String name = mediaPath.substring(dosmedia + 1, mediaPath.length());

            if (name.contains("mp4")) {
                /**
                 * 选择了视频
                 */
                videoName.setText(name);
                deleteVideo.setVisibility(View.VISIBLE);
                isSelectedVideo = true;
            } else {
                /**
                 * 选择了图片
                 */
                imageName.setText(name);
                deleteImage.setVisibility(View.VISIBLE);
                isSelectedPicture = true;
            }
        }

        if (isSelectedVideo) {
            musicPull.setSelected(true);
            imagePull.setSelected(true);
        } else {

            if (isSelectedMusic) {
                videoPull.setSelected(true);
            }
        }


        videoPull.setEnabled(!videoPull.isSelected());
        musicPull.setEnabled(!musicPull.isSelected());
        imagePull.setEnabled(!imagePull.isSelected());
        initSay();

    }


    /**
     * title添加词条的改变
     *
     * @param content
     */
    @Override
    public void setTitle(String content) {
        add_title.setText("添加" + content);

    }

    /**
     * 更新页面
     *
     * @param view 页面
     */
    public void updateView(View view) {

        if (view.isSelected()) {
            return;
        }

        for (int j = 0; j < size; j++) {
            viewList.get(j).setSelected(viewList.get(j).equals(view));
        }

        relSay.setVisibility(View.GONE);
        relBelt.setVisibility(View.GONE);
        faceActionView.setVisibility(View.GONE);
        addMediaView.setVisibility(View.GONE);
        tvTimeView.setVisibility(View.GONE);
        if (msaybtn.equals(view)) {
            relSay.setVisibility(View.VISIBLE);
        } else if (view.equals(mLightbtn)) {
            relBelt.setVisibility(View.VISIBLE);
        } else if (view.equals(mFace)) {
            tvAnctionsAndFaces.setVisibility(View.VISIBLE);
            faceActionView.setVisibility(View.VISIBLE);
        } else if (view.equals(mAction)) {
            tvAnctionsAndFaces.setVisibility(View.VISIBLE);
            faceActionView.setVisibility(View.VISIBLE);
        } else if (view.equals(mCreateScript)) {
            addCustom.setVisibility(View.VISIBLE);
        } else if (view.equals(tvAddMedia)) {
            addMediaView.setVisibility(View.VISIBLE);
        } else if (view.equals(tvTimeSpace)) {
            tvTimeView.setVisibility(View.VISIBLE);
        }

    }


    @Override
    protected void setOnListener() {
        super.setOnListener();


        videoPull.setOnClickListener(this);
        musicPull.setOnClickListener(this);
        imagePull.setOnClickListener(this);


        deleteImage.setOnClickListener(this);
        deleteMusic.setOnClickListener(this);
        deleteVideo.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);
        mbackView.setOnClickListener(this);

        mGridView.setOnItemClickListener(this);
        mRadioGroup.setOnCheckedChangeListener(this);

        etTimeStart.setOnClickListener(this);
        etTimeEnd.setOnClickListener(this);


        for (int i = 0; i < size; i++) {
            final TextView view = viewList.get(i);
            view.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**
             * 退出页面
             */
            case R.id.add_back:
                ((AddBodyShowPresenter) mPresenter).exitAdd();
//                music_img_tag = -1;
//                videotag = -1;
                break;
            /**
             * 说话
             */
            case R.id.say_value_btn:
                /**
                 * 闪光灯
                 */
                updateView(v);
                add_title.setText("添加迎宾语");
                break;
            case R.id.light_value_btn:
                updateView(v);
                add_title.setText("添加灯带");
                break;

            /**
             * 添加运动
             */
            case R.id.add_create_script:
                add_title.setText("创建动作");
                updateView(v);
                break;

            /**
             * 添加表情
             */
            case R.id.add_face_btn:
                ((AddBodyShowPresenter) mPresenter).showFaceAndAction(1);
                mTitle.setText("表情:");
                add_title.setText("添加表情");
                updateView(v);
                break;
            /**
             * 添加动作
             */
            case R.id.add_action_btn:
                ((AddBodyShowPresenter) mPresenter).showFaceAndAction(2);
                mTitle.setText("动作:");
                add_title.setText("添加动作");
                updateView(v);
                break;
            /**
             * 保存数据
             */
            case R.id.add_save_btn:
                ((AddBodyShowPresenter) mPresenter).saveData();
//                music_img_tag = -1;
//                videotag = -1;
                break;
            case R.id.tvAddMedia:
                /**
                 * 添加图片或者视频
                 */
                updateView(v);
                add_title.setText("添加自定义");
                break;
            case R.id.tvTimeSpace:
                updateView(v);
                add_title.setText("设置迎宾时间段");
                break;
            case R.id.videoPull:
                if (isSelectedMusic || isSelectedPicture) {
                    mPresenter.showToast("音乐图片模式下暂不支持上传视频");
                    return;
                }
                ((AddBodyShowPresenter) mPresenter).toAddMedia("video");
                break;
            case R.id.imagePull:
                if (isSelectedVideo) {
                    mPresenter.showToast("视频模式下暂不支持上传图片");
                    return;
                }
                ((AddBodyShowPresenter) mPresenter).toAddMedia("image");
                break;
            case R.id.musicPull:
                if (isSelectedVideo) {
                    mPresenter.showToast("视频模式下暂不支持上传音乐");
                    return;
                }
                ((AddBodyShowPresenter) mPresenter).toAddMedia("music");
                break;
            case R.id.deleteMusic:
                musicName.setText("");
                tvAddMedia.setText("自定义");
                ((AddBodyShowPresenter) mPresenter).addMusic = "";
                if (TextUtils.isEmpty(imageName.getText().toString())) {
                    videoPull.setSelected(false);
                    videoPull.setEnabled(!videoPull.isSelected());

                }
                deleteMusic.setVisibility(View.INVISIBLE);
                isSelectedMusic = false;
                fileTime = 0;
                getMaxTime();

                initSay();


                break;
            case R.id.deleteVideo:
                imagePull.setSelected(false);
                musicPull.setSelected(false);
                musicPull.setEnabled(!musicPull.isSelected());
                imagePull.setEnabled(!imagePull.isSelected());
                videoName.setText("");
                tvAddMedia.setText("自定义");
                ((AddBodyShowPresenter) mPresenter).addMedia = "";
                fileTime = 0;
                getMaxTime();
                deleteVideo.setVisibility(View.INVISIBLE);
                isSelectedVideo = false;
                initSay();
                break;
            case R.id.deleteImage:
                imageName.setText("");
                if (TextUtils.isEmpty(musicName.getText().toString())) {
                    videoPull.setSelected(false);
                    videoPull.setEnabled(!videoPull.isSelected());
                }
                ((AddBodyShowPresenter) mPresenter).addMedia = "";
                deleteImage.setVisibility(View.INVISIBLE);
                isSelectedPicture = false;
                if (TextUtils.isEmpty(((AddBodyShowPresenter) mPresenter).addMusic))
                    isSelectedPicture = false;
                break;
            case R.id.add_start_time_space:
                DatePickerUtils.getInstance().setDataPickDialog((TextView) v, null, getContext());
                break;
            case R.id.add_end_time_space:
                DatePickerUtils.getInstance().setDataPickDialog((TextView) v, null, getContext());
                break;
        }
    }

    private void initSay() {
        float time = (float) (mEditText.getText().length() * 0.27);

        if (!isSelectedMusic && !isSelectedVideo) {
            msaybtn.setText(mSayType + "(" + time + "'')");
            sayTime = (double) time;
        } else {
            msaybtn.setText(mSayType);
            sayTime = 0;
        }
    }

    @Override
    public void setAdapter(BaseAdapter mAdapter) {
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public String getEditContent() {
        return mEditText.getText().toString().trim();
    }

    @Override
    public void setEditContent(String content) {
        mEditText.setText(content);
        mEditText.setSelection(mEditText.getText().length());
        float time = (float) (mEditText.getText().length() * 0.27);

        tvSayTime.setText("语音时长：" + time + "秒");
        msaybtn.setText(mSayType + "(" + time + "'')");
        sayTime = (double) time;
        getMaxTime();
    }

    private void initTtsAdapterData() {
//        ttsContentAdapter = new TtsContentAdapter(getContext(), getPreList(itemNum));
//        ttsContentAdapter.setOnPrePareWordsOnClick(new TtsContentAdapter.OnPrePareWordsOnClick() {
//            @Override
//            public void onClick(TtsBean ttsBean) {
//                setEditContent(ttsBean.getmSay());
//            }
//        });
//        prepareSayLv.setAdapter(ttsContentAdapter);
        addFlowLayout.setAdapter(new TagAdapter<TtsBean>(getPreList(itemNum)) {
            @Override
            public View getView(FlowLayout parent, int position, final TtsBean ttsBean) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item_tts_content, addFlowLayout, false);
                TextView textView = (TextView) view.findViewById(R.id.item_tts_text);
                textView.setText(ttsBean.getmSay());
                if (itemNum == 1) {
                    if (position == 5) {
                        textView.setBackground(getResources().getDrawable(R.drawable.coffee_corners_bg));
                    } else if (position == 6) {
                        textView.setBackground(getResources().getDrawable(R.drawable.green_corners_bg));
                    } else if (position == 7) {
                        textView.setBackground(getResources().getDrawable(R.drawable.person_corners_bg));
                    }
                } else if (itemNum == 2) {
                    if (position == 3) {
                        textView.setBackground(getResources().getDrawable(R.drawable.coffee_corners_bg));
                    } else if (position == 4) {
                        textView.setBackground(getResources().getDrawable(R.drawable.purple_corners_bg));
                    }
                }
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setEditContent(ttsBean.getmSay());
                    }
                });
                return view;
            }
        });
    }


    /**
     * 设置左边表情选项的内容
     */
    @Override
    public void setFaceTime(double time) {
        faceTime = time;
        //删除表情
        if (time > 0) {
            mFace.setText("表情(" + time + "'')");
            getMaxTime();
        } else {
            mFace.setText("表情");
            getMaxTime();
        }
    }

    @Override
    public void setSystemActionTime(double time) {
        actionTime = Double.parseDouble(String.format(Locale.CHINA, "%.2f", time));
        //删除表情
        if (actionTime > 0) {
            mAction.setText("动作(" + actionTime + "'')");
            getMaxTime();
        } else {
            mAction.setText("动作");
            getMaxTime();
        }
    }

    /**
     * 设置选中的表情数据
     *
     * @param adapter
     */
    @Override
    public void setActionsAndFaces(FaceAndActionAdapter adapter) {
        if (adapter != null) {
            tvAnctionsAndFaces.setAdapter(adapter);
        }
    }

//    @Override
//    public void setMaxTime(String time) {
//        totalTime.setText("播放总时长:(" + time + "'')");
//    }

    /**
     * 设置滚动到最新添加的表情
     *
     * @param position
     */
    @Override
    public void setCurrentActionOrFace(int position) {
        if (position == -1)
            return;
        L.e("====>>>", "position=" + position);
        tvAnctionsAndFaces.smoothScrollToPosition(position);
    }


    /**
     * 自定义动作
     *
     * @param time
     */
    @Override
    public void setActionTime(String time) {
        if (!TextUtils.isEmpty(time)) {
            actionTime = Double.parseDouble(time);
            mCreateScript.setText("创建动作(" + time + "'')");
        } else {
            mCreateScript.setText("创建动作");
            actionTime = 0;
        }
        getMaxTime();
    }

    @Override
    public String getLight() {
        return light;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setFullScreen();
    }

    @Override
    public void setCreateScriptText(String text) {

        if (!TextUtils.isEmpty(text))
            mCreateScript.setText("创建动作(" + text + ")");
        else
            mCreateScript.setText("创建动作");
    }

    @Override
    public void setActionEnable(boolean b) {
//        mAction.setEnabled(b);
//        findViewById(R.id.delete_create_script).setVisibility(b ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((AddBodyShowPresenter) mPresenter).onItemClick(parent, view, position, id);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (mClose.getId() == checkedId) {
            light = "1";
            flickerSpinner.setEnabled(false);
            openSpinner.setEnabled(false);
            openSpinner.setSelection(0);
            flickerSpinner.setSelection(0);
            mLightbtn.setText("灯带");
            lightTime = 0;
            getMaxTime();
        } else if (mOpen.getId() == checkedId) {
            light = "0";
            flickerSpinner.setEnabled(false);
            openSpinner.setEnabled(true);
            flickerSpinner.setSelection(0);
            mLightbtn.setText("灯带");
            lightTime = 0;
            getMaxTime();
        } else if (mFlicker.getId() == checkedId) {
            light = "2";
            openSpinner.setEnabled(false);
            flickerSpinner.setEnabled(true);
            openSpinner.setSelection(0);
            mLightbtn.setText("灯带");
            lightTime = 0;
            getMaxTime();

        }
    }


    @Override
    public void setRightHeadLayoutGone(boolean isGone) {
//        if (isGone) {
//            head_right_layout.setVisibility(View.INVISIBLE);
//        } else {
//            head_right_layout.setVisibility(View.VISIBLE);
//        }

    }

    @Override
    public void setRightWheelLayoutGone(boolean isGone) {


    }

    @Override
    public void setRightWingLayoutGone(boolean isGone) {
//        if (isGone) {
//            wing_right_layout.setVisibility(View.INVISIBLE);
//        } else {
//            wing_right_layout.setVisibility(View.VISIBLE);
//        }
    }


    @Override
    public double getMaxTime() {
        double[] time = {sayTime, lightTime, fileTime, actionTime, faceTime};
        double max = time[0];
        for (int i = 1; i < time.length; i++) {
            if (max < time[i]) {
                max = time[i];
            }
        }
        BigDecimal bd = new BigDecimal(max);

        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalTime.setText("播放总时长:(" + bd.toString() + "'')");
        ((AddBodyShowPresenter) mPresenter).totalTime = bd.toString();
        return max;
    }

    @Override
    public String getGuestStartTime() {
        return etTimeStart.getText().toString();
    }

    @Override
    public String getGuestEndTime() {
        return etTimeEnd.getText().toString();
    }

    @Override
    public void setGuestTime(String time) {
        if (!TextUtils.isEmpty(time) && time.contains("@#")) {
            String[] times = time.split("@#");
            etTimeStart.setText(times[0]);
            etTimeEnd.setText(times[1]);
        }
    }


    @Override
    public void setMediaTime(double mediaTime) {
        fileTime = mediaTime;
        tvAddMedia.setText("自定义(" + mediaTime + "'')");
        getMaxTime();
    }


    @Override
    public void setActionEnabled(String enabled) {
        if ("0".equals(enabled)) {
            mCreateScript.setEnabled(false);
            mAction.setEnabled(true);
        } else if ("1".equals(enabled)) {
            mCreateScript.setEnabled(true);
            mAction.setEnabled(false);
        } else {
            mCreateScript.setEnabled(true);
            mAction.setEnabled(true);
        }
    }

    @Override
    public void setLightEnnabled(boolean isContentLight) {
        if (isContentLight) {
            mLightbtn.setEnabled(false);
            setLight("1");
            flickerSpinner.setSelection(0);
            openSpinner.setSelection(0);

        } else {
            mLightbtn.setEnabled(true);
        }
    }


    class ResourceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("efrobot.robot.resoure")) {
                final String path = intent.getStringExtra("path");
                final String selectType = intent.getStringExtra("type");
                Log.e("zhang", "接受广播多媒体地址=====" + path);
                if (!TextUtils.isEmpty(path)) {
                    if ("music".equals(selectType)) {

                        ((AddBodyShowPresenter) mPresenter).getMediaTime(path, new AddBodyShowPresenter.OnGetDuration() {
                            @Override
                            public void onGet(String mGetDuration) {

                                if (TextUtils.isEmpty(mGetDuration)) {
                                    return;
                                }


                                deleteMusic.setVisibility(View.VISIBLE);
                                /***
                                 * 音频路径
                                 */
                                ((AddBodyShowPresenter) mPresenter).addMusic = path;
                                isSelectedMusic = true;
                                tvAddMedia.setText("自定义(" + mGetDuration + "'')");
                                videoPull.setSelected(true);
                                fileTime = Double.parseDouble(mGetDuration);
                                initSay();
                                setMediaInfo(selectType, path);
                            }
                        });


                    } else if ("video".equals(selectType)) {


                        ((AddBodyShowPresenter) mPresenter).getMediaTime(path, new AddBodyShowPresenter.OnGetDuration() {
                            @Override
                            public void onGet(String mGetDuration) {
                                if (TextUtils.isEmpty(mGetDuration)) {
                                    return;
                                }
                                deleteVideo.setVisibility(View.VISIBLE);
                                /***
                                 * 媒体路径------视频
                                 */
                                ((AddBodyShowPresenter) mPresenter).addMedia = path;
                                isSelectedVideo = true;
                                tvAddMedia.setText("自定义(" + mGetDuration + "'')");
                                musicPull.setSelected(true);
                                imagePull.setSelected(true);
                                fileTime = Double.parseDouble(mGetDuration);
                                initSay();

                                setMediaInfo(selectType, path);
                            }
                        });

                    } else if ("image".equals(selectType)) {
                        deleteImage.setVisibility(View.VISIBLE);
                        /***
                         * 媒体路径----图片
                         */
                        ((AddBodyShowPresenter) mPresenter).addMedia = path;
                        isSelectedPicture = true;
                        videoPull.setSelected(true);

                        setMediaInfo(selectType, path);
                    }


                } else {
                    getMaxTime();
                    imageName.setText("");
                    musicName.setText("");
                    videoName.setText("");
                    imagePull.setSelected(false);
                    musicName.setSelected(false);
                    videoPull.setSelected(false);
                    ivPhoto.setImageResource(R.mipmap.tupainjiazai);
                    ((AddBodyShowPresenter) mPresenter).addMedia = "";
                    ((AddBodyShowPresenter) mPresenter).addMusic = "";
                }

            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                Log.d(TAG, "onReceive ACTION_USER_PRESENT: ");
            }
        }

    }

    private void setMediaInfo(String selectType, String path) {
        videoPull.setEnabled(!videoPull.isSelected());
        musicPull.setEnabled(!musicPull.isSelected());
        imagePull.setEnabled(!imagePull.isSelected());

        if (TextUtils.isEmpty(path))
            return;
        try {
            File file = new File(path);
            L.e("--->>>", "media  path=" + path.substring(1) + "     " + file.exists());
            if (file.exists()) {
                int dos = path.lastIndexOf("/");
                if (dos != -1) {
                    String name = path.substring(dos + 1, path.length());
                    if ("music".equals(selectType)) {
                        musicName.setText(name);
                    } else if ("video".equals(selectType)) {
                        videoName.setText(name);
                    } else if ("image".equals(selectType)) {
                        imageName.setText(name);
                    }
                }
            } else {
                videoName.setText("");
                musicName.setText("");
                imageName.setText("");
                ivPhoto.setImageResource(R.mipmap.tupainjiazai);
                ((AddBodyShowPresenter) mPresenter).addMedia = "";
                ((AddBodyShowPresenter) mPresenter).addMusic = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getMaxTime();
    }


    //    /**
//     * 设置图片和名称
//     *
//     * @param path
//     */
//    @Override
//    public void setMedia(String path) {

//    }
}
