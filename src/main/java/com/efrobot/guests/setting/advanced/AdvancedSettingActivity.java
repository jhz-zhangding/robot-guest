package com.efrobot.guests.setting.advanced;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.efrobot.guests.Env.SpContans;
import com.efrobot.guests.R;
import com.efrobot.guests.base.GuestsBaseActivity;
import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.PreferencesUtils;

public class AdvancedSettingActivity extends GuestsBaseActivity<AdvancedSettingPresenter> implements IAdvancedView, View.OnClickListener {

    private TextView exitBtn;

    private EditText guestTimeEt;

    private TextView correctionBtn;

    private TextView autoGuestBtn;

    private TextView closeWheelBtn;

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_advanced_setting;
    }

    @Override
    public BasePresenter createPresenter() {
        return new AdvancedSettingPresenter(this);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onViewInit() {
        super.onViewInit();

        exitBtn = (TextView) findViewById(R.id.advanced_setting_exit);
        guestTimeEt = (EditText) findViewById(R.id.advanced_setting_time_et);
        correctionBtn = (TextView) findViewById(R.id.advanced_setting_correction_btn);
        autoGuestBtn = (TextView) findViewById(R.id.advanced_setting_guest_btn);
        closeWheelBtn = (TextView) findViewById(R.id.advanced_setting_wheel_btn);

    }

    @Override
    protected void setOnListener() {
        super.setOnListener();

        guestTimeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s != null && !s.toString().isEmpty()) {
                    int time = Integer.parseInt(s.toString());
                    PreferencesUtils.putInt(getContext(), SpContans.AdvanceContans.SP_GUEST_DELAY_TIME, time);
                }
            }
        });

        exitBtn.setOnClickListener(this);
        correctionBtn.setOnClickListener(this);
        autoGuestBtn.setOnClickListener(this);
        closeWheelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(exitBtn)) {
            finish();
        } else if (v.equals(correctionBtn)) {
            updateStatus(v);
        } else if (v.equals(autoGuestBtn)) {
            updateStatus(v);
        } else if (v.equals(closeWheelBtn)) {
            updateStatus(v);
        }
    }

    private void updateStatus(View v) {
        if (v.equals(correctionBtn)) {
            boolean isOpenCorrection = PreferencesUtils.getBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION, false);
            if (isOpenCorrection) {
                correctionBtn.setText("关闭");
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION, false);
            } else {
                correctionBtn.setText("开启");
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION, true);
            }
        }

        if (v.equals(autoGuestBtn)) {
            boolean isOpenAutoGuest = PreferencesUtils.getBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, false);
            if (isOpenAutoGuest) {
                autoGuestBtn.setText("关闭");
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, false);
            } else {
                autoGuestBtn.setText("开启");
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, true);
            }
        }

        if (v.equals(closeWheelBtn)) {
            boolean isCloseWheel = PreferencesUtils.getBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_STOP_WHEEL, false);
            if (isCloseWheel) {
                closeWheelBtn.setText("开启");
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_STOP_WHEEL, false);
            } else {
                closeWheelBtn.setText("关闭");
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_STOP_WHEEL, true);
            }
        }
    }

    @Override
    public void showToast(String text) {
        super.showToast(text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setDelayTime(String s) {
        guestTimeEt.setText(s);
    }

    @Override
    public void setCorrectionState(String s) {
        correctionBtn.setText(s);
    }

    @Override
    public void setAutoGuestState(String s) {
        autoGuestBtn.setText(s);
    }

    @Override
    public void setWheelState(String s) {
        closeWheelBtn.setText(s);
    }
}
