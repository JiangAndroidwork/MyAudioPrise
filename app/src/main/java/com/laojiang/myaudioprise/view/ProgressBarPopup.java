package com.laojiang.myaudioprise.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.laojiang.myaudioprise.content.Constants;

public class ProgressBarPopup extends PopupWindow {

	private AudioManager mAudioManager;
	private int maxVolume = 0;
	private int currentVolume = 0;
		
	public ProgressBarPopup(Context context) {
		// TODO Auto-generated constructor stub
		int screenWidth = Constants.getScreenWidth(context);
		
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		VerticalSeekBar verticalSeekBar = new VerticalSeekBar(context);
		setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
		setHeight(screenWidth/4);
		setFocusable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(new BitmapDrawable());
		setContentView(verticalSeekBar);
		
		update();
		
		verticalSeekBar.setMax(maxVolume);
		
		verticalSeekBar.setProgress(currentVolume);
		
		verticalSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				// TODO Auto-generated method stub
				//����������С
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
				System.out.println(progress);
			}
		});
		
	}
	
	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		// TODO Auto-generated method stub
		super.showAtLocation(parent, gravity, x, y);
	}
	
}
