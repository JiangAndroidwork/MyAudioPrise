package com.laojiang.myaudioprise.function;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
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

import com.laojiang.myaudioprise.R;
import com.laojiang.myaudioprise.content.Constants;
import com.laojiang.myaudioprise.view.ProgressBarPopup;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * k歌
 */
public class RecordPlayActivity extends AppCompatActivity implements OnClickListener {

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
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private String filePath = "";
    private View btStart;
    private MediaPlayer mPlayer;
    private int bgtimetmp;
    private TextView bgTimeVew;
    private String bgtime;
    private View btVolume;
    private MediaPlayer mPlayerTwo;
    private int bgtimetmpTwo;
    private String bgtimeTwo;
    private boolean prepareOne;
    private boolean prepareTwo;
    private Button btSoundChange;
    private boolean isYuansheng = true;//原声
    private int mAudioMinBufSize;
    private AudioTrack mAudioTrack;
    private double ret;
    private boolean mThreadFlag;
    private short[] audioBuffer;
    private Thread mThread;
    private PlayThread mYuanSheng;
    private PlayThread playTwo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        btStart = findViewById(R.id.bt_start);
        bgTimeVew = findViewById(R.id.tv_player_time);
        btVolume = findViewById(R.id.btn_volume);
        btSoundChange = findViewById(R.id.bt_bg_sound_change);
        btSoundChange.setEnabled(false);
        btSoundChange.setOnClickListener(this);
        btVolume.setOnClickListener(this);
        btStart.setOnClickListener(this);
        this.setTitle("耳返");


        init();


    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1://开始录音
                    startRecoder();
                    break;
            }
        }
    };

    //    public native int decodeMp3ToPCM2(String mp3File, String mp3PCM);
    private void startPlayerTwo() {
//        playTwo = new PlayThread(this, "source.pcm");
//        playTwo.setChannel(true, true);
//        playTwo.start();
        String file = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "playerTest" + File.separator + "空空如也.pcm";
        File filePlay = new File(file);
        Log.i("开始播放",filePlay.exists()+"");
        mYuanSheng = new PlayThread(RecordPlayActivity.this,file );
        mYuanSheng.setChannel(true, true);
        mYuanSheng.play();
//        File file1 = new File(Constants.yuansheng);
//        if (file1.exists()) {
//            Mp3ToPcmUtil decodeMp3 = new Mp3ToPcmUtil(this, Constants.yuansheng, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "playerTest" + File.separator + "空空如也.pcm");
//            decodeMp3.addDecodeListener(new Mp3ToPcmUtil.DeCodeListener() {
//                @Override
//                public void decodeSuccess() {
//                    prepareTwo = true;
//                    mYuanSheng = new PlayThread(RecordPlayActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "playerTest" + File.separator + "空空如也.pcm");
//                    mYuanSheng.setChannel(true, true);
//                    mYuanSheng.play();
//                    handler.sendEmptyMessage(1);
//                }
//            });
//        }
    }

    public int writeCovertCount(int count) {
        Log.e(getClass().getName(), "writeCovertCount-->>" + count);
        return count;
    }

    private void init() {
        //文件保存路径
        filePath = Constants.getLocalPath();
        bt_exit = (Button) findViewById(R.id.bt_yinpinhuilu_testing_exit);

        bt_exit.setOnClickListener(this);
        inputBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, inputBufferSize);
        inputBytes = new byte[inputBufferSize];
        saveDataList = new LinkedList<byte[]>();

        outputBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, outputBufferSize,
                AudioTrack.MODE_STREAM);
        outBytes = new byte[outputBufferSize];
    }


    /**
     * 录音
     */
    class recordSound implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "........recordSound run()......");
            byte[] bytes_pkg;
            audioRecord.startRecording();

            int sizeInBytes = inputBufferSize;

            //初始化输出流
            try {
//                将录音输出到指定文件
                File audioFile = new File(filePath + "source.pcm");
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                while (flag) {
                    int size = audioRecord.read(inputBytes, 0, sizeInBytes);
//                    //用于边录边播
                    bytes_pkg = inputBytes.clone();
                    Log.i(TAG, "........recordSound bytes_pkg==" + bytes_pkg.length);
                    if (saveDataList.size() >= 2) {
                        saveDataList.removeFirst();
                    }
                    saveDataList.add(bytes_pkg);
                    //写入PCM文件
                    dos.write(inputBytes, 0, size);
                    dos.flush();


                }
                //关闭写入文件操作
                dos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /**
     * 播放
     */
    class playRecord implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "........playRecord run()......");
            byte[] bytes_pkg = null;

            audioTrack.play();

            while (flag) {
                try {
                    outBytes = saveDataList.getFirst();
                    bytes_pkg = outBytes.clone();
                    audioTrack.write(bytes_pkg, 0, bytes_pkg.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.bt_yinpinhuilu_testing_exit://结束
                stopRecoder();
                stopPlayerOne();

                stopPlayerTwo();
                break;
            case R.id.bt_start://开始
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
                if (isYuansheng) {
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        mPlayer.setVolume(1.0f, 1.0f);
                        mPlayer.start();
                    }
                    if (playTwo != null && playTwo.isAlive()) {
                        playTwo.setNoSound();
                    }
                    isYuansheng = false;
                    btSoundChange.setText("原声");
                } else {
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        mPlayer.setVolume(0f, 0f);
                    }
                    if (playTwo != null && playTwo.isAlive()) {
                        playTwo.setHaveSound();
                    }
                    isYuansheng = true;
                    btSoundChange.setText("伴奏");
                }
                break;
        }
    }


    private void stopPlayerOne() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();

        }
    }

    private void stopPlayerTwo() {
        if (mPlayerTwo != null && mPlayerTwo.isPlaying()) {
            mPlayerTwo.stop();
            mPlayerTwo = null;
        }
        if (playTwo != null) {
            playTwo.stopp();
            playTwo = null;
        }
    }

    private void stopRecoder() {
        if (audioRecord != null && audioTrack != null) {
            flag = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    //播放伴奏或者原声
    private void initPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        mPlayer = MediaPlayer.create(this, R.raw.banzou);
        mPlayer.start();
        System.out.println("播放背景音");

//					mylrc.setOffsetY( mylrc.SelectIndex(mPlayer.getCurrentPosition())* (mylrc.getSIZEWORD() + INTERVAL - 1));
        if (mPlayer != null)
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    prepareOne = false;
                    stopRecoder();

                    stopPlayerTwo();

                }
            });
        if (mPlayer != null)
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    prepareOne = true;
                    if (prepareOne && prepareTwo) {
                        mPlayer.start();
                        startRecoder();
                    }
                }
            });

    }


    //    private void initPlayerTwo() {
//        if (mPlayerTwo == null) {
//            mPlayerTwo = new MediaPlayer();
//        }
//        if (mPlayerTwo != null && !mPlayerTwo.isPlaying()) {
//            mPlayerTwo.release();
//            mPlayerTwo = null;
//            mPlayerTwo = MediaPlayer.create(this, Uri.parse(Constants.yuansheng));
//            System.out.println("播放原声");
//
////					mylrc.setOffsetY( mylrc.SelectIndex(mPlayer.getCurrentPosition())* (mylrc.getSIZEWORD() + INTERVAL - 1));
//            bgtimetmpTwo = mPlayerTwo.getDuration() / 1000;
//
//            if (bgtimetmpTwo < 60) {
//                if (bgtimetmpTwo < 10) {
//                    bgtimeTwo = "00:0" + bgtimetmp;
//                } else {
//                    bgtimeTwo = "00:" + bgtimetmp;
//                }
//            } else {
//                double bgmin = bgtimetmpTwo / 60.0;
//                int bgimin = (int) bgmin;
//                int bgsec = bgtimetmp % 60;
//
//                if (bgimin < 10) {
//                    bgtimeTwo = "0" + bgimin;
//                } else {
//                    bgtimeTwo = "" + bgimin;
//                }
//
//                if (bgsec < 10) {
//                    bgtimeTwo = bgtimeTwo + ":0" + bgsec;
//                } else {
//                    bgtimeTwo = bgtimeTwo + ":" + bgsec;
//                }
//            }
//
//        } else {
//            System.out.println("停止播放播放背景音");
//            mPlayerTwo.stop();
//        }
//        mPlayerTwo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                prepareTwo = false;
//                stopRecoder();
//
//            }
//        });
//        mPlayerTwo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                prepareTwo = true;
//                if (prepareOne && prepareTwo) {
//                    mPlayerTwo.start();
//                    startRecoder();
//                }
//            }
//        });
//
//    }
    private void initAudioPlayer() {
        // TODO Auto-generated method stub
        // 声音文件一秒钟buffer的大小
        mAudioMinBufSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, // 指定在流的类型
                // STREAM_ALARM：警告声
                // STREAM_MUSCI：音乐声，例如music等
                // STREAM_RING：铃声
                // STREAM_SYSTEM：系统声音
                // STREAM_VOCIE_CALL：电话声音

                sampleRateInHz,// 设置音频数据的采样率
                AudioFormat.CHANNEL_CONFIGURATION_STEREO,// 设置输出声道为双声道立体声
                AudioFormat.ENCODING_PCM_16BIT,// 设置音频数据块是8位还是16位
                mAudioMinBufSize, AudioTrack.MODE_STREAM);// 设置模式类型，在这里设置为流类型
        // AudioTrack中有MODE_STATIC和MODE_STREAM两种分类。
        // STREAM方式表示由用户通过write方式把数据一次一次得写到audiotrack中。
        // 这种方式的缺点就是JAVA层和Native层不断地交换数据，效率损失较大。
        // 而STATIC方式表示是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audiotrack，
        // 后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
        // 这种方法对于铃声等体积较小的文件比较合适。
    }

    private void startRecoder() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btSoundChange.setEnabled(true);
            }
        });

        recordThread = new Thread(new recordSound());
        playThread = new Thread(new playRecord());
        recordThread.start();
        playThread.start();
    }

}