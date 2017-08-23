package com.efrobot.guest.setting;

import android.widget.CheckBox;

import com.efrobot.library.mvp.view.UiView;

/**
 * Created by Administrator on 2017/3/2.
 */
public interface ISettingView extends UiView {
    int getIsOpenValue(int number);
    String getOpenDistanceValue(int number);

    void setIsOpenValue(int number, int isOpenValue);
    void setOpenDistanceValue(int number, String openDistanceValue);

    int getExchangeMode();

    void setVoiceTime(String time);
    String getVoiceTime();

    void setDistance0(String distance);
    void setDistance1(String distance);
    void setDistance7(String distance);
    void setDistance8(String distance);
    void setDistance9(String distance);
    void setDistance10(String distance);

    CheckBox getIsAutoOpen();
    void setAutoOpen(boolean isAuto);

    void setStartTime(String startTime);
    void setEndTime(String endTime);
    void setTimerPlace(String guestPlace);

}
