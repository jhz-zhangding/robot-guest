package com.efrobot.guests.Env;

import java.util.LinkedHashMap;

/**
 * Created by zd on 2017/9/14.
 */
public class SpContans {


    public final static String SP_REMEMBER_WHEEL_STATE = "sp_remember_wheel_state";

    public final static String SP_ULTRASONIC_SETTING_STATUS = "sp_ultrasonic_setting_status";

    public final static String SP_OPEN_TEST_DIALOG = "sp_open_test_dialog";

    /**
     * 高级设置
     **/
    public static class AdvanceContans {

        public final static String SP_GUEST_DELAY_TIME = "sp_guest_delay_time";

        public final static String SP_GUEST_NEED_SPEECH = "sp_guest_nedd_speech";

        public final static String SP_GUEST_LAST_OPEN_DELAY = "sp_guest_last_open_delay";

        public final static String SP_GUEST_LAST_CLOSE_DELAY = "sp_guest_last_close_delay";

        public final static String SP_GUEST_NEDD_CORRECION = "sp_guest_nedd_correcion";

        public final static String SP_GUEST_AUTO_GUEST = "sp_guest_auto_guest";

//        public final static String SP_GUEST_OPEN_WHEEL = "sp_guest_open_wheel";


    }

    private  static LinkedHashMap<Integer, String> direcMap = null;

    public static LinkedHashMap<Integer, String> getDialogData() {
        if (direcMap == null) {
            direcMap = new LinkedHashMap<Integer, String>();
            direcMap.put(2, "左1");
            direcMap.put(1, "左2");
            direcMap.put(0, "中1");
            direcMap.put(7, "右2");
            direcMap.put(6, "右1");

//            direcMap.put(12, "左2");
//            direcMap.put(10, "左4");
//            direcMap.put(9, "中2");
//            direcMap.put(8, "右4");
//            direcMap.put(11, "右2");
        }
        return direcMap;
    }

    public LinkedHashMap<Integer, String> getDirecMap() {
        return getDialogData();
    }
}
