package com.efrobot.guest.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.guest.R;
import com.efrobot.guest.action.AddBodyShowView;
import com.efrobot.guest.bean.ItemsContentBean;
import com.efrobot.guest.dao.DataManager;

import java.util.List;

/**
 * Created by zd on 2017/8/17.
 * 欢迎语设置
 */
public class GreetingAdapter extends BaseAdapter {

    private Context mContext;

    private List<ItemsContentBean> lists;

    private DataManager dataManager;

    private boolean isShowDel = false;

    private OnDeleteItemListener onDeleteItemListener;

    public GreetingAdapter(Context context) {
        this.mContext = context;
        dataManager = DataManager.getInstance(mContext);
    }

    public void isShowAllData(int type, boolean isShowAll) {
        List<ItemsContentBean> list = DataManager.getInstance(mContext).queryItem(type);
        if (list != null) {
            if (!isShowAll) {
                if (list != null && list.size() > 1) {
                    list = list.subList(0, 1);
                }
            }
        }
        this.lists = list;
        notifyDataSetChanged();
    }

    public void setSourceData(List<ItemsContentBean> datas) {
        this.lists = datas;
    }

    public List<ItemsContentBean> getSourceData() {
        return this.lists;
    }

    public int getSourceDataSize() {
        int dataSize = 0;
        if (this.lists != null) {
            dataSize = this.lists.size();
        }
        return dataSize;
    }

    public void setDelVisible(OnDeleteItemListener onDeleteItemListener) {
        this.onDeleteItemListener = onDeleteItemListener;
        isShowDel = !isShowDel;
        notifyDataSetChanged();
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
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.welcome_list_item, parent, false);
            mHolder.welcomeTts = (TextView) convertView.findViewById(R.id.welcome_item_tts_tv);
            mHolder.delItemBtn = (ImageView) convertView.findViewById(R.id.welcome_item_del_img);
            mHolder.welcomeTts.setTag(position);
            mHolder.delItemBtn.setTag(position);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.welcomeTts.setText(lists.get(position).getOther());
        if (isShowDel) {
            mHolder.delItemBtn.setVisibility(View.VISIBLE);
        } else {
            mHolder.delItemBtn.setVisibility(View.GONE);
        }

        mHolder.welcomeTts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddBodyShowView.class);
                intent.putExtra("content", lists.get(position));
                ((Activity) mContext).startActivityForResult(intent, 1);
            }
        });
        mHolder.delItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataManager.deleteContentById(lists.get(position).getId());
                lists.remove(position);
                if (lists.size() <= 1) {
                    if (onDeleteItemListener != null) {
                        onDeleteItemListener.existLessTwo(true);
                    }
                }
                if (lists.size() == 0) {
                    isShowDel = false;
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    private ViewHolder mHolder;

    private class ViewHolder {
        TextView welcomeTts;
        ImageView delItemBtn;
    }

    interface OnDeleteItemListener {
        void existLessTwo(boolean isExistLessTwo);
    }
}
