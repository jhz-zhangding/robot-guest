package com.efrobot.guests.fragment.direction;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.efrobot.guests.R;
import com.efrobot.guests.fragment.ControlActivity;
import com.efrobot.guests.setting.SettingActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class OutSettingFragment extends Fragment implements View.OnClickListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_out_setting, container, false);
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
    public void onDetach() {
        super.onDetach();
    }

}
