package com.laojiang.myaudioprise.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.laojiang.myaudioprise.R;


public class BottomArriveView extends PopupWindow implements OnClickListener {
	
	private Context context;
	
	public BottomArriveView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
		this.context = context;
		initPopupWindow();
	}

	public void initPopupWindow(){
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view  = inflater.inflate(R.layout.bottom_arrive_view, null);
		TextView tv_hcb_rerecord = (TextView)view.findViewById(R.id.tv_hcb_rerecord);
		TextView tv_hcb_pass = (TextView)view.findViewById(R.id.tv_hcb_pass);
		TextView tv_hcb_cancel = (TextView)view.findViewById(R.id.tv_hcb_cancel);
		tv_hcb_rerecord.setOnClickListener(this);
		tv_hcb_pass.setOnClickListener(this);
		tv_hcb_cancel.setOnClickListener(this);
		setContentView(view);
		setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setOutsideTouchable(true);
		update();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tv_hcb_rerecord:
			break;
		case R.id.tv_hcb_pass:
			break;
		case R.id.tv_hcb_cancel:
			
			break;
		default:
			break;
		}
		dismiss();
	}
	
	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		// TODO Auto-generated method stub
		super.showAtLocation(parent, gravity, x, y);
	}
	
}
