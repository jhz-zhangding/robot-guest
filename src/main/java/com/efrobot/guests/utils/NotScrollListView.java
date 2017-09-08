package com.efrobot.guests.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by zd on 2017/8/21.
 */
public class NotScrollListView extends ListView {

    public NotScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //设置不滚动
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}
