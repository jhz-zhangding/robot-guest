package com.efrobot.guest.action;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.efrobot.guest.R;
import com.efrobot.guest.bean.Action_Wing;

import java.util.List;

/**
 * Created by shuai on 2016/8/26.
 */
public class CustomWingAdapter extends BaseAdapter {
    private Context context;
    private List<Action_Wing> list;
    private boolean visible = false;

    public void setmCallbackWing(onCallbackWing mCallbackWing) {
        this.mCallbackWing = mCallbackWing;
    }

    private onCallbackWing mCallbackWing;

    public CustomWingAdapter(Context context, List<Action_Wing> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public void setData(List<Action_Wing> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
        if (mCallbackWing != null) {
            mCallbackWing.callBackWing(list);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setvis(boolean vis) {
        this.visible = vis;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.wing_listitem, parent, false);
        TextView wing_text = (TextView) convertView.findViewById(R.id.wing_text_listitem);
        final Spinner wing_direction_spin = (Spinner) convertView.findViewById(R.id.wing_direction_spin);
        final Spinner wing_angle_spin = (Spinner) convertView.findViewById(R.id.wing_angle_spin);
        final Spinner wing_time_spin = (Spinner) convertView.findViewById(R.id.wing_time_spin);
        final ImageView wing_del = (ImageView) convertView.findViewById(R.id.wing_del);
        wing_text.setTag(position);
        wing_direction_spin.setTag(position);
        wing_angle_spin.setTag(position);
        wing_time_spin.setTag(position);
        wing_del.setTag(position);
        if (visible) {
            wing_del.setVisibility(View.VISIBLE);
        } else {
            wing_del.setVisibility(View.GONE);
        }
        //String里的spinner数据
        Action_Wing wing = list.get(position);
        Resources res = context.getResources();
        final String[] wing_d = res.getStringArray(R.array.wing_direction);
        final String[] wing_t = res.getStringArray(R.array.wing_time);
        final String[] wing_a = res.getStringArray(R.array.wing_angle);
        wing_text.setText((Integer) wing_text.getTag() + 1 + "");
        wing_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = ((Integer) wing_del.getTag());
                list.remove(pos);
                notifyDataSetChanged();
                if (mCallbackWing != null)
                    mCallbackWing.callBackWing(list);
                notifyDataSetChanged();
            }
        });
        wing_direction_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                list.get((Integer) wing_direction_spin.getTag()).setDirection(wing_d[position]);
                if (mCallbackWing != null)
                    mCallbackWing.callBackWing(list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        wing_time_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                list.get((Integer) wing_time_spin.getTag()).setNext_action_time(wing_t[position]);
                if (mCallbackWing != null)
                    mCallbackWing.callBackWing(list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        wing_angle_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                list.get((Integer) wing_angle_spin.getTag()).setAngle(wing_a[position]);
                if (mCallbackWing != null)
                    mCallbackWing.callBackWing(list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (list.size() > 0) {
            for (int i = 0; i < wing_d.length; i++) {
                if (wing_d[i].equals(wing.getDirection())) {
                    wing_direction_spin.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < wing_t.length; i++) {
                if (wing_t[i].equals(wing.getNext_action_time())) {
                    wing_time_spin.setSelection(i);
                    break;
                }
            }
            for (int i = 0; i < wing_a.length; i++) {
                if (wing_a[i].equals(wing.getAngle())) {
                    wing_angle_spin.setSelection(i);
                    break;
                }
            }

        }
        return convertView;
    }

    public interface onCallbackWing {
        void callBackWing(List<Action_Wing> list);
    }
}
