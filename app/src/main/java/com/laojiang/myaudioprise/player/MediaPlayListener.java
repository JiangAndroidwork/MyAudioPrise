package com.laojiang.myaudioprise.player;

import android.media.MediaPlayer;

/**
 * 类介绍（必填）：
 * Created by Jiang on 2018/7/16 .
 */

public interface MediaPlayListener {
    void onPrepare();
    void onComplete();
    void onError();
   void  onBufferingUpdate(MediaPlayer mp, int percent);

}
