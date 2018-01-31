package com.efrobot.guests.fragment.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by zd on 2018/1/30.
 */
public class AutoListItemDecoration extends RecyclerView.ItemDecoration {

    private int spanSpace = 10;

    public AutoListItemDecoration(int spanSpace) {
        this.spanSpace = spanSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) != 0)
            outRect.top = spanSpace;
    }
}
