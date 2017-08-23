package com.efrobot.guests.base;

import android.app.Activity;

import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.library.mvp.view.UiView;

/**
 * Created by Administrator on 2017/1/5.
 */
public class GuestsBasePresenter<T extends UiView> extends BasePresenter<T> {

    public GuestsBasePresenter(T mView) {
        super(mView);
    }

    @Override
    public void showToast(final String text) {
        RobotToastUtil.getInstance(getContext()).showToastBusiness(text);
    }

    public void exit() {
        if (mView instanceof Activity)
            ((Activity) getContext()).finish();
        else L.e(TAG, "UiView is not Activity");
    }


}
