package com.laojiang.myaudioprise.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;

import com.laojiang.myaudioprise.R;


public class MyImageButton extends Button {
	private Bitmap bitmap;

	public MyImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		setBackgroundResource(R.drawable.icon_select);
		setTextSize(TypedValue.COMPLEX_UNIT_DIP ,14);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// ͼƬ����������ʾ
		int x = (this.getMeasuredWidth() - bitmap.getWidth()) >> 1;
		int y = 4;
		canvas.drawBitmap(bitmap, x, y, null);
		// �����Ҫת������ΪĬ�������Button�е����־�����ʾ
		// ������Ҫ�������ڵײ���ʾ
		canvas.translate(0,(this.getMeasuredHeight() >> 1) - (int) this.getTextSize()-2);
		super.onDraw(canvas);
	}

	public void setIcon(Bitmap bitmap) {
		this.bitmap = bitmap;
		invalidate();
	}

	public void setIcon(int resourceId) {
		this.bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
		invalidate();
	}
}
