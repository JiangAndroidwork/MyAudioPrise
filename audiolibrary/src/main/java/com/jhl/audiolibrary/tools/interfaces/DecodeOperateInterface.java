package com.jhl.audiolibrary.tools.interfaces;

/**
 * Created by 郑童宇 on 2016/05/10.
 */
public interface DecodeOperateInterface {
    public void updateDecodeProgress(int decodeProgress);

    public void decodeSuccess();

    public void decodeFail();
}
