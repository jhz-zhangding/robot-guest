package com.efrobot.guests.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.efrobot.library.mvp.view.PresenterActivity;

/**
 * Created by Administrator on 2017/1/5.
 */
public abstract class MyBaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();

    }

    public void setFullScreen() {
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
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            top = 300;
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
