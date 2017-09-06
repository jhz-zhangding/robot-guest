package com.efrobot.guest.setting;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.efrobot.guest.Env.EnvUtil;
import com.efrobot.guest.GuestsApplication;
import com.efrobot.guest.R;
import com.efrobot.guest.base.GuestsBasePresenter;
import com.efrobot.guest.bean.AddCustomMode;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.bean.UlPlaceBean;
import com.efrobot.guest.dao.DataManager;
import com.efrobot.guest.dao.UltrasonicDao;
import com.efrobot.guest.utils.CustomHintDialog;
import com.efrobot.guest.utils.PreferencesUtils;
import com.efrobot.guest.utils.TtsUtils;
import com.efrobot.library.RobotManager;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.task.UltrasonicTaskManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/3/2.
 */
public class SettingPresenter extends GuestsBasePresenter<ISettingView> implements RobotManager.OnGetUltrasonicCallBack, RobotManager.OnUltrasonicOccupyStatelistener {

    private boolean isReceiveUltrasonic = false;

    public SettingPresenter(ISettingView mView) {
        super(mView);
    }

    private UltrasonicDao ultrasonicDao;

    private UlPlaceBean ulPlaceBeen;

    private ArrayList<UlPlaceBean> ulPlaceBeans = null;

    //数据设置
    public static String SP_VOICE_TIME = "voiceTime";
    public static String SP_IS_AUTO_OPEN = "isAutoOpen";
    public static String SP_MODE = "mMode";

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
                    long valueNg2Cm = valueNG / 10;
                    switch (numberNg) {
                        case 0:
                            mView.setDistance0(valueNg2Cm + "");
                            break;
                        case 1:
                            mView.setDistance1(valueNg2Cm + "");
                            break;
                        case 7:
                            mView.setDistance7(valueNg2Cm + "");
                            break;
                        case 8:
                            mView.setDistance8(valueNg2Cm + "");
                            break;
                        case 9:
                            mView.setDistance9(valueNg2Cm + "");
                            break;
                        case 10:
                            mView.setDistance10(valueNg2Cm + "");
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
//        if (isCanUseGuest()) {
        initUltrasonicData();
//        }
    }

    public void initUltrasonicData() {
        isReceiveUltrasonic = false;
        ulHandle.sendEmptyMessageDelayed(0, 5000);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ultrasonicDao = GuestsApplication.from(getContext()).getUltrasonicDao();
        RobotManager.getInstance(getContext()).registerUltrasonicOccupylistener(this);


//        if (!isCanUseGuest()) {
//            showCanUserDialog(getContext().getString(R.string.error_use__hint));
//        } else {

        //超声波测试数据
//            initUltrasonicData();
        initSettingData();
//        }
    }

    private void initSettingData() {
        ulPlaceBeans = ultrasonicDao.queryAll();
        if (ulPlaceBeans != null && ulPlaceBeans.size() > 0) {
            for (int i = 0; i < ulPlaceBeans.size(); i++) {
                if (ulPlaceBeans.get(i).getIsOpenValue() == 1) {
                    mView.setIsOpenValue(ulPlaceBeans.get(i).getUltrasonicId(), 1);
                    mView.setOpenDistanceValue(ulPlaceBeans.get(i).getUltrasonicId(), ulPlaceBeans.get(i).getDistanceValue());
                } else {
                    //置空
                    mView.setIsOpenValue(ulPlaceBeans.get(i).getUltrasonicId(), 0);
                    mView.setOpenDistanceValue(ulPlaceBeans.get(i).getUltrasonicId(), "");
                }
            }
        }

        // 是否自动校正后超声波
        boolean isAutoOpen = PreferencesUtils.getBoolean(getContext().getApplicationContext(), SP_IS_AUTO_OPEN);
        mView.setAutoOpen(isAutoOpen);

        String exchangeTime = PreferencesUtils.getString(getContext().getApplicationContext(), SettingPresenter.SP_VOICE_TIME);
        if (!TextUtils.isEmpty(exchangeTime)) {
            mView.setVoiceTime(exchangeTime);
        }

    }

    private GreetingAdapter greetingAdapter;

    public GreetingAdapter getGreetingAdapter(boolean isOnlyShowFirst) {
        if (greetingAdapter == null) {
            greetingAdapter = new GreetingAdapter(getContext());
        }
        List<ItemsContentBean> list = DataManager.getInstance(getContext()).queryItem(1);
        if (isOnlyShowFirst) {
            if (list != null && list.size() > 1) {
                list = list.subList(0, 1);
            }
        }
        greetingAdapter.setSourceData(list);
        return greetingAdapter;
    }

    private GreetingAdapter endGreetingAdapter;

    public GreetingAdapter getEndGreetingAdapter(boolean isOnlyShowFirst) {
        if (endGreetingAdapter == null) {
            endGreetingAdapter = new GreetingAdapter(getContext());
        }
        List<ItemsContentBean> list = DataManager.getInstance(getContext()).queryItem(2);
        if (isOnlyShowFirst) {
            if (list != null && list.size() > 1) {
                list = list.subList(0, 1);
            }
        }
        endGreetingAdapter.setSourceData(list);
        return endGreetingAdapter;
    }

    private boolean isCanUseGuest() {
        boolean isCanUse = true;
        Uri uri = Uri.parse("content://com.efrobot.diy.diydataProvider/question");
        Cursor cursor = getContext().getContentResolver().query(uri, null, "type=?", new String[]{"3"}, null);
        if (cursor != null && cursor.moveToNext()) {
            isCanUse = false;
        }
        Cursor cursor1 = getContext().getContentResolver().query(uri, null, "type=?", new String[]{"4"}, null);
        if (cursor1 != null && cursor1.moveToNext()) {
            isCanUse = false;
        }
        L.i(TAG, "isCanUse = " + isCanUse);
        return isCanUse;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.i(TAG, "onDestroy");
        unRegisterAllCallBack();
        RobotManager.getInstance(getContext()).unRegisterOnGetInfraredCallBack();
    }

    public void unRegisterAllCallBack() {
        RobotManager.getInstance(getContext()).unRegisterOnGetUltrasonicCallBack();
        //TODO 新策略
        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getContext())).closeUltrasonicFeedback(EnvUtil.ULGST001);
    }

    public void cancle() {
        exit();
    }

    public void affrim() {

        //自动检测
        boolean isAutoOpenInit = mView.getIsAutoOpen().isChecked();
        PreferencesUtils.putBoolean(getContext().getApplicationContext(), SP_IS_AUTO_OPEN, isAutoOpenInit);

        //交流模式
        int mode = mView.getExchangeMode();
        PreferencesUtils.putInt(getContext().getApplicationContext(), SP_MODE, mode);

        ArrayList<AddCustomMode> modes = GuestsApplication.from(getContext()).getModeDao().queryAll();

        if (mode == SettingActivity.selectedCustomMode) {
            if (modes != null && modes.size() > 0) {
                if (modes.get(0).getMusic().isEmpty() && modes.get(0).getImage().isEmpty() &&
                        modes.get(0).getMedia().isEmpty()) {
                    showToast("自定义模式设置为空");
                    return;
                }
            } else {
                showToast("自定义模式设置为空");
                return;
            }
        } else {

            String voiceTime = mView.getVoiceTime();
            PreferencesUtils.putString(getContext().getApplicationContext(), SP_VOICE_TIME, voiceTime);

            boolean isDistanceEmpty = true;
            //设置超声波
            for (int i = 0; i < SettingActivity.MyUlNum; i++) {
                int isCheck = mView.getIsOpenValue(i);
                if (!mView.getOpenDistanceValue(i).isEmpty()) {
                    isDistanceEmpty = false;
                }
                if (isCheck == 1 && mView.getOpenDistanceValue(i).isEmpty()) {
                    showToast("选中的超声波距离不能为空哦");
                    return;
                }
                String ulDistance = mView.getOpenDistanceValue(i);
                boolean isExit = ultrasonicDao.isExits(i);
                if (isExit) {
                    ultrasonicDao.update(isCheck, i, ulDistance);
                } else {
                    ulPlaceBeen = new UlPlaceBean();
                    ulPlaceBeen.setUltrasonicId(i);
                    ulPlaceBeen.setIsOpenValue(isCheck);
                    ulPlaceBeen.setDistanceValue(ulDistance);
                    ultrasonicDao.insert(ulPlaceBeen);
                }

            }
            if (isDistanceEmpty) {
                showToast("超声波距离不能为空哦");
                return;
            }

            showToast("保存成功");
        }
    }

    private void reSend() {
        ulHandle.sendEmptyMessageDelayed(0, 5000);
    }

    private void sendOpenUltrasonicData() {
        L.i("sendOpenUltrasonicData", "start send");
//        byte[] data = new byte[12];
//        data[0] = (byte) 0x0c;
//        data[1] = (byte) 0x03;
//        data[2] = (byte) 0x06;
//        data[3] = (byte) 0x03;
//        data[4] = (byte) 0x1F;
//        data[5] = (byte) 0xFF;

//        data[4] = (byte) 0x0B;
//        data[5] = (byte) 0x83;
//
//        byte mByte1 = (byte) 0x01; // 探头1---0
//        byte mByte2 = (byte) 0x02; // 探头2---1
//        byte mByte8 = (byte) 0x80;
//        data[5] = ((byte) (mByte1 | mByte2 | mByte8));
//
//        byte mByte9 = (byte) 0x01; // 探头9---3
//        byte mByte10 = (byte) 0x02; // 探头10---4
//        byte mByte11 = (byte) 0x04;
//        data[4] = ((byte) (mByte9 | mByte10 | mByte11));
//
//        data[4] = (byte) 0x00;
//        data[5] = (byte) 0x01;

//        data[6] = (byte) 0x00;
//        data[7] = (byte) 7;
        //开启后8秒左右收到回调
//        RobotManager.getInstance(getContext()).getCustomTaskInstance().sendByteData(data);
        RobotManager.getInstance(getContext()).registerOnGetUltrasonicCallBack(this);
        //TODO 新策略
        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getContext())).openUltrasonicFeedback(EnvUtil.ULGST001, 1923);
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
                byte[] bytes = new byte[24];
                System.arraycopy(data, 5, bytes, 0, 24);
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
        TtsUtils.sendTts(getContext(), content);
        dialog = new CustomHintDialog(getContext(), -1);
        dialog.setMessage(content);
        dialog.setCancelable(true);
        sendTestUltrasonic(false);
        dialog.show();
        //5秒后关闭对话框
        ulHandle.sendEmptyMessageDelayed(2, 5000);
    }

    public void showCanUserDialog(String content) {
        if(hitDialog == null) {
            hitDialog = new CustomHintDialog(getContext(), -1);
            hitDialog.setTitle("提示");
            hitDialog.setMessage(content);
            hitDialog.setCancelable(false);
            hitDialog.setSubmitButton("确定", new CustomHintDialog.IButtonOnClickLister() {
                @Override
                public void onClickLister() {
                    exit();
                }
            });
        }
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
     * AVAILABLE = 1; //超声波模块可用
     * AVAILABLE = 0; //超声波模块不可用
     */
    @Override
    public void onUltrasonicOccupyState(String sceneCode, int isAvailable) {
        L.e("onUltrasonicOccupyState", "sceneCode" + sceneCode + "- - isAvailable = " + isAvailable);
        if (isAvailable == 0) {
            ulHandle.removeMessages(0);
            showCanUserDialog(getContext().getString(R.string.error_use__hint));
        }
    }
}
