package com.efrobot.guest.utils;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.efrobot.guest.R;

/**
 * 自定义弹出框
 */
public class CustomHintDialog {

    /**
     * 标题
     */
    private String title;
    /**
     * 提示文本
     */
    private String message;
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * Dialog主题
     */
    private int theme;
    /**
     * 取消按钮标题
     */
    private String cancelTitle;
    /**
     * 取消按钮点击回调
     */
    private IButtonOnClickLister iCancelButtonOnClickLister;
    /***
     * 确定按钮标题
     */
    private String submitTitle;
    /***
     * 确定按钮点击回调
     */
    private IButtonOnClickLister iSubmitButtonOnClickLister;
    /**
     *
     */
    private Dialog dialog;
    /**
     * 其余操作是否可以 dismiss Dialg
     */
    private boolean isCancel;
    private OnDismissListener onDismissListener;

    public CustomHintDialog(Context context, int theme) {
        this.mContext = context;
        this.theme = theme;
        this.theme = R.style.NewSettingDialog;
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 设置消息
     *
     * @param message 消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 设置Dialog dismiss 监听
     *
     * @param onDismissListener dismiss 监听
     */
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;

    }


    public interface IButtonOnClickLister {

        void onClickLister();
    }

    public void setCancleButton(String cancleTitle,
                                IButtonOnClickLister iButtonOnClickLister) {
        this.cancelTitle = cancleTitle;
        this.iCancelButtonOnClickLister = iButtonOnClickLister;

    }

    public void setSubmitButton(String submitTitle,
                                IButtonOnClickLister iButtonOnClickLister) {
        this.submitTitle = submitTitle;
        this.iSubmitButtonOnClickLister = iButtonOnClickLister;
    }

    public void setCancelable(boolean isCancel) {
        this.isCancel = isCancel;
    }

    private TextView tvTitle;
    private TextView tvEnsure;
    private TextView tvCancel;
    private TextView tvMessage;
    private View customview;

    public String transport(String inputStr) {
        char arr[] = inputStr.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == ' ') {
                arr[i] = '\u3000';
            } else if (arr[i] < '\177') {
                arr[i] = (char) (arr[i] + 65248);
            }

        }
        return new String(arr);
    }

    /**
     * 创建Dialog
     */
    public void create() {
        dialog = new Dialog(mContext, theme);

        customview = LayoutInflater.from(mContext).inflate(R.layout.message_dialog, null);
        dialog.setContentView(customview);
        dialog.setCancelable(isCancel);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (onDismissListener != null) {
                    onDismissListener.onDismiss();
                }


            }
        });
        DisplayMetrics m = mContext.getResources().getDisplayMetrics();
        int screenWidth = Math.min(m.widthPixels, m.heightPixels);


        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (screenWidth * 0.8);
        params.height = (int) (screenWidth * 0.5);
        dialog.getWindow().setAttributes(params);

        tvTitle = (TextView) customview.findViewById(R.id.title);
        tvEnsure = (TextView) customview.findViewById(R.id.tv_ok);
        tvCancel = (TextView) customview.findViewById(R.id.tv_cancle);
        tvMessage = (TextView) customview.findViewById(R.id.tv_message);
        tvMessage.setMovementMethod(new ScrollingMovementMethod());
        final ViewGroup.LayoutParams pa = tvEnsure.getLayoutParams();

        pa.width = (int) (params.width * 0.5);
        final ViewGroup.LayoutParams paSubmit = tvCancel.getLayoutParams();

        paSubmit.width = (int) (params.width * 0.5);
        updateView();
    }

    /**
     * 更新视图
     */

    public void updateView() {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        } else {
            tvTitle.setVisibility(View.GONE);
        }

        if (this.message != null && !this.message.equals("")) {


            if (message.length() > 25) {
                tvMessage.setGravity(Gravity.LEFT);
            } else {
                tvMessage.setGravity(Gravity.CENTER);
            }
            tvMessage.setText(message);
        } else
            tvMessage.setVisibility(View.GONE);

        if (this.cancelTitle != null && !this.cancelTitle.equals("")) {
            tvCancel.setText(cancelTitle);


            tvCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (iCancelButtonOnClickLister != null) {
                        iCancelButtonOnClickLister.onClickLister();
                    } else {
                        dismiss();
                    }

                }
            });
        } else {
            tvCancel.setVisibility(View.GONE);

            tvEnsure.setBackgroundResource(R.drawable.button_bg);
        }

        if (this.submitTitle != null && !this.submitTitle.equals("")) {
            tvEnsure.setText(submitTitle);


            tvEnsure.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (iSubmitButtonOnClickLister != null) {
                        iSubmitButtonOnClickLister.onClickLister();
                    } else {
                        dismiss();
                    }
                }
            });
        } else {
            tvEnsure.setVisibility(View.GONE);
            tvCancel.setBackgroundResource(R.drawable.button_bg);
        }
    }


    /**
     * 是否显示
     *
     * @return 是否显示
     */
    public boolean isShowing() {

        return dialog != null && dialog.isShowing();
    }

    /**
     * 显示
     */
    public void show() {
        if (dialog == null)
            create();
        else
            updateView();
        dialog.show();

    }

    public Window getWindow() {
        if (dialog == null)
            create();
        return dialog.getWindow();
    }

    /**
     * dialog 小时
     */
    public void dismiss() {
        if (isShowing()) {
            dialog.dismiss();
        }
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
