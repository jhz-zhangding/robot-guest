package com.efrobot.guests.setting.advanced;

import com.efrobot.library.mvp.view.UiView;

/**
 * Created by zd on 2017/9/14.
 */
public interface IAdvancedView extends UiView {

    void setDelayTime(String s);

    void setCorrectionState(Boolean openOrOff);

    void setAutoGuestState(Boolean openOrOff);

    void setWheelState(Boolean openOrOff);


}
