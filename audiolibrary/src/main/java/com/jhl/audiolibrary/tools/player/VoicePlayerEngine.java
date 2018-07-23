package com.jhl.audiolibrary.tools.player;

import android.media.MediaPlayer;

import com.jhl.audiolibrary.common.CommonFunction;
import com.jhl.audiolibrary.tools.data.MusicData;
import com.jhl.audiolibrary.tools.interfaces.VoicePlayerInterface;
import com.jhl.audiolibrary.utils.LogFunction;

/**
 * 类介绍（必填）：播放mp3
 * Created by Jiang on 2018/7/22 .
 */
public class VoicePlayerEngine {
    private int musicPlayerState;

    private String playingUrl;

    private VoicePlayerInterface voicePlayerInterface;

    private MediaPlayer voicePlayer;

    private static VoicePlayerEngine instance;

    private VoicePlayerEngine() {
        musicPlayerState = MusicData.MusicPlayerState.reset;

        voicePlayer = new MediaPlayer();

        voicePlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                start();
            }
        });

        voicePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (voicePlayerInterface != null) {
                    voicePlayerInterface.playVoiceFinish();
                }

                playingUrl = null;
            }
        });

        voicePlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(final MediaPlayer mediaPlayer, int what, int extra) {
                playFail();

                return true;
            }
        });
    }

    public static VoicePlayerEngine getInstance() {
        if (instance == null) {
            synchronized (VoicePlayerEngine.class) {
                if (instance == null) {
                    instance = new VoicePlayerEngine();
                }
            }
        }

        return instance;
    }

    public synchronized static void Destroy() {
        if (instance != null) {
            instance.destroy();
        }

        instance = null;
    }

    private void destroy() {
        if (voicePlayer != null) {
            if (voicePlayer.isPlaying()) {
                voicePlayer.stop();
            }
            voicePlayer.release();
            voicePlayer = null;
            if (voicePlayerInterface != null) {
                voicePlayerInterface.playVoiceFinish();
            }
        }

    }

    public void playVoice(String voiceUrl, VoicePlayerInterface voicePlayerInterface) {
        if (CommonFunction.isEmpty(voiceUrl)) {
            return;
        }

        stopVoice();

        this.voicePlayerInterface = voicePlayerInterface;

        prepareMusic(voiceUrl);
    }

    private synchronized void prepareMusic(String voiceUrl) {
        playingUrl = voiceUrl;

        musicPlayerState = MusicData.MusicPlayerState.preparing;

        try {
            voicePlayer.reset();
            voicePlayer.setDataSource(voiceUrl);
            voicePlayer.prepareAsync();
        } catch (Exception e) {
            playFail();


            LogFunction.error("播放语音异常", e);
        }
    }

    private void playFail() {
        if (voicePlayerInterface != null) {
            voicePlayerInterface.playVoiceFail();
        }

        playingUrl = null;
    }

    public boolean isPlaying() {
        return voicePlayer.isPlaying();
    }

    public void start() {
        voicePlayer.start();

        musicPlayerState = MusicData.MusicPlayerState.playing;

        if (voicePlayerInterface != null) {
            voicePlayerInterface.playVoiceBegin();
        }
    }

    public void reStart() {
        if (voicePlayer != null && musicPlayerState == MusicData.MusicPlayerState.pausing) {
            voicePlayer.start();
            musicPlayerState = MusicData.MusicPlayerState.playing;
        }
    }


    public void pause() {
        if (!voicePlayer.isPlaying()) {
            return;
        }

        playingUrl = null;

        voicePlayer.pause();

        musicPlayerState = MusicData.MusicPlayerState.pausing;


    }

    private void reset() {
        voicePlayer.reset();
        musicPlayerState = MusicData.MusicPlayerState.reset;

        playingUrl = null;
    }

    public void stopVoice() {
        switch (musicPlayerState) {
            case MusicData.MusicPlayerState.playing:
                pause();
                break;
            case MusicData.MusicPlayerState.preparing:
                reset();
                break;
        }
    }

    public void setVolume(float num) {
        if (voicePlayer != null) {
            voicePlayer.setVolume(num, num);
//            voicePlayer.start();
        }
    }

    public String getPlayingUrl() {
        return playingUrl == null ? "" : playingUrl;
    }
}