package com.efrobot.guests.setting.advanced;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.efrobot.guests.Env.SpContans;
import com.efrobot.guests.R;
import com.efrobot.guests.base.GuestsBaseActivity;
import com.efrobot.guests.face.ManageFaceActivity;
import com.efrobot.guests.face.RegisterImageCameraActivity;
import com.efrobot.guests.utils.UpdateUtils;
import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.PreferencesUtils;
import com.zcw.togglebutton.ToggleButton;

/**
 * 高级设置
 */
public class AdvancedSettingActivity extends GuestsBaseActivity<AdvancedSettingPresenter> implements IAdvancedView, View.OnClickListener {

    private TextView exitBtn;

    private EditText guestTimeEt;

    private TextView faceBaseManagerBtn;
    private ToggleButton faceOpenBtn;

    private ToggleButton correctionBtn;

    private ToggleButton autoGuestBtn;

    private TextView versionName;

//    private EditText faceCheckEt;

//    private ToggleButton closeWheelBtn;

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

        versionName = (TextView) findViewById(R.id.advanced_setting_version_name);
        String versionInfo = new UpdateUtils().getVersion(this, this.getPackageName());
        if(!TextUtils.isEmpty(versionInfo)) {
            versionName.setText("版本:" + versionInfo);
        }

        exitBtn = (TextView) findViewById(R.id.advanced_setting_exit);
        faceBaseManagerBtn = (TextView) findViewById(R.id.advanced_setting_face_base_manager_btn);
        guestTimeEt = (EditText) findViewById(R.id.advanced_setting_time_et);
        correctionBtn = (ToggleButton) findViewById(R.id.advanced_setting_correction_btn);
        autoGuestBtn = (ToggleButton) findViewById(R.id.advanced_setting_guest_btn);
        faceOpenBtn = (ToggleButton) findViewById(R.id.advanced_setting_face_btn);
//        faceCheckEt = (EditText) findViewById(R.id.face_check_count);
//        closeWheelBtn = (ToggleButton) findViewById(R.id.advanced_setting_wheel_btn);


//        String d = "<html>请谨慎关闭该功能！！！双轮运动关闭后，在迎宾状态下，机器人将不会再执行行走、转圈等动作，退出迎宾状态，双轮运动自动开启。<u><font color=\"#ff8f00\">异常情况退出迎宾状态，请重新进入迎宾app，否则无法恢复双轮状态。</u></html>";
//        TextView wheelHintTv = (TextView) findViewById(R.id.advanced_setting_wheel_html_text);
//        wheelHintTv.setText(Html.fromHtml(d));

        String e = "<html>此功能开启后，只需关闭面罩，机器人就会自动进入迎宾模式。关闭面罩前，请先确认超声波数据是否正常。<font color=\"#EA2000\">注：在其他应用场景中，若关闭面罩不需要开启迎宾，请关闭这个功能。</html>";
        TextView autoGuestTextView = (TextView) findViewById(R.id.advanced_setting_auto_html_text);
        autoGuestTextView.setText(Html.fromHtml(e));

//        faceCheckEt.setText(PreferencesUtils.getInt(getContext(), SpContans.AdvanceContans.SP_GUEST_FACE_CHECK_COUNT, 3) + "");
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
                if (s != null && !s.toString().isEmpty()) {
                    int time = Integer.parseInt(s.toString());
                    PreferencesUtils.putInt(getContext(), SpContans.AdvanceContans.SP_GUEST_DELAY_TIME, time);
                }
            }
        });

        exitBtn.setOnClickListener(this);
        correctionBtn.setOnClickListener(this);
        faceBaseManagerBtn.setOnClickListener(this);
        faceOpenBtn.setOnClickListener(this);
        autoGuestBtn.setOnClickListener(this);
//        closeWheelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(exitBtn)) {
//            if(!TextUtils.isEmpty(faceCheckEt.getText())) {
//                int time = Integer.parseInt(faceCheckEt.getText().toString());
//                PreferencesUtils.putInt(getContext(), SpContans.AdvanceContans.SP_GUEST_FACE_CHECK_COUNT, time);
//            }
            finish();
        } else if(v.equals(faceBaseManagerBtn)) {
            startActivity(new Intent(this, ManageFaceActivity.class));
        } else if(v.equals(faceOpenBtn)) {
            updateStatus(v, SpContans.AdvanceContans.SP_GUEST_AUTO_DETECTION_FACE);
        } else if (v.equals(correctionBtn)) {
            updateStatus(v, SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION);
        } else if (v.equals(autoGuestBtn)) {
            updateStatus(v, SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST);
        }
//        else if (v.equals(closeWheelBtn)) {
//            updateStatus(v);
//        }
    }

    private void updateStatus(View v, String textFiled) {

        ToggleButton toggleButton = ((ToggleButton)v);

        boolean isOpen = PreferencesUtils.getBoolean(getContext(), textFiled, false);

        if(isOpen) {
            toggleButton.setToggleOff();
            PreferencesUtils.putBoolean(getContext(), textFiled, false);
        } else {
            toggleButton.setToggleOn();
            PreferencesUtils.putBoolean(getContext(), textFiled, true);
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
    public void setCorrectionState(Boolean openOrOff) {
        updateToggleState(correctionBtn, openOrOff);
    }

    @Override
    public void setAutoGuestState(Boolean openOrOff) {
        updateToggleState(autoGuestBtn, openOrOff);
    }

    @Override
    public void setOpenFaceState(Boolean openOrOff) {
        updateToggleState(faceOpenBtn, openOrOff);
    }

    @Override
    public void setWheelState(Boolean openOrOff) {
//        updateToggleState(closeWheelBtn, openOrOff);
    }

    private void updateToggleState(ToggleButton button, Boolean openOrOff) {
        if (openOrOff)
            button.setToggleOn();
        else
            button.setToggleOff();

    }

}
