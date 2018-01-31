package com.efrobot.guests.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.efrobot.guests.R;
import com.efrobot.guests.utils.RobotMoveUtils;
import com.efrobot.guests.utils.ui.ControlView;

/**
 * Created by zd on 2018/1/24.
 */
public class ControlFragment extends Fragment implements ControlView.OnControlListener, View.OnClickListener {

    private ControlView controlView;

    private ImageView btnCenter, btnUp, btnDown, btnLeft, btnRight;

    private RobotMoveUtils robotMoveUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, null);

        robotMoveUtils = new RobotMoveUtils(getActivity());

        initView(view);
        setOnListener(view);
        return view;
    }

    private void initView(View view) {
        btnCenter = (ImageView) view.findViewById(R.id.btn_center);
        btnUp = (ImageView) view.findViewById(R.id.btn_up);
        btnDown = (ImageView) view.findViewById(R.id.btn_down);
        btnRight = (ImageView) view.findViewById(R.id.btn_right);
        btnLeft = (ImageView) view.findViewById(R.id.btn_left);
        controlView = (ControlView) view.findViewById(R.id.control_view);
        controlView.setControlView(btnCenter, btnUp, btnDown, btnLeft, btnRight);
    }

    private void setOnListener(View view) {
        controlView.setOnControlListener(this);
        view.findViewById(R.id.next_step_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.next_step_btn:
                ((ControlActivity) getActivity()).setWelcomeGuestFragment();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onControlUp() {
        robotMoveUtils.onControlUp(controlView.getControlMode());
    }

    @Override
    public void onControlDown() {
        robotMoveUtils.onControlDown(controlView.getControlMode());
    }

    @Override
    public void onControlLeft() {
        robotMoveUtils.onControlLeft(controlView.getControlMode());
    }

    @Override
    public void onControlRight() {
        robotMoveUtils.onControlRight(controlView.getControlMode());
    }

    @Override
    public void onControlStop() {
        robotMoveUtils.onControlStop(controlView.getControlMode());
    }

}
