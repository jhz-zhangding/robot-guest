package com.efrobot.guests.fragment.direction;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.R;
import com.efrobot.guests.dao.SelectedDao;
import com.efrobot.guests.fragment.ControlActivity;
import com.efrobot.guests.fragment.adapter.EnterDirecAdapter;
import com.efrobot.guests.setting.bean.SelectDirection;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
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

        view.findViewById(R.id.next_btn).setOnClickListener(this);
    }

    private void setData() {
        if (enterDirecAdapter == null) {
            enterDirecAdapter = new EnterDirecAdapter(getActivity(), mList);
            gridView.setAdapter(enterDirecAdapter);
        } else {
            enterDirecAdapter.notifyDataSetChanged();
        }
        enterDirecAdapter.setOnSelectedItem(new EnterDirecAdapter.OnSelectedItem() {
            @Override
            public void onSelect(SelectDirection selectDirection) {
                selectDirection.setType(currentType);
                if (!selectedDao.isExits(selectDirection.getUltrasonicId())) {
                    selectedDao.insert(selectDirection);
                }
                setTagAdapter();
            }
        });
    }

    private void setTagAdapter() {
        mSelectedList = selectedDao.queryOneType(currentType);
        myAdapter = new MyAdapter(mSelectedList);
        flowLayout.setAdapter(myAdapter);
    }

    private void getDirectionData() {
        mList.clear();
        if (direcMap == null) {
            direcMap = new LinkedHashMap<Integer, String>();
            direcMap.put(2, "左1");
            direcMap.put(1, "左2");
            direcMap.put(0, "中1");
            direcMap.put(7, "右2");
            direcMap.put(6, "右1");
        }

        List<SelectDirection> selectedList = selectedDao.queryAll();

        for (Map.Entry map : direcMap.entrySet()) {
            int id = (int) map.getKey();
            String value = (String) map.getValue();

            SelectDirection sel = new SelectDirection();
            sel.setUltrasonicId(id);
            sel.setValue(value);
            sel.setSelected(false);

            if (selectedList != null) {
                for (int i = 0; i < selectedList.size(); i++) {
                    if (id == selectedList.get(i).getUltrasonicId()) {
                        sel.setSelected(true);
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
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datas.remove(selectDirection);
                    notifyDataChanged();

                    if (selectedDao.isExits(selectDirection.getUltrasonicId())) {
                        selectedDao.delete(selectDirection.getUltrasonicId());
                    }
                    updateSelectContent();
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
