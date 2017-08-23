package com.efrobot.guest.explain;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.efrobot.guest.R;
import com.efrobot.guest.base.GuestsBaseActivity;
import com.efrobot.guest.main.MainPresenter;
import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.view.UiView;

/**
 * 迎宾说明页面
 */
public class ExplainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explain);

        findViewById(R.id.add_back).setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_back:
                finish();
                break;
        }
    }
}
