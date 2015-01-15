package com.netease.engagement.itemview;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.netease.date.R;
import com.netease.engagement.adapter.MsgListCursorAdapter;
import com.netease.engagement.adapter.UploadPictureHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;

public class RenderMsgListItemBase {
	
	protected MsgListCursorAdapter adapter;
	
	protected View root;
	
	protected String mNick;
	
	protected boolean isFromUserInfo;
	
	protected UploadPictureHelper mUploadPictureHelper;
    
	protected MessageInfo mMsgInfo ;
	
	protected ChatItemUserInfo mUserInfo;
	
	protected boolean isOpenFire ;
	
	public RenderMsgListItemBase(View root, MsgListCursorAdapter adapter) {
		this.root = root;
		this.adapter = adapter;
	}
	
	public void renderView(MessageInfo msgInfo,boolean timeShow, boolean isOpenFire, String nick){
		mNick = nick ;
		mMsgInfo = msgInfo ;
		this.isOpenFire = isOpenFire ;
	}

	public void setUploadPictureHelper(UploadPictureHelper helper){
        mUploadPictureHelper = helper;
    }
	
	public void setFromUserInfo(boolean isFromUserInfo) {
		this.isFromUserInfo = isFromUserInfo;
	}
	
	public MessageInfo getMessageInfo() {
		return mMsgInfo;
	}
	
	public ProgressBar getFireProgressBar() {
		return null;
	}
	
	public void visibleKeywordsView(){
		
	}
	
	public MsgListCursorAdapter getAdapter() {
		return adapter;
	}

	public void fireItemAnimation(final ChatItemUserInfo userInfo) {
		Animation animation = AnimationUtils.loadAnimation(root.getContext(), R.anim.alpha_1_to_0);
		animation.setDuration(500);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				EgmService.getInstance().doDelMsg(mMsgInfo, userInfo, false);
			}
		});
		root.startAnimation(animation);
		root.setVisibility(View.INVISIBLE);
	}
	
	
}
