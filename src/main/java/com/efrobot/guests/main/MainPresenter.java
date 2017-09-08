package com.efrobot.guests.main;

import android.os.Bundle;

import com.efrobot.guests.base.GuestsBasePresenter;

/**
 * Created by Administrator on 2017/3/2.
 */
public class MainPresenter extends GuestsBasePresenter<IMainView> {
    public MainPresenter(IMainView mView) {
        super(mView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
