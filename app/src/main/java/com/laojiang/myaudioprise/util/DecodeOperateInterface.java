package com.laojiang.myaudioprise.util;

/**
 * 类介绍（必填）：
 * Created by Jiang on 2018/7/16 .
 */

interface DecodeOperateInterface {
    public void updateDecodeProgress(int decodeProgress);

    public void decodeSuccess();

    public void decodeFail();
}
