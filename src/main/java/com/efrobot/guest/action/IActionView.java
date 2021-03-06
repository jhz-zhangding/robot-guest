package com.efrobot.guest.action;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.efrobot.library.mvp.view.UiView;

/**
 * Created by Administrator on 2017/3/2.
 */
public interface IActionView extends UiView {
    void setAdapter(BaseAdapter mAdapter);

    void head_setAdapter(CustomHeadAdapter mAdapter, LinearLayout head_footView);

    void wing_setAdapter(CustomWingAdapter mAdapter, LinearLayout wing_footView);

    Button getBtnEdit();

    Button getBtnClrar();

    /**
     * 设置自定义动作的Textview时间
     *
     */
    void setActionTime(String time);

    /**
     * 设置选中的表情数据
     *
     * @param adapter
     */
    void setFaces(FaceAdapter adapter);

//    /**
//     * 设置播放时长
//     *
//     * @param time
//     */
//    void setMaxTime(String time);

    /**
     * 设置滚动到最新添加的表情
     *
     * @param position
     */
    void setCurrentFace(int position);

    void setLightData(int type, String time);

    int getType();

    int getLightType();

    String getLightDuration();
}
