package com.efrobot.guests.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.guests.R;
import com.efrobot.guests.bean.ItemsContentBean;
import com.efrobot.guests.dao.DataManager;
import com.efrobot.guests.fragment.adapter.AutoListAdapter;
import com.efrobot.guests.fragment.adapter.AutoListItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AutoGuestFragment extends Fragment implements View.OnClickListener {

    private RecyclerView autoRecyclerView;

    private List<ItemsContentBean> mainList;
    private List<ItemsContentBean> list = new ArrayList<>();

    private AutoListAdapter autoListAdapter;

    private FrameLayout frameLayout;

    private EditText editText;

    private TextView commitBtn, cancelBtn;

    /**
     * 当前收起还是展开
     */
    private ImageView spanOrPickBtn;
    private boolean isPickUpList = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auto_guest, container, false);
        initView(view);
        setData();

        return view;
    }

    private void initView(View view) {
        autoRecyclerView = (RecyclerView) view.findViewById(R.id.auto_recycler_view);
        autoRecyclerView.addItemDecoration(new AutoListItemDecoration(15));
        autoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        spanOrPickBtn = (ImageView) view.findViewById(R.id.open_or_span);

        frameLayout = (FrameLayout) view.findViewById(R.id.bottom_layout);
        editText = (EditText) view.findViewById(R.id.bottom_content);
        commitBtn = (TextView) view.findViewById(R.id.bottom_commit);
        cancelBtn = (TextView) view.findViewById(R.id.bottom_cancel);

        spanOrPickBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        view.findViewById(R.id.guest_next_step_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_or_span:
                isPickUpList = !isPickUpList;
                setData();
                break;
            case R.id.bottom_commit:
                if (itemsContentBean != null) {
                    String content = editText.getText().toString().trim();
                    itemsContentBean.setOther(content);
                    DataManager.getInstance(getActivity()).upateContent(itemsContentBean);
                    handler.sendEmptyMessageDelayed(UPDATE_ADAPTER_DATA, 200);
                }
                frameLayout.setVisibility(View.GONE);
                break;
            case R.id.bottom_cancel:
                frameLayout.setVisibility(View.GONE);
                break;
            case R.id.guest_next_step_btn:
                ((ControlActivity) getActivity()).setWelcomeGuestFragment();
                break;
        }
    }

    private void setData() {
        list.clear();
        mainList = DataManager.getInstance(getActivity()).queryAllContent();
        if (mainList != null) {
            for (int i = 0; i < mainList.size(); i++) {
                if (isPickUpList) {
                    if (i < 3) {
                        list.add(mainList.get(i));
                    }
                } else {
                    list.add(mainList.get(i));
                }
            }
        }
        setListAdapter();
    }


    private ItemsContentBean itemsContentBean;

    private void setListAdapter() {
        if (autoListAdapter == null) {
            autoListAdapter = new AutoListAdapter(getActivity(), list);
            autoListAdapter.setOnRecycleViewItemClick(new AutoListAdapter.OnRecycleViewItemClick() {
                @Override
                public void onClick(int potion) {
                    frameLayout.setVisibility(View.VISIBLE);
                    itemsContentBean = list.get(potion);
                    editText.setText(itemsContentBean.getOther());
                    editText.requestFocus();
                    showInputSoft();
                }
            });
        } else
            autoListAdapter.notifyDataSetChanged();
        autoRecyclerView.setAdapter(autoListAdapter);
    }


    private final int UPDATE_ADAPTER_DATA = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_ADAPTER_DATA:
                    setData();
                    break;
            }
        }
    };

    private void showInputSoft() {
        InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
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
