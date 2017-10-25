package com.efrobot.guests.setting;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.R;
import com.efrobot.guests.base.GuestsBasePresenter;
import com.efrobot.guests.bean.ItemsContentBean;
import com.efrobot.guests.bean.UlDistanceBean;
import com.efrobot.guests.dao.DataManager;
import com.efrobot.guests.dao.SelectedDao;
import com.efrobot.guests.dao.UltrasonicDao;
import com.efrobot.guests.setting.bean.SelectDirection;
import com.efrobot.guests.utils.CustomHintDialog;
import com.efrobot.guests.utils.TtsUtils;
import com.efrobot.guests.utils.UpdateUtils;
import com.efrobot.library.RobotManager;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.net.TextMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2017/3/2.
 */
public class SettingPresenter extends GuestsBasePresenter<ISettingView> implements RobotManager.OnGetUltrasonicCallBack
//        ,RobotManager.OnUltrasonicOccupyStatelistener
{

    private boolean isReceiveUltrasonic = false;

    public SettingPresenter(ISettingView mView) {
        super(mView);
    }

    private DataManager dataManager;
    private SelectedDao selectedDao;
    private UltrasonicDao ultrasonicDao;

    private ArrayList<UlDistanceBean> ulDistanceBeen = null;

    Handler ulHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    L.i(TAG, "Resend open UltrasonicData");
                    //发送探头信息
                    sendOpenUltrasonicData();
                    break;
                case 1:
                    int numberNg = msg.arg1;
                    int valueNG = msg.arg2;
                    long valueNgCM = valueNG / 10;
                    switch (numberNg) {
                        case 0:
                            mView.setDistance1(valueNgCM + "");
                            break;
                        case 1:
                            mView.setDistance2(valueNgCM + "");
                            break;
                        case 2:
                            mView.setDistance3(valueNgCM + "");
                            break;
                        case 6:
                            mView.setDistance7(valueNgCM + "");
                            break;
                        case 7:
                            mView.setDistance8(valueNgCM + "");
                            break;

                        case 8:
                            mView.setDistance9(valueNgCM + "");
                            break;
                        case 9:
                            mView.setDistance10(valueNgCM + "");
                            break;
                        case 10:
                            mView.setDistance11(valueNgCM + "");
                            break;
                        case 11:
                            mView.setDistance12(valueNgCM + "");
                            break;
                        case 12:
                            mView.setDistance13(valueNgCM + "");
                            break;
                    }
                    break;
                case 2:
                    if (null != dialog) {
                        //是否没有正常退出弹出框
                        if (!isNormalExit) {
                            dialog.dismiss();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        L.e(TAG, "SettingPresenter onResume");
        if (TtsUtils.isCanUseGuest(getContext())) {
            initUltrasonicData();
        } else {
            showCanUserDialog(getContext().getString(R.string.error_use__hint));
        }
    }

    String versionName = "";

    public void initUltrasonicData() {
        isReceiveUltrasonic = false;
        ulHandle.sendEmptyMessageDelayed(0, 5000);
        versionName = new UpdateUtils().getVersion(getContext(), getContext().getPackageName());
    }


    CustomHintDialog mUpdateDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        RobotManager.getInstance(getContext()).registerUltrasonicOccupylistener(this);


        new UpdateUtils().getInstance().getAppDetail(getContext(), getContext().getPackageName(), new UpdateUtils.onAppCallBack() {
            @Override
            public void onSuccess(TextMessage message, String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("appVersion")) {
                        String newVersion = jsonObject.optString("appVersion");
                        if (!TextUtils.isEmpty(versionName) && !TextUtils.isEmpty(newVersion)) {
                            float mVersionName = Float.parseFloat(versionName);
                            float mNewVersionName = Float.parseFloat(newVersion);
                            if (mNewVersionName > mVersionName) {
                                /** 检测商城有新的版本号 需要提示更新 */
                                mUpdateDialog = new CustomHintDialog(getContext(), -1);
                                mUpdateDialog.setTitle("提示");
                                mUpdateDialog.setMessage("检测到迎宾有新版本，请前往商城管理页面打开已购项目进行更新");
//                                mUpdateDialog.setSubmitButton("前往", new CustomHintDialog.IButtonOnClickLister() {
//                                    @Override
//                                    public void onClickLister() {
//                                        Intent intent = new Intent();
//                                        ComponentName componentName = new ComponentName("com.efrobot.appstore", "com.efrobot.appstore.activity.SplashView");
//                                        intent.setComponent(componentName);
//                                        intent.putExtra("msg", 5);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        getContext().startActivity(intent);
//                                    }
//                                });
                                mUpdateDialog.setCancleButton("确 定", new CustomHintDialog.IButtonOnClickLister() {
                                    @Override
                                    public void onClickLister() {
                                        if (mUpdateDialog != null) {
                                            mUpdateDialog.dismiss();
                                        }
                                    }
                                });
                                mUpdateDialog.show();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(TextMessage message, int errorCode, String errorMessage) {

            }
        });

        ultrasonicDao = GuestsApplication.from(getContext()).getUltrasonicDao();
        dataManager = DataManager.getInstance(getContext());
        selectedDao = GuestsApplication.from(getContext()).getSelectedDao();

        {
            //超声波测试数据
            initUltrasonicData();
        }
    }

    private GreetingAdapter greetingAdapter;

    public GreetingAdapter getLeftGreetingAdapter() {
        if (greetingAdapter == null) {
            greetingAdapter = new GreetingAdapter(getContext());
        }
        return greetingAdapter;
    }

    private GreetingAdapter rightGreetingAdapter;

    public GreetingAdapter getRightGreetingAdapter() {
        if (rightGreetingAdapter == null) {
            rightGreetingAdapter = new GreetingAdapter(getContext());
        }
        return rightGreetingAdapter;
    }

    private GreetingAdapter finishGreetingAdapter;

    public GreetingAdapter getFinishGreetingAdapter() {
        if (finishGreetingAdapter == null) {
            finishGreetingAdapter = new GreetingAdapter(getContext());
        }
        return finishGreetingAdapter;
    }


    private Dialog helpDialog;
    private RelativeLayout hintRl1, hintRl2, hintRl3, hintRl4;

    public void showFunHelpDialog() {
        helpDialog = new Dialog(getContext(), R.style.Dialog_Help_Fullscreen);
        View currentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_use_help, null);
        hintRl1 = (RelativeLayout) currentView.findViewById(R.id.hint_1);
        hintRl2 = (RelativeLayout) currentView.findViewById(R.id.hint_2);
        hintRl3 = (RelativeLayout) currentView.findViewById(R.id.hint_3);
        hintRl4 = (RelativeLayout) currentView.findViewById(R.id.hint_4);

        hintRl2.setVisibility(View.GONE);
        hintRl3.setVisibility(View.GONE);
        hintRl4.setVisibility(View.GONE);

        showHint1();

        helpDialog.setContentView(currentView);
        helpDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        helpDialog.show();

    }

    private Handler viewHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {

//                TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -300);
//                translateAnimation.setDuration(2000);
//                translateAnimation.setFillAfter(true);
//                hand1.startAnimation(translateAnimation);


                ValueAnimator animator = ValueAnimator.ofFloat(hand1.getTranslationY(), -200.0f);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (Float) animation.getAnimatedValue();
                        hand1.setTranslationY(value);
                    }
                });
                animator.setDuration(400);
                animator.start();

            } else if (msg.what == 1) {
                ValueAnimator animator = ValueAnimator.ofFloat(help_hand_3.getTranslationY(), 230.0f);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (Float) animation.getAnimatedValue();
                        help_hand_3.setTranslationY(value);
                    }
                });
                animator.setDuration(1000);
                animator.start();
            }
        }
    };

    private ImageView knownImg1;
    private RelativeLayout hand1;

    private void showHint1() {
        hintRl1.setVisibility(View.VISIBLE);
        knownImg1 = (ImageView) hintRl1.findViewById(R.id.dialog_help_known_1);
        hand1 = (RelativeLayout) hintRl1.findViewById(R.id.hand_1);

        knownImg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knownImg1.setVisibility(View.GONE);
                hand1.setVisibility(View.VISIBLE);
                hand1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        viewHandle.sendEmptyMessage(0);
                    }
                });

            }
        });
        hand1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hintRl1.setVisibility(View.GONE);
                showHint2();
            }
        });
    }

    private ImageView knownImg2;

    private void showHint2() {
        hintRl2.setVisibility(View.VISIBLE);
        knownImg2 = (ImageView) hintRl2.findViewById(R.id.dialog_help_known_2);
        knownImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hintRl2.setVisibility(View.GONE);
                showHint3();
            }
        });

    }

    private RelativeLayout guestSetImg;
    private ImageView knownImg3, hintText;
    private RelativeLayout handDir1RlBtn, handDir2RlBtn, hintSelectDirRl;
    private ImageView knownImg3_1;

    private void showHint3() {
        hintRl3.setVisibility(View.VISIBLE);
        guestSetImg = (RelativeLayout) hintRl3.findViewById(R.id.dialog_help_guest_content);
        knownImg3 = (ImageView) hintRl3.findViewById(R.id.dialog_help_known_3);
        hintText = (ImageView) hintRl3.findViewById(R.id.hint_3_text);
        handDir1RlBtn = (RelativeLayout) hintRl3.findViewById(R.id.hint_3_1_dir_rl);
        hintSelectDirRl = (RelativeLayout) hintRl3.findViewById(R.id.hint_3_dir_view);
        knownImg3_1 = (ImageView) hintRl3.findViewById(R.id.dialog_help_known_3_1);

        knownImg3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knownImg3.setVisibility(View.GONE);
                hintText.setVisibility(View.GONE);
                handDir1RlBtn.setVisibility(View.VISIBLE);
                handDir1RlBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handDir1RlBtn.setVisibility(View.GONE);
                        guestSetImg.setVisibility(View.GONE);
                        hintSelectDirRl.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        handDir2RlBtn = (RelativeLayout) hintRl3.findViewById(R.id.hint_3_2_dir_rl);

        knownImg3_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hintSelectDirRl.setVisibility(View.GONE);
                guestSetImg.setVisibility(View.VISIBLE);
                handDir2RlBtn.setVisibility(View.VISIBLE);
                handDir2RlBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hintRl3.setVisibility(View.GONE);
                        showHint4();
                    }
                });
            }
        });

    }

    private ImageView known4Img, known5Img, help_hand_3;
    private RelativeLayout hint4Child1, hint4Child2;
    private ImageView dialog_help_add_time_bg, dialog_help_add_guest_bg;

    private void showHint4() {
        hintRl4.setVisibility(View.VISIBLE);
        known4Img = (ImageView) hintRl4.findViewById(R.id.dialog_help_known4);
        known5Img = (ImageView) hintRl4.findViewById(R.id.dialog_help_known5);
        help_hand_3 = (ImageView) hintRl4.findViewById(R.id.help_hand_3);
        hint4Child1 = (RelativeLayout) hintRl4.findViewById(R.id.hint_4_child_1);
        hint4Child2 = (RelativeLayout) hintRl4.findViewById(R.id.hint_4_child_2);
        dialog_help_add_time_bg = (ImageView) hintRl4.findViewById(R.id.dialog_help_add_time_bg);
        dialog_help_add_guest_bg = (ImageView) hintRl4.findViewById(R.id.dialog_help_add_guest_bg);

        known4Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hint4Child1.setVisibility(View.GONE);
                hint4Child2.setVisibility(View.VISIBLE);
                dialog_help_add_guest_bg.setVisibility(View.GONE);
                dialog_help_add_time_bg.setVisibility(View.VISIBLE);
                viewHandle.sendEmptyMessageDelayed(1, 200);
            }
        });

        known5Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helpDialog != null) {
                    helpDialog.dismiss();
                    helpDialog = null;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.i(TAG, "onDestroy");
        unRegisterAllCallBack();
    }

    public void unRegisterAllCallBack() {
        ulHandle.removeMessages(0);
        ulHandle.removeMessages(1);
        ulHandle.removeMessages(2);
        RobotManager.getInstance(getContext()).unRegisterOnGetUltrasonicCallBack();
//        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getContext())).closeUltrasonicFeedback(EnvUtil.ULGST001);
    }

    public void cancel() {
        exit();
    }

    public boolean affirm() {

        ArrayList<UlDistanceBean> ulDistanceBeen = ultrasonicDao.queryAll();
        boolean isAllDistanceEmpty = true;
        //设置超声波
        for (int i = 0; i < ulDistanceBeen.size(); i++) {
            if (!TextUtils.isEmpty(ulDistanceBeen.get(i).getDistanceValue())) {
                isAllDistanceEmpty = false;
                break;
            } else {
                isAllDistanceEmpty = true;
            }
        }
        if (isAllDistanceEmpty) {
            showToast("超声波距离不能为空哦");
            return false;
        }


        ArrayList<SelectDirection> selectDirections = selectedDao.queryAll();
        if (selectDirections.size() <= 0) {
            showToast("超声波方向不能为空哦");
            return false;
        }

        ArrayList<ItemsContentBean> itemsLeftContentBeans = dataManager.queryItem(1);
        ArrayList<ItemsContentBean> itemsRightContentBeans = dataManager.queryItem(2);
        if (itemsLeftContentBeans.size() == 0 && itemsRightContentBeans.size() == 0) {
            showToast("迎宾语不能为空哦");
            return false;
        }

        showToast("开始迎宾");
        return true;
    }

    private void reSend() {
        ulHandle.sendEmptyMessageDelayed(0, 5000);
    }

    byte mByte1 = (byte) 0x01; // 探头1
    byte mByte2 = (byte) 0x02;
    byte mByte3 = (byte) 0x04;
    byte mByte7 = (byte) 0x40;
    byte mByte8 = (byte) 0x80;

    byte mByte9 = (byte) 0x01; // 探头9
    byte mByte10 = (byte) 0x02;
    byte mByte11 = (byte) 0x04;
    byte mByte12 = (byte) 0x08;
    byte mByte13 = (byte) 0x10;

    private final int OpenUltrasonicNum = 10;

//    /**
//     * 新策略
//     */
//    private void sendOpenUltrasonicData() {
//        L.i("sendOpenUltrasonicData", "start send");
//
//        RobotManager.getInstance(getContext()).registerOnGetUltrasonicCallBack(this);
//
//        byte topOpen = (byte) (mByte1 | mByte2 | mByte3 | mByte7 | mByte8);
//
//        byte bottomOpen = (byte) (mByte9 | mByte10 | mByte11 | mByte12 | mByte13);
//
//        int needOpenUl = (topOpen & 0xFFFF) | (bottomOpen & 0xFFFF << 8);
//
//        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getContext())).openUltrasonicFeedback(EnvUtil.ULGST001, needOpenUl);
//        if (!isReceiveUltrasonic) { //是否接受到超声波检测信息
//            reSend();
//        }
//    }

    private void sendOpenUltrasonicData() {
        L.i("sendOpenUltrasonicData", "start send");
        byte[] data = new byte[12];
        data[0] = (byte) 0x0c;
        data[1] = (byte) 0x03;
        data[2] = (byte) 0x06;
        data[3] = (byte) 0x03;
//        data[4] = (byte) 0x1F;
//        data[5] = (byte) 0xFF;

//        data[4] = (byte) 0x0B;
//        data[5] = (byte) 0x83;

        data[5] = ((byte) (mByte1 | mByte2 | mByte3 | mByte7 | mByte8));
        data[4] = ((byte) (mByte9 | mByte10 | mByte11 | mByte12 | mByte13));

        data[6] = (byte) 0x00;
        data[7] = (byte) 7;
        //开启后8秒左右收到回调
        RobotManager.getInstance(getContext()).registerOnGetUltrasonicCallBack(this);
        RobotManager.getInstance(getContext()).getCustomTaskInstance().sendByteData(data);
        if (!isReceiveUltrasonic) { //是否接受到超声波检测信息
            reSend();
        }
    }

    /**
     *
     * */
    @Override
    public void onGetUltrasonic(byte[] data) {
        try {
            isReceiveUltrasonic = true;
            ulHandle.removeMessages(0);
            if (isUltraData(data)) {
//                L.i(TAG, "---------------data--" + Arrays.toString(data));
                byte[] bytes = new byte[4 * OpenUltrasonicNum];
                System.arraycopy(data, 5, bytes, 0, 4 * OpenUltrasonicNum);
                for (int i = 0; i < bytes.length; i++) {
                    if ((i - 3) % 4 == 0) {
                        int valueNG = (bytes[i] & 255) | ((bytes[i - 1] & 255) << 8); // 距离
                        int numberNg = (bytes[i - 2] & 255) | ((bytes[i - 3] & 255) << 8); // 返回的探头编号 0-12
//                        L.i(TAG, "第" + (numberNg) + "号超声波-----valueNG-" + valueNG);
                        Message message = ulHandle.obtainMessage();
                        message.arg1 = numberNg;
                        message.arg2 = valueNG;
                        message.what = 1;
                        ulHandle.sendMessage(message);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提示框
     */
    CustomHintDialog dialog, hitDialog;

    private boolean isNormalExit = false;

    public void showDialog(String content) {
        TtsUtils.sendTts(getContext(), content + ",标定开始");
        dialog = new CustomHintDialog(getContext(), -1);
        dialog.setTitle("提示");
        dialog.setMessage(content);
        dialog.setCancelable(true);
        sendTestUltrasonic(false);
        dialog.show();
        //5秒后关闭对话框
        ulHandle.sendEmptyMessageDelayed(2, 5000);
    }

    public void showCanUserDialog(String content) {
        hitDialog = new CustomHintDialog(getContext(), -1);
        hitDialog.setTitle("冲突提示");
        hitDialog.setMessage(content);
        hitDialog.setCancelable(false);
        hitDialog.setSubmitButton("朕 知 道 了", new CustomHintDialog.IButtonOnClickLister() {
            @Override
            public void onClickLister() {
                exit();
            }
        });
        hitDialog.show();
    }

    /**
     * 是否超声波检测
     *
     * @param data
     */
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
                    TtsUtils.sendTts(getContext(), "标定成功");
                    showToast("超声波标定成功 \t\tTime: " + getCurrentTime());
                    isReceiveUltrasonic = false;
                    sendOpenUltrasonicData();
                } else {
//                    失败
                    TtsUtils.sendTts(getContext(), "标定失败");
                    showToast("超声波标定失败 \t\tTime: " + getCurrentTime());
                }
                //getHandler().sendEmptyMessageDelayed(ULTRASONIC_REMOVE_MSG, 5000);
                if (null != dialog) {
                    dialog.dismiss();
                }
                isNormalExit = true;
            }
        }
        return bool;
    }

    //超声波初始化
    public void sendTestUltrasonic(boolean isWriteFlash) {
        byte[] data = new byte[11];
        data[0] = (byte) 0x0c;
        data[1] = (byte) 0x03;
        data[2] = (byte) 0x06;
        data[3] = (byte) 0x02;

        //data[4] = (byte) 0x1F;
        data[4] = (byte) 0x1F;

        //data[5] = (byte) 0xFF;
        data[5] = (byte) 0xFF;

        if (isWriteFlash)
            data[6] = (byte) 0x01;
        else
            data[6] = (byte) 0x00;
        RobotManager.getInstance(getContext()).getCustomTaskInstance().sendByteData(data);
    }

    public String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return (df.format(new Date()));     // new Date()为获取当前系统时间
    }

    /**
     * 1; 超声波模块可用
     * 0; 超声波模块不可用
     */
//    @Override
//    public void onUltrasonicOccupyState(String sceneCode, int isAvailable) {
//        L.e(TAG, "sceneCode = " + sceneCode + "---isAvailable = " + isAvailable);
//        if (isAvailable == 0) {
//            showCanUserDialog("检测到超声波被占用暂不可用");
//        }
//    }

}
