package com.efrobot.guests.utils;


import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.efrobot.guests.R;
import com.efrobot.guests.bean.WeekBean;

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

    public static DatePickerUtils getInstance() {
        if (null == datePickerUtils) {
            datePickerUtils = new DatePickerUtils();
        }
        return datePickerUtils;
    }

    private String hourStr, minuteStr;
    private int hour, minute;

    public void setDataPickDialog(final TextView view, final Context context) {
        this.mContext = context;
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
            }
        });
        dialog.getWindow().findViewById(R.id.time_sure).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (hour < 10) {
                    hourStr = "0" + hour;
                } else
                    hourStr = "" + hour;
                if (minute < 10) {
                    minuteStr = "0" + minute;
                } else
                    minuteStr = "" + minute;
                Log.i("time-------------->", hour + ":" + minute);

                view.setText(hourStr + ":" + minuteStr);
                dialog.dismiss();
            }
        });
    }

    /***
     * 24小时比较大小
     * params 23:56 to 24:40
     */
    public boolean isGreaterThanLast(String timeOne, String timeTwo) {
        boolean isGreaterThan = false;
        if (!TextUtils.isEmpty(timeOne) && timeOne.contains(":") &&
                !TextUtils.isEmpty(timeTwo) && timeTwo.contains(":")) {
            String[] startTimeStr = timeOne.split(":");
            int startTimeHour = Integer.parseInt(startTimeStr[0]);
            int startTimeMinutes = Integer.parseInt(startTimeStr[1]);

            String[] endTimeStr = timeTwo.split(":");
            int endTimeHour = Integer.parseInt(endTimeStr[0]);
            int endTimeMinutes = Integer.parseInt(endTimeStr[1]);

            if (startTimeHour > endTimeHour) {
                isGreaterThan = true;
            } else if (startTimeHour == endTimeHour) {
                if (startTimeMinutes >= endTimeMinutes)
                    isGreaterThan = true;
                else
                    isGreaterThan = false;
            } else {
                isGreaterThan = false;
            }
        }
        return isGreaterThan;
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
