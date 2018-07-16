package com.laojiang.myaudioprise;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.laojiang.myaudioprise.function.MixRunnable;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File sdDir = Environment.getExternalStorageDirectory();//获取根目录
        String banzou = sdDir.getAbsolutePath()+File.separator+"playerTest"+File.separator+"空空如也 (伴奏).mp3";
        String yuansheng = sdDir.getAbsolutePath()+File.separator+"playerTest"+File.separator+"空空如也.mp3";
        try {
            MixRunnable runnable = new MixRunnable(this,banzou,yuansheng);
            new Thread(runnable).start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
