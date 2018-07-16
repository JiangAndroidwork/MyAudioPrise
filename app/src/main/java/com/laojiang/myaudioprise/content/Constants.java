package com.laojiang.myaudioprise.content;

import android.content.Context;
import android.os.Environment;
import android.view.WindowManager;

import java.io.File;

public class Constants {
	public static String getLocalPath(){
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.EXTERPATH;
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return path;
	}
	public static String getPCM(){
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/playTest/"+PCM;
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return path;
	}
	public static final String EXTERPATH = "/AudioPlayer/";
	public static final String PCM = "pcm/";
	public static String  banzou = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"playerTest"+File.separator+ "raw/banzou.mp3";
	public static  String yuansheng = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"playerTest"+File.separator+"空空如也.mp3";
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
