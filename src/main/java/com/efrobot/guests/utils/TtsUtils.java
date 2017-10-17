package com.efrobot.guests.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.efrobot.library.mvp.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2017/2/27.
 */
public class TtsUtils {

    /**
     * 停止语音识别的广播
     */
    public String ACTION_SPEECH_STOP = "com.efrobot.speech.action.SPEECH_STOP";
    /**
     * 开启语音识别的广播
     */
    public String ACTION_SPEECH_START = "com.efrobot.speech.action.SPEECH_START";

    private static TtsUtils instance;

    public static TtsUtils getInstance() {
        if(instance == null) {
            instance = new TtsUtils();
        }
        return instance;
    }

    public static boolean isCanUseGuest(Context context) {
        boolean isCanUse = true;
        Uri uri = Uri.parse("content://com.efrobot.diy.diydataProvider/question");
        Cursor cursor = context.getContentResolver().query(uri, null, "type=?", new String[]{"3"}, null);
        if (cursor != null && cursor.moveToNext()) {
            isCanUse = false;
        }
        Cursor cursor1 = context.getContentResolver().query(uri, null, "type=?", new String[]{"4"}, null);
        if (cursor1 != null && cursor1.moveToNext()) {
            isCanUse = false;
        }
        L.i("TtsUtils", "isCanUse = " + isCanUse);
        return isCanUse;
    }

    public static void sendTts(Context context, String content) {
        L.i("TtsUtils", "content--" + content);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", content);
            Intent mIntent = new Intent("com.efrobot.speech.voice.ACTION_TTS");
            mIntent.putExtra("data", jsonObject.toString());
            context.sendBroadcast(mIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void sendSpeechTts(Context context, String type, String detail, String reply) {
        L.i("TtsUtils", "detail--" + detail);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", type);
            jsonObject.put("detail", detail);
            jsonObject.put("reply", reply);
            Intent mIntent = new Intent("cn.efrobot.speech.ACTION_REMOTE_CONTROLLER");
            mIntent.putExtra("content", jsonObject.toString());
            context.sendBroadcast(mIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭TTS
     */
    public static void closeTTs(Context context) {
        Intent intent = new Intent("com.efrobot.speech.voice.ACTION_TTS");
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("modelType", "stopTTS");
        intent.putExtra("data", simpleMapToJsonStr(map));
        L.i("TtsUtils", "data = " + simpleMapToJsonStr(map));
        context.sendBroadcast(intent);
    }

    /**
     * 生成Json
     */
    public static String simpleMapToJsonStr(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "null";
        }
        String jsonStr = "{";
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            jsonStr += "\"" + key + "\":\"" + map.get(key) + "\",";
        }
        jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
        jsonStr += "}";
        return jsonStr;
    }

    /**
     * 开启语音识别广播
     */
    public void sendOpenSpeechBroadcast(Context context) {
        Intent intent = new Intent(ACTION_SPEECH_START);
        context.sendBroadcast(intent);
    }

    /**
     * 发送关闭语音识别广播
     * */
    public void sendCloseSpeechBroadCast(Context context) {
        Intent intent = new Intent(ACTION_SPEECH_STOP);
        context.sendBroadcast(intent);
    }

}
