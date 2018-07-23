package com.jhl.audiolibrary.tools.interfaces;

public interface VoiceRecorderOperateInterface {
    public void recordVoiceBegin();

    public void recordVoiceStateChanged(int volume, long recordDuration);

    public void prepareGiveUpRecordVoice();

    public void recoverRecordVoice();

    public void giveUpRecordVoice();

    public void recordVoiceFail();

    public void recordVoiceFinish();
}
