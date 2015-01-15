package com.netease.engagement.itemview;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.media.MediaPlayerWrapper.MediaListener;
import com.netease.service.media.MediaPlayerWrapper.PlayStatus;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;
/**
 * 聊天发送方语音item
 */
public class RenderAudioItem extends RenderBase implements OnClickListener,MediaListener,OnLongClickListener{
	
	//最大时长
	private static long mDefaultMaxDuration = 60 * 1000;
	private RelativeLayout mAudioLayout ;
	//动画
	private TextView mAnimTextView;
	//时间
	private TextView mTimeTextView;
	//最小宽度
	private int mMinWidth = 60 ;
	//最大宽度
	private int mMaxWidth = 180 ;  // bug fix #140559  by gzlichangjie
	
	private MessageInfo mMsgInfo ;
	private String mNick ;
	
	private AnimationDrawable mAnimationDrawable ;
	private View mRoot;
	
	private ProgressBar mProgressBar ;
	private ImageView mFail ;
	
	private MsgAttach mAttach ;
	
	public RenderAudioItem(View root){
		
	    mRoot = root;
	  
	    mProgressBar = (ProgressBar)root.findViewById(R.id.audio_progress);
	    mFail = (ImageView)root.findViewById(R.id.audio_fail);
	   
	    mAudioLayout = (RelativeLayout) root.findViewById(R.id.audio_layout);
		mAnimTextView = (TextView)root.findViewById(R.id.item_view_info_audio_anim);
		mTimeTextView = (TextView)root.findViewById(R.id.item_view_info_audio_time);
		
		mAudioLayout.setOnLongClickListener(this);
		mAudioLayout.setOnClickListener(this);
		
        resetAinm();
        
        mMinWidth = EgmUtil.dip2px(root.getContext(), mMinWidth);
        mMaxWidth = EgmUtil.dip2px(root.getContext(), mMaxWidth);
	}
	
	public void renderView(MessageInfo msgInfo, String nick){
		mNick = nick;
		
		mMsgInfo = msgInfo ;
		if(!TextUtils.isEmpty(msgInfo.attach)){
			mAttach = MsgAttach.toMsgAttach(msgInfo.attach);
		}
		
		switch(msgInfo.status){
			case EgmConstants.Sending_State.SENDING:
				mProgressBar.setVisibility(View.VISIBLE);
				mFail.setVisibility(View.GONE);
				break;
			case EgmConstants.Sending_State.SEND_SUCCESS:
				mProgressBar.setVisibility(View.GONE);
				mFail.setVisibility(View.GONE);
				break;
			case EgmConstants.Sending_State.SEND_FAIL:
				mProgressBar.setVisibility(View.GONE);
				mFail.setVisibility(View.VISIBLE);
				break;
		}
		
		ViewGroup.LayoutParams params = mAudioLayout.getLayoutParams();
        params.width = getWidthByDuration(msgInfo.duration * 1000);
        mAudioLayout.setLayoutParams(params);
        
        if (mTimeTextView != null) {
        	mTimeTextView.setText(getStringByDuration(mMsgInfo.duration * 1000));
        }
        
        mAnimationDrawable.stop();
        resetAinm();
        
        MediaPlayerWrapper mediaPlayer = MediaPlayerWrapper.getInstance();
        
        if (isMyself()) {
            if (PlayStatus.PLAYING == mediaPlayer.getPlayStatus()) {
            	new Handler().post(new Runnable() {
					@Override
					public void run() {
						mAnimationDrawable.stop();
		                mAnimationDrawable.start();
					}
				});
                mAnimTextView.setSelected(true);
            } 
        }
        mediaPlayer.registerMediaListener(this);
	}
	
	private boolean isMyself() {
        boolean bRes = false;
        if(mAttach != null 
        		&& !TextUtils.isEmpty(mAttach.audioPath)
        		&& mAttach.audioPath.equals(MediaPlayerWrapper.getInstance().getFilePath())){
        	bRes = true ;
        }
        return bRes;
    }
	
	/**
	 * 获取语音消息背景显示宽度
	 */
	private int getWidthByDuration(long duration) {
        int width = 0;
        if (duration >= mDefaultMaxDuration) {
            width = mMaxWidth;
        } else {
            if (mMinWidth == 0 && mMaxWidth == 0) {
            	mMinWidth = EngagementApp.getAppInstance().getResources().getDimensionPixelSize(mMinWidth);
                mMaxWidth = EngagementApp.getAppInstance().getResources().getDimensionPixelSize(mMaxWidth);
            }
            width = (int) (mMinWidth + ((mMaxWidth - mMinWidth) * duration) / mDefaultMaxDuration);
        }
        return width;
    }
	
	/**
	 * 获取语音消息显示时长
	 * @param duration
	 * @return
	 */
	public static String getStringByDuration(long duration) {
        String value = null;

        if (duration <= 0) {
            value = "0\"";
            return value;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC +08:00"));
        value = formatter.format(duration);
        String[] splits = value.split(":");
        if (splits[0].endsWith("00")) {
            if (splits[1].endsWith("00")) {
                value = splits[2] + "\"";
            } else {
                value = splits[1] + "'" + splits[2] + "\"";
            }
        } else {
            value = splits[0] + ":" + splits[1] + "'" + splits[2] + "\"";
        }
        return value;
    }
	
	@Override
	public void onClick(View v) {
		MediaPlayerWrapper.getInstance().registerMediaListener(this);
		if(v.isSelected()){
			MediaPlayerWrapper.getInstance().stop();
		} else {
			MediaPlayerWrapper.getInstance().play(mAttach.audioPath, mMsgInfo.getMediaUrl(),MediaPlayerWrapper.getAudioStreamType(v.getContext()));
		}
	}

	@Override
	public void onMediaPrePare() {
		if(!isMyself()){
			resetAinm();
		}
	}

	@Override
	public void onMediaPlay() {
		if(isMyself()){
			mAudioLayout.setSelected(true);
			mAnimTextView.setSelected(true);
			mAnimationDrawable.stop();
			mAnimationDrawable.start();
		}else{
			mAnimationDrawable.stop();
		}
	}

	@Override
	public void onMediaPause() {
		if(isMyself()){
			mAnimationDrawable.stop();
			mAudioLayout.setSelected(false);
			mAnimTextView.setSelected(false);
		}
	}

	@Override
	public void onMediaRelease() {
		MediaPlayerWrapper.getInstance().removeMediaListener(this);
        resetAinm();
	}

	@Override
	public void onMediaCompletion() {
		MediaPlayerWrapper.getInstance().stop();
        MediaPlayerWrapper.getInstance().removeMediaListener(this);
        resetAinm();
	}
	
	/**
	 * 初始化动画状态
	 */
	private void resetAinm() {
		if (null != mAnimTextView) {
			Drawable d = mRoot.getContext().getResources().getDrawable(R.drawable.icon_speaker_anim_chat_list);
			ViewCompat.setBackground(mAnimTextView,d);
			mAnimationDrawable = (AnimationDrawable)mAnimTextView.getBackground();
			mAudioLayout.setSelected(false);
			mAnimTextView.setSelected(false);
        }
	}

	@Override
	public boolean onLongClick(View v) {
		if(mMsgInfo.status != EgmConstants.Sending_State.SENDING){
			showDelChatItemDialog();
		}
		return true ;
	}
	
	private AlertDialog mDelChatItemDialog ;
	private void showDelChatItemDialog(){
		CharSequence[] operations = null ;
		String audiomode = EgmPrefHelper.getReceiverModeOn(mRoot.getContext()) ? mRoot
				.getContext().getResources()
				.getString(R.string.setting_audio_mode_music) : mRoot
				.getContext().getResources()
				.getString(R.string.setting_audio_mode_incall);
		switch(mMsgInfo.status){
			case EgmConstants.Sending_State.SEND_SUCCESS:
				operations = new CharSequence[] {audiomode,mRoot.getContext().getResources().getString(R.string.delete_audio) };
				break;
			case EgmConstants.Sending_State.SEND_FAIL:
				operations = new CharSequence[] {
					audiomode,mRoot.getContext().getResources().getString(R.string.chat_msg_rensend),
					mRoot.getContext().getResources().getString(R.string.delete_audio) };
				break;
		}
		mDelChatItemDialog = EgmUtil.createEgmMenuDialog(
				mRoot.getContext(), 
				mNick, 
				operations,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int which = (Integer)v.getTag();
						if(mMsgInfo.status == EgmConstants.Sending_State.SEND_SUCCESS){
							switch(which){
							case 0:{
								//音频模式设置
								EgmUtil.changeAudioStreamType(v.getContext());
							}
								break;
							case 1:
								//删除消息
								delMsg();
								break;
						}
						}else if(mMsgInfo.status == EgmConstants.Sending_State.SEND_FAIL){
							switch(which){
								case 0:{
									//音频模式设置
									EgmUtil.changeAudioStreamType(v.getContext());
								}
								break;
								case 1:
									//重新发送
									LoopBack lp = new LoopBack();
									lp.mType = EgmConstants.LOOPBACK_TYPE.msg_resend;
									lp.mData = mMsgInfo ;
									EgmService.getInstance().doLoopBack(lp);
									break;
								case 2:
									//删除消息
									delMsg();
									break;
							}
						}
						mDelChatItemDialog.dismiss();
					}
				});
		mDelChatItemDialog.show();
	}
	
	private void delMsg(){
		LoopBack lp = new LoopBack();
		lp.mType = EgmConstants.LOOPBACK_TYPE.msg_delete ;
		lp.mData = mMsgInfo ;
		EgmService.getInstance().doLoopBack(lp);
	}
}
