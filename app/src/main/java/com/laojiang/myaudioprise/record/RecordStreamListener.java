package com.laojiang.myaudioprise.record;

/**
 * 类介绍（必填）：获取录音的音频流,用于拓展的处理
 * Created by Jiang on 2018/7/16 .
 */

public interface RecordStreamListener {
    void recordOfByte(byte[] data,int begin,int end);
}
