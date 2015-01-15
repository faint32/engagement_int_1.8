package com.netease.engagement.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;

public class MyPageTabView extends AbsTabView{

	private long[] data = new long[3];
	private LayoutInflater inflater ;
	
	public MyPageTabView(Context context, AttributeSet attrs) {
		super(context,attrs,false);
		this.setGravity(Gravity.CENTER_VERTICAL);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getTabCount() {
		return 3;
	}

	@Override
	public int getTabDividerStyle() {
		return AbsTabView.DIVIDER_MIDDLE;
	}

	@Override
	public int getTabDividerResource() {
		return R.drawable.indicator_tab_divider_black;
	}
	
	public void createTabs(long a ,long b ,long c){
		data[0] = a ;
		data[1] = b ;
		data[2] = c ;
		super.create();
	}
	public void clear(){
		super.clear();
	}

	@Override
	public View getTabView(int index) {
		LinearLayout container = (LinearLayout) inflater.inflate(R.layout.view_item_tab_user_center,null);
		TextView txt_up = (TextView) container.findViewById(R.id.txt_up);
		TextView txt_bottom = (TextView) container.findViewById(R.id.txt_bottom);
		if(index == 0){
			txt_up.setText(String.valueOf(data[0]));
			txt_bottom.setText(getContext().getResources().getString(R.string.send_gift));
		}else if(index == 1){
			txt_up.setText(String.valueOf(data[1]));
			txt_bottom.setText(getContext().getResources().getString(R.string.private_image));
		}else if(index == 2){
			txt_up.setText(String.valueOf(data[2]));
			txt_bottom.setText(getContext().getResources().getString(R.string.public_image));
		}
		return container;
	}
}
