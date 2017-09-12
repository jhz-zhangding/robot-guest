package com.efrobot.guests.utils;

import android.content.Context;
import android.content.Intent;

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

}
