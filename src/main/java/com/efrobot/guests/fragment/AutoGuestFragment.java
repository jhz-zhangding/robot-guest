package com.efrobot.guests.fragment;

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

import com.efrobot.guests.Env.EnvUtil;
import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.R;
import com.efrobot.guests.bean.ItemsContentBean;
import com.efrobot.guests.dao.DataManager;
import com.efrobot.guests.dao.SelectedDao;
import com.efrobot.guests.fragment.adapter.AutoListAdapter;
import com.efrobot.guests.fragment.adapter.AutoListItemDecoration;
import com.efrobot.guests.setting.bean.SelectDirection;
import com.efrobot.guests.utils.PreferencesUtils;
import com.efrobot.library.RobotManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AutoGuestFragment extends Fragment implements View.OnClickListener {

    private int currentItemNum = 1;

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

    /**
     * 显示条数
     */
    private final int showItemNum = 3;
    private SelectedDao selectedDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auto_guest, container, false);
        selectedDao = GuestsApplication.from(getActivity()).getSelectedDao();
        initView(view);
        setData();

        return view;
    }

    private void initView(View view) {
        autoRecyclerView = (RecyclerView) view.findViewById(R.id.auto_recycler_view);
        autoRecyclerView.addItemDecoration(new AutoListItemDecoration(25));
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
                    if (itemsContentBean.getItemNum() != 0) {
                        DataManager.getInstance(getActivity()).upateContent(itemsContentBean);
                    } else {
                        itemsContentBean.setItemNum(1);
                        DataManager.getInstance(getActivity()).insertContent(itemsContentBean);
                    }
                    handler.sendEmptyMessageDelayed(UPDATE_ADAPTER_DATA, 200);
                }
                frameLayout.setVisibility(View.GONE);
                break;
            case R.id.bottom_cancel:
                frameLayout.setVisibility(View.GONE);
                break;
            case R.id.guest_next_step_btn:
                PreferencesUtils.putInt(getActivity(), EnvUtil.SP_CURRENT_TYPE, currentItemNum);
                saveData();
                ((ControlActivity) getActivity()).setWelcomeGuestFragment();
                break;
        }
    }

    private HashMap<Integer, String> direcMap = null;

    private void saveData() {
        List<SelectDirection> select = selectedDao.queryOneType(currentItemNum);
        if (select != null && select.size() > 0) {
            selectedDao.delete();
        }
        Map<Integer, String> ultrasonicMaps = new HashMap<>();
        ultrasonicMaps.put(0, "100");
        ultrasonicMaps.put(1, "100");
        ultrasonicMaps.put(2, "100");
        ultrasonicMaps.put(6, "100");
        ultrasonicMaps.put(7, "100");
        GuestsApplication.from(getActivity()).saveUserSetting(ultrasonicMaps);


        if (direcMap == null) {
            direcMap = new HashMap<>();
            direcMap.put(6, "左1");
            direcMap.put(7, "左2");
            direcMap.put(0, "中1");
            direcMap.put(1, "右2");
            direcMap.put(2, "右1");
        }

        for (Map.Entry entry : direcMap.entrySet()) {
            int ultrasonicId = (Integer) entry.getKey();
            String value = (String) entry.getValue();
            SelectDirection selectDirection = new SelectDirection();
            selectDirection.setUltrasonicId(ultrasonicId);
            selectDirection.setValue(value);
            selectDirection.setSelected(true);
            selectDirection.setType(currentItemNum);
            selectedDao.insert(selectDirection);
        }

    }

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
