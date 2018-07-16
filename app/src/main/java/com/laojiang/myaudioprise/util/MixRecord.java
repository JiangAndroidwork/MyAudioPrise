package com.laojiang.myaudioprise.util;

/**
 * 类介绍（必填）：
 * Created by Jiang on 2018/7/13 .
 */

public class MixRecord {
    public native int decodeMp3ToPCM2(String mp3File, String mp3PCM);

    public native int mix2PCMToPCM(String sourcePCM, String mp3PCM, String mixPCM);
}
