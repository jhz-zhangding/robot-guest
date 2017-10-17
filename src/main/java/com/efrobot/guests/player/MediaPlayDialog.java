package com.efrobot.guests.player;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.efrobot.guests.GuestsApplication;
import com.efrobot.guests.R;

public class MediaPlayDialog extends Dialog {

    private VideoView mVideoView;
    private String filePath;

    private MediaPlayer player;

    private GuestsApplication application;
    private Context mContext;


    public MediaPlayDialog(Context context) {
        super(context, R.style.Dialog_Fullscreen);
        setContentView(R.layout.activity_media_play);
        this.mContext = context;
        application = GuestsApplication.from(mContext);

        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mVideoView = (VideoView) findViewById(R.id.media_surface_view);
    }

    public MediaPlayDialog(Context context, int theme) {
        super(context, theme);
    }

    protected MediaPlayDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void show() {
        super.show();
        player = new MediaPlayer();
        //设置视频控制器
        MediaController mc = new MediaController(mContext);
        mc.setVisibility(View.INVISIBLE);
        mVideoView.setMediaController(mc);
        //播放完成回调
        mVideoView.setOnCompletionListener(new MyPlayerOnCompletionListener());
        mVideoView.setVideoPath(filePath);
        mVideoView.start();
    }

    class MyPlayerOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Toast.makeText(mContext, "播放完成", Toast.LENGTH_SHORT).show();

            if(application != null && application.ultrasonicService != null) {
                if(application.ultrasonicService.mHandle != null) {
                    application.ultrasonicService.mHandle.sendEmptyMessage(application.ultrasonicService.VIDEO_FINISH);
                }
            }
            dismiss();
        }
    }

}
