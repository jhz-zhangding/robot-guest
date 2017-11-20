package com.efrobot.guests.setting;

import com.efrobot.library.mvp.view.UiView;

/**
 * Created by Administrator on 2017/3/2.
 */
public interface ISettingView extends UiView {
    void setDistance1(int id, int distance);

    void setDistance2(int id, int distance);

    void setDistance3(int id, int distance);

    void setDistance7(int id, int distance);

    void setDistance8(int id, int distance);

}
