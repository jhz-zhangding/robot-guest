package com.efrobot.guests.fragment;

import android.app.Activity;
import android.os.Bundle;

import com.efrobot.guests.R;

public class ControlActivity extends Activity {

    private ControlFragment controlFragment;

    private SceneSelectionFragment sceneSelectionFragment;

    private AutoGuestFragment autoGuestFragment;

    private WelcomeGuestFragment welcomeGuestFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        setControlFragment();
    }

    private void setControlFragment() {
        if (controlFragment == null) {
            controlFragment = new ControlFragment();
        }
        getFragmentManager().beginTransaction().replace(R.id.main_container, controlFragment).commit();
    }

    public void setSceneSelectionFragment() {
        if (sceneSelectionFragment == null) {
            sceneSelectionFragment = new SceneSelectionFragment();
        }
        getFragmentManager().beginTransaction().replace(R.id.main_container, sceneSelectionFragment).commit();
    }

    public void setAutoGuestFragment() {
        if (autoGuestFragment == null) {
            autoGuestFragment = new AutoGuestFragment();
        }
        getFragmentManager().beginTransaction().replace(R.id.main_container, autoGuestFragment).commit();
    }

    public void setWelcomeGuestFragment() {
        if (welcomeGuestFragment == null) {
            welcomeGuestFragment = new WelcomeGuestFragment();
        }
        getFragmentManager().beginTransaction().replace(R.id.main_container, welcomeGuestFragment).commit();
    }
}
