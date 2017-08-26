package com.efrobot.guest.setting;

import android.widget.CheckBox;

import com.efrobot.library.mvp.view.UiView;

/**
 * Created by Administrator on 2017/3/2.
 */
public interface ISettingView extends UiView {
    int getExchangeMode();

    void setVoiceTime(String time);
    String getVoiceTime();

    void setDistance1(String distance);
    void setDistance2(String distance);
    void setDistance3(String distance);
    void setDistance7(String distance);
    void setDistance8(String distance);
    void setDistance9(String distance);
    void setDistance10(String distance);
    void setDistance11(String distance);
    void setDistance12(String distance);
    void setDistance13(String distance);

    void setStartTime(String startTime);
    void setEndTime(String endTime);
    void setTimerPlace(String guestPlace);

}
