package com.netease.engagement.view;

import com.netease.date.R;
import com.netease.engagement.app.EngagementApp;
import com.netease.service.Utils.EgmUtil;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class ChatMsgView extends BaseMsgView{
	
	private TextView mMsgText;
	private View mRootView ;
	private RelativeLayout.LayoutParams lp ;

    public ChatMsgView(ViewGroup root) {
        super(root);
    }

    public void showAndAutoHide(String text) {
        mMsgText.setText(text);
        lp = getLayoutParams();
        super.showAndAutoHide(lp);
    }
    
    private RelativeLayout.LayoutParams getLayoutParams(){
    	lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    	lp.topMargin = EgmUtil.dip2px(EngagementApp.getAppInstance().getApplicationContext(),
    			50);
    	return lp ;
    }
    
    public void showAndAutoHide(){
    	lp = getLayoutParams();
    	super.showAndAutoHide(lp);
    }
    
    public void show(){
    	lp = getLayoutParams();
    	super.show(lp);
    }
    
    public void hide(){
    	super.hide();
    }
    
    @Override
    protected View getView(Context context) {
        View v = View.inflate(context, R.layout.view_item_msg_tip, null);
        mRootView = v ;
        mMsgText = (TextView)v.findViewById(R.id.msg_tip);
        return v;
    }
    
    public void setOnClickListener(View.OnClickListener listener){
    	if(listener != null){
    		mRootView.setOnClickListener(listener);
    	}
    }
}
