package com.efrobot.guests.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Administrator on 2016/2/22.
 */
public class GuestProvider {

    public static final String AUTHORITY = "com.efrobot.bodyshow.fa";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.efrobot.bodyshow";

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.efrobot.bodyshow";

    public static final String ACTION_TYPE = "vnd.android.cursor.action/vnd.efrobot.bodyshow";

    public static final String SET_TYPE = "vnd.android.cursor.set/vnd.efrobot.bodyshow";

    /**
     * 项目表常量
     */
    public static final class RobotItemColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/item");
        public static final String TABLE_NAME = "itemsbean";
        public static final String ITEMNAME = "itemName";
        public static final String ITEMTIME = "itemTime";
    }

    /**
     * 内容表常量
     * <p/>
     * {"_id integer primary key autoincrement", "itemNum integer", "sport text", "face integer"
     * , "action integer", "light text", "other text"}
     */
    public static final class RobotContentColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/content");
        public static final String TABLE_NAME = "itemscontentbean";
        public static final String ITEMNUM = "itemNum";
        public static final String SPORT = "sport";
        public static final String FACE = "face";
        public static final String ACTION = "action";
        public static final String LIGHT = "light";
        public static final String OTHER = "other";
        public static final String MEDIA = "media";
        public static final String TIME = "time";
        public static final String MUSIC = "music";
        public static final String HEAD = "head";
        public static final String WHEEL = "wheel";
        public static final String WING = "wing";
        public static final String OPENLIGHTTIME = "openLightTime";
        public static final String FLICKERLIGHTTIME = "flickerLightTime";
        public static final String FACETIME = "faceTime";
        public static final String ACTIONSYSTEMTIME = "actionSystemTime";
        public static final String MAXTIME = "maxTime";
        public static final String STARTAPPACTION = "startAppAction";
        public static final String STARTAPPNAME = "startAppName";
    }

    /**
     * 当前选择项 表常量
     */
    public static final class RobotPitchOnColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/PitchOn");
        public static final String TABLE_NAME = "PitchOn";
        public static final String ITEMNUM = "itemNum";
    }

    /**
     * 动作表表常量
     */
    public static final class RobotActionColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/bodyaction");
        public static final String TABLE_NAME = "faceandactionentity";
        public static final String ACRIONNUM = "actionNum";
        public static final String ACRIONNAME = "actionName";
        public static final String ACRIONTIME = "actionTime";
    }


    /**
     * 设置表表常量
     */
    public static final class RobotSetColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/bodyset");
        public static final String TABLE_NAME = "setbean";
        public static final String SETMODEL = "setmodel";
        public static final String SETNUM = "setnum";
    }
}
