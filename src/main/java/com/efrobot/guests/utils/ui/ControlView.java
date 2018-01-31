package com.efrobot.guests.utils.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.efrobot.guests.R;
import com.efrobot.guests.utils.CustomHintDialog;
import com.efrobot.guests.utils.TtsUtils;
import com.efrobot.library.RobotManager;
import com.efrobot.library.RobotState;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.RobotToastUtil;

import java.lang.ref.WeakReference;

/**
 * 机器人运动控制View
 * Created by Administrator on 2015/11/10.
 */
public class ControlView extends RelativeLayout {

    private static final String TAG = "ControlView";
    public static final int TOUCH_CENTER = 0;
    public static final int TOUCH_UP = 1;
    public static final int TOUCH_DOWN = 2;
    public static final int TOUCH_LEFT = 3;
    public static final int TOUCH_RIGHT = 4;
    private static final int COUNT_DOWN = 5;
    private static final int REMOVE_COUNT = 6;
    private ImageView btnCenter, btnUp, btnDown, btnLeft, btnRight;
    private int centreX, centreY;//屏幕中心点坐标
    private boolean initView = false;//控件是否初始化
    public boolean isSpeaking = false;//是否在开启语音
    private int absy;
    private int absx;
    //按下时的x,y的值
    private int downy;
    private int downx;
    //抬起时的x,y的值
    private int upx;
    private int upy;
    //用于计算点击处的角度
    private int tanx;
    private int tany;
    //按下时x,y相对于中心点的坐标
    private int coordinatex;
    private int coordinatey;
    private boolean btnEnable = true;


    private Context context;

    /**
     * 响应Touch事件
     * btnCenter.setPressed();
     *
     * @param touchPosition
     */
    private boolean isBtnUpTouchDown = false;//BtnUp按钮是否被按下
    private boolean isBtnDownTouchDown = false;//BtnDown按钮是否被按下
    private boolean isBtnLeftTouchDown = false;//BtnLeft按钮是否被按下
    private boolean isBtnRightTouchDown = false;//BtnCenter按钮是否被按下

    private OnControlListener mControlListener;


    public static final int CONTROL_MODE_FINE_TUNING = 0;//微调模式
    public static final int CONTROL_MODE_MOVE = 1;//移动模式
    public static final int CONTROL_MODE_HIGH_SPEED = 2;//高速模式

    private int controlMode = CONTROL_MODE_FINE_TUNING;//运动模式切换
    private boolean isFineTuningMode = false;//是否是移动模式 默认否
    private CustomHintDialog highSpeedDialog;//高速模式提醒dialog
    private boolean touchDown = false;

    public int getControlMode() {
        return controlMode;
    }

    public ControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initHandler();
    }

    public void setOnControlListener(OnControlListener mControlListener) {
        this.mControlListener = mControlListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public void setControlView(ImageView btnCenter, ImageView btnUp, ImageView btnDown, ImageView btnLeft, ImageView btnRight) {
        this.btnCenter = btnCenter;
        this.btnUp = btnUp;
        this.btnDown = btnDown;
        this.btnLeft = btnLeft;
        this.btnRight = btnRight;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!initView) {
            int width = getWidth();
            centreX = width / 2;
            int height = getHeight();
            centreY = height / 2;
            switch (controlMode) {
                case CONTROL_MODE_FINE_TUNING:
                    btnCenter.setImageResource(R.mipmap.weitiao_unclick);
                    isFineTuningMode = false;
                    break;
                case CONTROL_MODE_MOVE:
                    btnCenter.setImageResource(R.mipmap.move_click);
                    isFineTuningMode = true;
                    break;
                case CONTROL_MODE_HIGH_SPEED:
                    btnCenter.setImageResource(R.mipmap.high_speed);
                    isFineTuningMode = false;
                    break;
            }


            initView = true;
        }
    }


    //是否在充电桩或在充电
    public boolean hasOutPile() {
        int robotLocation = RobotManager.getInstance(getContext()).getRobotLocation();
        int batteryState = RobotManager.getInstance(getContext()).getBatteryState();
        L.i(TAG, "是否在充电桩或在充电 robotLocation=" + robotLocation + ",batteryState=" + batteryState);
        if (robotLocation == RobotState.ROBOT_LOCATION_CHARGING_PILE || (batteryState != RobotState.BATTERY_STATE_UN_CHARGING & batteryState != RobotState.UNKNOWN)) {
            return true;
        }
        return false;
    }

    //是否正在回充电桩
    public boolean inPileing() {
        int navigationState = RobotManager.getInstance(getContext()).getNavigationState();
        L.i(TAG, "是否正在回充电桩----robotLocation4=" + navigationState);
        if (navigationState == RobotState.NAVIGATION_STATE_CHARGING || navigationState == RobotState.NAVIGATION_STATE_CONNECT_CHARGING_PILE) {
            return true;
        }
        return false;
    }

    //    判断是否在充电
    public boolean isCharging() {
        int batteryState = RobotManager.getInstance(getContext()).getBatteryState();
        if (batteryState != RobotState.BATTERY_STATE_UN_CHARGING) {
            return true;
        }
        return false;
    }

    private int downTouchPosition = -2;
    private boolean isTouch = false;

    public boolean isTouch() {
        return isTouch;
    }

    public void setBtnEnable(boolean enable) {
        this.btnEnable = enable;
        if (enable && onControlTouchListener != null)
            onControlTouchListener.OnControlTouch(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && touchDown) {
            touchDown = !touchDown;
            updateViewState();
        }
        if (!btnEnable) {
            L.d(TAG, "按钮不可用");
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (onControlTouchListener != null)
                    onControlTouchListener.OnControlTouch(true);
                downTouchPosition = getTouchPosition(event);
                L.d(TAG, "onTouchEvent: downTouchPosition=" + downTouchPosition);
                if (hasOutPile()) {//在充电桩上或者在充电
                    if (inPileing()) {
                        RobotToastUtil.getInstance(getContext()).showToast("请等待回充电桩完成!");
                        return true;
                    }
                    if (downTouchPosition == -1) {
                        updateViewState();
                        if (mControlListener != null) mControlListener.onControlStop();
                    } else if (downTouchPosition == TOUCH_DOWN) {
                        btnDown.setPressed(true);
                        setBtnEnable(false);
                        touchDown = true;
                        TtsUtils.sendTts(getContext(), getResources().getString(R.string.go_out_pile));
                        getHandler().sendEmptyMessageDelayed(COUNT_DOWN, 7 * 1000);
                        L.d(TAG, "小胖要出充电桩了  ");
                    } else if (downTouchPosition == TOUCH_CENTER) {
                        L.d(TAG, "点击中间按钮");
                    } else {
                        L.d(TAG, "小胖还想再吃点饭  ");
                        TtsUtils.sendTts(getContext(), getResources().getString(R.string.is_charging));
                    }
                } else {
                    if (downTouchPosition == -1) {
                        updateViewState();
                        if (mControlListener != null) mControlListener.onControlStop();
                    } else {
                        dispatchDownEvent(downTouchPosition);
                    }
                }
                isTouch = true;
                break;

            case MotionEvent.ACTION_MOVE:
                int touchPosition = getTouchPosition(event);
                if (downTouchPosition != touchPosition) {
                    dispatchUnPressedEvent();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                if (onControlTouchListener != null)
                    onControlTouchListener.OnControlTouch(false);
                if (hasOutPile()) {
                    btnDown.setPressed(false);
                } else {
                    isTouch = false;
                }
                dispatchUnPressedEvent();
                break;
        }
        return true;
    }

//    private ProjectionFragmentPresenter projectionFragmentPresenter;
//
//    public void setProjectionFragmentPresenter(ProjectionFragmentPresenter projectionFragmentPresenter) {
//        this.projectionFragmentPresenter = projectionFragmentPresenter;
//    }

    private void dispatchDownEvent(int downTouchPosition) {
        //请放开我的头提示
        L.d(TAG, "controlMode:" + controlMode);
//        if (projectionFragmentPresenter != null && projectionFragmentPresenter.isRacketHead()) {
//            L.i(TAG, "请放开我的头,头部被按下");
//        } else if (projectionFragmentPresenter != null && projectionFragmentPresenter.isWiredCharge()) {
//            ShowToast.getInstance().show(R.string.please_pull_up_wired_charge);
//        }
//        else
            switch (downTouchPosition) {
                case TOUCH_CENTER:
                    if (controlMode == CONTROL_MODE_HIGH_SPEED)
                        controlMode = -1;
                    controlMode++;
                    switch (controlMode) {
                        case CONTROL_MODE_FINE_TUNING:
                            btnCenter.setImageResource(R.mipmap.weitiao_unclick);
                            isFineTuningMode = false;
                            break;
                        case CONTROL_MODE_MOVE:
                            btnCenter.setImageResource(R.mipmap.move_click);
                            isFineTuningMode = true;
                            break;
                        case CONTROL_MODE_HIGH_SPEED:
                            btnCenter.setImageResource(R.mipmap.high_speed);
                            isFineTuningMode = false;
                            if (highSpeedDialog == null) {
                                highSpeedDialog = new CustomHintDialog(context, -1);
//                                highSpeedDialog.setScale(0.85, 0.40);
                                highSpeedDialog.setMessage("温馨提示：机器人即将进入“高速”模式，在此模式下机器人的移动速度将提高到40cm/s。为了避免机器人误碰人和物品情况，请小心使用此功能。");
                                highSpeedDialog.setCancleButton("我知道了", new CustomHintDialog.IButtonOnClickLister() {
                                    @Override
                                    public void onClickLister() {
                                        highSpeedDialog.dismiss();
                                    }
                                });
                            }
                            highSpeedDialog.show();
                            break;
                    }
                    L.i(TAG, "dispatchDownEvent TOUCH_CENTER");
                    break;
                case TOUCH_UP:

                    if (!isBtnUpTouchDown) {
                        btnUp.setPressed(true);
                        btnDown.setPressed(false);
                        btnLeft.setPressed(false);
                        btnRight.setPressed(false);
                        isBtnDownTouchDown = false;
                        isBtnLeftTouchDown = false;
                        isBtnRightTouchDown = false;
                        if (mControlListener != null && !isCharging())
                            mControlListener.onControlUp();
                    } else {
                        btnUp.setPressed(false);
                        if (mControlListener != null) mControlListener.onControlStop();
                    }
                    if (isFineTuningMode)
                        isBtnUpTouchDown = !isBtnUpTouchDown;

                    break;
                case TOUCH_DOWN:
                    if (!isBtnDownTouchDown) {
                        btnDown.setPressed(true);
                        btnUp.setPressed(false);
                        btnLeft.setPressed(false);
                        btnRight.setPressed(false);
                        isBtnUpTouchDown = false;
                        isBtnLeftTouchDown = false;
                        isBtnRightTouchDown = false;

                        if (mControlListener != null && !isCharging())
                            mControlListener.onControlDown();
                    } else {
                        btnDown.setPressed(false);
                        if (mControlListener != null) mControlListener.onControlStop();
                    }
                    if (isFineTuningMode)
                        isBtnDownTouchDown = !isBtnDownTouchDown;
                    break;
                case TOUCH_LEFT:
                    if (!isBtnLeftTouchDown) {
                        btnRight.setPressed(true);
                        btnUp.setPressed(false);
                        btnLeft.setPressed(false);
                        btnDown.setPressed(false);
                        isBtnDownTouchDown = false;
                        isBtnRightTouchDown = false;
                        isBtnUpTouchDown = false;
                        if (mControlListener != null && !isCharging())
                            mControlListener.onControlLeft();
                    } else {
                        btnRight.setPressed(false);
                        if (mControlListener != null) mControlListener.onControlStop();
                    }
                    if (isFineTuningMode)
                        isBtnLeftTouchDown = !isBtnLeftTouchDown;
                    break;
                case TOUCH_RIGHT:

                    if (!isBtnRightTouchDown) {
                        btnLeft.setPressed(true);
                        btnUp.setPressed(false);
                        btnDown.setPressed(false);
                        btnRight.setPressed(false);
                        isBtnDownTouchDown = false;
                        isBtnLeftTouchDown = false;
                        isBtnUpTouchDown = false;

                        if (mControlListener != null && !isCharging())
                            mControlListener.onControlRight();
                    } else {
                        btnLeft.setPressed(false);
                        if (mControlListener != null) mControlListener.onControlStop();
                    }
                    if (isFineTuningMode)
                        isBtnRightTouchDown = !isBtnRightTouchDown;
                    break;
            }

    }

    //停止响应各个操作
    private void dispatchUnPressedEvent() {
        if (!isFineTuningMode) {
            btnUp.setPressed(false);
            btnDown.setPressed(false);
            isBtnDownTouchDown = false;
            isBtnUpTouchDown = false;
            btnLeft.setPressed(false);
            btnRight.setPressed(false);
            isBtnRightTouchDown = false;
            isBtnLeftTouchDown = false;
            if (mControlListener != null)
                mControlListener.onControlStop();
        }


    }


    /***
     * 计算触摸点是否在BtnCenter内
     *
     * @return true 说明点击区域在BtnCenter按钮内
     */

    private boolean isBtnCenterInside(float x, float y, double centerImageBtnRadius) {
        double diagonalSquare = Math.pow(x - centreX, 2) + Math.pow(centreY - y, 2);//对角线的平方根
        double diagonal = Math.sqrt(diagonalSquare);
        if (centerImageBtnRadius >= diagonal) {
            return true;
        }
        return false;
    }

    /***
     * 计算触摸点是否在那个一个区域内
     * 三角函数
     *
     * @return TOUCH_UP 说明点击区域在BtnTop按钮内
     * <p/>
     * TOUCH_LEFT 说明点击区域在BtnDown按钮内
     * <p/>
     * TOUCH_LEFT 说明点击区域在BtnLeft按钮内
     * <p/>
     * TOUCH_RIGHT 说明点击区域在BtnRight按钮内
     */
    private int getRadianView(float x, float y) {
        //按下时候的x，y
        downx = (int) x;
        downy = (int) y;
        //计算按下的x,y与坐标中点的距离
        absx = Math.abs(downx - centreX);
        absy = Math.abs(downy - centreY);
        //用于计算正切值
        tanx = Math.abs(downx - centreX);
        tany = Math.abs(downy - centreY);
        //按下时x,y相对于中心点的坐标
        coordinatex = downx - centreX;
        coordinatey = centreY - downy;
        //抬起来时候的x,y
        upx = (int) x;
        upy = (int) y;
        //
        boolean t = ((upx - downx) * (upx - downx) + (upy - downy) * (upy - downy)) < 25;
        int a = absx * absx + absy * absy;
        if (a < (bigCircleRadius * bigCircleRadius) && t) {
            double angle = getAngle(tanx, tany, coordinatex, coordinatey);
            //L.i(TAG, "角度=" + angle);
            if (angle > 315 && angle <= 360 || angle >= 0 && angle < 45) {
                return TOUCH_RIGHT;
            } else if (angle > 45 && angle < 135) {
                return TOUCH_DOWN;
            } else if (angle > 135 && angle < 225) {
                return TOUCH_LEFT;
            } else if (angle > 225 && angle < 315) {
                return TOUCH_UP;
            }

        }
        return -1;
    }

    /**
     * 弧度计算
     *
     * @param x     用于计算正切值的x
     * @param y     用于计算正切值的y
     * @param downx downy 用于判断按下时候  在第几块
     * @return 得到按下时候的角度
     */
    protected double getAngle(int x, int y, int downx, int downy) {
        double angle = (float) 0.0;
        if (downx > 0 && downy < 0) {
            double c = (double) y / (double) x;
            double d = Math.toDegrees(Math.atan(c));
            angle = d;
        } else if (downx <= 0 && downy <= 0) {
            double c = (double) x / (double) y;
            double d = Math.toDegrees(Math.atan(c));
            angle = d + 90;
        } else if (downx < 0 && downy >= 0) {
            double c = (double) y / (double) x;
            double d = Math.toDegrees(Math.atan(c));
            angle = 180 + d;
        } else {
            double c = (double) x / (double) y;
            double d = Math.toDegrees(Math.atan(c));
            angle = d + 270;
        }
        return angle;
    }


    /***
     * @param x 触摸点x
     * @param y 触摸点y
     * @return
     */
    private double centerImageBtnRadius = -1;//圆形按钮的半径
    private double bigCircleRadius = -1;//大圆的半径
    private boolean isBtnCentre = false;//中间按钮

    private int getTouchPosition(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (centerImageBtnRadius == -1)
            centerImageBtnRadius = btnCenter.getWidth() / 2;
        if (bigCircleRadius == -1)
            bigCircleRadius = Math.abs(btnDown.getBottom() - btnUp.getTop()) / 2;
        isBtnCentre = isBtnCenterInside(x, y, centerImageBtnRadius);

        if (isBtnCentre) {
            return TOUCH_CENTER;
        }

        return getRadianView(x, y);
    }

    //各个放心控制按钮回调
    public interface OnControlListener {
        void onControlUp();

        void onControlDown();

        void onControlLeft();

        void onControlRight();

        void onControlStop();
    }

    private OnControlTouchListener onControlTouchListener;

    public void setOnControlTouchListener(OnControlTouchListener onControlTouchListener) {
        this.onControlTouchListener = onControlTouchListener;
    }

    public interface OnControlTouchListener {
        void OnControlTouch(boolean isTouch);

    }

    /**
     * 更新按钮状态
     */
    public void updateViewState() {
        if (btnDown != null) {
            btnDown.setPressed(false);
            isBtnDownTouchDown = false;
        }
        if (btnUp != null) {
            btnUp.setPressed(false);
            isBtnUpTouchDown = false;
        }
        if (btnLeft != null) {
            btnLeft.setPressed(false);
            isBtnLeftTouchDown = false;
        }
        if (btnRight != null) {
            btnRight.setPressed(false);
            isBtnRightTouchDown = false;

        }

    }


    private BaseHandler mBaseHandler;

    /**
     * 初始化一个Handler，如果需要使用Handler，先调用此方法，
     * 然后可以使用postRunnable(Runnable runnable)，
     * sendMessage在handleMessage（Message msg）中接收msg
     */
    public void initHandler() {
        mBaseHandler = new BaseHandler(this);
    }

    /**
     * 返回Handler，在此之前确定已经调用initHandler（）
     *
     * @return Handler
     */
    public Handler getHandler() {
        return mBaseHandler;
    }

    protected class BaseHandler extends Handler {
        private final WeakReference<ControlView> mObjects;

        public BaseHandler(ControlView mPresenter) {
            mObjects = new WeakReference<ControlView>(mPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            ControlView mPresenter = mObjects.get();
            if (mPresenter != null)
                mPresenter.handleMessage(msg);
        }
    }


    private void handleMessage(Message msg) {
        switch (msg.what) {
            case COUNT_DOWN:
                RobotManager.getInstance(getContext()).getNavigationInstance().outChargingPile();
                isSpeaking = false;
                L.d(TAG, "COUNT_DOWN");
                break;
        }
    }

    public void removeMessages() {
        if (getHandler().hasMessages(COUNT_DOWN)) {
            TtsUtils.sendTts(getContext(), getResources().getString(R.string.task_cancel));
            getHandler().removeMessages(COUNT_DOWN);
        }
    }

}
