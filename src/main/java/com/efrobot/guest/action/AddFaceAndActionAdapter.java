package com.efrobot.guest.action;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.efrobot.guest.R;
import com.efrobot.guest.bean.FaceAndActionEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/2/18.
 */
public class AddFaceAndActionAdapter extends BaseAdapter {

    private Context context;

    private List<FaceAndActionEntity> entities;
    private int type = -1;
    private List<String> titleFace = new ArrayList<String>();
    private List<String> titleAction = new ArrayList<String>();
    public AddFaceAndActionAdapter(Context context) {
        this.context = context;
        entities = new ArrayList<FaceAndActionEntity>();
    }

    public void setDataResource(HashMap<String, String> value, List<String> actions, List<String> faces, int type) {

        if (type == 1) {
            this.titleFace = faces;
        } else if(type == 2){
            this.titleAction = actions;
        }
        this.type = type;
        getList(value);
        notifyDataSetChanged();
    }

    /**
     * 将Map数据转化为List形式
     *
     * @param value
     */
    private void getList(final HashMap<String, String> value) {

        if (this.entities == null) {
            this.entities = new ArrayList<FaceAndActionEntity>();
        }

        this.entities.clear();

        if (value != null) {
            for (Map.Entry<String, String> me : value.entrySet()) {
                this.entities.add(new FaceAndActionEntity(me.getKey(), me.getValue()));
            }

        }


    }

    @Override
    public int getCount() {
        return entities.size();
    }

    @Override
    public FaceAndActionEntity getItem(int position) {
        return entities.size() > 0 ? entities.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.face_action_item, parent, false);
            mHolder.tv = (TextView) convertView.findViewById(R.id.face_action_item_text);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        FaceAndActionEntity entity = entities.get(position);

//        mHolder.tv.setBackgroundResource(title.equals(entity.content) ? R.mipmap.btn_bg_press : R.mipmap.btn_bg_normal);
        if (type == 1) {
            mHolder.tv.setBackgroundResource(R.mipmap.btn_bg_normal);
        } else if (type == 2) {
            mHolder.tv.setBackgroundResource(R.mipmap.btn_bg_normal);
        }


        mHolder.tv.setText(entity.content);


        return convertView;
    }

    class ViewHolder {
        TextView tv;
    }
}
