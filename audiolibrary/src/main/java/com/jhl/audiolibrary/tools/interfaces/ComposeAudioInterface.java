package com.jhl.audiolibrary.tools.interfaces;

/**
 * 类介绍（必填）：合成监听
 * Created by Jiang on 2018/7/22 .
 */
public interface ComposeAudioInterface {
    public void updateComposeProgress(int composeProgress);

    public void composeSuccess();

    public void composeFail();
}
