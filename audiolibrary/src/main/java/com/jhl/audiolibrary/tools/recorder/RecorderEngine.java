package com.jhl.audiolibrary.tools.recorder;

import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import com.jhl.audiolibrary.Constant;
import com.jhl.audiolibrary.tools.interfaces.VoiceRecorderOperateInterface;
import com.jhl.audiolibrary.tools.recorder.thread.AudioRecorder;
import com.jhl.audiolibrary.utils.FileFunction;

import java.util.LinkedList;

/**
 * 类介绍（必填）：录音
 * Created by Jiang on 2018/7/22 .
 */

public class RecorderEngine {
    private final AudioRecorder recorder;
    private boolean recording;

    private final int sampleDuration = 500;// 间隔取样时间

    private long recordStartTime;
    private long recordDuration;

    private String recordFileUrl;

    private VoiceRecorderOperateInterface voiceRecorderInterface;

    private AudioManager audioManager;

    private Handler handler;


    //    private static NativeRecorder recorder;

    private static RecorderEngine instance;
    private String fileName;

    private RecorderEngine() {

        handler = new Handler();

        //        recorder = new NativeRecorder();

        recorder = new AudioRecorder();
    }

    public static RecorderEngine getInstance() {
        if (instance == null) {
            synchronized (RecorderEngine.class) {
                if (instance == null) {
                    instance = new RecorderEngine();
                }
            }
        }

        return instance;
    }


    public boolean IsRecording() {
        return recording;
    }

    public void readyRecord(String fileName) {
        recorder.initAudioRecord(fileName);
    }

    private boolean startRecordVoice() {

        return recorder.startRecordVoice();
    }

    public void startRecordVoice(
            VoiceRecorderOperateInterface voiceRecorderOperateInterface) {
        stopRecordVoice();

        recordDuration = 0;

        recording = startRecordVoice();

        if (recording) {
            this.voiceRecorderInterface = voiceRecorderOperateInterface;

            recordStartTime = System.currentTimeMillis();

            updateMicStatus();

            if (voiceRecorderOperateInterface != null) {
                voiceRecorderOperateInterface.recordVoiceBegin();
            }
        } else {

            if (voiceRecorderOperateInterface != null) {
                voiceRecorderOperateInterface.recordVoiceFail();
            }
        }
    }

    //暂停录音
    public void pauseRecordVoice() {
        recording = false;
        recorder.pauseRecord();
    }

    public void stopRecordVoice() {
        if (recording) {
            boolean recordVoiceSuccess = recorder.stopRecordVoice();
            long recordDuration = System.currentTimeMillis() - recordStartTime;

            recording = false;

            if (recordDuration < Constant.OneSecond) {
                recordVoiceSuccess = false;
            }

            if (!recordVoiceSuccess) {
                Log.i("录音太短", "不行滴");
                if (voiceRecorderInterface != null) {
                    voiceRecorderInterface.recordVoiceFail();
                }

                FileFunction.DeleteFile(recordFileUrl);
                return;
            }

            if (voiceRecorderInterface != null) {
                voiceRecorderInterface.recordVoiceFinish();
            }
        }
    }

    //获取录音数据==
    public LinkedList<byte[]> getSaveDataList() {
        return recorder.getSaveDataList();
    }

    public void onDestoryRecorder() {
        recorder.onDestory();
    }

    public void giveUpRecordVoice(boolean fromHand) {
        if (recording) {
            boolean stopRecordSuccess = recorder.stopRecordVoice();

            recording = false;

            if (stopRecordSuccess) {
                if (voiceRecorderInterface != null) {
                    voiceRecorderInterface.recordVoiceFinish();
                }
            } else {
                if (voiceRecorderInterface != null) {
                    voiceRecorderInterface.recordVoiceFail();
                }
            }

            FileFunction.DeleteFile(recordFileUrl);

            if (voiceRecorderInterface != null) {
                voiceRecorderInterface.giveUpRecordVoice();
            }
        }
    }

    public void prepareGiveUpRecordVoice(boolean fromHand) {
        if (voiceRecorderInterface != null) {
            voiceRecorderInterface.prepareGiveUpRecordVoice();
        }
    }

    public void recoverRecordVoice(boolean fromHand) {
        if (voiceRecorderInterface != null) {
            voiceRecorderInterface.recoverRecordVoice();
        }
    }

    public void recordVoiceStateChanged(int volume) {
        if (voiceRecorderInterface != null) {
            voiceRecorderInterface.recordVoiceStateChanged(volume, recordDuration);
        }
    }

    private void updateMicStatus() {
        int volume = recorder.getVolume();

        recordVoiceStateChanged(volume);

        handler.postDelayed(updateMicStatusThread, sampleDuration);
    }

    private Runnable updateMicStatusThread = new Runnable() {
        public void run() {
            if (recording) {
                // 判断是否超时
                recordDuration = System.currentTimeMillis() - recordStartTime;

                updateMicStatus();
            }
        }
    };
}
