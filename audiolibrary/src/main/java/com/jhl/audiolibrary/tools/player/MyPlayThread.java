package com.jhl.audiolibrary.tools.player;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by kqw on 2016/8/26.
 * 播放音乐的线程
 */
public class MyPlayThread extends Thread {

    // 采样率
    private int mSampleRateInHz = 44100;
    // 单声道
    private int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    // 双声道（立体声）
    // private int mChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;

    private static final String TAG = "播放伴奏===";
    private Activity mActivity;
    private AudioTrack mAudioTrack;
    private byte[] data;
    private String mFileName;
    // 播放进度
    private int playIndex = 0;
    // 是否缓冲完成
    boolean isLoaded = false;
    private final int bufferSize;
    private DataInputStream mDataInputStream;
    private int amplitude;

    private boolean isPlaying;
    private int pauseInt;//暂停的位置

    public MyPlayThread(Activity activity, String fileName) {
        mActivity = activity;
        mFileName = fileName;

        bufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                mSampleRateInHz,
                mChannelConfig,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2,
                AudioTrack.MODE_STREAM);

    }

    @Override
    public void run() {
        super.run();
        if (null != mAudioTrack)
            mAudioTrack.play();

        // 缓冲区
        byte[] buffer = new byte[1024];

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(mFileName));
            mDataInputStream = new DataInputStream(fileInputStream);
            byte[] audioDataArray = new byte[bufferSize];
            short[] shortArray = new short[bufferSize];
            int readLength = 0;
            isPlaying = true;
            while (mDataInputStream.available() > 0) {
                if (null != mAudioTrack && AudioTrack.PLAYSTATE_PLAYING == mAudioTrack.getPlayState()) {
                    readLength = mDataInputStream.read(audioDataArray);
                    if (readLength > 0) {
                        playIndex += mAudioTrack.write(audioDataArray, 0, readLength);
                    }
                }
            }
            if (AudioTrack.PLAYSTATE_PAUSED != mAudioTrack.getPlayState()) {
                isPlaying = false;
            }
//                stop();
//            stopp();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "run: play end");
    }


    //是否正在播放
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 设置左右声道平衡
     *
     * @param max     最大值
     * @param balance 当前值
     */
    public void setBalance(int max, int balance) {
        float b = (float) balance / (float) max;
        Log.i(TAG, "setBalance: b = " + b);
        if (null != mAudioTrack)
            mAudioTrack.setStereoVolume(1 - b, b);
    }

    /**
     * 设置左右声道是否可用
     *
     * @param left  左声道
     * @param right 右声道
     */
    public void setChannel(boolean left, boolean right) {
        if (null != mAudioTrack) {
            mAudioTrack.setStereoVolume(left ? 1 : 0, right ? 1 : 0);
            mAudioTrack.play();
        }
    }

    /**
     * @param num 0-1
     */
    public void setVolume(float num) {
        mAudioTrack.setStereoVolume(num, num);
    }

    public void setNoSound() {
        mAudioTrack.setStereoVolume(0, 0);
    }

    public void setHaveSound() {
        mAudioTrack.setStereoVolume(1, 1);
    }


    public void pause() {
        if (null != mAudioTrack && AudioTrack.PLAYSTATE_PLAYING == mAudioTrack.getPlayState()) {
            mAudioTrack.pause();
        }
    }

    public void play() {
        if (null != mAudioTrack && AudioTrack.PLAYSTATE_PLAYING != mAudioTrack.getPlayState())
            mAudioTrack.play();
    }

    public void stopp() {
        releaseAudioTrack();
    }

    private void releaseAudioTrack() {
        if (null != mAudioTrack) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
            playIndex = 0;
            isLoaded = false;
        }
    }

    /**
     * 播放进度 max 100
     *
     * @param progress
     */
    public void seekTo(int progress) {
        if (mAudioTrack != null) {
            if (data.length > playIndex) {
                playIndex = progress;
            }
        }
    }

    //获取当前的进度
    public int getCurrentPosition() {
        return playIndex;
    }

    //获取总进度
    public int getDuraction() {
        if (isLoaded) {
            return data.length;
        } else {
            return 0;
        }
    }
}
