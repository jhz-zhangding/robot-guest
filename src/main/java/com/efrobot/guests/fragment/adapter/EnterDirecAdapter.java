package com.efrobot.guests.fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.efrobot.guests.R;
import com.efrobot.guests.setting.bean.SelectDirection;

import java.util.List;

/**
 * Created by zd on 2017/8/17.
 * 超声波选择
 */
public class EnterDirecAdapter extends BaseAdapter {

    private Context mContext;

    private List<SelectDirection> lists;

    private OnSelectedItem onSelectedItem;

    public EnterDirecAdapter(Context mContext, List<SelectDirection> lists) {
        this.mContext = mContext;
        this.lists = lists;
    }

    public void setSourceData(List<SelectDirection> datas) {
        this.lists = datas;
    }

    public List<SelectDirection> getSourceData() {
        return this.lists;
    }

    public void setOnSelectedItem(OnSelectedItem onSelectedItem) {
        this.onSelectedItem = onSelectedItem;
    }

    public void resetTextView(SelectDirection selectDirection) {
        if (lists != null) {
            for (int i = 0; i < lists.size(); i++) {
                if (lists.get(i).getUltrasonicId() == selectDirection.getUltrasonicId()) {
                    lists.get(i).setSelected(false);
                }
            }
            notifyDataSetChanged();
        }
    }


    @Override
    public int getCount() {
        return this.lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
        mHolder = new ViewHolder();
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_select_derc_ultrasonic, null);
        mHolder.direcValue = (TextView) convertView.findViewById(R.id.select_dec_tv_btn);
        convertView.setTag(mHolder);
//        } else {
//            mHolder = (ViewHolder) convertView.getTag();
//        }


        mHolder.direcValue.setText(lists.get(position).getValue());
        if (lists.get(position).isEnabled()) {
            mHolder.direcValue.setEnabled(false);
            mHolder.direcValue.setBackground(mContext.getResources().getDrawable(R.drawable.select_text_btn_choose));
            mHolder.direcValue.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            mHolder.direcValue.setEnabled(true);
            if (lists.get(position).isSelected()) {
                mHolder.direcValue.setBackground(mContext.getResources().getDrawable(R.drawable.text_type_bg));
                mHolder.direcValue.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                mHolder.direcValue.setBackground(mContext.getResources().getDrawable(R.drawable.select_text_btn_unchoose));
                mHolder.direcValue.setTextColor(mContext.getResources().getColor(R.color.black));
            }
        }

        mHolder.direcValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lists.get(position).isSelected()) {
                    lists.get(position).setSelected(true);
                    notifyDataSetChanged();
                    if (onSelectedItem != null) {
                        onSelectedItem.onSelect(lists.get(position));
                    }
                }
            }
        });
        return convertView;
    }

    private ViewHolder mHolder;

    private class ViewHolder {
        TextView direcValue;
    }

    public interface OnSelectedItem {
        void onSelect(SelectDirection selectDirection);
    }

}