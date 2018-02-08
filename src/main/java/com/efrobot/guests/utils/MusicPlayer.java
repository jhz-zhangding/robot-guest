package com.efrobot.guests.utils;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.SeekBar;

import com.efrobot.library.mvp.utils.L;

/**
 * 音乐播放器
 */

public class MusicPlayer implements MediaPlayer.OnBufferingUpdateListener {

    public MediaPlayer mediaPlayer; // 媒体播放器
    boolean isOnERROE;
    private OnMusicCompletionListener onCompletionListener;
    private int percent = 0;
    private int index = 0;

    private CountDownTimer timer = new CountDownTimer(15 * 1000, 5 * 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (onCompletionListener != null) {
                onCompletionListener.onCompletion(false);
            }
        }
    };
    private int mCurrentPosition;

    // 初始化播放器
    public MusicPlayer(SeekBar seekBar) {
        super();

    }

    private void initMediaplayer() {
        try {
            mediaPlayer = new MediaPlayer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMediaData() {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
        mediaPlayer.setOnBufferingUpdateListener(this);
    }

    /**
     * @param url url地址
     */
    public void playUrl(String url, final OnMusicCompletionListener mCompletionListener) {

        percent = 0;
        isOnERROE = false;
        this.onCompletionListener = mCompletionListener;

        L.e(MusicPlayer.class.getSimpleName(), "  启动播放音乐" + url);

        try {

            if (mediaPlayer == null) {
                initMediaplayer();

            } else {
                mediaPlayer.reset();
            }
            initMediaData();

            mediaPlayer.setDataSource(url); // 设置数据源
            mediaPlayer.prepareAsync();

            timer.start();

            mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {

                    if (timer != null)
                        timer.cancel();

                    if (onCompletionListener != null) {
                        onCompletionListener.onPrepare(mp.getDuration());
                    }
                    try {
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        isOnERROE = true;
                        if (onCompletionListener != null) {
                            onCompletionListener.onCompletion(false);
                        }
                    }
                }
            });

            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(final MediaPlayer mp) {

                    if (timer != null)
                        timer.cancel();

                    if (onCompletionListener != null && !isOnERROE) {
                        isOnERROE = false;
                        onCompletionListener.onCompletion(true);
                    }
                    Log.e("mediaPlayer", "onCompletion    " + true);
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(final MediaPlayer mp, final int what, final int extra) {
                    if (timer != null)
                        timer.cancel();
                    isOnERROE = true;
                    if (onCompletionListener != null) {
                        onCompletionListener.onCompletion(false);
                    }
                    Log.e("mediaPlayer", "onCompletion    " + false);

                    return false;
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            if (timer != null)
                timer.cancel();
            isOnERROE = true;
            if (onCompletionListener != null)
                onCompletionListener.onCompletion(false);
            Log.e("mediaPlayer", "onCompletion    " + false);
        }
    }

    /**
     * 开始播放内置音乐
     * @param context
     * @param rawId
     * @param mCompletionListener
     */
    public void playResource(Context context, int rawId, final OnMusicCompletionListener mCompletionListener) {

        percent = 0;
        isOnERROE = false;
        this.onCompletionListener = mCompletionListener;

        L.e(MusicPlayer.class.getSimpleName(), "  启动播放音乐");

        try {

            if (mediaPlayer == null) {
                initMediaplayer();

            } else {
                mediaPlayer.reset();
            }
            initMediaData();

            // 播放当前默认铃声
            AssetFileDescriptor afd =  context.getResources().openRawResourceFd(rawId);
            if (afd != null) {
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                        afd.getLength());
                afd.close();
            }

            mediaPlayer.prepareAsync();

            timer.start();

            mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {

                    if (timer != null)
                        timer.cancel();

                    if (onCompletionListener != null) {
                        onCompletionListener.onPrepare(mp.getDuration());
                    }
                    try {
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        isOnERROE = true;
                        if (onCompletionListener != null) {
                            onCompletionListener.onCompletion(false);
                        }
                    }
                }
            });

            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(final MediaPlayer mp) {

                    if (timer != null)
                        timer.cancel();

                    if (onCompletionListener != null && !isOnERROE) {
                        isOnERROE = false;
                        onCompletionListener.onCompletion(true);
                    }
                    Log.e("mediaPlayer", "onCompletion    " + true);
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(final MediaPlayer mp, final int what, final int extra) {
                    if (timer != null)
                        timer.cancel();
                    isOnERROE = true;
                    if (onCompletionListener != null) {
                        onCompletionListener.onCompletion(false);
                    }
                    Log.e("mediaPlayer", "onCompletion    " + false);

                    return false;
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            if (timer != null)
                timer.cancel();
            isOnERROE = true;
            if (onCompletionListener != null)
                onCompletionListener.onCompletion(false);
            Log.e("mediaPlayer", "onCompletion    " + false);
        }
    }

    // 停止
    public void stop() {
        try {

            if (timer != null)
                timer.cancel();
            onCompletionListener = null;
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 缓冲更新
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (percent != 100)
            if (percent == this.percent) {
                index++;
            } else {
                index = 0;
            }
        if (index == 15) {

            isOnERROE = true;
            if (onCompletionListener != null) {
                onCompletionListener.onCompletion(false);
            }
            index = 0;
        }
        this.percent = percent;
    }


    // 暂停
    public void mediaPlayPause() throws Exception {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mCurrentPosition = mediaPlayer.getCurrentPosition();
        }
    }

    /***
     * 继续播放
     *
     * @throws Exception
     */
    public void mediaPlayContinue() throws Exception {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(mCurrentPosition);
            mediaPlayer.start();
        }
    }

    public boolean isPlayIng() {

        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public interface OnMusicCompletionListener {
        void onCompletion(boolean isPlaySuccess);

        void onPrepare(int mDuration);
    }

}