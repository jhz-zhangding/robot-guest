package com.efrobot.guest.utils;


import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.efrobot.guest.R;
import com.efrobot.guest.bean.WeekBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by hp on 2017/4/13.
 */
public class DatePickerUtils {

    public static DatePickerUtils datePickerUtils;
    private AlertDialog dialog;
    private AlertDialog dayPickDialog;
    public Context mContext;

    public boolean isAlreadyStart = false;

    public static DatePickerUtils getInstance() {
        if (null == datePickerUtils) {
            datePickerUtils = new DatePickerUtils();
        }
        return datePickerUtils;
    }

    private int hour, minute;

    public void setDataPickDialog(final EditText view, final Context context) {
        this.mContext = context;
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isAlreadyStart = PreferencesUtils.getBoolean(context.getApplicationContext(), "isAlreadyStart", false);
                ;
                if (isAlreadyStart) {
                    Toast.makeText(context, "已经开启了定时任务", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog = new AlertDialog.Builder(mContext).create();
                dialog.setCancelable(true);
                dialog.show();
                dialog.getWindow().setContentView(R.layout.timer_hh_mm_pick);
                Log.i("DatePickerUtils", "setDataPickDialog show");

                Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
                ((TimePicker) dialog.getWindow().findViewById(R.id.time_picker)).setIs24HourView(true);
                ((TimePicker) dialog.getWindow().findViewById(R.id.time_picker)).setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {


                    public void onTimeChanged(TimePicker view, int hourOfDay, int minutes) {
                        hour = hourOfDay;
                        minute = minutes;


                        Log.i("time-------------->", hour + ":" + minute);
                    }
                });


                dialog.getWindow().findViewById(R.id.time_sure).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (minute < 10) {
                            view.setText(hour + ":" + "0" + minute);
                        } else
                            view.setText(hour + ":" + minute);
                        dialog.dismiss();
                    }
                });
            }

        });
    }

    public void setDayPickDialog(List<WeekBean> weekList, Context context, OnDayCheckListener onDayCheckListener) {
        this.mContext = context;
        if (weekList != null) {
            dayPickDialog = new AlertDialog.Builder(mContext).create();
            dayPickDialog.setCancelable(true);
            dayPickDialog.show();
            dayPickDialog.getWindow().setContentView(R.layout.timer_choose_day_list);
            ListView listView = (ListView) dayPickDialog.getWindow().findViewById(R.id.timer_choose_day_lv);
            DayAdapter adapter = new DayAdapter(mContext, weekList);
            adapter.setOnDayCheckListener(onDayCheckListener);
            listView.setAdapter(adapter);
        }
    }

    class DayAdapter extends BaseAdapter {
        private Context context;
        private List<WeekBean> weekList = new ArrayList<WeekBean>();
        private OnDayCheckListener onDayCheckListener;

        private LinkedHashMap<String, Boolean> maps = new LinkedHashMap<String, Boolean>();

        public DayAdapter(Context context, List<WeekBean> weekList) {
            this.context = context;
            maps.put("周一", false);
            maps.put("周二", false);
            maps.put("周三", false);
            maps.put("周四", false);
            maps.put("周五", false);
            maps.put("周六", false);
            maps.put("周日", false);
            for (int i = 0; i < weekList.size(); i++) {
                this.maps.put(weekList.get(i).getDay(), weekList.get(i).isCheck());
            }
            for (LinkedHashMap.Entry<String, Boolean> ket : maps.entrySet()) {
                String key = ket.getKey();
                boolean value = ket.getValue();
                WeekBean weekBean = new WeekBean();
                weekBean.setDay(key);
                weekBean.setCheck(value);
                this.weekList.add(weekBean);
            }
        }

        public void setOnDayCheckListener(OnDayCheckListener onDayCheckListener) {
            this.onDayCheckListener = onDayCheckListener;
        }

        @Override
        public int getCount() {
            return weekList.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            convertView = LayoutInflater.from(context).inflate(R.layout.timer_choose_day_item, viewGroup, false);
            final CheckBox placeCk = (CheckBox) convertView.findViewById(R.id.timer_day_item_ck);
            TextView placeText = (TextView) convertView.findViewById(R.id.timer_day_item_text);
            final WeekBean weekItem = weekList.get(i);
            placeText.setText(weekItem.getDay());
            placeCk.setChecked(weekItem.isCheck());
            placeCk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    boolean isCheck = placeCk.isChecked();
                    if (onDayCheckListener != null) {
                        maps.put(weekItem.getDay(), isCheck);
                        onDayCheckListener.onCheckListData(maps);
                    }
                }
            });
            return convertView;
        }
    }

    public interface OnDayCheckListener {
        void onCheckListData(LinkedHashMap<String, Boolean> maps);
    }


}
