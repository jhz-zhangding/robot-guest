package com.efrobot.guests.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.efrobot.guests.R;
import com.efrobot.guests.base.MyBaseActivity;
import com.efrobot.guests.fragment.direction.EnterGuestFragment;
import com.efrobot.guests.fragment.direction.EnterSettingFragment;
import com.efrobot.guests.fragment.direction.OutGuestFragment;
import com.efrobot.guests.fragment.direction.OutSettingFragment;

public class ControlActivity extends MyBaseActivity {

    private ControlFragment controlFragment;

    private SceneSelectionFragment sceneSelectionFragment;

    private AutoGuestFragment autoGuestFragment;

    private WelcomeGuestFragment welcomeGuestFragment;

    private EnterGuestFragment enterGuestFragment;
    private EnterSettingFragment enterSettingFragment;
    private OutGuestFragment outGuestFragment;
    private OutSettingFragment outSettingFragment;

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        setControlFragment();
    }

    private void setControlFragment() {
        if (controlFragment == null) {
            controlFragment = new ControlFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, controlFragment).commit();
    }

    public void setSceneSelectionFragment() {
        if (sceneSelectionFragment == null) {
            sceneSelectionFragment = new SceneSelectionFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, sceneSelectionFragment).commit();
    }

    public void setAutoGuestFragment() {
        if (autoGuestFragment == null) {
            autoGuestFragment = new AutoGuestFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, autoGuestFragment).commit();
    }

    public void setWelcomeGuestFragment() {
        if (welcomeGuestFragment == null) {
            welcomeGuestFragment = new WelcomeGuestFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, welcomeGuestFragment).commit();
    }

    public void setEnterGuestFragment() {
        if (enterGuestFragment == null) {
            enterGuestFragment = new EnterGuestFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, enterGuestFragment).commit();
    }

    public void setEnterSettingFragment() {
        if (enterSettingFragment == null) {
            enterSettingFragment = new EnterSettingFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, enterSettingFragment).commit();
    }

    public void setOutGuestFragment() {
        if (outGuestFragment == null) {
            outGuestFragment = new OutGuestFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, outGuestFragment).commit();
    }

    public void setOutSettingFragment() {
        if (outSettingFragment == null) {
            outSettingFragment = new OutSettingFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, outSettingFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else
            super.onBackPressed();

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

}
