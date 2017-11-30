package com.efrobot.guests.setting.advanced;

import android.os.Bundle;

import com.efrobot.guests.Env.SpContans;
import com.efrobot.guests.base.GuestsBasePresenter;
import com.efrobot.library.mvp.utils.PreferencesUtils;

/**
 * Created by zd on 2017/9/14.
 */
public class AdvancedSettingPresenter extends GuestsBasePresenter<IAdvancedView> {

    public AdvancedSettingPresenter(IAdvancedView mView) {
        super(mView);
    }

    @Override
    public void onViewCreateBefore() {
        super.onViewCreateBefore();
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateBtnStatus();
    }

    public void updateBtnStatus() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
