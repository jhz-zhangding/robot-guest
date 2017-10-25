package com.efrobot.guests.setting.advanced;

import android.content.Context;
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

    private EditText guestTimeEt;

    private ToggleButton correctionBtn;

    private ToggleButton autoGuestBtn;

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
        guestTimeEt = (EditText) findViewById(R.id.advanced_setting_time_et);
        correctionBtn = (ToggleButton) findViewById(R.id.advanced_setting_correction_btn);
        autoGuestBtn = (ToggleButton) findViewById(R.id.advanced_setting_guest_btn);
//        closeWheelBtn = (ToggleButton) findViewById(R.id.advanced_setting_wheel_btn);


//        String d = "<html>请谨慎关闭该功能！！！双轮运动关闭后，在迎宾状态下，机器人将不会再执行行走、转圈等动作，退出迎宾状态，双轮运动自动开启。<u><font color=\"#ff8f00\">异常情况退出迎宾状态，请重新进入迎宾app，否则无法恢复双轮状态。</u></html>";
//        TextView wheelHintTv = (TextView) findViewById(R.id.advanced_setting_wheel_html_text);
//        wheelHintTv.setText(Html.fromHtml(d));

        String e = "<html>此功能开启后，只需关闭面罩，机器人就会自动进入迎宾模式。关闭面罩前，请先确认超声波数据是否正常。<font color=\"#EA2000\">注：在其他应用场景中，若关闭面罩不需要开启迎宾，请关闭这个功能。</html>";
        TextView autoGuestTextView = (TextView) findViewById(R.id.advanced_setting_auto_html_text);
        autoGuestTextView.setText(Html.fromHtml(e));
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
        autoGuestBtn.setOnClickListener(this);
//        closeWheelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(exitBtn)) {
            finish();
        } else if (v.equals(correctionBtn)) {
            updateStatus(v);
        } else if (v.equals(autoGuestBtn)) {
            updateStatus(v);
        }
//        else if (v.equals(closeWheelBtn)) {
//            updateStatus(v);
//        }
    }

    private void updateStatus(View v) {
        if (v.equals(correctionBtn)) {
            boolean isOpenCorrection = PreferencesUtils.getBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION, false);
            if (isOpenCorrection) {
//                correctionBtn.setText("关闭");
                correctionBtn.setToggleOff();
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION, false);
            } else {
                correctionBtn.setToggleOn();
//                correctionBtn.setText("开启");
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_NEDD_CORRECION, true);
            }
        }

        if (v.equals(autoGuestBtn)) {
            boolean isOpenAutoGuest = PreferencesUtils.getBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, false);
            if (isOpenAutoGuest) {
//                autoGuestBtn.setText("关闭");
                autoGuestBtn.setToggleOff();
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, false);
            } else {
//                autoGuestBtn.setText("开启");
                autoGuestBtn.setToggleOn();
                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_AUTO_GUEST, true);
            }
        }

//        if (v.equals(closeWheelBtn)) {
//            boolean isOpenWheel = PreferencesUtils.getBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_OPEN_WHEEL, false);
//            if (isOpenWheel) {
//                //关闭轮子需要提示
//                showWheelWaringDialog();
//            } else {
//                closeWheelBtn.setToggleOn();
//                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_OPEN_WHEEL, true);
//            }
//        }
    }

//    private CustomHintDialog waringDialog;
//
//    private void showWheelWaringDialog() {
//        waringDialog = new CustomHintDialog(this, 0);
//        waringDialog.setMessage("关闭双轮运动会导致机器人无法运动\n" +
//                "确认要关闭双轮运动吗？");
//        waringDialog.setCancleButton("取消", new CustomHintDialog.IButtonOnClickLister() {
//            @Override
//            public void onClickLister() {
//                if (waringDialog != null) {
//                    waringDialog.dismiss();
//                }
//            }
//        });
//
//        waringDialog.setSubmitButton("确认", new CustomHintDialog.IButtonOnClickLister() {
//            @Override
//            public void onClickLister() {
////                closeWheelBtn.setText("关闭");
//                closeWheelBtn.setToggleOff();
//                PreferencesUtils.putBoolean(getContext(), SpContans.AdvanceContans.SP_GUEST_OPEN_WHEEL, false);
//                if (waringDialog != null) {
//                    waringDialog.dismiss();
//                }
//            }
//        });
//
//        waringDialog.show();
//
//    }

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
