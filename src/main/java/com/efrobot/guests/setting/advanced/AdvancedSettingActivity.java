package com.efrobot.guests.setting.advanced;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.efrobot.guests.Env.SpContans;
import com.efrobot.guests.R;
import com.efrobot.guests.base.GuestsBaseActivity;
import com.efrobot.guests.utils.UpdateUtils;
import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.PreferencesUtils;
import com.zcw.togglebutton.ToggleButton;

/**
 * 高级设置
 */
public class AdvancedSettingActivity extends GuestsBaseActivity<AdvancedSettingPresenter> implements IAdvancedView, View.OnClickListener {

    private TextView versionName;

    private TextView exitBtn;

    private TextView openSpeechText, closeSpeechText;

    private EditText guestTimeEt;

    private ToggleButton speechBtn;

    private ToggleButton correctionBtn;

    private ToggleButton autoSetting;

//    private ToggleButton autoGuestBtn;

//    private ToggleButton closeWheelBtn;

    private int lastOpenDelay = 3;

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
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    @Override
    protected void onViewInit() {
        super.onViewInit();

        versionName = (TextView) findViewById(R.id.advanced_setting_version_name);
        String versionInfo = new UpdateUtils().getVersion(this, this.getPackageName());
        if (!TextUtils.isEmpty(versionInfo)) {
            versionName.setText("版本:" + versionInfo);
        }

        openSpeechText = (TextView) findViewById(R.id.advanced_setting_open_speech_note);
        closeSpeechText = (TextView) findViewById(R.id.advanced_setting_close_speech_note);

        exitBtn = (TextView) findViewById(R.id.advanced_setting_exit);
        guestTimeEt = (EditText) findViewById(R.id.advanced_setting_time_et);
        speechBtn = (ToggleButton) findViewById(R.id.advanced_setting_speech_btn);
        correctionBtn = (ToggleButton) findViewById(R.id.advanced_setting_correction_btn);
        autoSetting = (ToggleButton) findViewById(R.id.advanced_setting_auto);
//        autoGuestBtn = (ToggleButton) findViewById(R.id.advanced_setting_guest_btn);
//        closeWheelBtn = (ToggleButton) findViewById(R.id.advanced_setting_wheel_btn);


//        String d = "<html>请谨慎关闭该功能！！！双轮运动关闭后，在迎宾状态下，机器人将不会再执行行走、转圈等动作，退出迎宾状态，双轮运动自动开启。<u><font color=\"#ff8f00\">异常情况退出迎宾状态，请重新进入迎宾app，否则无法恢复双轮状态。</u></html>";
//        TextView wheelHintTv = (TextView) findViewById(R.id.advanced_setting_wheel_html_text);
//        wheelHintTv.setText(Html.fromHtml(d));

//        String e = "<html>此功能开启后，只需关闭面罩，机器人就会自动进入迎宾模式。关闭面罩前，请先确认超声波数据是否正常。<br /><font color=\"#FF2227\">注：在其他应用场景中，若关闭面罩不需要开启迎宾，请关闭这个功能。</html>";
//        TextView autoGuestTextView = (TextView) findViewById(R.id.advanced_setting_auto_html_text);
//        autoGuestTextView.setText(Html.fromHtml(e));

        initToogleButton();
        updateEditData();
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
        speechBtn.setOnClickListener(this);
        correctionBtn.setOnClickListener(this);
        autoSetting.setOnClickListener(this);
//        autoGuestBtn.setOnClickListener(this);
//        closeWheelBtn.setOnClickListener(this);
    }

    private void initToogleButton() {
        updateSpData(SpContans.AdvanceContans.SP_GUEST_NEED_SPEECH, speechBtn);
        updateSpData(SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, autoSetting);
//        updateSpData(SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, autoGuestBtn);
    }

    private void updateEditData() {
        boolean isOpenCorrection = PreferencesUtils.getBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_NEED_SPEECH, false);
        if (isOpenCorrection) {
            int lastDelay = PreferencesUtils.getInt(getContext(), SpContans.AdvanceContans.SP_GUEST_LAST_OPEN_DELAY, 3);
            guestTimeEt.setText(lastDelay + "");
            PreferencesUtils.putInt(getContext(), SpContans.AdvanceContans.SP_GUEST_DELAY_TIME, 3);
            openSpeechText.setBackgroundColor(getContext().getResources().getColor(R.color.et_color_0066FF));
            closeSpeechText.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        } else {
            int closeCloseDelay = PreferencesUtils.getInt(getContext(), SpContans.AdvanceContans.SP_GUEST_LAST_CLOSE_DELAY, 1);
            guestTimeEt.setText(closeCloseDelay + "");
            PreferencesUtils.putInt(getContext(), SpContans.AdvanceContans.SP_GUEST_DELAY_TIME, 1);
            closeSpeechText.setBackgroundColor(getContext().getResources().getColor(R.color.et_color_0066FF));
            openSpeechText.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        }
    }


    @Override
    public void onClick(View v) {
        if (v.equals(exitBtn)) {
            //记录上次开启语音时的交流时间
            if (guestTimeEt != null && !TextUtils.isEmpty(guestTimeEt.getText())) {
                boolean isOpenCorrection = PreferencesUtils.getBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_NEED_SPEECH, false);
                int time = Integer.parseInt(guestTimeEt.getText().toString());
                if (isOpenCorrection) {
                    PreferencesUtils.putInt(getContext(), SpContans.AdvanceContans.SP_GUEST_LAST_OPEN_DELAY, time);
                } else {
                    PreferencesUtils.putInt(getContext(), SpContans.AdvanceContans.SP_GUEST_LAST_CLOSE_DELAY, time);
                }
            }
            finish();
        } else if (v.equals(speechBtn)) {
            setClickData(SpContans.AdvanceContans.SP_GUEST_NEED_SPEECH, v);
            updateEditData();
        } else if (v.equals(autoSetting)) {
            setClickData(SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, v);
        }
//        else if (v.equals(correctionBtn)) {
//            setClickData(SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION, v);
//        }
//        else if (v.equals(autoGuestBtn)) {
//            setClickData(SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, v);
//        }
    }

    private void setClickData(String spFlag, View view) {
        boolean isOpenCorrection = PreferencesUtils.getBoolean(getContext(), spFlag, false);
        PreferencesUtils.putBoolean(getContext(), spFlag, !isOpenCorrection);
        updateSpData(spFlag, view);
    }

    private void updateSpData(String spFlag, View view) {
        boolean isOpenCorrection = PreferencesUtils.getBoolean(getContext(), spFlag, false);
        if (isOpenCorrection) {
            ((ToggleButton) view).setToggleOn();
        } else {
            ((ToggleButton) view).setToggleOff();
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
//        updateToggleState(autoGuestBtn, openOrOff);
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
