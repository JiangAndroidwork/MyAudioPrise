package com.laojiang.myaudioprise.function;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 类介绍（必填）：混音评分
 * Created by Jiang on 2018/7/12 .
 */

public class MixRunnable implements Runnable {
    private Context context;

    /**
     * AudioRecord创建参数类
     *
     * @author christ
     */
    private static class RecorderParameter {
        // 音频获取源
        private static int audioSource = MediaRecorder.AudioSource.MIC;
        // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
        private static final int sampleRateInHz = 44100;
        // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
        private static final int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
        private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        // 缓冲区字节大小
        private static int bufferSizeInBytes;
    }

    // 设置运行状态
    private boolean isRunning = true;
    // AudioRecord对象
    private static AudioRecord recorder;
    // 设置MediaPlayer对象
    private static MediaPlayer mediaPlayer;
    // 伴奏文件
    private FileInputStream accompany;
    // 原唱文件
    private FileInputStream original;
    // 得分
    private int score;
    private boolean isFirst = true;

    /**
     * 混音评分线程的构造方法
     *
     * @param accompany
     *                ：伴奏文件路径
     * @param original
     *                ：原唱文件路径
     * @throws FileNotFoundException
     */
    public MixRunnable(Context context, String accompany, String original) throws FileNotFoundException {
        this.context = context;
        this.accompany = new FileInputStream(accompany);
        this.original = new FileInputStream(original);
        creatAudioRecord();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void run() {
        try {
            // MediaPlayer准备

            mediaPlayer.reset();
//            mediaPlayer.setDataSource("/sdcard/111.wav");
             mediaPlayer.setDataSource(accompany.getFD());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    isRunning = false;
                }
            });
            mediaPlayer.prepare();
            // 跳过头
            accompany.read(new byte[44]);
            original.read(new byte[44]);
            String s = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "playerTest" + File.separator + "oneOut.raw";
            FileOutputStream fos = new FileOutputStream(new File(s));
            // 开始读
            byte[] sourceReader = new byte[RecorderParameter.bufferSizeInBytes * 2];
            short[] sourceShortArray;
            short[] audioReader = new short[sourceReader.length / 4];
            mediaPlayer.start();
            recorder.startRecording();
            while (isRunning) {
                int sourceReadSize = accompany.read(sourceReader, 0, sourceReader.length);
                if (sourceReadSize < 0) {
                    isRunning = false;
                    continue;
                }
                sourceShortArray = byteToShortArray(sourceReader, sourceReadSize / 2);
                recorder.read(audioReader, 0, audioReader.length);
                short[] oneSecond = mixVoice(sourceShortArray, audioReader, sourceReadSize / 2);
                byte[] outStream = new byte[oneSecond.length * 2];
                for (int i = 0; i < oneSecond.length; i++) {
                    byte[] b = shortToByteArray(oneSecond[i]);
                    outStream[2 * i] = b[0];
                    outStream[2 * i + 1] = b[1];
                }
                Log.d("mtime4", "" + System.currentTimeMillis());
                fos.write(outStream);
                // 评分
                byte[] srcBuffer = new byte[outStream.length];
                original.read(srcBuffer);
                int x = score(byteToShortArray(srcBuffer, srcBuffer.length / 2), oneSecond);
                System.out.println("评分分数："+x);
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            recorder.stop();
            recorder.release();
            mediaPlayer.release();
            fos.close();
            String s1 = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "playerTest" + File.separator + "oneOut.raw";
            String s2 = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "playerTest" + File.separator + "twoOut.wav";
            copyWaveFile(s1, s2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建AudioRecord对象方法
     */
    private void creatAudioRecord() {
        // 获得缓冲区字节大小
        RecorderParameter.bufferSizeInBytes = AudioRecord.getMinBufferSize(RecorderParameter.sampleRateInHz,
                RecorderParameter.channelConfig, RecorderParameter.audioFormat) * 20;
        // 创建AudioRecord对象
        recorder = new AudioRecord(RecorderParameter.audioSource, RecorderParameter.sampleRateInHz,
                RecorderParameter.channelConfig, RecorderParameter.audioFormat,
                RecorderParameter.bufferSizeInBytes);
    }

    private short[] mixVoice(short[] source, short[] audio, int items) {
        short[] array = new short[items];
        for (int i = 0; i < items; i++) {
            array[i]= (short) ((source[i] + audio[i / 2]) / 2);
        }
        return array;
    }

    /**
     * byte数组转换成short数组
     *
     * @param data
     * @param items
     * @return
     */
    private short[] byteToShortArray(byte[] data, int items) {
        short[] retVal = new short[items];
        for (int i = 0; i < retVal.length; i++)
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);
        return retVal;
    }

    /**
     * short转byte数组
     *
     * @param s
     * @return
     */
    private byte[] shortToByteArray(short s) {
        byte[] shortBuf = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (shortBuf.length - 2 + i) * 8;
            shortBuf[i] = (byte) ((s >>> offset) & 0xff);
        }
        return shortBuf;
    }

    /**
     * <a href="\"http://www.eoeandroid.com/home.php?mod=space&uid=7300\"" target="\"_blank\"">@return</a> the recorder
     */
    public static AudioRecord getRecorder() {
        return recorder;
    }

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * 设置线程运行状态
     *
     * @param isRunning
     */
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     * 获取线程运行状态
     *
     * @return
     */
    public boolean IsRunning() {
        return isRunning;
    }

    public int getScore() {
        return score;
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RecorderParameter.sampleRateInHz;
        int channels = 2;
        long byteRate = 16 * RecorderParameter.sampleRateInHz * channels / 8;
        byte[] data = new byte[RecorderParameter.bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有 自己特有的头文件。
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen,
                                     long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    private int score(short[] src, short[] user) {
        int srcZ = 0, userZ = 0;
        boolean isAsc = false;
        boolean uisAsc = false;
        for (int i = 1; i < src.length; i++) {
            if (isAsc) {
                if (src[i - 1] > src[i]) {
                    isAsc = false;
                }
            } else {
                if (src[i - 1] < src[i]) {
                    isAsc = true;
                    srcZ += 1;
                }
            }
            if (uisAsc) {
                if (user[i - 1] > user[i]) {
                    uisAsc = false;
                }
            } else {
                if (user[i - 1] < user[i]) {
                    uisAsc = true;
                    userZ += 1;
                }
            }
        }
        return Math.abs(srcZ - userZ);
    }

}

