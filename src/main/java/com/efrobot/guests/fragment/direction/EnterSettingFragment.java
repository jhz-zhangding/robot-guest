package com.efrobot.guests.fragment.direction;

import android.app.Fragment;
import android.content.Context;
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
import com.efrobot.guests.fragment.ControlActivity;
import com.efrobot.guests.fragment.adapter.AutoListAdapter;
import com.efrobot.guests.fragment.adapter.AutoListItemDecoration;
import com.efrobot.library.RobotManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnterSettingFragment extends Fragment implements View.OnClickListener {

    private int currentItemNum = 1;

    private RecyclerView recyclerView;

    private List<ItemsContentBean> list = new ArrayList<>();

    private List<ItemsContentBean> mainList = new ArrayList<>();

    private AutoListAdapter listAdapter;

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
        View view = inflater.inflate(R.layout.fragment_enter_setting, container, false);
        initView(view);
        setData();

        return view;
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.auto_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new AutoListItemDecoration(15));

        frameLayout = (FrameLayout) view.findViewById(R.id.bottom_layout);
        editText = (EditText) view.findViewById(R.id.bottom_content);
        commitBtn = (TextView) view.findViewById(R.id.bottom_commit);
        cancelBtn = (TextView) view.findViewById(R.id.bottom_cancel);

        spanOrPickBtn = (ImageView) view.findViewById(R.id.open_or_span);

        view.findViewById(R.id.next_btn).setOnClickListener(this);
        spanOrPickBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    private int showItemNum = 3;

    private String[] str = new String[]{"贵客光临，蓬荜生辉，ROBOT_NICKNAME_VALUE欢迎您的到来",
            "尊敬的来宾，很高兴见到您，里面请",
            "您好，我是迎宾机器人ROBOT_NICKNAME_VALUE，欢迎您来到ROBOT_NICKNAME_VALUE的大家庭"};

    private void setData() {
        list.clear();
        mainList = DataManager.getInstance(getActivity()).queryItem(currentItemNum);
        if (mainList != null) {

            if (mainList.size() == 0) {
                for (int i = 0; i < str.length; i++) {
                    ItemsContentBean itemsContentBean = new ItemsContentBean();
                    itemsContentBean.setOther(str[i].replace("ROBOT_NICKNAME_VALUE", RobotManager.getInstance(getActivity()).getRobotName()));
                    itemsContentBean.setItemNum(currentItemNum);
                    DataManager.getInstance(getActivity()).insertContent(itemsContentBean);
                    mainList.add(itemsContentBean);
                }
            }
            if (mainList.size() >= showItemNum) {
                if (mainList.size() > showItemNum)
                    spanOrPickBtn.setVisibility(View.VISIBLE);
                else
                    spanOrPickBtn.setVisibility(View.GONE);
                for (int i = 0; i < mainList.size(); i++) {
                    if (isPickUpList) {
                        if (i < 3) {
                            list.add(mainList.get(i));
                        }
                    } else {
                        list.add(mainList.get(i));
                    }
                }
            } else {
                list.addAll(mainList);
                spanOrPickBtn.setVisibility(View.GONE);
                int emptyListNum = showItemNum - mainList.size();
                for (int i = 0; i < emptyListNum; i++) {
                    ItemsContentBean itemsContentBean = new ItemsContentBean();
                    list.add(itemsContentBean);
                }
            }


        }
        setListAdapter();
    }

    private ItemsContentBean itemsContentBean;

    private void setListAdapter() {
        listAdapter = new AutoListAdapter(getActivity(), list);
        listAdapter.setOnRecycleViewItemClick(new AutoListAdapter.OnRecycleViewItemClick() {
            @Override
            public void onClick(int potion) {
                frameLayout.setVisibility(View.VISIBLE);
                itemsContentBean = list.get(potion);
                editText.setText(itemsContentBean.getOther());
                editText.requestFocus();
                showInputSoft();
            }
        });
        recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.next_btn:
                ((ControlActivity) getActivity()).setOutGuestFragment();
                break;
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
        }
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
