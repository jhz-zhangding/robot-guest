package com.efrobot.guest.utils;

import android.content.Context;
import android.content.Intent;

import com.efrobot.library.mvp.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

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
}
