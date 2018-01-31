package com.efrobot.guests.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.efrobot.guests.utils.ui.ControlView;
import com.efrobot.library.RobotManager;
import com.efrobot.library.RobotState;
import com.efrobot.library.mvp.utils.L;

/**
 * Created by zd on 2018/1/23.
 */
public class RobotMoveUtils {

    private final String TAG = RobotMoveUtils.class.getSimpleName();

    private RobotManager robotManager;

    private Context mContext;

    private static final int MOVEFRONT = 0;
    private static final int MOVEBACK = 1;
    private static final int MOVELEFT = 2;
    private static final int MOVERIGHT = 3;
    private static final int MOVESTOP = 4;
    private static final int WHAT_TIME_OUT = 100;

    private static final int WHAT_RESEND = 0;

    private static final int TIME_RESEND = 500;

    public RobotMoveUtils(Context context) {
        this.mContext = context;
        robotManager = RobotManager.getInstance(mContext);
    }

    public void onControlUp(int currentMode) {
        sendWheelOrder(MOVEBACK, currentMode);
        clearWheelElectricityState();
    }

    public void onControlDown(int currentMode) {
        sendWheelOrder(MOVEFRONT, currentMode);
        clearWheelElectricityState();
    }

    public void onControlLeft(int currentMode) {
        sendWheelOrder(MOVERIGHT, currentMode);
        clearWheelElectricityState();
    }

    public void onControlRight(int currentMode) {
        sendWheelOrder(MOVELEFT, currentMode);
        clearWheelElectricityState();
    }

    public void onControlStop(int currentMode) {
        sendWheelOrder(MOVESTOP, currentMode);
    }

    private int lastDire = -1;
    private int lastMode = -1;

    //发送身体运动命令
    private void sendWheelOrder(int dire, int controlMode) {
        /**
         * 判断如果上一个动作为暂停，那么下一个暂停动作将不再触发
         */
        if (lastDire == dire && lastDire == MOVESTOP) {
            return;
        }
        L.d(TAG, "sendWheelOrder code=" + dire);
        switch (dire) {
            case MOVEFRONT:
                if (controlMode == ControlView.CONTROL_MODE_HIGH_SPEED) {
                    robotManager.getWheelInstance().moveFront(400);
                } else {
                    robotManager.getWheelInstance().moveFront();
                }
                handler.sendEmptyMessageDelayed(WHAT_RESEND, TIME_RESEND);
                L.d(TAG, "MOVEFRONT");
                break;
            case MOVEBACK:
                if (controlMode == ControlView.CONTROL_MODE_HIGH_SPEED) {
                    robotManager.getWheelInstance().moveBack(400);
                } else {
                    robotManager.getWheelInstance().moveBack();
                }
                handler.sendEmptyMessageDelayed(WHAT_RESEND, TIME_RESEND);
                L.d(TAG, "MOVEBACK");
                break;
            case MOVELEFT:
                robotManager.getWheelInstance().moveLeft();
                handler.sendEmptyMessageDelayed(WHAT_RESEND, TIME_RESEND);
                break;
            case MOVERIGHT:
//                if (mView.getControlView().getControlMode() == ControlView.CONTROL_MODE_HIGH_SPEED) {
//                    //robotManager.getWheelInstance().moveRight(400);
//                } else {
//                    robotManager.getWheelInstance().moveRight();
//                }
                robotManager.getWheelInstance().moveRight();
                handler.sendEmptyMessageDelayed(WHAT_RESEND, TIME_RESEND);
                break;
            case MOVESTOP:
                robotManager.getWheelInstance().stop();
                if (handler.hasMessages(WHAT_RESEND))
                    handler.removeMessages(WHAT_RESEND);
                break;
        }
        lastDire = dire;
        lastMode = controlMode;
    }


    public void clearWheelElectricityState() {
        if (robotManager.getRobotStateWheelElectricity() == RobotState.ROBOT_WHEEL_ELECTRICITY_OVERCURRENT) {
            Log.i(TAG, "clearWheelElectricityState: getRobotStateWheelElectricity=" + robotManager.getRobotStateWheelElectricity());
            robotManager.clearWheelElectricityState();
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_RESEND:
                    sendWheelOrder(lastDire, lastMode);
                    break;
            }
        }
    };

}
