package com.laojiang.myaudioprise.function;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jhl.audiolibrary.tools.decode.AudioFunction;
import com.jhl.audiolibrary.tools.interfaces.ComposeAudioInterface;
import com.jhl.audiolibrary.tools.interfaces.VoicePlayerInterface;
import com.jhl.audiolibrary.tools.player.MyPlayThread;
import com.jhl.audiolibrary.tools.player.VoicePlayerEngine;
import com.laojiang.myaudioprise.R;
import com.laojiang.myaudioprise.content.Constant;

/**
 * 类介绍（必填）：合成pcm界面 调节音量
 * Created by Jiang on 2018/7/22 .
 */

public class MixPcmActivity extends AppCompatActivity implements View.OnClickListener, ComposeAudioInterface, VoicePlayerInterface {

    private int max;
    private View btHeCheng;
    private TextView tvProgress;

    public static void start(Context context) {
        Intent starter = new Intent(context, MixPcmActivity.class);
        context.startActivity(starter);
    }

    private SeekBar seekBarPlayer;
    private SeekBar seekBarPersonVolum;
    private SeekBar seekBarBgVolum;
    private MyPlayThread personPlayer;
    private MyPlayThread banzou;
    private Handler handler;
    private long recordStartTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix);
        seekBarPlayer = findViewById(R.id.seek_bar);
        seekBarPersonVolum = findViewById(R.id.seek_bar_volum_1);
        seekBarBgVolum = findViewById(R.id.seek_bar_volum_2);
        btHeCheng = findViewById(R.id.bt_hecheng);
        tvProgress = findViewById(R.id.tv_progerss);
        btHeCheng.setOnClickListener(this);
        recordStartTime = System.currentTimeMillis();

        personPlayer = new MyPlayThread(this, Constant.getPCM() + "mysource.pcm");
        banzou = new MyPlayThread(this, Constant.banzouPcm);
        personPlayer.setChannel(true, true);
        banzou.setChannel(true, true);
        personPlayer.start();
        banzou.start();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //https://blog.csdn.net/saberhao/article/details/53909841
        //安卓N以上有个勿打扰权限，必须获取了勿打扰权限才能去更改声音，比如 如果手机开启了静音，就需要根据获取权限让用户去开启。
        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            getApplicationContext().startActivity(intent);
            return;
        }
        max = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM
                , AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        seekBarPersonVolum.setMax(max);
        seekBarBgVolum.setMax(max);


        seekBarPersonVolum.setProgress(current);
        seekBarBgVolum.setProgress(current);


        initListener();

    }


    private void initListener() {


        seekBarPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarPersonVolum.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("设置的声音大小==person", progress + "");
                if (progress < 20) {
                    personPlayer.setVolume(0.2f);
                } else {
                    personPlayer.setVolume(progress / 100);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarBgVolum.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("设置的声音大小==bg", progress + "");
                float num = (float) progress / max;
                if (progress < 0.2 * max) {
                    banzou.setVolume(0.2f);
                } else {
                    banzou.setVolume(num);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        personPlayer.stopp();
        banzou.stopp();
        int progress = seekBarPersonVolum.getProgress();
        int bgProgress = seekBarBgVolum.getProgress();
        int max = seekBarPersonVolum.getMax();
        int bgMax = seekBarBgVolum.getMax();

        float v1 = (2.0f * progress) / max;
        float v2 = (2.0f * bgProgress) / bgMax;
        AudioFunction.BeginComposeAudio(Constant.getPCM() + "mysource.pcm", Constant.banzouPcm, Constant.getLocalPath() + "record/mix.mp3", false,
                v1, v2,
                -1 * com.jhl.audiolibrary.Constant.MusicCutEndOffset / 2 * com.jhl.audiolibrary.Constant.RecordDataNumberInOneSecond, this);
    }

    @Override
    public void updateComposeProgress(int composeProgress) {
        tvProgress.setText(composeProgress + "%");
    }

    @Override
    public void composeSuccess() {
        tvProgress.setText("合成完成");
        VoicePlayerEngine.getInstance().playVoice(Constant.getLocalPath() + "record/mix.mp3", this);
    }

    @Override
    public void composeFail() {

    }

    //====================播放进度==================
    @Override
    public void playVoiceBegin() {

    }

    @Override
    public void playVoiceFail() {

    }

    @Override
    public void playVoiceFinish() {

    }
}
