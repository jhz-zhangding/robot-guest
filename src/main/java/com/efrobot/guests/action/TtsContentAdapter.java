package com.efrobot.guests.action;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.efrobot.guests.R;

import java.util.List;


/**
 * Created by zd on 2017/9/25.
 */
public class TtsContentAdapter extends BaseAdapter {

    private Context context;

    private List<TtsBean> ttsBean;

    private OnPrePareWordsOnClick onPrePareWordsOnClick;

    public TtsContentAdapter(Context context, List<TtsBean> ttsBean) {
        this.context = context;
        this.ttsBean = ttsBean;
    }

    public void setOnPrePareWordsOnClick(OnPrePareWordsOnClick onPrePareWordsOnClick) {
        this.onPrePareWordsOnClick = onPrePareWordsOnClick;
    }

    @Override
    public int getCount() {
        return ttsBean.size();
    }

    @Override
    public Object getItem(int position) {
        return ttsBean.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tts_content, parent, false);
            viewHolder.ttsContent = (TextView) convertView.findViewById(R.id.item_tts_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.ttsContent.setText(ttsBean.get(position).getmSay());
        viewHolder.ttsContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPrePareWordsOnClick != null) {
                    onPrePareWordsOnClick.onClick(ttsBean.get(position));
                }
            }
        });


        return convertView;
    }

    class ViewHolder {

        TextView ttsContent;
    }

    interface OnPrePareWordsOnClick {
        void onClick(TtsBean ttsBean);
    }

}
