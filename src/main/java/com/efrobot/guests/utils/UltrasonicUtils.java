package com.efrobot.guests.utils;

import android.content.Context;

import com.efrobot.library.RobotManager;
import com.efrobot.library.mvp.utils.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zd on 2017/10/18.
 */
public class UltrasonicUtils {

    private final String TAG = this.getClass().getSimpleName();

    private UltrasonicUtils instance;

    public UltrasonicUtils getInstance() {
        if (instance == null) {
            instance = new UltrasonicUtils();
        }
        return instance;
    }

    byte byte5, byte4;

    public void openSomeUltrasonic(Context context, List<Integer> customUlData, String systemVersion) {
        initAllOpenData();

        List<Byte> byteList5 = new ArrayList<Byte>(); // byte5 前8个
        List<Byte> byteList4 = new ArrayList<Byte>(); // byte4 后5个
        if (customUlData != null && customUlData.size() > 0) {
            for (int i = 0; i < customUlData.size(); i++) {
                if (customUlData.get(i) < 8) {
                    byteList5.add(ultrasonicOpenMap.get(customUlData.get(i)));
                } else {
                    byteList4.add(ultrasonicOpenMap.get(customUlData.get(i)));
                }
            }

            for (int i = 0; i < byteList5.size(); i++) {
                byte currentByte5 = byteList5.get(i);
                byte5 |= currentByte5;
            }
            for (int i = 0; i < byteList4.size(); i++) {
                byte currentByte4 = byteList4.get(i);
                byte4 |= currentByte4;
            }
            sendUserUltrasonic(context, systemVersion);

        }
    }

    /**
     * 打开用户定义的超声波
     */
    private void sendUserUltrasonic(Context context, String systemVersion) {

        L.i(TAG, "Send user custom data to open ultrasonic");
        byte[] data = new byte[12];
        data[0] = (byte) 0x0c;
        data[1] = (byte) 0x03;
        data[2] = (byte) 0x06;
        data[3] = (byte) 0x03;
        data[4] = byte4;//        data[4] = (byte) 0x1F; 打开全部
        data[5] = byte5;//        data[5] = (byte) 0xFF;
        data[6] = (byte) 0x00;
        data[7] = (byte) 7;
        //开启后8秒左右收到回调
        RobotManager.getInstance(context).getCustomTaskInstance().sendByteData(data);
        //TODO 新策略
//        UltrasonicTaskManager.getInstance(RobotManager.getInstance(getApplication())).openUltrasonicFeedback(EnvUtil.ULGST001, byte4 << 8 | byte5);

    }


    Map<Integer, Byte> ultrasonicOpenMap = null;

    /**
     * 设置要打开的的探头字节
     * position 探头的下标
     */
    private void initAllOpenData() {
        if (ultrasonicOpenMap == null) {
            ultrasonicOpenMap = new HashMap<Integer, Byte>();
            //byte[5]
            ultrasonicOpenMap.put(0, (byte) 0x01);
            ultrasonicOpenMap.put(1, (byte) 0x02);
            ultrasonicOpenMap.put(2, (byte) 0x04);
            ultrasonicOpenMap.put(6, (byte) 0x40);
            ultrasonicOpenMap.put(7, (byte) 0x80);

            ultrasonicOpenMap.put(8, (byte) 0x01);
            ultrasonicOpenMap.put(9, (byte) 0x02);
            ultrasonicOpenMap.put(10, (byte) 0x04);
            ultrasonicOpenMap.put(11, (byte) 0x08);
            ultrasonicOpenMap.put(12, (byte) 0x10);
        }
    }

}
