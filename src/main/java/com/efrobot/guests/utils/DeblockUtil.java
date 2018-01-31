package com.efrobot.guests.utils;

/**
 * 解锁工具类
 * Created by chirenjie on 2016/8/18.
 */
public class DeblockUtil {
    private OnDeblockLinstener mDeblock;

    /**
     * 输入密码正确与否的返回结果
     */
    public interface OnDeblockLinstener {
        void onDeblockResult(boolean result);
    }

    /**
     * 按钮个数
     */
    private int mCount;
    /**
     * 用来记录上一次点击事件数字
     */
    private int mNumber;
    /**
     * 顺序是否正确
     */
    private boolean sequence;

    /**
     * 传入密码个数
     *
     * @param count
     */
    public DeblockUtil(int count) {
        mCount = count - 1;
        sequence = true;
        mNumber = 0;
    }

    /**
     * 位置从0开始进行记录
     *
     * @param position
     */
    public void clickNumber(int position) {
        if (mNumber == position && sequence) {
            if(checkPasswordIsRight()){
                if(mDeblock!=null)
                    mDeblock.onDeblockResult(true);
            }
        } else {
            sequence = false;
        }
        mNumber++;
    }

    public void resetPassword() {
        sequence = true;
        mNumber = 0;
    }

    public boolean checkPasswordIsRight() {
        return mCount == mNumber && sequence;
    }

    public void setOnDebockListener(OnDeblockLinstener debockListener) {
        mDeblock = debockListener;
    }
}
