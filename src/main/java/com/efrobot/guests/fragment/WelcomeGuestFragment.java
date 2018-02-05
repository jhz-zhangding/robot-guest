package com.efrobot.guests.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.efrobot.guests.R;
import com.efrobot.guests.service.UltrasonicService;
import com.efrobot.guests.setting.SettingActivity;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.speechsdk.SpeechManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeGuestFragment extends Fragment implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter dynamic_filter = new IntentFilter();
        dynamic_filter.addAction(ROBOT_MASK_CHANGE);
        getActivity().registerReceiver(lidBoardReceive, dynamic_filter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_guest, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.guide_setting).setOnClickListener(this);
        view.findViewById(R.id.more_setting).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.back:
                getActivity().finish();
                break;
            case R.id.guide_setting:
                ((ControlActivity) getActivity()).setSceneSelectionFragment();
                break;
            case R.id.more_setting:
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                getActivity().startActivity(intent);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (lidBoardReceive != null) {
            getActivity().unregisterReceiver(lidBoardReceive);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public final static String ROBOT_MASK_CHANGE = "android.intent.action.MASK_CHANGED";
    public final static String KEYCODE_MASK_ONPROGRESS = "KEYCODE_MASK_ONPROGRESS"; //开闭状态
    public final static String KEYCODE_MASK_CLOSE = "KEYCODE_MASK_CLOSE"; //关闭面罩
    public final static String KEYCODE_MASK_OPEN = "KEYCODE_MASK_OPEN";  //打开面罩
    private boolean alreadyClosed = false;
    private BroadcastReceiver lidBoardReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ROBOT_MASK_CHANGE.equals(intent.getAction())) {
                boolean close = intent.getBooleanExtra(KEYCODE_MASK_CLOSE, false);
                boolean maskOnProgress = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
                boolean maskOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);
                L.i("WelcomeGuestFragment", "lidBoardReceive get data----" + "close----" + close + "  maskOnProgress----" + maskOnProgress + "  maskOpen----" + maskOpen);
                if (close) {
                    alreadyClosed = true;
                    try {
                        SpeechManager.getInstance().removeSpeechState(getActivity().getApplicationContext(), 11);
                        L.i(TAG, "lidBoardReceive close----" + close + "   start service");
                        Intent mServiceIntent = new Intent(getActivity(), UltrasonicService.class);
                        getActivity().getApplicationContext().startService(mServiceIntent);
                        UltrasonicService.IsOpenRepeatLight = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

}
