package com.myaudio.mange;

/**
 * 类介绍（必填）：解码和 合成
 * Created by Jiang on 2018/7/13 .
 */

public class AudioManager {
    static {
        System.loadLibrary("mad");
    }
    //mp3转pcm
    public static native int decodeMp3ToPCM2(String mp3File, String mp3PCM);
    //两个pcm合成一个
    public static native int mix2PCMToPCM2(String sourcePCM, String mp3PCM, String mixPCM);





}
