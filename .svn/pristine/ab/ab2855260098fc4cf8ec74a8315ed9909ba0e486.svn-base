package com.efrobot.guest.utils;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/1/6.
 */
public class FaceAndActionUtils {
    private Context context;
    private static FaceAndActionUtils manager;
    /**
     * ContentProvider管理
     */
    private HashMap<String, String> mFaceList;
    private HashMap<String, String> mActionList;
    private HashMap<String, String> mActionTime;

    public FaceAndActionUtils(Context context) {
        this.context = context;

//        readFaceData();
//        readActionData();
    }

    public static FaceAndActionUtils getInstance(Context context) {
        if (manager == null) {
            manager = new FaceAndActionUtils(context);
        }
        return manager;
    }

    /**
     * 读取表情数据
     */
    public HashMap<String, String> readFaceData() {
        if (mFaceList == null || mFaceList.isEmpty())
            mFaceList =  ProviderManager.getInstance(context).queryAllFace();
        return mFaceList;
    }

    /**
     * 读取动作数据（自己写入的）
     */
//    public HashMap<String, String> readActionData() {
//        if (mActionList == null || mActionList.isEmpty()) {
//            mActionList = new HashMap<String, String>();
//            mActionTime = new HashMap<String, String>();
//
//            List<FaceAndActionEntity> list = GuestsApplication.from(context).getDbManager().queryAllAction();
//            if (list == null || list.isEmpty() || !PreferencesUtils.hasString(context, "action") || !"action2".equals(PreferencesUtils.getString(context, "action"))) {
//                BodyShowApplication.from(context).getDbManager().actionAndFace(context, "action.txt", 1);
//                list = BodyShowApplication.from(context).getDbManager().queryAllAction();
//                PreferencesUtils.putString(context, "action", "action2");
//            }
//            if (list != null && !list.isEmpty()) {
//                int size = list.size();
//                for (int i = 0; i < size; i++) {
//                    mActionList.put(list.get(i).index, list.get(i).content);
//                    mActionTime.put(list.get(i).index, list.get(i).time);
//                }
//            }
//        }
//        return mActionList;
//    }

    /**
     * 根据表情id获取对应的信息
     *
     * @param FaceId
     * @return
     */
    public String contrastFace(String FaceId) {
        if (mFaceList != null && !TextUtils.isEmpty(FaceId)) {
            if (mFaceList.containsKey(FaceId + "")) {
                return mFaceList.get(FaceId + "");
            }
        }
        return "";
    }

    /**
     * 根据动作id获取对应的信息
     *
     * @param finishAction
     * @return
     */
    public String contrastAction(String finishAction) {
        if (mActionList != null && !TextUtils.isEmpty(finishAction)) {
            if (mActionList.containsKey(finishAction + "")) {
                return mActionList.get(finishAction);
            }
        }
        return "";
    }

    /**
     * 根据动作id获取对应的信息
     *
     * @param finishAction
     * @return
     */
    public String contrastActionTime(String finishAction) {
        if (mActionTime != null && !TextUtils.isEmpty(finishAction)) {
            if (mActionTime.containsKey(finishAction + "")) {
                return "(" + mActionTime.get(finishAction + "") + ")";
            }
        }
        return "";
    }

    /**
     * 根据动作id获取动作执行的时间
     *
     * @param finishAction
     * @return
     */
    public double getActionTime(String finishAction) {
        if (mActionTime != null && !TextUtils.isEmpty(finishAction)) {
            if (mActionTime.containsKey(finishAction + "")) {
                String time = mActionTime.get(finishAction + "");
                if (!TextUtils.isEmpty(time)) {
                    try {
                        String tt = time.substring(0, time.length() - 1);
                        return Double.parseDouble(tt);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0.00;
                    }
                }
                return 0.00;
            }
        }
        return 0.00;
    }


//    public String contrastAction(int action) {
//        switch (action) {
//            case 0:
//                return "停止";
//            case 1:
//                return "向左看";
//            case 2:
//                return "向右看";
//            case 3:
//                return "摇头";
//            case 4:
//                return "摆翅";
//            case 5:
//                return "左转";
//            case 6:
//                return "右转";
//            case 7:
//                return "身体摇摆";
//            case 8:
//                return "前进";
//            case 9:
//                return "后退";
//            case 10:
//                return "原地转一圈";
//            case 11:
//                return "摇头摆翅";
//            case 12:
//                return "拍打";
//            case 13:
//                return "鼓掌";
//            case 14:
//                return "撒娇";
//            case 15:
//                return "向后转";
//            case 20:
//                return "滑步";
//            case 21:
//                return "喝多了";
//            case 22:
//                return "芭蕾";
//            case 23:
//                return "翱翔1";
//            case 24:
//                return "翱翔2";
//            case 25:
//                return "画圈诅咒";
//            case 27:
//                return "归位";
//            case 29:
//                return "呼吸灯一次";
//            case 30:
//                return "唤醒状态";
//            case 31:
//                return "惊恐";
//            case 32:
//                return "举右手";
//            case 33:
//                return "举左手";
//            case 34:
//                return "困";
//            case 36:
//                return "拍拍翅膀";
//            case 37:
//                return "平举";
//            case 38:
//                return "调皮坏笑";
//            case 39:
//                return "偷笑";
//            case 40:
//                return "委屈难过";
//            case 47:
//                return "摇头晃身";
//            case 48:
//                return "疑问";
//            default:
//                return "无";
//        }
//    }
//
//    public String contrastFace(int face) {
//        switch (face) {
//            case 1:
//                return "傲慢";
//            case 2:
//                return "闭嘴";
//            case 3:
//                return "财迷";
//            case 4:
//                return "呲牙";
//            case 5:
//                return "大佬";
//            case 6:
//                return "大笑";
//            case 7:
//                return "说话";
//            case 8:
//                return "嘚瑟";
//            case 9:
//                return "东张西望";
//            case 10:
//                return "发呆";
//            case 11:
//                return "尴尬";
//            case 12:
//                return "感动";
//            case 13:
//                return "害羞";
//            case 14:
//                return "含税";
//            case 15:
//                return "憨笑";
//            case 16:
//                return "好冷";
//            case 17:
//                return "花痴";
//            case 18:
//                return "坏笑";
//            case 19:
//                return "惊喜";
//            case 20:
//                return "惊讶";
//            case 21:
//                return "囧";
//            case 22:
//                return "可怜";
//            case 23:
//                return "困";
//            case 24:
//                return "冷汗";
//            case 25:
//                return "流鼻血";
//            case 26:
//                return "卖萌";
//            case 27:
//                return "难过";
//            case 28:
//                return "撇嘴";
//            case 29:
//                return "糗大了";
//            case 30:
//                return "示爱";
//            case 31:
//                return "耍贱";
//            case 32:
//                return "得意";
//            case 33:
//                return "调皮";
//            case 34:
//                return "偷笑";
//            case 35:
//                return "土豪";
//            case 36:
//                return "微笑";
//            case 37:
//                return "委屈";
//            case 38:
//                return "眩晕";
//            case 39:
//                return "眼花缭乱";
//            case 40:
//                return "疑问";
//            default:
//                return "说话";
//        }
//    }

}
