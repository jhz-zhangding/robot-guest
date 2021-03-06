package com.efrobot.guest.main;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.efrobot.guest.R;
import com.efrobot.guest.base.GuestsBaseActivity;
import com.efrobot.library.mvp.presenter.BasePresenter;

public class MainActivity extends GuestsBaseActivity<MainPresenter> implements IMainView,View.OnClickListener{

    private Button startBtn;
//
    private EditText mPassword;

    public static String SP_GUEST_PASSWORD = "guest_password";

    public static String SP_IS_ALREADY_LOGIN = "is_login";

    @Override
    public BasePresenter createPresenter() {
        return new MainPresenter(this);
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_main;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onViewInit() {
        super.onViewInit();

        mPassword = (EditText) findViewById(R.id.start_guests_password);
        startBtn = (Button) findViewById(R.id.setting);
        startBtn.setOnClickListener(this);
        findViewById(R.id.add_back).setOnClickListener(this);

    }

    @Override
    protected void setOnListener() {
        super.setOnListener();

    }

    @Override
    protected void onViewCreateBefore() {
        super.onViewCreateBefore();
//        boolean isLogin = PreferencesUtils.getBoolean(this, SP_IS_ALREADY_LOGIN, false);
//        if(isLogin) {
//            finish();
//            startActivity(SettingActivity.class);
//        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.setting:
//                if(!TextUtils.isEmpty(mPassword.getText())) {
//                    String savedPassword = PreferencesUtils.getString(this, SP_GUEST_PASSWORD);
//                    if(!TextUtils.isEmpty(savedPassword)) {
//                        if (EnvUtil.IS_DEBUG) {
//                            L.e("密码调试", "输入密码：" + mPassword.getText().toString().trim() + "本地密码：" + savedPassword);
//                        }
//                        if(savedPassword.equals(mPassword.getText().toString().trim())) {
//                            PreferencesUtils.putBoolean(this, SP_IS_ALREADY_LOGIN, true);
//                            startActivity(SettingActivity.class);
//                            finish();
//                        } else {
//                            Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
//                }
                break;
            case R.id.add_back:
                finish();
                break;
        }
    }
}
