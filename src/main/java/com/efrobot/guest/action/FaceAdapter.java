package com.efrobot.guest.action;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.guest.R;
import com.efrobot.guest.utils.FaceAndActionUtils;

import java.util.List;

/**
 * 表情页面
 * Created by jqr111 on 2016/9/18.
 */
public class FaceAdapter extends RecyclerView.Adapter<FaceAdapter.ViewHolder> {


    private Context context;
    /**
     * 选择的表情
     */
    private List<String> faces;
    /**
     * 删除表情的回调
     */
    private OnFaceDeleteCallBack callBack;
    /**
     * 获取表情的工具类
     */
    private FaceAndActionUtils util;

    public FaceAdapter(Context context, List<String> faces, OnFaceDeleteCallBack callBack) {
        this.context = context;
        this.faces = faces;
        this.callBack = callBack;
        util = FaceAndActionUtils.getInstance(context);
    }

    /**
     * 刷新数据
     */
    public void setDataResource(List<String> faces) {
        this.faces = faces;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_face_layout, viewGroup, false);
        RecyclerView.ViewHolder viewHolder = new ViewHolder(view);
        return (ViewHolder) viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        if (faces.get(i) != null) {
            viewHolder.tvFaceName.setText(util.contrastFace(faces.get(i)));
            viewHolder.ivDeleteFace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null && !isQuicklyClick()) {
                        callBack.deleteCallBack(i);
                    }
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return faces != null && !faces.isEmpty() ? faces.size() : 0;
    }

    public interface OnFaceDeleteCallBack {
        void deleteCallBack(int positon);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * 表情名称
         */
        TextView tvFaceName;
        /**
         * 删除表情
         */
        ImageView ivDeleteFace;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFaceName = (TextView) itemView.findViewById(R.id.tvFaceName);
            ivDeleteFace = (ImageView) itemView.findViewById(R.id.ivDeleteFace);
        }
    }

    private long lastTime = 0;

    /**
     * 是否是快速点击
     *
     * @return
     */
    private boolean isQuicklyClick() {
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastTime < 500)
            return true;
        lastTime = nowTime;
        return false;
    }

}
