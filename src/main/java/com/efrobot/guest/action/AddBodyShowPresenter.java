package com.efrobot.guest.action;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.efrobot.guest.R;
import com.efrobot.guest.base.GuestsBasePresenter;
import com.efrobot.guest.bean.FaceAndActionEntity;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.dao.DataManager;
import com.efrobot.guest.utils.CustomHintDialog;
import com.efrobot.guest.utils.DiyFaceAndActionUtils;
import com.efrobot.library.model.AppInfo;
import com.efrobot.library.mvp.utils.L;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2016/2/18.
 */
public class AddBodyShowPresenter extends GuestsBasePresenter<IAddBodyShowView> {

    private HashMap<String, String> mFaceList;
    private HashMap<String, String> mActionList;

    private double fileTime = 0;

    public AddBodyShowPresenter(IAddBodyShowView mView) {
        super(mView);
    }

    /**
     * 本地数据库管理
     */
    DataManager localDB;
    /**
     * 标记表情和动作
     * 1表情 2动作
     */
    private int type = 0;
    private int itemNum = -1;
    //0系统动作，1自定义
    private String finishSport = "-1";
    private String finishFace = "";
    public String finishAction = "";
    private String finishLight = "";
    private String finishOther = "";
    private double finishFaceTime = 0;
    private double finishActionTime = 0;
    //灯带开的时长
    private String finishOpenLightTime = "";
    //灯带闪烁的时长
    private String finishFlickerLightTime = "";
    /**
     * 添加的图片或视频
     */
    public String addMedia = "";

    /**
     * 添加音乐
     */
    public String addMusic = "";
    /**
     * 标记为 1添加 2修改
     */
    private int dbType = 1;

    /**
     * 编辑后的内容
     */
    private ItemsContentBean bean;

    /**
     * 表情、动作帮助类
     */
    public DiyFaceAndActionUtils util;

    //头部的footerview中的添加按钮
    private Button headBtn;
    //翅膀的footerview中的添加按钮
    private Button wheelBtn;
    //轮子的footerview中的添加按钮
    private Button wingBtn;
    /**
     * 当前选中的部位
     */
    public int radiocheck = 1;
    /**
     * 表情集
     */
    private List<String> titleFace = new ArrayList<String>();
    /**
     * 表情时间集合
     */
    private String[] faceTimeArray;
    /**
     * 动作集
     */
    private List<String> titleAction = new ArrayList<String>();

    /**
     * 动作时间集合
     */
    private HashMap<String, String> actionTimeArray;

    /**
     * 包含灯带的系统动作
     */
    private String[] syActionContentLight;

    /**
     * 表情数据适配器
     */
    private FaceAndActionAdapter faceAdapter;

    /**
     * 表情数据适配器
     */
    private FaceAndActionAdapter actionAdapter;
    /**
     * 运行总时长
     */
    public String totalTime;

    private DiyFaceAndActionUtils diyFaceAndActionUtils;

    public List<AppInfo> getAppInfos() {
        return mAppInfos;
    }

    private List<AppInfo> mAppInfos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        dataBaseManager = new DataBaseManager(getContext());
        localDB = DataManager.getInstance(getContext());
        util = DiyFaceAndActionUtils.getInstance(getContext());
        mActionList = new HashMap<String, String>();
//        mActionList = FaceAndActionUtils.getInstance(getContext()).readActionData();
        mActionList = new HashMap<String, String>();
        diyFaceAndActionUtils = new DiyFaceAndActionUtils(getContext());
        final List<FaceAndActionEntity> list = localDB.queryAllAction();

        if (list != null && !list.isEmpty()) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                FaceAndActionEntity faceAndActionEntity = list.get(i);
                mActionList.put(faceAndActionEntity.index, faceAndActionEntity.content + "(" + faceAndActionEntity.time + ")");
            }
        }
        if (mActionList != null) {
            L.e("-===>>", mActionList.toString());
        }

        mFaceList = util.readFaceData();

        /**
         * 获取表情分别对应的时间
         */
        Resources res = getContext().getResources();
        faceTimeArray = res.getStringArray(R.array.face_time_bodycommercial);
        actionTimeArray = diyFaceAndActionUtils.readActionTime();
        /**
         * 获取表情分别对应的时间
         */
        syActionContentLight = res.getStringArray(R.array.action_light);

        if (mFaceList != null) {
//            L.e("-===>>", mFaceList.toString());
        }


        Intent intent = ((AddBodyShowView) mView).getIntent();

        if (intent.hasExtra("itemNum")) {
            itemNum = intent.getIntExtra("itemNum", -1);
        }
//            dbType = 1;
        bean = new ItemsContentBean();

        initValue();
//        } else
        if (intent.hasExtra("content")) {

            dbType = 2;
            bean = (ItemsContentBean) intent.getSerializableExtra("content");

            if (bean != null) {
                itemNum = bean.getItemNum();
                finishFace = bean.getFace();
                finishAction = bean.getAction();
                finishOther = bean.getOther();
                finishLight = bean.getLight();
                finishSport = bean.getSport();
                addMedia = bean.getMedia();
                addMusic = bean.getMusic();
                finishOpenLightTime = bean.getOpenLightTime();
                finishFlickerLightTime = bean.getFlickerLightTime();
                if (!TextUtils.isEmpty(finishAction)) {
                    mView.setLightEnnabled(Arrays.asList(syActionContentLight).contains(finishAction));
                }

                //设置选择的表情时间
                if (bean.getFaceTime() != null && !TextUtils.isEmpty(bean.getFaceTime())) {
                    finishFaceTime = Double.parseDouble(bean.getFaceTime());
                } else {
                    if (!TextUtils.isEmpty(bean.getFace())) {
                        try {
                            finishFaceTime = Double.parseDouble(faceTimeArray[Integer.parseInt(bean.getFace()) - 1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                mView.setFaceTime(finishFaceTime);

                //设置选择的系统动作时间
                if (bean.getActionSystemTime() != null && !TextUtils.isEmpty(bean.getActionSystemTime())) {
                    finishActionTime = Double.parseDouble(bean.getActionSystemTime());
                } else {
                    if (!TextUtils.isEmpty(bean.getActionSystemTime())) {
                        try {
                            finishActionTime = Double.parseDouble(actionTimeArray.get(bean.getAction()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                mView.setSystemActionTime(finishActionTime);

                //表情数据显示
                if (!TextUtils.isEmpty(finishFace)) {
                    String[] faces = finishFace.split("、");
                    if (faces.length > 0) {
                        int size = faces.length;
                        for (int i = 0; i < size; i++) {
                            titleFace.add(faces[i]);
                        }
                    }
                }

                //动作数据显示
                if (!TextUtils.isEmpty(finishAction)) {
                    String[] actions = finishAction.split("、");
                    if (actions.length > 0) {
                        int size = actions.length;
                        for (int i = 0; i < size; i++) {
                            titleAction.add(actions[i]);
                        }
                    }
                }
//                initFacesAdater();


                mView.setEditContent(finishOther);


                if (addMedia != null && !TextUtils.isEmpty(addMedia)) {


                    if (addMedia.endsWith(".mp4")) {
                        /***
                         * 视频
                         */
                        getMediaTime(addMedia, new OnGetDuration() {
                            @Override
                            public void onGet(String mGetDuration) {

                                if (TextUtils.isEmpty(mGetDuration)) {
                                    addMedia = "";
                                    //自定义数据显示
                                    initMusic();
                                    return;
                                }

                                fileTime = Double.parseDouble(mGetDuration);
                                mView.setMediaTime(fileTime);
                                initData("11111111");
                            }
                        });
                    } else {
                        File mFile = new File(addMedia);
                        if (!mFile.exists()) {
                            addMedia = "";
                        }
                        initMusic();
                    }

                } else {
                    initMusic();
                }


            }
        }

    }

    private void initMusic() {
        //自定义数据显示
        if (addMusic != null && !TextUtils.isEmpty(addMusic)) {
            getMediaTime(addMusic, new OnGetDuration() {
                @Override
                public void onGet(String mGetDuration) {
                    if (TextUtils.isEmpty(mGetDuration)) {
                        addMusic = "";
                        initData("55555555555555555555555");
                        return;
                    }

                    fileTime = Double.parseDouble(mGetDuration);
                    mView.setMediaTime(fileTime);
                    initData("2222222222222");
                }
            });

        } else {
            initData("3333333333333333333333");
        }
    }

    private void initValue() {
        mView.setFirstViewShow();
    }

    private void initData(String value) {
        L.e("===============>>>>>>>>>>", "初始化数据" + value);

        if (!TextUtils.isEmpty(addMusic) || !TextUtils.isEmpty(addMedia)) {
            mView.setMedia2(addMusic, addMedia);
        }

        mView.setActionEnabled(finishSport);

        //灯带的设置
        if (finishOpenLightTime != null && finishFlickerLightTime != null) {
            //设置灯带的spinner
            mView.setOpenAndFlickerTime(finishOpenLightTime, finishFlickerLightTime);
        }


        mView.setLight(finishLight);

//        //动作
//        if (!TextUtils.isEmpty(finishAction)) {
//            mView.setActionText(finishAction);
//        }
        boolean isUpdateTime = false;

        if (bean.getMaxTime() != null && !TextUtils.isEmpty(bean.getMaxTime())) {
            //播放总时长
            double time = mView.getMaxTime();
            double max = Double.parseDouble(bean.getMaxTime());

            if (max != time) {
                isUpdateTime = true;
                /***
                 * 更新下数据库
                 */
                bean.setMaxTime(time + "");
            }
        }

        boolean isToast = false;
        if (TextUtils.isEmpty(addMedia) && !TextUtils.isEmpty(bean.getMedia())) {
            bean.setMedia("");
            isToast = true;

        }
        if (TextUtils.isEmpty(addMusic) && !TextUtils.isEmpty(bean.getMusic())) {
            bean.setMusic("");
            isToast = true;
        }
        if (isToast) {
            showToast("请注意！自定义上传文件已丢失");
        }
        if (isUpdateTime || isToast) {
            localDB.updateItem(bean);
        }

        initValue();
    }

    public interface OnGetDuration {
        void onGet(String mGetDuration);
    }


    public void getMediaTime(String string, final OnGetDuration mOnGetDuration) {
        //使用此方法可以直接在后台获取音频文件的播放时间，而不会真的播放音频


        File mFile = new File(string);

        if (mFile.exists()) {
            MediaPlayer player = new MediaPlayer();  //首先你先定义一个mediaplayer
            try {
                player.setDataSource(string);  //String是指音频文件的路径
                player.prepare();        //这个是mediaplayer的播放准备 缓冲
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {//监听准备

                    @Override
                    public void onPrepared(MediaPlayer player) {

                        double size = player.getDuration();
                        L.e("=========>>>>", "获取的大小是：" + size);
                        String timelong = (int) Math.ceil((size / 1000)) + "";//转换为秒 单位为''
                        player.stop();//暂停播放
                        player.release();//释放资源
                        mOnGetDuration.onGet(timelong);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                mOnGetDuration.onGet("");
            }

        } else {
            mOnGetDuration.onGet("");
        }
    }


    /**
     * 选中表情数据适配
     */
    private void initActionandFacesAdater(final int type) {
        if (type == 1) {
            if (faceAdapter == null) {
                faceAdapter = new FaceAndActionAdapter(getContext(), new FaceAndActionAdapter.OnFaceDeleteCallBack() {
                    @Override
                    public void deleteCallBack(int positon) {
                        if (titleFace.isEmpty() || positon >= titleFace.size())
                            return;
                        int index = -1;
                        try {
                            index = Integer.parseInt(titleFace.get(positon));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (index - 1 >= 0 && index - 1 < faceTimeArray.length) {
                            finishFaceTime = finishFaceTime - Double.parseDouble(faceTimeArray[index - 1]);
                            mView.setFaceTime(finishFaceTime);
                            titleFace.remove(positon);
                            initActionandFacesAdater(type);
                        }
                    }
                });

                faceAdapter.setList(titleFace, type);
                mView.setActionsAndFaces(faceAdapter);
            } else {
                faceAdapter.setList(titleFace, type);
                mView.setActionsAndFaces(faceAdapter);
            }
            mView.setCurrentActionOrFace(titleFace.size());
        } else if (type == 2) {
            if (actionAdapter == null) {
                actionAdapter = new FaceAndActionAdapter(getContext(), new FaceAndActionAdapter.OnFaceDeleteCallBack() {
                    @Override
                    public void deleteCallBack(int positon) {
                        if (titleAction.isEmpty() || positon >= titleAction.size())
                            return;
                        int index = -1;
                        try {
                            index = Integer.parseInt(titleAction.get(positon));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finishActionTime = finishActionTime - Double.parseDouble(actionTimeArray.get(titleAction.get(positon)));
                        mView.setSystemActionTime(finishActionTime);
                        titleAction.remove(positon);
                        initActionandFacesAdater(type);
                    }
                });

                actionAdapter.setList(titleAction, type);
                mView.setActionsAndFaces(actionAdapter);
            } else {
                actionAdapter.setList(titleAction, type);
                mView.setActionsAndFaces(actionAdapter);
            }
            mView.setCurrentActionOrFace(titleAction.size());

        }
    }


    AddFaceAndActionAdapter mAdapter;

    /**
     * 初始化Adapter
     */
    private void initAdapter() {
        if (mAdapter == null) {
            mAdapter = new AddFaceAndActionAdapter(getContext());
            mView.setAdapter(mAdapter);
        }
    }

    /**
     * 显示动作或表情
     */
    public void showFaceAndAction(int myType) {
        this.type = myType;
        initAdapter();
        if (type == 1) {
            initActionandFacesAdater(type);
            mAdapter.setDataResource(mFaceList, titleAction, titleFace, 1);
        } else if (type == 2) {
            initActionandFacesAdater(type);
            mAdapter.setDataResource(mActionList, titleAction, titleFace, 2);
        }

    }

    /**
     * 获取表情数据
     *
     * @return
     */
    private String getAllFace(boolean isSave) {
        String value = "";
        if (titleFace != null && !titleFace.isEmpty()) {
            int size = titleFace.size();
            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    if (isSave)
                        value = value + titleFace.get(i);
                    else
                        value = value + util.contrastFace(titleFace.get(i));
                } else {
                    if (isSave)
                        value = value + "、" + titleFace.get(i);
                    else
                        value = value + "、" + util.contrastFace(titleFace.get(i));
                }
            }

        }
        return value;
    }


    /**
     * 获取动作数据
     *
     * @return
     */
    private String getAllAction(boolean isSave) {
        String value = "";
        if (titleAction != null && !titleAction.isEmpty()) {
            int size = titleAction.size();
            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    if (isSave)
                        value = value + titleAction.get(i);
                    else
                        value = value + util.contrastAction(titleAction.get(i));
                } else {
                    if (isSave)
                        value = value + "、" + titleAction.get(i);
                    else
                        value = value + "、" + util.contrastAction(titleAction.get(i));
                }
            }

        }
        return value;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (type == 1) {
            try {
                String face = mAdapter.getItem(position).index;
                if (titleFace.size() > 0) {
                    showToast("最多只能添加1个表情哦");
                    return;
                }
                //增加表情
                titleFace.add(face);
                finishFaceTime = finishFaceTime + Double.parseDouble(faceTimeArray[Integer.parseInt(face) - 1]);
                mView.setFaceTime(finishFaceTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showFaceAndAction(1);

        } else if (type == 2) {
            try {
                String action = mAdapter.getItem(position).index;
                if (titleAction.size() > 9) {
                    showToast("最多只能添加10个动作哦");
                    return;
                }
                //增加动作
                titleAction.add(action);
                finishActionTime = finishActionTime + Double.parseDouble(actionTimeArray.get(action));
                mView.setSystemActionTime(finishActionTime);
//                boolean isCon = Arrays.asList(syActionContentLight).contains(action);
//                mView.setLightEnnabled(isCon);
//                if (isCon){
//                    showToast("此动作附带灯带效果，因此关闭上方灯带选择项");
//                }
//                mView.setLightEnnabled(false);
//                finishSport = "0";
//                mView.setActionEnabled(finishSport);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showFaceAndAction(2);
        }

    }

    /**
     * 保存
     */
    public void saveData() {
//        if (itemNum == -1) {
//            showToast("添加失败");
//            return;
//        }

        finishOther = mView.getEditContent();

        if (titleFace.isEmpty() && titleAction.isEmpty() && TextUtils.isEmpty(finishOther) && "1".equals(mView.getLight()) &&
                TextUtils.isEmpty(addMedia) && TextUtils.isEmpty(addMusic)) {
            showToast("内容空空，添加内容后才能保存哦");
            return;
        }

        bean.setItemNum(itemNum);
        if (!titleFace.isEmpty()) {
            bean.setFace(getAllFace(true));
        } else {
            bean.setFace("");
        }
//        else {
//            bean.setFace("");
//            finishFaceTime = 0.00;
//            if (TextUtils.isEmpty(totalTime)) {
//                totalTime = "3.00";
//            } else {
//                double time = Double.parseDouble(totalTime);
//                if (time < 3.00) {
//                    totalTime = "3.00";
//                }
//            }
//
//        }

        if (!titleAction.isEmpty()) {
            bean.setAction(getAllAction(true));
        } else {
            bean.setAction("");
        }

        //判断是自定义动作还是系统动作
        if (!titleAction.isEmpty()) {
            finishSport = "0";
        } else {
            finishSport = "-1";
        }
        bean.setSport(finishSport);
        bean.setOther(finishOther);
        bean.setMedia(addMedia);

        bean.setMusic(addMusic);
        bean.setFaceTime("" + finishFaceTime);
        bean.setActionSystemTime(String.format(Locale.CHINA, "%.2f", finishActionTime));
        bean.setMaxTime(totalTime);
        bean.setLight(mView.getLight());

        if ("1".equals(mView.getLight())) {
            bean.setOpenLightTime("");
            bean.setFlickerLightTime("");
            bean.setLight(mView.getLight());
        }
        if ("0".equals(mView.getLight())) {
            if ("无".equals(mView.getOpenSpinner().getSelectedItem().toString())) {
                showToast("您在灯带选项中有时间未选择，可能会无法执行灯带效果。");
                return;
            } else {
                bean.setFlickerLightTime("");
                bean.setOpenLightTime(mView.getOpenSpinner().getSelectedItem().toString());
                bean.setLight(mView.getLight());
            }
        }
        if ("2".equals(mView.getLight())) {
            if ("无".equals(mView.getFlickerSpinner().getSelectedItem().toString())) {
                showToast("您在灯带选项中有时间未选择，可能会无法执行灯带效果。");
                return;
            } else {
                bean.setOpenLightTime("");
                bean.setFlickerLightTime(mView.getFlickerSpinner().getSelectedItem().toString());
                bean.setLight(mView.getLight());
            }
        }
        if (dbType == 1) {
            //添加
            localDB.insertContent(bean);
        } else if (dbType == 2) {
            //修改
            localDB.upateContent(bean);
        }

        setResult(new Intent(), 1);
        exit();
    }

    /**
     * 判断 编辑条目框是否做了添加的操作。
     */
    public void exitAdd() {
        if (!titleFace.isEmpty())
            finishFace = getAllFace(true);
        if (!titleAction.isEmpty())
            finishAction = getAllAction(true);
        if (dbType == 1) {
            if (!TextUtils.isEmpty(finishAction)
                    || !TextUtils.isEmpty(finishFace)
                    || !TextUtils.isEmpty(mView.getEditContent())
                    || !TextUtils.isEmpty(finishSport)
                    || !mView.getLight().equals("1")
                    || !TextUtils.isEmpty(addMedia)
                    || !TextUtils.isEmpty(addMusic)
                    ) {
                showDialog("确定放弃本次添加？", "back");
            } else {
                exit();
            }
        } else if (dbType == 2) {
            if (!finishLight.equals(mView.getLight())
                    || !getAllAction(true).equals(bean.getAction())
                    || !getAllFace(true).equals(bean.getFace())
                    || (!TextUtils.isEmpty(bean.getOther()) && !bean.getOther().equals(mView.getEditContent()))
                    || !bean.getSport().equals(finishSport)
                    || (!TextUtils.isEmpty(bean.getMedia()) && !bean.getMedia().equals(addMedia))
                    || (!TextUtils.isEmpty(bean.getMusic()) && !bean.getMedia().equals(addMusic))
                    ) {
                showDialog("确定放弃本次修改？", "back");
            } else {
                radiocheck = 1;
                exit();
            }
        }
    }

    /**
     * 提示框
     */
    private void showDialog(String content, final String check) {
        final CustomHintDialog dialog = new CustomHintDialog(getContext(), -1);
        dialog.setMessage(content);
        dialog.setCancleButton(getContext().getString(R.string.cancel), new CustomHintDialog.IButtonOnClickLister() {
            @Override
            public void onClickLister() {
                dialog.dismiss();
            }
        });
        dialog.setSubmitButton(getContext().getString(R.string.confirm), new CustomHintDialog.IButtonOnClickLister() {
            @Override
            public void onClickLister() {
                if ("custom".equals(check)) {
                    finishSport = "-1";
                    mView.setActionEnabled(finishSport);
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    radiocheck = 1;
                    exit();
                }
            }
        });
        dialog.show();
    }

    @Override
    public boolean onBackPressed() {
        exitAdd();
        return false;
    }


    /**
     * 添加图片或视频
     */
    public void toAddMedia(String check) {
        try {
            Intent intent = new Intent("efrobot.robot.bodyshow");
            intent.putExtra("pick_folder", true);
            intent.putExtra("come", 1);
            intent.putExtra("check", check);
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("zhang", "e=" + e.toString());
            showToast("打开文件管理失败");
        }

    }

}
