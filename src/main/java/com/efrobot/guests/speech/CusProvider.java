package com.efrobot.guests.speech;

import android.content.Intent;
import android.util.Log;

import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.service.UltrasonicService;
import com.efrobot.guests.utils.ActivityManager;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.speechsdk.SpeechManager;
import com.efrobot.speechsdk.SpeechSdkProvider;

/**
 * Created by lhy on 2017/2/14
 */
public class CusProvider extends SpeechSdkProvider {
    private static final String TAG = "CusProvider";
    public static final String AUTHORITY = "com.example.aidlclient1.provider";

    private Intent mServiceIntent;

    private boolean isVoiceAwak = false;

    private static int num = 0;

    @Override
    public void onReceiveMessage(String code, String speechContent) {
        try {
            Log.i(TAG, "service receive=" + code + " speechContent=" + speechContent + " Thread=" + Thread.currentThread().getName());

            if (code.equals("1000")) {
                Log.i(TAG, "enter welcome");
                if (!UltrasonicService.IsRunning) {
                    SpeechManager.getInstance().closeSpeechDiscern(getContext());
                    SpeechManager.getInstance().removeSpeechState(getContext(), 11);
//                    GuestsApplication.from(getContext()).startMainActiviyt(0);
                    Log.i(TAG, "initData");
                    //禁止轮子运动
//                    WheelActionUtils.getInstance(getContext()).closeWheelAction();
                    //开启迎宾
                    mServiceIntent = new Intent(getContext(), UltrasonicService.class);
                    getContext().getApplicationContext().startService(mServiceIntent);
                    isVoiceAwak = true;
                    num = 0;
                }
            } else if (code.equals("1001")) {
                SpeechManager.getInstance().openSpeechDiscern(getContext());
                ActivityManager.getInstance().finishActivity();
                if (null != mServiceIntent) {
                    getContext().getApplicationContext().stopService(mServiceIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isIntoVoice = false;

    @Override
    public void TTSEnd() {
        super.TTSEnd();
        L.e(TAG, "TTSEnd");
        if (UltrasonicService.isWelcomeTTsStart) {
            GuestsApplication application = GuestsApplication.from(getContext());
            if(application != null && application.ultrasonicService != null) {
                application.ultrasonicService.ttsEnd();
            }
            UltrasonicService.isWelcomeTTsStart = false;
        }
    }

}
