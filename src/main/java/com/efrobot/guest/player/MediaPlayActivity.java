package com.efrobot.guest.player;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.efrobot.guest.GuestsApplication;
import com.efrobot.guest.R;
import com.efrobot.guest.utils.ActivityManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MediaPlayActivity extends Activity {

    private VideoView mVideoView;
    private ImageView mImageView;
    private String filePath;
    private String fileType;

    private MediaPlayer player;

    private boolean isRepeat = false;
    private GuestsApplication application;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_play);

        application = GuestsApplication.from(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ActivityManager.getInstance().addActivity(this);

        mVideoView = (VideoView) findViewById(R.id.media_surface_view);
        mImageView = (ImageView) findViewById(R.id.image_view);

        Intent intent = getIntent();
        filePath = intent.getStringExtra("file_path");
        fileType = intent.getStringExtra("file_type");

        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (!file.exists()) {
                finish();
                return;
            }
        }
        if (fileType.equals("image")) {
            mImageView.setVisibility(View.VISIBLE);
            Bitmap bitmap = getLocalBitmap(filePath);
            if (null != bitmap) {
                mImageView.setImageBitmap(bitmap);
            }
        } else if (fileType.equals("video")) {
            mVideoView.setVisibility(View.VISIBLE);
            player = new MediaPlayer();
            //设置视频控制器
            MediaController mc = new MediaController(this);
            mc.setVisibility(View.INVISIBLE);
            mVideoView.setMediaController(mc);
            //播放完成回调
            mVideoView.setOnCompletionListener(new MyPlayerOnCompletionListener());
            mVideoView.setVideoPath(filePath);
            mVideoView.start();
        }
    }

    class MyPlayerOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Toast.makeText(MediaPlayActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
            if (isRepeat) {
                mVideoView.setVideoPath(filePath);
            }

            if(application != null && application.ultrasonicService != null) {
                if(application.ultrasonicService.mHandle != null) {
                    application.ultrasonicService.mHandle.sendEmptyMessage(application.ultrasonicService.VIDEO_FINISH);
                }
            }
        }
    }

    private Bitmap getLocalBitmap(String path) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            return BitmapFactory.decodeStream(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
