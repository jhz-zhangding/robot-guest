package com.efrobot.guest.action;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import com.efrobot.guest.GuestsApplication;
import com.efrobot.guest.base.GuestsBasePresenter;
import com.efrobot.guest.bean.AddCustomMode;
import com.efrobot.guest.dao.ExchangeModeDao;
import com.efrobot.library.mvp.utils.L;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by zhaodekui on 2017/3/27.
 */
public class CustomPresenter extends GuestsBasePresenter<ICustomView> {

    private ExchangeModeDao modeDao;
    private ArrayList<AddCustomMode> modes;
    /**
     * 添加的图片或视频
     */
    public String addMedia = "";

    /**
     * 添加的图片或视频
     */
    public String addImage = "";

    /**
     * 添加音乐
     */
    public String addMusic = "";

    public CustomPresenter(ICustomView mView) {
        super(mView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modeDao = GuestsApplication.from(getContext()).getModeDao();
        modes = modeDao.queryAll();
        initData();
    }

    private void initData() {
        if(modes != null && modes.size() > 0) {
            String music = modes.get(0).getMusic();
            String video = modes.get(0).getMedia();
            String image = modes.get(0).getImage();
            addMedia = video;
            addImage = image;
            addMusic = music;
            if(!music.isEmpty() || !video.isEmpty()) {
                mView.setMedia2(music, video);
            }
            if(!image.isEmpty()) {
                mView.setMedia2("", image);
            }
        }
    }

    /**
     * 添加图片或视频
     */
    public void toAddMedia(String check) {
        try {
            Intent intent = new Intent("efrobot.robot.bodyshow");
            intent.putExtra("pick_folder", true);
            intent.putExtra("come", 1);
            intent.putExtra("check", check);
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("zhang", "e=" + e.toString());
            showToast("打开文件管理失败");
        }

    }

    public interface OnGetDuration {
        void onGet(String mGetDuration);
    }

    public void getMediaTime(String string, final OnGetDuration mOnGetDuration) {
        //使用此方法可以直接在后台获取音频文件的播放时间，而不会真的播放音频


        File mFile = new File(string);

        if (mFile.exists()) {
            MediaPlayer player = new MediaPlayer();  //首先你先定义一个mediaplayer
            try {
                player.setDataSource(string);  //String是指音频文件的路径
                player.prepare();        //这个是mediaplayer的播放准备 缓冲
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {//监听准备

                    @Override
                    public void onPrepared(MediaPlayer player) {

                        double size = player.getDuration();
                        L.e("=========>>>>", "获取的大小是：" + size);
                        String timelong = (int) Math.ceil((size / 1000)) + "";//转换为秒 单位为''
                        player.stop();//暂停播放
                        player.release();//释放资源
                        mOnGetDuration.onGet(timelong);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                mOnGetDuration.onGet("");
            }

        } else {
            mOnGetDuration.onGet("");
        }
    }

    public void saveModeInfo() {
//        if(!addMedia.isEmpty() || !addMusic.isEmpty() || !addImage.isEmpty()) {
            modeDao.delete();
//        } else {
//            showToast("请选择模式");
//            return;
//        }
        AddCustomMode mode = new AddCustomMode();
        mode.setMusic(addMusic);
        mode.setMedia(addMedia);
        mode.setImage(addImage);

        modeDao.insert(mode);
        exit();
    }

}
