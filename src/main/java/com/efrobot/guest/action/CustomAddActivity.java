package com.efrobot.guest.action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.guest.R;
import com.efrobot.guest.base.GuestsBaseActivity;
import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.L;

import java.io.File;

public class CustomAddActivity extends GuestsBaseActivity<CustomPresenter> implements ICustomView, View.OnClickListener{

    private Button saveBtn, backBtn;
    /**
     * 上传文件
     */
    private TextView videoPull, musicPull, imagePull;
    /**
     * 选择的图片，视频，音频的名称
     */
    private TextView imageName, videoName, musicName;
    /**
     * 删除选择的图片，视频，音频
     */
    private ImageView deleteImage, deleteVideo, deleteMusic;
    /***
     * 是否选择了图片
     */
    public boolean isSelectedPicture;
    /***
     * 是否选择了音乐
     */
    public boolean isSelectedMusic;
    /***
     * 是否选择了视频
     */
    public boolean isSelectedVideo;

    //定义一个过滤器；
    private IntentFilter intentFilter;

    //定义一个广播监听器；
    private ResourceBroadcastReceiver resourcereceiver;

    @Override
    public BasePresenter createPresenter() {
        return new CustomPresenter(this);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_custom;
    }

    @Override
    protected void onViewInit() {
        super.onViewInit();

        saveBtn = (Button) findViewById(R.id.add_save_btn);
        backBtn = (Button) findViewById(R.id.add_back_btn);

        videoPull = (TextView) findViewById(R.id.videoPull);
        musicPull = (TextView) findViewById(R.id.musicPull);
        imagePull = (TextView) findViewById(R.id.imagePull);

        videoName = (TextView) findViewById(R.id.videoName);
        imageName = (TextView) findViewById(R.id.imageName);
        musicName = (TextView) findViewById(R.id.musicName);

        deleteImage = (ImageView) findViewById(R.id.deleteImage);
        deleteMusic = (ImageView) findViewById(R.id.deleteMusic);
        deleteVideo = (ImageView) findViewById(R.id.deleteVideo);

        //文件管理数据接受广播
        intentFilter = new IntentFilter();
        //添加过滤的Action值；
        intentFilter.addAction("efrobot.robot.resoure");
        //实例化广播监听器；
        resourcereceiver = new ResourceBroadcastReceiver();
        //将广播监听器和过滤器注册在一起；
        registerReceiver(resourcereceiver, intentFilter);
    }

    @Override
    protected void setOnListener() {
        super.setOnListener();
        saveBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        videoPull.setOnClickListener(this);
        musicPull.setOnClickListener(this);
        imagePull.setOnClickListener(this);

        deleteMusic.setOnClickListener(this);
        deleteVideo.setOnClickListener(this);
        deleteImage.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_save_btn:
                //保存
                ((CustomPresenter)mPresenter).saveModeInfo();
                break;
            case R.id.add_back_btn:
                //退出
                finish();
                break;
            case R.id.videoPull:
                if (isSelectedMusic || isSelectedPicture) {
                    mPresenter.showToast("音乐图片模式下暂不支持上传视频");
                    return;
                }
                ((CustomPresenter)mPresenter).toAddMedia("video");
                break;
            case R.id.imagePull:
                if (isSelectedVideo) {
                    mPresenter.showToast("视频模式下暂不支持上传图片");
                    return;
                }
                ((CustomPresenter)mPresenter).toAddMedia("image");
                break;
            case R.id.musicPull:
                if (isSelectedVideo) {
                    mPresenter.showToast("视频模式下暂不支持上传音乐");
                    return;
                }
                ((CustomPresenter)mPresenter).toAddMedia("music");
                break;
            case R.id.deleteMusic:
                musicName.setText("");
                ((CustomPresenter)mPresenter).addMusic = "";
                if (TextUtils.isEmpty(imageName.getText().toString())) {
                    videoPull.setSelected(false);
                    videoPull.setEnabled(!videoPull.isSelected());
                }
                deleteMusic.setVisibility(View.INVISIBLE);
                isSelectedMusic = false;
                break;
            case R.id.deleteVideo:
                imagePull.setSelected(false);
                musicPull.setSelected(false);
                musicPull.setEnabled(!musicPull.isSelected());
                imagePull.setEnabled(!imagePull.isSelected());
                videoName.setText("");
                ((CustomPresenter)mPresenter).addMedia = "";
                deleteVideo.setVisibility(View.INVISIBLE);
                isSelectedVideo = false;
                break;
            case R.id.deleteImage:
                imageName.setText("");
                if (TextUtils.isEmpty(musicName.getText().toString())) {
                    videoPull.setSelected(false);
                    videoPull.setEnabled(!videoPull.isSelected());
                }
                ((CustomPresenter)mPresenter).addImage = "";
                deleteImage.setVisibility(View.INVISIBLE);
                isSelectedPicture = false;
                if (TextUtils.isEmpty(((CustomPresenter)mPresenter).addMusic))
                    isSelectedPicture = false;
                break;
        }
    }

    class ResourceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("efrobot.robot.resoure")) {
                final String path = intent.getStringExtra("path");
                final String selectType = intent.getStringExtra("type");
                Log.e("zhang", "接受广播多媒体地址=====" + path);
                if (!TextUtils.isEmpty(path)) {
                    if ("music".equals(selectType)) {

                        ((CustomPresenter)mPresenter).getMediaTime(path, new CustomPresenter.OnGetDuration() {
                            @Override
                            public void onGet(String mGetDuration) {

                                if (TextUtils.isEmpty(mGetDuration)) {
                                    return;
                                }
                                deleteMusic.setVisibility(View.VISIBLE);
                                /***
                                 * 音频路径
                                 */
                                ((CustomPresenter)mPresenter).addMusic = path;
                                isSelectedMusic = true;
//                                tvAddMedia.setText("自定义(" + mGetDuration + "'')");
                                videoPull.setSelected(true);
//                                fileTime = Double.parseDouble(mGetDuration);
//                                initSay();
                                setMediaInfo(selectType, path);
                            }
                        });


                    } else if ("video".equals(selectType)) {


                        ((CustomPresenter)mPresenter).getMediaTime(path, new CustomPresenter.OnGetDuration() {
                            @Override
                            public void onGet(String mGetDuration) {
                                if (TextUtils.isEmpty(mGetDuration)) {
                                    return;
                                }
                                deleteVideo.setVisibility(View.VISIBLE);
                                /***
                                 * 媒体路径------视频
                                 */
                                ((CustomPresenter)mPresenter).addMedia = path;
                                isSelectedVideo = true;
//                                tvAddMedia.setText("自定义(" + mGetDuration + "'')");
                                musicPull.setSelected(true);
                                imagePull.setSelected(true);
//                                fileTime = Double.parseDouble(mGetDuration);
//                                initSay();

                                setMediaInfo(selectType, path);
                            }
                        });

                    } else if ("image".equals(selectType)) {
                        deleteImage.setVisibility(View.VISIBLE);
                        /***
                         * 媒体路径----图片
                         */
                        ((CustomPresenter)mPresenter).addImage = path;
                        isSelectedPicture = true;
                        videoPull.setSelected(true);

                        setMediaInfo(selectType, path);
                    }


                } else {
//                    getMaxTime();
                    imageName.setText("");
                    musicName.setText("");
                    videoName.setText("");
                    imagePull.setSelected(false);
                    musicName.setSelected(false);
                    videoPull.setSelected(false);
//                    ivPhoto.setImageResource(R.mipmap.tupainjiazai);
                    ((CustomPresenter)mPresenter).addMedia = "";
                    ((CustomPresenter)mPresenter).addMusic = "";
                }

            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                Log.d(TAG, "onReceive ACTION_USER_PRESENT: ");
            }
        }

    }

    private void setMediaInfo(String selectType, String path) {
        videoPull.setEnabled(!videoPull.isSelected());
        musicPull.setEnabled(!musicPull.isSelected());
        imagePull.setEnabled(!imagePull.isSelected());

        if (TextUtils.isEmpty(path))
            return;
        try {
            File file = new File(path);
            L.e("--->>>", "media  path=" + path.substring(1) + "     " + file.exists());
            if (file.exists()) {
                int dos = path.lastIndexOf("/");
                if (dos != -1) {
                    String name = path.substring(dos + 1, path.length());
                    if ("music".equals(selectType)) {
                        musicName.setText(name);
                    } else if ("video".equals(selectType)) {
                        videoName.setText(name);
                    } else if ("image".equals(selectType)) {
                        imageName.setText(name);
                    }
                }
            } else {
                videoName.setText("");
                musicName.setText("");
                imageName.setText("");
//                ivPhoto.setImageResource(R.mipmap.tupainjiazai);
                ((CustomPresenter)mPresenter).addMedia = "";
                ((CustomPresenter)mPresenter).addMusic = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        getMaxTime();
    }

    @Override
    public void setMedia2(String musicPath, String mediaPath) {
        L.e("==============>>>", "设置媒体显示=============》》》》》》》》》》》》》》》");
        int dosmusic = -1;
        int dosmedia = -1;

        isSelectedPicture = false;
        isSelectedMusic = false;
        isSelectedVideo = false;

        if (!TextUtils.isEmpty(musicPath)) {
            dosmusic = musicPath.lastIndexOf("/");
        }
        if (!TextUtils.isEmpty(mediaPath)) {
            dosmedia = mediaPath.lastIndexOf("/");
        }

        /***
         * 选了音频
         */
        if (dosmusic != -1) {
            String musicname = musicPath.substring(dosmusic + 1, musicPath.length());
            musicName.setText(musicname);
            deleteMusic.setVisibility(View.VISIBLE);
            isSelectedMusic = true;
        }
        /***
         * 判断是否选择了视频或者图片
         */
        if (dosmedia != -1) {
            String name = mediaPath.substring(dosmedia + 1, mediaPath.length());

            if (name.contains("mp4")) {
                /**
                 * 选择了视频
                 */
                videoName.setText(name);
                deleteVideo.setVisibility(View.VISIBLE);
                isSelectedVideo = true;
            } else {
                /**
                 * 选择了图片
                 */
                imageName.setText(name);
                deleteImage.setVisibility(View.VISIBLE);
                isSelectedPicture = true;
            }
        }

        if (isSelectedVideo) {
            musicPull.setSelected(true);
            imagePull.setSelected(true);
        } else {

            if (isSelectedMusic) {
                videoPull.setSelected(true);
            }
        }


        videoPull.setEnabled(!videoPull.isSelected());
        musicPull.setEnabled(!musicPull.isSelected());
        imagePull.setEnabled(!imagePull.isSelected());
    }
}
