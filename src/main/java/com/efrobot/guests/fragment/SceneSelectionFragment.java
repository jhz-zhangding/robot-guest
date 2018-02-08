package com.efrobot.guests.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.efrobot.guests.R;
import com.efrobot.guests.utils.PreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SceneSelectionFragment extends Fragment implements View.OnClickListener {

    private final String SP_MODE = "sp_mode";

    private int currentSelectedType = 1;
    private final int AUTO_TYPE = 1;
    private final int DIREC_TYPE = 2;

    private ImageView guestChooseImg;
    private ImageView guestOrOutChooseImg;

    private ImageView guestImage, leaveImage;

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
        currentSelectedType = PreferencesUtils.getInt(getActivity(), SP_MODE, 1);

        view.findViewById(R.id.guest_next_step_btn).setOnClickListener(this);
        view.findViewById(R.id.robot_auto_guest_rl).setOnClickListener(this);
        view.findViewById(R.id.robot_setting_guest_rl).setOnClickListener(this);

        guestChooseImg = (ImageView) view.findViewById(R.id.guest_mode_image);
        guestOrOutChooseImg = (ImageView) view.findViewById(R.id.guest_leave_mode_image);

        guestImage = (ImageView) view.findViewById(R.id.guest_sence_selected_image);
        leaveImage = (ImageView) view.findViewById(R.id.leave_sence_selected_image);
        updateModeView();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.robot_auto_guest_rl:
                currentSelectedType = AUTO_TYPE;
                updateModeView();
                PreferencesUtils.putInt(getActivity(), SP_MODE, currentSelectedType);
                break;
            case R.id.robot_setting_guest_rl:
                currentSelectedType = DIREC_TYPE;
                updateModeView();
                PreferencesUtils.putInt(getActivity(), SP_MODE, currentSelectedType);
                break;
            case R.id.guest_next_step_btn:
                if (currentSelectedType == AUTO_TYPE)
                    ((ControlActivity) getActivity()).setAutoGuestFragment();
                else
                    ((ControlActivity) getActivity()).setEnterGuestFragment();
                break;
        }
    }

    private void updateModeView() {
        if (currentSelectedType == AUTO_TYPE) {
            guestChooseImg.setBackgroundResource(R.mipmap.sence_choose);
            guestOrOutChooseImg.setBackgroundResource(R.mipmap.sence_unchoose);

            guestImage.setBackgroundResource(R.mipmap.guest_sence_selected_image);
            leaveImage.setImageResource(R.mipmap.leave_sence_unselected_image);
        } else if (currentSelectedType == DIREC_TYPE) {
            guestChooseImg.setBackgroundResource(R.mipmap.sence_unchoose);
            guestOrOutChooseImg.setBackgroundResource(R.mipmap.sence_choose);

            guestImage.setBackgroundResource(R.mipmap.guest_sence_unselected_image);
            leaveImage.setImageResource(R.mipmap.leave_sence_selected_image);
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
