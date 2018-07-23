package com.laojiang.myaudioprise.content;

import android.content.Context;
import android.os.Environment;
import android.view.WindowManager;

import java.io.File;

public class Constant {
	public static final String NAME = "AudioEdit";

	public static final int ExportChannelNumber = 2;  // 输出声道为双声道
	public static final int ExportByteNumber = 2; //输出采样精度字节数
	public static final int ExportSampleRate = 44100; //输出采样率

	public static final int OneSecond = 1000;

	public static final int NormalMaxProgress = 100;

	public static boolean isBigEnding = false;

	public static String SUFFIX_WAV = ".wav";
	public static String SUFFIX_PCM = ".pcm";


	public static String getLocalPath(){
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.EXTERPATH;
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return path;
	}

	public static String getPCM(){
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/playerTest/"+PCM;
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return path;
	}
	public static final String EXTERPATH = "/playerTest/";
	public static final String PCM = "pcm/";
	public static String  banzou = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"playerTest"+File.separator+ "raw/banzou.mp3";
	public static  String yuansheng = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"playerTest"+File.separator+"空空如也.mp3";
	public static  String banzou2 = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"playerTest"+File.separator+"空空如也(伴奏).mp3";
	public static  String guangming = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "playerTest" + File.separator + "mix.pcm";
	public static  String testSoud = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "pauseRecordDemo" + File.separator +"wav"+ File.separator+"20180716062520.wav";

	public static String banzouPcm = getPCM()+"空空如也(伴奏).pcm";
    private static int screenWidth = 0;

    /**
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context)
    {
        if(screenWidth==0){
            WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            screenWidth = manager.getDefaultDisplay().getWidth();
        }
        return screenWidth;
    }
}
