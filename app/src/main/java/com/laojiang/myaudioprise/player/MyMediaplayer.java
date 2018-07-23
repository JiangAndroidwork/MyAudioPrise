package com.laojiang.myaudioprise.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

/**
 * 类介绍（必填）：音乐播放器
 * Created by Jiang on 2018/7/16 .
 */

public class MyMediaplayer implements Runnable, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
    private MediaPlayer mPlayer;
    private Context context;
    private String filePath;
    private boolean isPrepare;
    private MediaPlayListener listener;

    public MyMediaplayer(Context context, String filePath) {
        this.context = context;
        this.filePath = filePath;


    }

    @Override
    public void run() {
        if (filePath.toLowerCase().startsWith("http:") || filePath.toLowerCase().startsWith("https:")) {
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(filePath);
                    mPlayer.prepareAsync();
                    mPlayer.setOnPreparedListener(this);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mPlayer = MediaPlayer.create(context, Uri.parse(filePath));
            isPrepare = true;
        }
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
    }

    public void startPlay() {
        if (mPlayer != null && isPrepare && !mPlayer.isPlaying())
            mPlayer.start();
    }

    public void pausePlay() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void stopPlay() {
        if (mPlayer != null) {
            isPrepare = false;
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void setVolume(float num){
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.setVolume(num, num);
            mPlayer.start();
        }
    }

    public void setMediaPlayerListener(MediaPlayListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepare = true;
        if (listener != null) {
            listener.onPrepare();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (listener != null) {
            listener.onComplete();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (listener != null) {
            listener.onError();
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (listener != null) {
            listener.onBufferingUpdate(mp, percent);
        }
    }
}
