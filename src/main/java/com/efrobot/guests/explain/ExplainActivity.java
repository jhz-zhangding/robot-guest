package com.efrobot.guests.explain;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.efrobot.guests.R;

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
