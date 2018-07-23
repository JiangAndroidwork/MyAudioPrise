package com.jhl.audiolibrary.tools.recorder.thread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import com.jhl.audiolibrary.Constant;
import com.jhl.audiolibrary.Variable;
import com.jhl.audiolibrary.common.CommonFunction;
import com.jhl.audiolibrary.common.CommonThreadPool;
import com.jhl.audiolibrary.tools.recorder.PCMFormat;
import com.jhl.audiolibrary.utils.AudioEncodeUtil;
import com.jhl.audiolibrary.utils.FileFunction;
import com.jhl.audiolibrary.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 类介绍（必填）：录音  有暂停继续功能 最终由多个pcm合成一个wav。
 * Created by Jiang on 2018/7/22 .
 */

public class AudioRecorder {

    private final static int sampleDuration = 100;

    private static final int recordSleepDuration = 500;

    //自定义 每160帧作为一个周期，通知一下需要进行编码
    private static final int FRAME_COUNT = 160;

    private int realSampleDuration;
    private static final PCMFormat pcmFormat = PCMFormat.PCM_16BIT;

    private short[] audioRecordBuffer;
    private int bufferSizeInBytes;
    private int realSampleNumberInOneDuration;
    private AudioRecord audioRecord;
    private LinkedList<byte[]> saveDataList;
    private Status status;
    //录音文件
    private List<String> filesName = new ArrayList<>();
    //wav文件名称
    private String fileName;
    private byte[] bytes_pkg;
    private BufferedOutputStream bufferedOutputStream;
    private int amplitude;

    public AudioRecorder() {
        init();
    }

    private void init() {

        //存储字节 用于边录边播
        saveDataList = new LinkedList<byte[]>();

    }

    public void initAudioRecord(String fileName) {
        filesName.clear();
        this.fileName = fileName;
        status = Status.STATUS_READY;
        int audioRecordMinBufferSize = AudioRecord
                .getMinBufferSize(Constant.RecordSampleRate, AudioFormat.CHANNEL_IN_MONO,//CHANNEL_CONFIGURATION_MONO  CHANNEL_IN_MONO
                        pcmFormat.getAudioFormat());

        bufferSizeInBytes =
                Constant.RecordSampleRate * pcmFormat.getBytesPerFrame() / (1000 / sampleDuration);

        if (audioRecordMinBufferSize > bufferSizeInBytes) {
            bufferSizeInBytes = audioRecordMinBufferSize;
        }

    /*
     * 使能被整除，方便下面的周期性通知
     * */
        int bytesPerFrame = pcmFormat.getBytesPerFrame();
        int frameSize = bufferSizeInBytes / bytesPerFrame;

        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            bufferSizeInBytes = frameSize * bytesPerFrame;
        }

        audioRecordBuffer = new short[bufferSizeInBytes];

        double sampleNumberInOneMicrosecond = (double) Constant.RecordSampleRate / 1000;

        realSampleDuration = bufferSizeInBytes * 1000 /
                (Constant.RecordSampleRate * pcmFormat.getBytesPerFrame());

        realSampleNumberInOneDuration = (int) (sampleNumberInOneMicrosecond * realSampleDuration);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Constant.RecordSampleRate,
                AudioFormat.CHANNEL_IN_MONO, pcmFormat.getAudioFormat(), bufferSizeInBytes);
    }

    /**
     * 暂停录音
     */
    public void pauseRecord() {
        Log.d("AudioRecorder", "===pauseRecord===");
        if (status != Status.STATUS_START) {
            throw new IllegalStateException("没有在录音");
        } else {
            audioRecord.stop();
            status = Status.STATUS_PAUSE;
        }
    }

    /**
     * 停止录音
     */
    public boolean stopRecordVoice() {
        Log.d("AudioRecorder", "===stopRecord===");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            onDestory();
            release();
        }
        return true;
    }

    public void onDestory() {
        if (audioRecord != null) {
            if (status == Status.STATUS_START) {
                audioRecord.stop();
            }
            status = Status.STATUS_STOP;
            audioRecord.release();
            audioRecord = null;
        }


    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d("AudioRecorder", "===release===");
        //假如有暂停录音
        try {
            if (filesName.size() > 0) {
                List<String> filePaths = new ArrayList<>();
                for (String fileName : filesName) {
                    filePaths.add(FileUtils.getPcmFileAbsolutePath(fileName));
                }
                //清除
                filesName.clear();
                saveDataList.clear();
                //将多个pcm文件转化为wav文件
                mergePCMFilesToWAVFile(filePaths);

            } else {
                //这里由于只要录音过filesName.size都会大于0,没录音时fileName为null
                //会报空指针 NullPointerException
                // 将单个pcm文件转化为wav文件
                //Log.d("AudioRecorder", "=====makePCMFileToWAVFile======");
                //makePCMFileToWAVFile();
            }
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }

    /**
     * 取消录音
     */
    public void canel() {
        filesName.clear();
        fileName = null;
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }

    public int getVolume() {
        int volume = (int) (Math.sqrt(amplitude)) * Constant.RecordVolumeMaxRank / 60;
        return volume;
    }

    /**
     * 开始录音
     */
    public boolean startRecordVoice() {

        if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(fileName)) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (status == Status.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }
        Log.d("AudioRecorder", "===startRecord===" + audioRecord.getState());


        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                writeDataTOFile();
            }
        };
        CommonThreadPool.getThreadPool().addCachedTask(runnable);


        return true;
    }

    private void writeDataTOFile() {


        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];
        int sizeInBytes = bufferSizeInBytes;
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        int readsize = 0;
        try {
            audioRecord.startRecording();
            String currentFileName = fileName;
            if (status == Status.STATUS_PAUSE) {
                //假如是暂停录音 将文件名后面加个数字,防止重名文件内容被覆盖
                currentFileName += filesName.size();
            }
            filesName.add(currentFileName);
//            File file = new File(FileUtils.getPcmFileAbsolutePath(currentFileName));
//            if (file.exists()) {
//                file.delete();
//            }
            bufferedOutputStream = FileFunction
                    .GetBufferedOutputStreamFromFile(FileUtils.getPcmFileAbsolutePath(currentFileName));
//            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
//            dos = new DataOutputStream(new BufferedOutputStream(fos));
        } catch (IllegalStateException e) {
            Log.e("AudioRecorder", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        if (saveDataList != null) {
            saveDataList.clear();
        } else {
            saveDataList = new LinkedList<>();
        }
        //将录音状态设置成正在录音状态
        status = Status.STATUS_START;
        while (status == Status.STATUS_START) {

            readsize = audioRecord.read(audioRecordBuffer, 0, sizeInBytes);

            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                if (readsize > 0) {
                    calculateRealVolume(audioRecordBuffer, readsize);
                    if (bufferedOutputStream != null) {
                        //pcm写入文件
                        byte[] outputByteArray = CommonFunction
                                .GetByteBuffer(audioRecordBuffer,
                                        readsize, Variable.isBigEnding);
                        try {
                            bufferedOutputStream.write(outputByteArray);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //用于边录边播
                        bytes_pkg = outputByteArray.clone();
                        if (saveDataList.size() >= 2) {
                            saveDataList.removeFirst();
                        }
                        saveDataList.add(bytes_pkg);
                    }
                }
            }
        }
        try {
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
        } catch (IOException e) {
            Log.e("AudioRecorder", e.getMessage());
        }
    }

    /**
     * 此计算方法来自samsung开发范例
     *
     * @param buffer   buffer
     * @param readSize readSize
     */
    private void calculateRealVolume(short[] buffer, int readSize) {
        int sum = 0;

        for (int index = 0; index < readSize; index++) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += Math.abs(buffer[index]);
        }

        if (readSize > 0) {
            amplitude = sum / readSize;
        }
    }

    /**
     * 将pcm合并成wav
     *
     * @param filePaths
     */
    private void mergePCMFilesToWAVFile(final List<String> filePaths) {
                if (AudioEncodeUtil.mergePCMFilesToWAVFile(filePaths, FileUtils.getWavFileAbsolutePath(fileName))) {
                    //操作成功
                    filePaths.clear();
                } else {
                    //操作失败
                    Log.e("AudioRecorder", "mergePCMFilesToWAVFile fail");
                    throw new IllegalStateException("mergePCMFilesToWAVFile fail");
                }
        fileName = null;
    }

    //用于边录边播
    public LinkedList<byte[]> getSaveDataList() {
        if (saveDataList == null) saveDataList = new LinkedList<>();
        return saveDataList;
    }

    /**
     * 录音对象的状态
     */
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }
}
