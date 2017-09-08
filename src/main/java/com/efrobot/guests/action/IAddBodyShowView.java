package com.efrobot.guests.action;

import android.widget.BaseAdapter;
import android.widget.Spinner;

import com.efrobot.library.mvp.view.UiView;

/**
 * Created by Administrator on 2016/2/18.
 */
public interface IAddBodyShowView extends UiView {
    void setAdapter(BaseAdapter mAdapter);


    String getEditContent();

    void setEditContent(String content);


    String getLight();

    void setLight(String light);

    void setCreateScriptText(String text);

    void setActionEnable(boolean b);

    void setFirstViewShow();

//    void setMedia(String path);

    void setMedia2(String musicPath, String mediaPath);

    void setTitle(String content);

    Spinner getOpenSpinner();

    Spinner getFlickerSpinner();

    void setOpenAndFlickerTime(String open, String flicker);

    void setRightHeadLayoutGone(boolean isGone);

    void setRightWheelLayoutGone(boolean isGone);

    void setRightWingLayoutGone(boolean isGone);
    /**
     * 设置动作和自定义动作是否可点击
     *
     */
    void setActionEnabled(String enabled);
    /**
     * 设置上传文件的Textview时间
     *
     */
    void setMediaTime(double mediaTime);
    /**
     * 设置自定义动作的Textview时间
     *
     */
    void setActionTime(String time);
    /**
     * 设置表情的Textview时间
     *
     */
    void setFaceTime(double time);

    /**
     * 设置动作的Textview时间
     *
     */
    void setSystemActionTime(double time);

    /**
     * 设置选中的表情动作数据
     *
     * @param adapter
     */
    void setActionsAndFaces(FaceAndActionAdapter adapter);


//    /**
//     * 设置播放时长
//     *
//     * @param time
//     */
//    void setMaxTime(String time);

    /**
     * 设置滚动到最新添加的表情活表情
     *
     * @param position
     */
    void setCurrentActionOrFace(int position);

    /**
     * 设置灯带是否可以点击
     *
     */
    void setLightEnnabled(boolean isContentLight);



    double getMaxTime();
}
