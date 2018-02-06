package com.efrobot.guests.fragment.direction;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.R;
import com.efrobot.guests.dao.SelectedDao;
import com.efrobot.guests.fragment.ControlActivity;
import com.efrobot.guests.fragment.adapter.EnterDirecAdapter;
import com.efrobot.guests.setting.bean.SelectDirection;
import com.efrobot.guests.widget.CircleTextView;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnterGuestFragment extends Fragment implements View.OnClickListener {

    protected final int currentType = 1;

    private TagFlowLayout flowLayout;

    private MyAdapter myAdapter;

    private GridView gridView;

    private EnterDirecAdapter enterDirecAdapter;

    private List<SelectDirection> mSelectedList = new ArrayList<>();

    private List<SelectDirection> mList = new ArrayList<>();

    private LinkedHashMap<Integer, String> direcMap;

    private SelectedDao selectedDao;

    private CircleTextView circleTextView1, circleTextView2, circleTextView3, circleTextView4, circleTextView5;
    private Map<Integer, CircleTextView> circleMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_guest, container, false);
        selectedDao = GuestsApplication.from(getActivity()).getSelectedDao();
        initView(view);
        getDirectionData();
        setTagAdapter();
        setData();

        return view;
    }

    private void initView(View view) {
        flowLayout = (TagFlowLayout) view.findViewById(R.id.select_flow_layout);
        gridView = (GridView) view.findViewById(R.id.enter_recycler_view);

        circleTextView1 = (CircleTextView) view.findViewById(R.id.circle_left_btn2);
        circleTextView2 = (CircleTextView) view.findViewById(R.id.circle_left_btn1);
        circleTextView3 = (CircleTextView) view.findViewById(R.id.circle_middle_btn);
        circleTextView4 = (CircleTextView) view.findViewById(R.id.circle_right_btn1);
        circleTextView5 = (CircleTextView) view.findViewById(R.id.circle_right_btn2);
        circleMap.put(6, circleTextView1);
        circleMap.put(7, circleTextView2);
        circleMap.put(0, circleTextView3);
        circleMap.put(1, circleTextView4);
        circleMap.put(2, circleTextView5);

        view.findViewById(R.id.next_btn).setOnClickListener(this);
    }

    private void setData() {
        enterDirecAdapter = new EnterDirecAdapter(getActivity(), mList);
        gridView.setAdapter(enterDirecAdapter);
        enterDirecAdapter.setOnSelectedItem(new EnterDirecAdapter.OnSelectedItem() {
            @Override
            public void onSelect(SelectDirection selectDirection) {
                selectDirection.setType(currentType);
                if (!selectedDao.isExits(selectDirection.getUltrasonicId())) {
                    selectedDao.insert(selectDirection);
                } else {
                    selectedDao.update(selectDirection);
                }
                handler.sendEmptyMessageDelayed(1, 200);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    setTagAdapter();
                    break;
            }
        }
    };

    private void setTagAdapter() {
        mSelectedList = selectedDao.queryOneType(currentType);
        myAdapter = new MyAdapter(mSelectedList);
        flowLayout.setAdapter(myAdapter);

        updateCircleView();
    }

    private void updateCircleView() {
        for (Map.Entry maps : circleMap.entrySet()) {
            CircleTextView circleTextView = (CircleTextView) maps.getValue();
            circleTextView.setTextColor(getActivity().getResources().getColor(R.color.black));
            circleTextView.setBackgroundColor(getActivity().getResources().getColor(R.color.fine_tuning_text_color));
        }

        for (int i = 0; i < mSelectedList.size(); i++) {
            int id = mSelectedList.get(i).getUltrasonicId();
            if (circleMap.containsKey(id)) {
                circleMap.get(id).setBackgroundColor(getActivity().getResources().getColor(R.color.rounded_image_color_orange));
                circleMap.get(id).setTextColor(getActivity().getResources().getColor(R.color.white));
            }
        }
    }

    private void getDirectionData() {
        mList.clear();
        if (direcMap == null) {
            direcMap = new LinkedHashMap<>();
            direcMap.put(6, "左1");
            direcMap.put(7, "左2");
            direcMap.put(0, "中1");
            direcMap.put(1, "右2");
            direcMap.put(2, "右1");
        }

        List<SelectDirection> selectedList1 = selectedDao.queryOneType(1);
        List<SelectDirection> selectedList2 = selectedDao.queryOneType(2);

        for (Map.Entry map : direcMap.entrySet()) {
            int id = (int) map.getKey();
            String value = (String) map.getValue();

            SelectDirection sel = new SelectDirection();
            sel.setUltrasonicId(id);
            sel.setValue(value);
            sel.setSelected(false);
            sel.setEnabled(false);

            if (selectedList1 != null) {
                for (int i = 0; i < selectedList1.size(); i++) {
                    if (id == selectedList1.get(i).getUltrasonicId()) {
                        sel.setSelected(true);
                    }
                }
            }

            if (selectedList2 != null) {
                for (int i = 0; i < selectedList2.size(); i++) {
                    if (id == selectedList2.get(i).getUltrasonicId()) {
                        sel.setEnabled(true);
                    }
                }
            }

            mList.add(sel);
        }

    }

    private class MyAdapter extends TagAdapter<SelectDirection> {

        private List<SelectDirection> datas;

        public MyAdapter(List<SelectDirection> datas) {
            super(datas);
            this.datas = datas;
        }

        @Override
        public View getView(FlowLayout parent, int position, final SelectDirection selectDirection) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_flow_view, null);
            TextView textView = (TextView) view.findViewById(R.id.content);
            ImageView imageView = (ImageView) view.findViewById(R.id.del_img);
            textView.setText(selectDirection.getValue());
            view.findViewById(R.id.parent_view).findViewById(R.id.parent_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datas.remove(selectDirection);
                    notifyDataChanged();

                    if (selectedDao.isExits(selectDirection.getUltrasonicId())) {
                        selectedDao.delete(selectDirection.getUltrasonicId());
                    }
                    updateSelectContent();
                    updateCircleView();
                }
            });
            return view;
        }
    }

    private void updateSelectContent() {
        getDirectionData();
        if (enterDirecAdapter != null) {
            enterDirecAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.next_btn:
                ((ControlActivity) getActivity()).setEnterSettingFragment();
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
