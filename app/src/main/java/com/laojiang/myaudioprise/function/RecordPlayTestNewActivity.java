package com.laojiang.myaudioprise.function;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.jhl.audiolibrary.common.CommonThreadPool;
import com.jhl.audiolibrary.tools.decode.AudioFunction;
import com.jhl.audiolibrary.tools.interfaces.ComposeAudioInterface;
import com.jhl.audiolibrary.tools.interfaces.DecodeOperateInterface;
import com.jhl.audiolibrary.tools.interfaces.VoicePlayerInterface;
import com.jhl.audiolibrary.tools.interfaces.VoiceRecorderOperateInterface;
import com.jhl.audiolibrary.tools.player.EarReturnPlayer;
import com.jhl.audiolibrary.tools.player.MyPlayThread;
import com.jhl.audiolibrary.tools.player.VoicePlayerEngine;
import com.jhl.audiolibrary.tools.recorder.RecorderEngine;
import com.jhl.audiolibrary.utils.Wav2PcmListener;
import com.laojiang.myaudioprise.R;
import com.laojiang.myaudioprise.content.Constant;
import com.laojiang.myaudioprise.player.MediaPlayListener;
import com.laojiang.myaudioprise.player.MyMediaplayer;
import com.laojiang.myaudioprise.record.AudioRecorder;
import com.laojiang.myaudioprise.util.AudioEncodeUtil;
import com.laojiang.myaudioprise.view.ProgressBarPopup;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * k歌
 */
public class RecordPlayTestNewActivity extends AppCompatActivity implements OnClickListener, VoiceRecorderOperateInterface, DecodeOperateInterface, VoicePlayerInterface, MediaPlayListener, ComposeAudioInterface, Wav2PcmListener {

    private static final String TAG = "RecordPlayActivity";
    /**
     */
    private Button bt_exit;
    /**
     */
    protected int inputBufferSize;
    /**
     */
    private AudioRecord audioRecord;
    /**
     */
    private byte[] inputBytes;
    /**
     */
    private LinkedList<byte[]> saveDataList;
    /**
     */
    private int outputBufferSize;
    /**
     * 播放流
     */
    private AudioTrack audioTrack;
    /**
     * 输出的字节
     */
    private byte[] outBytes;
    /**
     * 录制线程
     */
    private Thread recordThread;
    /**
     * 播放线程
     */
    private Thread playThread;
    /**
     * 是否录制
     */
    private boolean flag = true;
    private int sampleRateInHz = 44100;
    /**
     * 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
     */
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private String filePath = "";
    private Button btStart;
    private MediaPlayer mPlayer;
    private int bgtimetmp;
    private TextView bgTimeVew;
    private String bgtime;
    private Button btVolume;
    private MediaPlayer mPlayerTwo;
    private int bgtimetmpTwo;
    private String bgtimeTwo;
    private boolean prepareOne;
    private boolean prepareTwo;
    private Button btSoundChange;
    private boolean isYuansheng = false;//原声
    private int mAudioMinBufSize;
    private AudioTrack mAudioTrack;
    private double ret;
    private boolean mThreadFlag;
    private short[] audioBuffer;
    private Thread mThread;
    private MyPlayThread mBanzou;
    private View btMix;
    private ExecutorService executorService;
    private AudioRecorder audioRecorder;
    private Button btPause;
    private Button btErFan;
    private EarReturnPlayer earReturnPlayer;
    private MyMediaplayer myMediaplayer;
    private int recordTime;
    private boolean recordVoiceBegin;
    private int actualRecordTime;
    private LinkedList<byte[]> saveDataList1;
    private MyMediaplayer myMediaplayer1;
    private ExecutorService executorService1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        btStart = findViewById(R.id.bt_start);
        bgTimeVew = findViewById(R.id.tv_player_time);
        btVolume = findViewById(R.id.btn_volume);
        btSoundChange = findViewById(R.id.bt_bg_sound_change);
        btPause = findViewById(R.id.bt_pause);
        btErFan = findViewById(R.id.bt_erfan);
        btSoundChange.setEnabled(false);
        btErFan.setEnabled(false);
        btErFan.setOnClickListener(this);
        btPause.setOnClickListener(this);
        btSoundChange.setOnClickListener(this);
        btVolume.setOnClickListener(this);
        btStart.setOnClickListener(this);
        this.setTitle("耳返");

        init();


    }

    private static final int START_RECORD = 1;
    private static final int PLAY_ONE = 2;
    //
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_RECORD://开始录音
                    startRecoder();
                    btErFan.setEnabled(true);
                    break;
                case PLAY_ONE://开始播放原声

                    break;
            }
        }
    };


    public int writeCovertCount(int count) {
        Log.e(getClass().getName(), "writeCovertCount-->>" + count);
        return count;
    }

    private void init() {
        executorService = Executors.newSingleThreadExecutor();
        //文件保存路径
        filePath = Constant.getLocalPath();
        bt_exit = (Button) findViewById(R.id.bt_yinpinhuilu_testing_exit);

        bt_exit.setOnClickListener(this);
        audioRecorder = AudioRecorder.getInstance();


    }


    /**
     * 录音
     */


    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.bt_yinpinhuilu_testing_exit://结束
                stopAll();
                btStart.setText("开始录音");
                btPause.setText("暂停录音");
                btPause.setVisibility(View.GONE);
                stopPlayerTwo();

//
                break;
            case R.id.bt_start://开始
                File fileWav = new File(Constant.getLocalPath() + "record/mix.mp3");
                File filepcm = new File(Constant.getLocalPath() + "recordpcm/");
                if (fileWav.exists()) {
                    fileWav.delete();
                }
                filepcm.delete();
//                initPlayer();
//                startRecoder();
                startPlayerTwo();
                break;
            case R.id.btn_volume://音量
                int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                btVolume.measure(w, h);
                int height = btVolume.getMeasuredHeight();
                int width = btVolume.getMeasuredWidth();

                ProgressBarPopup progressBarPopup = new ProgressBarPopup(this);
                progressBarPopup.showAtLocation(btVolume, Gravity.CENTER, width / 3, height + (height));
                break;
            case R.id.bt_bg_sound_change://原声伴奏切换
                changeBgSound();
                break;

            case R.id.bt_pause:
                if (btPause.getText().equals("暂停录音")) {
                    RecorderEngine.getInstance().pauseRecordVoice();
                    mBanzou.pause();
                    VoicePlayerEngine.getInstance().pause();
                    btPause.setText("开始录音");
                } else {
                    RecorderEngine.getInstance().startRecordVoice(this);
                    mBanzou.play();
                    VoicePlayerEngine.getInstance().reStart();
                    btPause.setText("暂停录音");
                }
                break;
            case R.id.bt_erfan:
                if (btErFan.getText().equals("没有")) {
                    earReturnPlayer.setNoSound();
                    btErFan.setText("有");
                } else {
                    earReturnPlayer.setHaveSound();
                    btErFan.setText("没有");
                }
                break;
        }
    }

    private void stopAll() {
        if (mBanzou != null)
            mBanzou.stopp();
        VoicePlayerEngine.Destroy();
        if (earReturnPlayer != null)
            earReturnPlayer.stopErReturn();

        stopPlayerTwo();
    }

    //伴奏原声切换
    private void changeBgSound() {
        if (isYuansheng) {
            VoicePlayerEngine.getInstance().setVolume(0);

            if (mBanzou != null && mBanzou.isAlive()) {
                mBanzou.setHaveSound();
            }
            isYuansheng = false;
            btSoundChange.setText("伴奏");
        } else {
            VoicePlayerEngine.getInstance().setVolume(1);
            if (mBanzou != null && mBanzou.isAlive()) {
                mBanzou.setNoSound();
            }
            isYuansheng = true;
            btSoundChange.setText("原声");
        }
    }

    private void startPlayerTwo() {
        //开始解码 伴奏
        AudioFunction.DecodeMusicFile(Constant.banzou2, Constant.banzouPcm, 0,
                (3 * 60 + 31), this);

    }

    private void prepareMedia(String yuansheng) {

    }

    private void stopPlayerOne() {

    }

    private void stopPlayerTwo() {

        RecorderEngine.getInstance().stopRecordVoice();

    }

    private void startRecoder() {
        RecorderEngine.getInstance().readyRecord("mySound");
        RecorderEngine.getInstance().startRecordVoice(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RecorderEngine.getInstance().onDestoryRecorder();
        earReturnPlayer.stopErReturn();
        VoicePlayerEngine.Destroy();
        mBanzou.stopp();
    }

    //=======================================录制监听事件==================================================================================
    @Override
    public void recordVoiceBegin() {
        recordVoiceBegin = true;


    }

    @Override
    public void recordVoiceStateChanged(int volume, long recordDuration) {
        Log.i(TAG, "正在录音==" + volume + "\n" + recordDuration);
        if (recordDuration > 0) {
            recordTime = (int) (recordDuration / Constant.OneSecond);

        }
    }

    @Override
    public void prepareGiveUpRecordVoice() {

    }

    @Override
    public void recoverRecordVoice() {

    }

    @Override
    public void giveUpRecordVoice() {

    }

    @Override
    public void recordVoiceFail() {

    }

    @Override
    public void recordVoiceFinish() {
        Log.i(TAG, "录音完成");
        if (recordVoiceBegin) {
            actualRecordTime = recordTime;

        }

        File file = new File(Constant.getPCM() + "source.pcm");
        if (file.exists()) {
            file.delete();
        }
        AudioEncodeUtil.convertWav2Pcm(Constant.getLocalPath() + "wav/mySound.wav", Constant.getPCM() + "mysource.pcm", RecordPlayTestNewActivity.this);

    }

    //=======================================解码监听===========================
    @Override
    public void updateDecodeProgress(int decodeProgress) {
        Log.i(TAG, "正在解码==" + decodeProgress);
    }

    @Override
    public void decodeSuccess() {
        Log.i(TAG, "解码成功了");
        //开始录音
        startRecoder();
        //耳返声音
        earReturnPlayer = new EarReturnPlayer();
        CommonThreadPool.getThreadPool().addFixedTask(earReturnPlayer);
        earReturnPlayer.setNoSound();
        //开始播放背景音乐
        mBanzou = new MyPlayThread(this, Constant.banzouPcm);
        mBanzou.setChannel(true, true);
        //
        btSoundChange.setEnabled(true);
        //播放原唱
        VoicePlayerEngine.getInstance().playVoice(Constant.yuansheng, this);

    }

    @Override
    public void decodeFail() {
        Log.i(TAG, "解码失败了");
    }

    //========================================播放原唱==================
    @Override
    public void playVoiceBegin() {
        mBanzou.start();
        Log.i(TAG, "开始播放原唱");
        VoicePlayerEngine.getInstance().setVolume(0f);
        btPause.setVisibility(View.VISIBLE);
        btErFan.setEnabled(true);
        btStart.setText("开始录音");
        btPause.setText("暂停录音");

        //耳返开始有声音
        earReturnPlayer.setHaveSound();
    }

    @Override
    public void playVoiceFail() {

    }

    @Override
    public void playVoiceFinish() {
        //播放完成
    }

    //=====================================播放器监听=================暂时不用
    @Override
    public void onPrepare() {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    //===================================pcm文件合成===========================
    @Override
    public void updateComposeProgress(int composeProgress) {
        Log.i(TAG, "pcm合成进度==" + composeProgress);
    }

    @Override
    public void composeSuccess() {
        Log.i(TAG, "pcm合成成功了");
    }

    @Override
    public void composeFail() {

    }

    //===================================wav转pcm监听
    @Override
    public void wav2PcmSucess() {
        MixPcmActivity.start(this);
    }

    @Override
    public void wav2PcmFail() {

    }
}