package com.efrobot.guests.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.efrobot.guests.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SceneSelectionFragment extends Fragment implements View.OnClickListener {

    private int currentSelectedType = 1;
    private final int AUTO_TYPE = 1;
    private final int DIREC_TYPE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scene_selection, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.guest_next_step_btn).setOnClickListener(this);
        view.findViewById(R.id.robot_auto_guest_rl).setOnClickListener(this);
        view.findViewById(R.id.robot_setting_guest_rl).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.robot_auto_guest_rl:
                currentSelectedType = AUTO_TYPE;
                break;
            case R.id.robot_setting_guest_rl:
                currentSelectedType = DIREC_TYPE;
                break;
            case R.id.guest_next_step_btn:
                if (currentSelectedType == AUTO_TYPE)
                    ((ControlActivity) getActivity()).setAutoGuestFragment();
                else
                    ((ControlActivity) getActivity()).setAutoGuestFragment();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
