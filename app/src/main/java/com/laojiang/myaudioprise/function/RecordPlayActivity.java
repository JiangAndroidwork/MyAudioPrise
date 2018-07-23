//package com.laojiang.myaudioprise.function;
//
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.AudioTrack;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.laojiang.myaudioprise.R;
//import com.laojiang.myaudioprise.content.Constant;
//import com.jhl.audiolibrary.tools.player.EarReturnPlayer;
//import com.laojiang.myaudioprise.player.MediaPlayListener;
//import com.laojiang.myaudioprise.player.MyMediaplayer;
//import com.laojiang.myaudioprise.record.AudioRecorder;
//import com.laojiang.myaudioprise.util.AudioEncodeUtil;
//import com.laojiang.myaudioprise.view.ProgressBarPopup;
//import com.myaudio.mange.SimpleLame;
//
//import java.io.File;
//import java.util.LinkedList;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * k歌
// */
//public class RecordPlayActivity extends AppCompatActivity implements OnClickListener, MediaPlayListener {
//
//    private static final String TAG = "RecordPlayActivity";
//    /**
//     */
//    private Button bt_exit;
//    /**
//     */
//    protected int inputBufferSize;
//    /**
//     */
//    private AudioRecord audioRecord;
//    /**
//     */
//    private byte[] inputBytes;
//    /**
//     */
//    private LinkedList<byte[]> saveDataList;
//    /**
//     */
//    private int outputBufferSize;
//    /**
//     * 播放流
//     */
//    private AudioTrack audioTrack;
//    /**
//     * 输出的字节
//     */
//    private byte[] outBytes;
//    /**
//     * 录制线程
//     */
//    private Thread recordThread;
//    /**
//     * 播放线程
//     */
//    private Thread playThread;
//    /**
//     * 是否录制
//     */
//    private boolean flag = true;
//    private int sampleRateInHz = 44100;
//    /**
//     * 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
//     */
//    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
//    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
//    private String filePath = "";
//    private Button btStart;
//    private MediaPlayer mPlayer;
//    private int bgtimetmp;
//    private TextView bgTimeVew;
//    private String bgtime;
//    private Button btVolume;
//    private MediaPlayer mPlayerTwo;
//    private int bgtimetmpTwo;
//    private String bgtimeTwo;
//    private boolean prepareOne;
//    private boolean prepareTwo;
//    private Button btSoundChange;
//    private boolean isYuansheng = true;//原声
//    private int mAudioMinBufSize;
//    private AudioTrack mAudioTrack;
//    private double ret;
//    private boolean mThreadFlag;
//    private short[] audioBuffer;
//    private Thread mThread;
//    private PlayThread mBanzou;
//    private PlayThread playTwo;
//    private View btMix;
//    private ExecutorService executorService;
//    private AudioRecorder audioRecorder;
//    private Button btPause;
//    private Button btErFan;
//    private EarReturnPlayer earReturnPlayer;
//    private MyMediaplayer myMediaplayer;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_player);
//        btStart = findViewById(R.id.bt_start);
//        bgTimeVew = findViewById(R.id.tv_player_time);
//        btVolume = findViewById(R.id.btn_volume);
//        btSoundChange = findViewById(R.id.bt_bg_sound_change);
//        btMix = findViewById(R.id.btn_mix);
//        btPause = findViewById(R.id.bt_pause);
//        btErFan = findViewById(R.id.bt_erfan);
//        btSoundChange.setEnabled(false);
//        btErFan.setEnabled(false);
//        btErFan.setOnClickListener(this);
//        btMix.setOnClickListener(this);
//        btPause.setOnClickListener(this);
//        btSoundChange.setOnClickListener(this);
//        btVolume.setOnClickListener(this);
//        btStart.setOnClickListener(this);
//        this.setTitle("耳返");
//
//
//        init();
//
//
//    }
//
//    private static final int START_RECORD = 1;
//    private static final int PLAY_ONE = 2;
//    //
//    private Handler handler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case START_RECORD://开始录音
//                    startRecoder();
//                    btErFan.setEnabled(true);
//                    break;
//                case PLAY_ONE://开始播放原声
//
//                    break;
//            }
//        }
//    };
//
//
//    public int writeCovertCount(int count) {
//        Log.e(getClass().getName(), "writeCovertCount-->>" + count);
//        return count;
//    }
//
//    private void init() {
//        //文件保存路径
//        filePath = Constant.getLocalPath();
//        bt_exit = (Button) findViewById(R.id.bt_yinpinhuilu_testing_exit);
//
//        bt_exit.setOnClickListener(this);
//        audioRecorder = AudioRecorder.getInstance();
//
//        executorService = Executors.newFixedThreadPool(5);
//
//
//    }
//
//
//    /**
//     * 录音
//     */
//
//
//    public void onClick(View v) {
//        // TODO Auto-generated method stub
//        switch (v.getId()) {
//
//            case R.id.bt_yinpinhuilu_testing_exit://结束
//                earReturnPlayer.stopErReturn();
//                myMediaplayer.stopPlay();
//
//                stopPlayerTwo();
//                audioRecorder.stopRecord();
//                btStart.setText("开始录音");
//                btPause.setText("暂停录音");
//                btPause.setVisibility(View.GONE);
//                break;
//            case R.id.bt_start://开始
////                initPlayer();
////                startRecoder();
//                startPlayerTwo();
//                break;
//            case R.id.btn_volume://音量
//                int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//                int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//                btVolume.measure(w, h);
//                int height = btVolume.getMeasuredHeight();
//                int width = btVolume.getMeasuredWidth();
//
//                ProgressBarPopup progressBarPopup = new ProgressBarPopup(this);
//                progressBarPopup.showAtLocation(btVolume, Gravity.CENTER, width / 3, height + (height));
//                break;
//            case R.id.bt_bg_sound_change://原声伴奏切换
//                changeBgSound();
//                break;
//            case R.id.btn_mix://合成
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        AudioEncodeUtil.convertWav2Pcm(Constant.getLocalPath()+"wav/source.wav", Constant.getPCM() + "source.pcm");
//                        if (new File(Constant.getPCM() + "source.pcm").exists()) {
//                            int r = com.myaudio.mange.AudioManager.mix2PCMToPCM(Constant.getPCM() + "source.pcm", Constant.getPCM() + "空空如也(伴奏).pcm", filePath + "pcm/mix.pcm");
//
//                            if (r == 0) {
//                                int bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
////													String fileName = ""+DateFormat.format("yyyyMMddhmmss", System.currentTimeMillis());
//                                int rs = SimpleLame.convert(filePath + "pcm/mix.pcm", filePath + "record/" + "mymusic.mp3", bufferSize);
//                                System.out.println("rs:" + rs);
//                                if (rs == 0) {
//                                    prepareMedia(filePath + "record/" + "mymusic.mp3");
//                                    myMediaplayer.startPlay();
//
////                                PlayThread playThread = new PlayThread(RecordPlayActivity.this, filePath + "record/" + "光明.mp3");
////                                playThread.setChannel(true,true);
////                                playThread.start();
//                                }
//                            }
//                        }
//                    }
//                }).start();
//                break;
//            case R.id.bt_pause:
//                try {
//                    if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
//                        //暂停录音
//                        audioRecorder.pauseRecord();
//                        btPause.setText("继续录音");
//                        break;
//
//                    } else {
//                        audioRecorder.startRecord(null);
//                        btPause.setText("暂停录音");
//                    }
//                } catch (IllegalStateException e) {
//                    Toast.makeText(RecordPlayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case R.id.bt_erfan:
//                if (btErFan.getText().equals("没有")) {
//                    earReturnPlayer.setNoSound();
//                    btErFan.setText("有");
//                } else {
//                    earReturnPlayer.setHaveSound();
//                    btErFan.setText("没有");
//                }
//                break;
//        }
//    }
//
//    //伴奏原声切换
//    private void changeBgSound() {
//        if (isYuansheng) {
//            myMediaplayer.setVolume(1);
//
//            if (mBanzou != null && mBanzou.isAlive()) {
//                mBanzou.setNoSound();
//            }
//            isYuansheng = false;
//            btSoundChange.setText("原声");
//        } else {
//            if (isYuansheng) {
//                myMediaplayer.setVolume(0);
//            }
//            if (mBanzou != null && mBanzou.isAlive()) {
//                mBanzou.setHaveSound();
//            }
//            isYuansheng = true;
//            btSoundChange.setText("伴奏");
//        }
//    }
//
//    private void startPlayerTwo() {
//        prepareMedia(Constant.yuansheng);
//        File file1 = new File(Constant.banzou2);
//
//        if (file1.exists()) {
//            String filePath = file1.getAbsolutePath();
//            final String pcmPath = Constant.getPCM() + "空空如也(伴奏).pcm";
//            Mp3ToPcmUtil decodeMp3 = new Mp3ToPcmUtil(this, filePath, pcmPath);
//            decodeMp3.addDecodeListener(new Mp3ToPcmUtil.DeCodeListener() {
//                @Override
//                public void decodeSuccess(String pcmPath) {
//                    Log.i("解码结束了===", pcmPath);
////                    prepareTwo = true;
//
//                    mBanzou = new PlayThread(RecordPlayActivity.this, pcmPath);
//                    mBanzou.setChannel(true, true);
//                    mBanzou.start();
//                    myMediaplayer.startPlay();
//                    handler.sendEmptyMessage(PLAY_ONE);
//                    handler.sendEmptyMessage(START_RECORD);
//
//
//                }
//            });
//        }
//    }
//
//    private void prepareMedia(String yuansheng) {
//        myMediaplayer = new MyMediaplayer(this,yuansheng );
//        myMediaplayer.setMediaPlayerListener(this);
//        executorService.execute(myMediaplayer);
//    }
//
//    private void stopPlayerOne() {
//        if (mPlayer != null && mPlayer.isPlaying()) {
//            mPlayer.stop();
//
//        }
//    }
//
//    private void stopPlayerTwo() {
//
//        if (mBanzou != null) {
//            mBanzou.stopp();
//        }
//    }
//
//
//
//
//
//
//    private void startRecoder() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                btSoundChange.setEnabled(true);
//            }
//        });
//
//        try {
//            if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_NO_READY) {
//                //初始化录音
//                String fileName = "source";
//                audioRecorder.createDefaultAudio(fileName);
//                audioRecorder.startRecord(null);
//                btPause.setVisibility(View.VISIBLE);
//            }
//
//        } catch (IllegalStateException e) {
//            Toast.makeText(RecordPlayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
////        executorService.execute(new recordSound());
//        earReturnPlayer = new EarReturnPlayer();
//        executorService.execute(earReturnPlayer);
////        recordThread = new Thread(new recordSound());
////        playThread = new Thread(new playRecord());
////        recordThread.start();
////        playThread.start();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        audioRecorder.release();
//    }
//
//    @Override
//    public void onPrepare() {
//
//    }
//
//    @Override
//    public void onComplete() {
//        stopPlayerTwo();
//        earReturnPlayer.stopErReturn();
//        audioRecorder.stopRecord();
//        btStart.setText("开始录音");
//        btPause.setText("暂停录音");
//        btPause.setVisibility(View.GONE);
//
//    }
//
//    @Override
//    public void onError() {
//
//    }
//
//    @Override
//    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//
//    }
//}