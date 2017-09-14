package com.efrobot.guests.utils;

import android.content.Context;
import android.content.Intent;

import com.efrobot.library.RobotManager;
import com.efrobot.library.mvp.utils.L;

/**
 * Created by hp on 2017/4/6.
 */
public class WheelActionUtils {

    /**
     * 控制机器人双轮运动的开关
     */
    public final static String ROBOT_SWITCH_OF_WHEELS_MOVE_ACTION = "com.efrobot.robot.action.SWITCH_OF_WHEELS_MOVE";

    private static String TAG = WheelActionUtils.class.getSimpleName();

    private static WheelActionUtils instance;

    private static Context mContext;

    public static boolean defaultWheelsState = true;

    public static WheelActionUtils getInstance(Context context) {
        if(instance == null){
            instance = new WheelActionUtils();
        }
        mContext = context;
        return instance;
    }

    public void closeWheelAction() {
        boolean setWheelsState = RobotManager.getInstance(mContext).setSettingWheelsState(false);
        setMoveAction();
        L.d(TAG, " 设置停止运动setWheelsState : " + setWheelsState);
    }

    public void openWheelAction() {
        boolean settingWheelsState = RobotManager.getInstance(mContext).setSettingWheelsState(true);
        setMoveAction();
        L.d(TAG, " 设置可以运动setWheelsState : " + settingWheelsState);
    }

    public void remmberRobotWheel() {
        defaultWheelsState = RobotManager.getInstance(mContext).getSettingWheelsState();
    }

    //回复之前轮子的状态
    public void setLastStatusWheel() {
        L.d(TAG, " 退出设置setWheelsState : " + defaultWheelsState);
        RobotManager.getInstance(mContext).setSettingWheelsState(defaultWheelsState);
        setMoveAction();
    }


    //    Zuzo佐左 2017/4/6 13:24:18
    private void setMoveAction() {
        Intent intent = new Intent();
        intent.setAction(ROBOT_SWITCH_OF_WHEELS_MOVE_ACTION);
        mContext.sendBroadcast(intent);
    }

}
