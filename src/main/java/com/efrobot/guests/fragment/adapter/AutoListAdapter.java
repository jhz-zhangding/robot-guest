package com.efrobot.guests.fragment.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.efrobot.guests.R;
import com.efrobot.guests.bean.ItemsContentBean;

import java.util.List;

/**
 * Created by zd on 2018/1/30.
 */
public class AutoListAdapter extends RecyclerView.Adapter<AutoListAdapter.ViewHolder> {

    private Context context;

    private List<ItemsContentBean> list;

    private OnRecycleViewItemClick onRecycleViewItemClick;

    public AutoListAdapter(Context context, List<ItemsContentBean> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnRecycleViewItemClick(OnRecycleViewItemClick onRecycleViewItemClick) {
        this.onRecycleViewItemClick = onRecycleViewItemClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_auto_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onRecycleViewItemClick != null) {
                    onRecycleViewItemClick.onClick((Integer) view.getTag());
                }
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.textView.setText("迎宾语" + (position + 1));
        holder.editText.setText(list.get(position).getOther());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        private TextView editText;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_num);
            editText = (TextView) itemView.findViewById(R.id.content_edit);
        }
    }

    public interface OnRecycleViewItemClick {
        void onClick(int potion);
    }

}
