package com.efrobot.guests.fragment;

import android.app.Activity;
import android.os.Bundle;

import com.efrobot.guests.R;
import com.efrobot.guests.fragment.direction.EnterGuestFragment;
import com.efrobot.guests.fragment.direction.EnterSettingFragment;
import com.efrobot.guests.fragment.direction.OutGuestFragment;
import com.efrobot.guests.fragment.direction.OutSettingFragment;

public class ControlActivity extends Activity {

    private ControlFragment controlFragment;

    private SceneSelectionFragment sceneSelectionFragment;

    private AutoGuestFragment autoGuestFragment;

    private WelcomeGuestFragment welcomeGuestFragment;

    private EnterGuestFragment enterGuestFragment;
    private EnterSettingFragment enterSettingFragment;
    private OutGuestFragment outGuestFragment;
    private OutSettingFragment outSettingFragment;

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
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, controlFragment).commit();
    }

    public void setSceneSelectionFragment() {
        if (sceneSelectionFragment == null) {
            sceneSelectionFragment = new SceneSelectionFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, sceneSelectionFragment).commit();
    }

    public void setAutoGuestFragment() {
        if (autoGuestFragment == null) {
            autoGuestFragment = new AutoGuestFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, autoGuestFragment).commit();
    }

    public void setWelcomeGuestFragment() {
        if (welcomeGuestFragment == null) {
            welcomeGuestFragment = new WelcomeGuestFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, welcomeGuestFragment).commit();
    }

    public void setEnterGuestFragment() {
        if (enterGuestFragment == null) {
            enterGuestFragment = new EnterGuestFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, enterGuestFragment).commit();
    }

    public void setEnterSettingFragment() {
        if (enterSettingFragment == null) {
            enterSettingFragment = new EnterSettingFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, enterSettingFragment).commit();
    }

    public void setOutGuestFragment() {
        if (outGuestFragment == null) {
            outGuestFragment = new OutGuestFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, outGuestFragment).commit();
    }

    public void setOutSettingFragment() {
        if (outSettingFragment == null) {
            outSettingFragment = new OutSettingFragment();
        }
        getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container, outSettingFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else
            super.onBackPressed();

    }
}
