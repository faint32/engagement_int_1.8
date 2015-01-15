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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityFire;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.fragment.FragmentPrivateSession;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.media.MediaPlayerWrapper.MediaListener;
import com.netease.service.media.MediaPlayerWrapper.PlayStatus;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;
/**
 * 聊天other方语音显示item
 */
public class RenderOtherAudioItem extends RenderBase implements OnClickListener,MediaListener,OnLongClickListener{
	
	//最大时长
	private static int mDefaultMaxDuration = 60 * 1000;
	private RelativeLayout mAudioLayout ;
	//动画
	private TextView mAnimTextView;
	//时间
	private TextView mTimeTextView;
	//未播放标识
	private ImageView mPlayStatusIV;
	
	//最小宽度
	private int mMinWidth = 60 ;
	//最大宽度
	private int mMaxWidth = 180 ;   // bug fix #140559  by gzlichangjie
	
	private MessageInfo mMsgInfo ;
	private String mNick ;
	
	private AnimationDrawable mAnimationDrawable ;
	private View mRoot;
	
	private boolean isStopByClick;
	
	public RenderOtherAudioItem(View root){
		
	    mRoot = root;
	    
	    mNick = (String)(MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CAHT_OTHER_NICK));
	    mAudioLayout = (RelativeLayout) root.findViewById(R.id.audio_layout);
		mAnimTextView = (TextView)root.findViewById(R.id.item_view_info_audio_anim);
		mPlayStatusIV = (ImageView)root.findViewById(R.id.item_view_info_audio_no_play);
		//整个布局设置监听
		mAudioLayout.setOnLongClickListener(this);
		mAudioLayout.setOnClickListener(this);
        mTimeTextView = (TextView)root.findViewById(R.id.item_view_info_audio_time);
        resetAinm();
        
        mMinWidth = EgmUtil.dip2px(root.getContext(), mMinWidth);
        mMaxWidth = EgmUtil.dip2px(root.getContext(), mMaxWidth);
	}
	
	public void renderView(MessageInfo msgInfo, String nick){
		mNick = nick;
		
		mMsgInfo = msgInfo ;
		
		ViewGroup.LayoutParams params = mAudioLayout.getLayoutParams();
		
		//push过来的语音duration的单位为秒
        params.width = getWidthByDuration(msgInfo.duration * 1000);
        mAudioLayout.setLayoutParams(params);
        
        if (mTimeTextView != null) {
        	mTimeTextView.setText(getStringByDuration(mMsgInfo.duration*1000));
        }
        
        mAnimationDrawable.stop();
        if (msgInfo.isPlayStatus()) {
        	mPlayStatusIV.setVisibility(View.INVISIBLE);
        } else {
        	mPlayStatusIV.setVisibility(View.VISIBLE);
        }
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
        	if(!TextUtils.isEmpty(mMsgInfo.mediaUrl)
        			&& mMsgInfo.mediaUrl.equals(MediaPlayerWrapper.getInstance().getFilePath())){
        		bRes = true;
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
		if (mMsgInfo.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_COMMON) {
			MediaPlayerWrapper.getInstance().registerMediaListener(this);
			if(v.isSelected()){
				isStopByClick = true;
				MediaPlayerWrapper.getInstance().stop();
			}else{
				MediaPlayerWrapper.getInstance().play(mMsgInfo.mediaUrl,MediaPlayerWrapper.getAudioStreamType(v.getContext()));
			}
		} else if (mMsgInfo.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
			if (mPlayStatusIV.getVisibility() == View.VISIBLE) {
				mPlayStatusIV.setVisibility(View.INVISIBLE);
				MsgDBManager.updateMsgPlayState(mMsgInfo);
			}
			Long time = adapter.getFireStart(mMsgInfo);
			if (time == null) {
				FragmentPrivateSession.notNeedRefresh = true;
				adapter.setItemJumpToFireAudioOrView(outerContinaer);
				ActivityFire.startActivityForAudio(v.getContext(), mMsgInfo);
			}
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
			isStopByClick = false;
			
			mAudioLayout.setSelected(true);
			mAnimTextView.setSelected(true);
			mAnimationDrawable.stop();
			mAnimationDrawable.start();
			if (mPlayStatusIV.getVisibility() == View.VISIBLE) {
				mPlayStatusIV.setVisibility(View.INVISIBLE);
				MsgDBManager.updateMsgPlayState(mMsgInfo);
			}
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
        resetAinm();
        if(isMyself()) {
//        	MediaPlayerWrapper.getInstance().removeMediaListener(this);
        	if (!isStopByClick) {
        		mAudioLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						MessageInfo nextMessageInfo = MsgDBManager.getNextMessageInfo(mMsgInfo.getTime(), mMsgInfo.getReceiver(), mMsgInfo.getSender());
						if (nextMessageInfo != null) {
							if (nextMessageInfo.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO) { 
								if (nextMessageInfo.isFireMsg() == false) {
									if (nextMessageInfo.playStatus == false) {
										if (nextMessageInfo.sender == mMsgInfo.getSender()) {
											MediaPlayerWrapper.getInstance().play(nextMessageInfo.mediaUrl,MediaPlayerWrapper.getAudioStreamType(mAudioLayout.getContext()));
										}
									}
								}
							}
						}
					}
				}, 50);
        		
        	}
        }
	}

	@Override
	public void onMediaCompletion() {
		if(isMyself()) {
			MediaPlayerWrapper.getInstance().stop();
//	        MediaPlayerWrapper.getInstance().removeMediaListener(this);
	        resetAinm();
		}
	}
	
	/**
	 * 初始化动画状态
	 */
	private void resetAinm() {
		if (null != mAnimTextView) {
			Drawable d;
			if (mMsgInfo!=null && mMsgInfo.isPlayStatus()) {
				d = mRoot.getContext().getResources().getDrawable(R.drawable.icon_speaker_anim_chat_list_other2);
			} else {
				d = mRoot.getContext().getResources().getDrawable(R.drawable.icon_speaker_anim_chat_list_other);
			}
			ViewCompat.setBackground(mAnimTextView,d);
			mAnimationDrawable = (AnimationDrawable)mAnimTextView.getBackground();
			mAnimTextView.setSelected(false);
			
			mAudioLayout.setSelected(false);    // bug fix #140559   by gzlichangjie
        }
	}

	@Override
	public boolean onLongClick(View v) {
		showDelChatItemDialog();
		return true;
	}
	
	private AlertDialog mDelChatItemDialog ;
	private void showDelChatItemDialog(){
		String audiomode = EgmPrefHelper.getReceiverModeOn(mRoot.getContext()) ? mRoot
				.getContext().getResources()
				.getString(R.string.setting_audio_mode_music) : mRoot
				.getContext().getResources()
				.getString(R.string.setting_audio_mode_incall);
		mDelChatItemDialog = EgmUtil.createEgmMenuDialog(
				mRoot.getContext(), 
				mNick, 
				new CharSequence[]{audiomode,mRoot.getContext().getResources().getString(R.string.delete_audio)}, 
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int which = (Integer)v.getTag();
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
						mDelChatItemDialog.dismiss();  // bug fix #141214  by gzlichangjie
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
