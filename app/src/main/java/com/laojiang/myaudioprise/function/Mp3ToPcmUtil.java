package com.laojiang.myaudioprise.function;

import android.content.Context;

import com.myaudio.mange.AudioManager;

/**
 * 类介绍（必填）：
 * Created by Jiang on 2018/7/12 .
 */

public class Mp3ToPcmUtil {
    static {
        System.loadLibrary("mad");
    }
    private Context context;
    private String filepath;
    private String pcmPath;
    private DeCodeListener listener;


    public Mp3ToPcmUtil(Context context, String filepath, String pcmPath) {
        this.context = context;
        this.filepath = filepath;
        this.pcmPath = pcmPath;
    }

    private void init() {
        new Thread() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                super.run();
//                int decode= ((RecordPlayActivity)context).decodeMp3ToPCM2(filepath, pcmPath);
                int decode = AudioManager.decodeMp3ToPCM2(filepath, pcmPath);
                if (decode == 0) {
                    if (listener != null)
                        listener.decodeSuccess();
                }
            }

        }.start();
    }

    //添加监听
    public void addDecodeListener(DeCodeListener listener) {
        this.listener = listener;
        init();
    }

    public interface DeCodeListener {
        void decodeSuccess();
    }

}
