package com.jhl.audiolibrary.tools.recorder;

import android.media.AudioFormat;

/**
 * 类介绍（必填）：设置通道参数
 * Created by Jiang on 2018/7/22 .
 */
public enum PCMFormat {
    PCM_8BIT(1,AudioFormat.ENCODING_PCM_8BIT),
    PCM_16BIT(2, AudioFormat.ENCODING_PCM_16BIT);

    private int bytesPerFrame;
    private int audioFormat;

    PCMFormat(int bytesPerFrame, int audioFormat) {
        this.bytesPerFrame = bytesPerFrame;
        this.audioFormat = audioFormat;
    }

    public int getBytesPerFrame() {
        return bytesPerFrame;
    }

    public int getAudioFormat() {
        return audioFormat;
    }
}
