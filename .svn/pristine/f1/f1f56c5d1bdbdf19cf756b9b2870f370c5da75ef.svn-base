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
import com.efrobot.guest.bean.Action_Head;

import java.util.List;

/**
 * Created by shuai on 2016/8/26.
 */
public class CustomHeadAdapter extends BaseAdapter {
    private Context context;
    private List<Action_Head> list;
    private boolean visible = false;

    public void setmOnCallbackHead(onCallbackHead mOnCallbackHead) {
        this.mOnCallbackHead = mOnCallbackHead;
    }

    private onCallbackHead mOnCallbackHead;

    public CustomHeadAdapter(Context context, List<Action_Head> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public void setData(List<Action_Head> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();

        if (mOnCallbackHead != null)
            mOnCallbackHead.callback(list);
    }

    @Override
    public Object getItem(int position) {
        return position;
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
        convertView = LayoutInflater.from(context).inflate(R.layout.head_listitem, parent, false);
        TextView head_text = (TextView) convertView.findViewById(R.id.head_text_listitem);
        final ImageView head_del = (ImageView) convertView.findViewById(R.id.head_del);
        final Spinner head_direction_spin = (Spinner) convertView.findViewById(R.id.head_direction_spin);
        final Spinner head_time_spin = (Spinner) convertView.findViewById(R.id.head_time_spin);
        final Spinner head_angle_spin = (Spinner) convertView.findViewById(R.id.head_angle_spin);
        Resources res = context.getResources();
        final String[] head_d = res.getStringArray(R.array.head_direction);
        final String[] head_t = res.getStringArray(R.array.head_time);
        final String[] head_a = res.getStringArray(R.array.head_angle);
        head_text.setTag(position);
        head_direction_spin.setTag(position);
        head_time_spin.setTag(position);
        head_angle_spin.setTag(position);
        head_del.setTag(position);
        if (visible) {
            head_del.setVisibility(View.VISIBLE);
        } else {
            head_del.setVisibility(View.GONE);
        }
        final Action_Head head = list.get(position);
        head_text.setText((Integer) head_text.getTag() + 1 + "");
        head_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = ((Integer) head_del.getTag());
                list.remove(pos);
                notifyDataSetChanged();
                if (mOnCallbackHead != null)
                    mOnCallbackHead.callback(list);
            }
        });
        head_direction_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                list.get((Integer) head_direction_spin.getTag()).setDirection(head_d[position]);
                if ("归位".equals(head_d[position])) {
                    list.get((Integer) head_direction_spin.getTag()).setAngle("120");
                    head_angle_spin.setSelection(0);
                    head_angle_spin.setEnabled(false);
                } else {
                    head_angle_spin.setEnabled(true);
                }
                if (mOnCallbackHead != null)
                    mOnCallbackHead.callback(list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        head_angle_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                list.get((Integer) head_angle_spin.getTag()).setAngle(head_a[position]);
                if (mOnCallbackHead != null)
                    mOnCallbackHead.callback(list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        head_time_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                list.get((Integer) head_time_spin.getTag()).setNext_action_time(head_t[position]);
                if (mOnCallbackHead != null)
                    mOnCallbackHead.callback(list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (list.size() > 0) {
            for (int i = 0; i < head_d.length; i++) {
                if (head_d[i].equals(head.getDirection())) {
                    head_direction_spin.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < head_t.length; i++) {
                if (head_t[i].equals(head.getNext_action_time())) {
                    head_time_spin.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < head_a.length; i++) {
                if (head_a[i].equals(head.getAngle())) {
                    head_angle_spin.setSelection(i);
                    break;
                }
            }
        }
        return convertView;
    }

    public interface onCallbackHead {
        void callback(List<Action_Head> list);
    }

}
