package com.netease.engagement.itemview;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.netease.common.log.NTLog;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityChatPriPicList;
import com.netease.engagement.activity.ActivityPrivateSession;
import com.netease.engagement.activity.ActivityUtil;
import com.netease.engagement.activity.ActivityVideoList;
import com.netease.engagement.activity.ActivityVideoRec;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentPrivateSession;
import com.netease.engagement.image.explorer.FileExplorerActivity;
import com.netease.engagement.view.ViewCompat;
import com.netease.engagement.widget.ChatAnimView;
import com.netease.engagement.widget.GiftKeyboardView;
import com.netease.engagement.widget.RecordingView2;
import com.netease.engagement.widget.RecordingView2.OnRecordListener;
import com.netease.engagement.widget.emot.EmotEdit;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.EgmDBProviderExport;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.share.sticker.view.EmoticonPickerView;
import com.netease.share.sticker.view.IEmojiSelectedListener;

public class RenderEmotView {
	private LinearLayout mSendBottomLayout ;
	//更多，语音，表情
	private ImageView mSendMore, mSendAudio, mSendEmot,mSendMsg ;
	//表情键盘
	private EmoticonPickerView mEmoticonView;
	//礼物键盘
	private GiftKeyboardView mGiftKeyboardView ;
	//录音键盘
	private FrameLayout mRecordingLayout ;
	public RecordingView2 mRecordingView ;
	private TextView mRecordingTip;
	//输入框
	private EmotEdit mEmotEdit;
	private InputMethodManager mImm;
	
	private ActivityPrivateSession mFragmentActivity ;
	private FragmentPrivateSession mFragment ;
	
//	private ChatAnimView animView;
	
	private TextView manGiftTip;
	private ImageView manGiftTipHalo;
	
	// 表情和键盘切换
    private boolean mEmot = true;
    //录音和键盘切换
    public boolean mAudio  = true ;
    
    private boolean mStateGift = false ;
    
    private boolean mStateMore = false ;
    
    //男性首次进入聊天界面，送礼物提示层标记
    public boolean mGiftTipShowing = false;
    
	public RenderEmotView(FragmentPrivateSession fragment , View root){
		if(fragment == null || fragment.getActivity() == null || root == null){
			throw new IllegalArgumentException();
		}
		mFragment = fragment ;
		mFragmentActivity = (ActivityPrivateSession) fragment.getActivity() ;
		init(root);
	}
	
	private void init(View root){
		mSendBottomLayout = (LinearLayout)root.findViewById(R.id.send_button_layout);
		
		mSendMsg = (ImageView)root.findViewById(R.id.send_msg);
		mSendMsg.setOnClickListener(mClickListener);
		
		mSendMore = (ImageView)root.findViewById(R.id.send_button_more);
        mSendMore.setOnClickListener(mClickListener);
        
        mSendAudio = (ImageView)root.findViewById(R.id.send_button_audio);
        mSendAudio.setOnClickListener(mClickListener);
        
        mSendEmot = (ImageView)root.findViewById(R.id.send_button_emoticon);
        mSendEmot.setOnClickListener(mClickListener);
        
        mEmotEdit = (EmotEdit)root.findViewById(R.id.send_edit);
        mEmotEdit.setMaxLines(4);
        mEmotEdit.setOnClickListener(mClickListener);
        
        mEmotEdit.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() > 0){
					mSendMore.setVisibility(View.GONE);
					mSendMsg.setVisibility(View.VISIBLE);
				}else{
					mSendMore.setVisibility(View.VISIBLE);
					mSendMsg.setVisibility(View.GONE);
				}
			}
        });
        
        mGiftKeyboardView = (GiftKeyboardView)root.findViewById(R.id.gift_view);
        
        mEmoticonView = (EmoticonPickerView)root.findViewById(R.id.emoticon_picker_view);
        mEmoticonView.show(iEmojiSelectedListener);
        
        mRecordingLayout = (FrameLayout)root.findViewById(R.id.recording_layout);
        mRecordingView = (RecordingView2)root.findViewById(R.id.recordingview);
        mRecordingView.setOnRecordListener(new OnRecordListener(){
			@Override
			public void onRecordStoped(long duration,boolean cancelState,String filePath) {
				mFragment.hideRecordTip();
				if(!cancelState){
					//发送语音
					mFragment.sendAudio(filePath,String.valueOf(duration/1000));
				}
			}
			@Override
			public void onRecordTooShort() {
				//提示录音时间太短
				ToastUtil.showToast(mFragmentActivity,R.string.record_time_too_short);
				mFragment.hideRecordTip();
			}
			@Override
			public void onRecordCancel(boolean cancel) {
				mFragment.setCancelState(cancel);
			}
			@Override
			public void onRecordStart() {
				mFragment.showRecordTip();
			}
        });
        mRecordingTip = (TextView)root.findViewById(R.id.recording_layout_tip);
        
        mImm = (InputMethodManager)mFragmentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
//	private OnEmoticonClickListener mOnEmoticonClickListener = new OnEmoticonClickListener(){
//		@Override
//		public void onClick(String phrase, boolean isDelete) {
//			if(isDelete){
//                KeyEvent evt = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
//                mEmotEdit.dispatchKeyEvent(evt);
//            }else if(!TextUtils.isEmpty(phrase)){
//                int start = mEmotEdit.getSelectionStart();
//                int end = mEmotEdit.getSelectionEnd();
//                Editable txt = mEmotEdit.getText();
//                int min = Math.min(start, end); 
//                int max = Math.max(start, end);
//                txt.replace(min, max, phrase);
//                if(txt.length() + phrase.length() <= EgmConstants.PRIVATE_MSG_EDIT_MAX){
//                	mEmotEdit.setSelection(min + phrase.length());
//                }
//            }
//		}
//	};
	
	private IEmojiSelectedListener iEmojiSelectedListener = new IEmojiSelectedListener() {
		@Override
		public void onEmojiSelected(String phrase) {
			if ("[删除]".equalsIgnoreCase(phrase)) {
				KeyEvent evt = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
				mEmotEdit.dispatchKeyEvent(evt);
			} else {
				int start = mEmotEdit.getSelectionStart();
	              int end = mEmotEdit.getSelectionEnd();
	              Editable txt = mEmotEdit.getText();
	              int min = Math.min(start, end); 
	              int max = Math.max(start, end);
	              txt.replace(min, max, phrase);
	              if(txt.length() + phrase.length() <= EgmConstants.PRIVATE_MSG_EDIT_MAX){
	            	  mEmotEdit.setSelection(min + phrase.length());
	              }
			}
		}
		@Override
		public void onStickerSelected(String identifer, String url, String desc, String name) {
			if (!TextUtils.isEmpty(desc)) {
				desc = "[" + desc + "]";
			}
			sendFaceMsg(name, desc);
		}
		
	};
	
	private AlertDialog mSendPicDialog ;
	private void showSendPicDialog(){
		if(mSendPicDialog == null){
			mSendPicDialog = EgmUtil.createEgmMenuDialog(mFragmentActivity, 
					mFragmentActivity.getString(R.string.send_pic), 
					mFragmentActivity.getResources().getStringArray(R.array.send_pic_array), 
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int which = (Integer)view.getTag();
                            switch (which) {
	                            case 0: 
	                            	//发送私密照片，展示私密照片列表
//	                            	showSendPubPicDialog(true);
	                            	// 发送私照，只能从相册选择，不能拍摄
	                            	String uid = ManagerAccount.getInstance().getCurrentIdString();
	                            	ActivityChatPriPicList.startActivity(mFragmentActivity, uid);
	                                break;
	                            case 1: 
	                            	//发送公开照片
	                            	showSendPubPicDialog(false);
	                                break;
                            }
                            if(mSendPicDialog.isShowing()){
                            	mSendPicDialog.dismiss();
                            }
                        }
                    });
		}
		mSendPicDialog.show();
	}
	
	private String mId = null ;
	private final String KEY_CAPTURE_ID = "key_capture_id";
	
	private AlertDialog mSendPubPicDialog ;
	private void showSendPubPicDialog(final boolean isPrivate){
		//每次重新创建
		mSendPubPicDialog = null ;
		if(mSendPubPicDialog == null){
			String title = null ;
			if(isPrivate){
				title = mFragmentActivity.getString(R.string.send_pri_pic) ;
			}else{
				title = mFragmentActivity.getString(R.string.send_pub_pic) ;
				if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male) {
					title = mFragmentActivity.getString(R.string.send_pic) ;
				}
			}
			mSendPubPicDialog = EgmUtil.createEgmMenuDialog(mFragmentActivity, 
					title, 
					mFragmentActivity.getResources().getStringArray(R.array.send_pub_pic_array), 
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int which = (Integer)view.getTag();
                            switch (which) {
	                            case 0: 
	                            	//拍照
	                            	//用来生成图片名称
	                                mId = String.valueOf(System.currentTimeMillis());
	                                ActivityUtil.capturePhotoForResult(
	                                		mFragment, 
	                                		EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mId), 
	                   					 	EgmConstants.REQUEST_CAPTURE_PHOTO);
	                                break;
	                            case 1: 
	                            	if(isPrivate){
		                            	String uid = ManagerAccount.getInstance().getCurrentIdString() ;
		                            	ActivityChatPriPicList.startActivity(mFragmentActivity, uid);
	                            	}else{
		                            	FileExplorerActivity.startForUploadPicture(
		                            			mFragment,
		                            			EgmConstants.Photo_Type.TYPE_CHAT_PUBLIC_PIC,
		                            			EgmConstants.REQUEST_SELECT_PICTURE, 
		                            			EgmConstants.SIZE_MAX_PICTURE, 
		                            			0);
	                            	}
	                                break;
                            }
                            if(mSendPubPicDialog.isShowing()){
                            	mSendPubPicDialog.dismiss();
                            }
                        }
                    });
		}
		mSendPubPicDialog.show();
	}
	
	private AlertDialog mChooseVideoDialog ;
	private void showChooseVideoDialog(){
		if(mChooseVideoDialog == null){
			mChooseVideoDialog = EgmUtil.createEgmMenuDialog(
					mFragmentActivity, 
					mFragmentActivity.getString(R.string.send_video), 
					mFragmentActivity.getResources().getStringArray(R.array.send_pub_pic_array), 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							int which = (Integer) v.getTag();
							switch(which){
								case 0:
									/**
									 * 拍摄视频 
									 */
									ActivityVideoRec.startActivityForResult(mFragment,EgmConstants.REQUEST_RECORD_VIDEO);
									break;
								case 1:
									/**
									 * 从相册选择视频
									 */
									ActivityVideoList.startActivityForResult(mFragment,EgmConstants.REQUEST_SELECT_VIDEO);
									break;
							}
							if(mChooseVideoDialog.isShowing()){
								mChooseVideoDialog.dismiss();
                            }
						}
					});
		}
		mChooseVideoDialog.show();
	}
	
	/**
	 * 显示礼物键盘
	 */
	private void showGiftKeyBoard(){
		immControl(false);
		mEmoticonGridControl(false);
		mRecordingLayout.setVisibility(View.GONE);
		mGiftKeyboardView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 刷新礼物键盘
	 */
	public void renderGiftKeyBoard(){
		mGiftKeyboardView.renderView();
	}
	
	private PopupWindow mPopupWindow ;
	private void showPopupWindow(final View anchor, final boolean isFirstTime){
		if(anchor == null)
			return;
		final LinearLayout container = new LinearLayout(mFragmentActivity);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setGravity(Gravity.CENTER);
		
		final ChatAnimView animView = (ChatAnimView) View.inflate(mFragmentActivity,R.layout.view_chat_anim_layout,null);
		ImageView image = (ImageView)animView.findViewById(R.id.image);
		ImageView video = (ImageView)animView.findViewById(R.id.video);
		
		manGiftTip = (TextView) animView.findViewById(R.id.man_gift_tip_txt);
		manGiftTipHalo = (ImageView) animView.findViewById(R.id.man_gift_tip_halo);
		
		if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male){
			ViewCompat.setBackground(video, mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_gift_selector));
		}
		
		image.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				switch(ManagerAccount.getInstance().getCurrentGender()){
					case EgmConstants.SexType.Female:
						showSendPicDialog();
						break;
					case EgmConstants.SexType.Male:
						showSendPubPicDialog(false);
						break;
				}
				if(mPopupWindow != null && mPopupWindow.isShowing()){
					animView.hideAllViews();
					mPopupWindow.dismiss();
				}
			}
		});
		
		video.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				switch(ManagerAccount.getInstance().getCurrentGender()){
				case EgmConstants.SexType.Female:
					/**
					 * 女性发送视频
					 */
					showChooseVideoDialog();
					break;
				case EgmConstants.SexType.Male:
					/**
					 * 男性发送礼物
					 */
					showGiftKeyBoard();
					break;
				}
				if(mPopupWindow != null && mPopupWindow.isShowing()){
					animView.hideAllViews();
					mPopupWindow.dismiss();
				}
			}
		});
		
		container.addView(animView);
		
		mPopupWindow = new PopupWindow(container,LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
//		if(isFirstTime) {
//			mPopupWindow.setTouchable(false);
//			mPopupWindow.setOutsideTouchable(false);
//		} else {
//			mPopupWindow.setTouchable(true);
//			mPopupWindow.setOutsideTouchable(true);
//		}
		
		if(!isFirstTime) {
			manGiftTipHalo.setBackgroundResource(0);
		}
		
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setClippingEnabled(false);
		mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
		
		final int anchorLocation[] = new int[2];
		anchor.getLocationInWindow(anchorLocation);
		
		int width = View.MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        container.measure(width, height);
        
        final int screenWidth = mFragmentActivity.getResources().getDisplayMetrics().widthPixels ;
        
        mPopupWindow.setOnDismissListener(new OnDismissListener(){
			@Override
			public void onDismiss() {
				if(mSendMore != null){
					mSendMore.startAnimation(getRotateAnim(false));
				}
				mStateMore = false ;
			}
        });
        
        
        new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				if(mPopupWindow != null && anchor != null){
					try{
						mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY,
							screenWidth - container.getMeasuredWidth(),
							anchorLocation[1] - container.getMeasuredHeight());
					}catch (Exception e){
						
					}
				}
			}
		}, 150);
        
        
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				if(animView != null){
					animView.startAnim(isFirstTime);
				}
			}
		}, 300);
	}
	
	public void showGiftTipForMan() {
		if(!mStateMore) {
			immControl(false);
        	mRecordingLayout.setVisibility(View.GONE);
        	mGiftKeyboardView.setVisibility(View.GONE);
        	mEmoticonGridControl(false);
        	new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					if(mSendMore == null)
						return;
					if(!mStateMore){
						mSendMore.startAnimation(getRotateAnim(true));
						showPopupWindow(mSendBottomLayout, true);
					}else{
						mSendMore.startAnimation(getRotateAnim(false));
					}
					mStateMore = !mStateMore ;
				}
        	},500);
		}
	}
	
	
	public void hideManGiftTip() {
		if(manGiftTip != null && manGiftTipHalo != null){
			AlphaAnimation anim = getAlphAnimation(false);
			manGiftTip.startAnimation(anim);
			manGiftTipHalo.startAnimation(anim);
			manGiftTip.setVisibility(View.INVISIBLE);
			manGiftTipHalo.setVisibility(View.INVISIBLE);
		}
		
//		mPopupWindow.setTouchable(true);
//		mPopupWindow.setOutsideTouchable(true);
//		mPopupWindow.setFocusable(true);
		
		mGiftTipShowing = false;
	}
	
	public void showRecordingLayout(boolean isFirstTime) {
		/**
		 * 语音或者键盘
		 */
		ViewCompat.setBackground(mSendEmot,
				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_emot_selector));
		mEmot = true ;
		
    	if(mAudio){
    		immControl(false);
    		mRecordingLayout.setVisibility(View.VISIBLE);
    		ViewCompat.setBackground(mSendAudio,
    				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_keyboard_selector));
    		
    		if(isFirstTime) {
    			
    			new Handler().postDelayed(new Runnable() {
    				@Override
    				public void run() {
    					if(mRecordingTip == null)
    						return;
    					AlphaAnimation anim = getAlphAnimation(true);
    					mRecordingTip.startAnimation(anim);
    					mRecordingTip.setVisibility(View.VISIBLE);
    				}
    			}, 200);
    			
    			
    			new Handler().postDelayed(new Runnable() {
    				@Override
    				public void run() {
    					if(mRecordingTip == null)
    						return;
    					AlphaAnimation anim = getAlphAnimation(false);
    					mRecordingTip.startAnimation(anim);
    					mRecordingTip.setVisibility(View.GONE);
    				}
    			}, EgmConstants.AUDIO_GUIDE_SHOW_DURATION);
    		}
    		
    	}else{
    		immControl(true);
    		mRecordingLayout.setVisibility(View.GONE);
    		ViewCompat.setBackground(mSendAudio,
    				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_voice_selector));
    	}
    	mAudio = !mAudio ;
    	mEmoticonGridControl(false);
    	mGiftKeyboardView.setVisibility(View.GONE);
	}
	
	private AlphaAnimation getAlphAnimation(boolean show) {
		AlphaAnimation mAlphaAnim = null;
		
		if(show) {
			mAlphaAnim = new AlphaAnimation(0, 1);
		} else {
			mAlphaAnim = new AlphaAnimation(1, 0);
		}
		mAlphaAnim.setDuration(500);
		mAlphaAnim.setFillAfter(true);
		return mAlphaAnim;
	}
	
	
	private RotateAnimation getRotateAnim(boolean show){
		RotateAnimation animation = null ;
		if(show){
			animation = new RotateAnimation(0,45,RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f) {
			};
		}else{
			animation = new RotateAnimation(45,0,RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
		}
		animation.setDuration(200);
		if(show){
			animation.setFillAfter(true);
		}
		return animation ;
	}
	
	private OnClickListener mClickListener = new OnClickListener(){
        @Override
        public void onClick(final View view) {
            switch(view.getId()){
                case R.id.send_button_more:
	                	/**
	        			 * 更多，弹出发送照片和视频
	        			 */
//modified by lishang  为了保持输入键盘不回缩                	
//	                	immControl(false);
	                	mRecordingLayout.setVisibility(View.GONE);
	                	mGiftKeyboardView.setVisibility(View.GONE);
	                	mEmoticonGridControl(false);
	                	new Handler().postDelayed(new Runnable(){
							@Override
							public void run() {
								if(!mStateMore){
									view.startAnimation(getRotateAnim(true));
									showPopupWindow(mSendBottomLayout, false);
								}else{
									view.startAnimation(getRotateAnim(false));
								}
								mStateMore = !mStateMore ;
							}
	                	},200);
	                break;
                    
                case R.id.send_button_audio:
                	showRecordingLayout(false);
	                break;
                    
                case R.id.send_button_emoticon:
	                	/**
	        			 * 表情
	        			 */
	                	ViewCompat.setBackground(mSendAudio,
	            				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_voice_selector));
	                	mAudio = true ;
	                	if(mEmot){
	                		immControl(false);
	                		mRecordingLayout.setVisibility(View.GONE);
	                		mGiftKeyboardView.setVisibility(View.GONE);
	                		mEmoticonGridControl(true);
	                		ViewCompat.setBackground(mSendEmot,
	                				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_keyboard_selector));
	                	}else{
	                		immControl(true);
	                		mRecordingLayout.setVisibility(View.GONE);
	                		mGiftKeyboardView.setVisibility(View.GONE);
	                		mEmoticonGridControl(false);
	                		ViewCompat.setBackground(mSendEmot,
	                				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_emot_selector));
	                	}
	                	mEmot = !mEmot ;
                    break;
                case R.id.send_msg:
	                	/**
	        			 * 发送
	        			 */
	                	if(mStateGift){
	                		mFragment.sendGift(String.valueOf(mCurGiftId));
	                		mEmotEdit.setText("");
	                		mGiftKeyboardView.setVisibility(View.GONE);
	                		mGiftKeyboardView.refreshGiftState();
	                		mStateGift = false ;
	                		mCurGiftId = 0 ;
	                		return ;
	                	}
	                	sendTextMsg();
	                	break;
                	
                case R.id.send_edit:
	                	/**
	        			 * 输入框
	        			 */
                		// 显示不直接索要礼物引导
                    if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female) {
                        long mMyId = ManagerAccount.getInstance().getCurrentId();
                        if (EgmPrefHelper.getShouldGirlGiftsTipOn(mFragmentActivity, mMyId))
                            if (!EgmPrefHelper.getGirlGiftsTipOn(mFragmentActivity, mMyId)) {
                            		EgmPrefHelper.putGirlGiftsTipOn(mFragmentActivity, mMyId);
                                showGiftsTipGForWoman();
                            }
                    }

	                	ViewCompat.setBackground(mSendAudio,
	            				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_voice_selector));
	            		ViewCompat.setBackground(mSendEmot,
	            				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_emot_selector));
	            		mEmot = true ;
	            		mAudio = true ;
	            		
	                	immControl(true);
	            		mRecordingLayout.setVisibility(View.GONE);
	            		mGiftKeyboardView.setVisibility(View.GONE);
	            		mEmoticonView.setVisibility(View.GONE);
	            		
	            		if(mStateGift){
	            			mEmotEdit.setText("");
	            			mGiftKeyboardView.refreshGiftState();
	            			mStateGift = false ;
	            		}
	                	break;
            }
        }
    };
    
    /*
    // 设置找话题数据
    public void setSendTopic(String topic){
    	if(!TextUtils.isEmpty(topic)){
    		mEmotEdit.setText(topic);
//    		mEmotEdit.setSelection(mEmotEdit.getText().length());
    		mEmotEdit.selectAll();
    	}
    }
    */
    // 设置聊天技数据
    public void setSendTalkSkill(String skill){
    	if(!TextUtils.isEmpty(skill)){
    		mEmotEdit.setText(skill);
    		mEmotEdit.selectAll();
    	}
    }
    
    /**
     * 发送礼物
     * @param giftName
     * @param giftId
     */
    private int mCurGiftId ;
    public void setSendGift(String giftName ,int giftId){
    	if(!TextUtils.isEmpty(giftName) || giftId != 0){
    		mCurGiftId = giftId ;
    		StringBuilder sb = new StringBuilder();
    		sb.append("[").append(giftName).append("]");
    		mEmotEdit.setText(sb.toString());
    		mEmotEdit.setSelection(mEmotEdit.getText().length());
    		mStateGift = true ;
    	}
    }
    
    /**
     * 发送文本消息
     */
    private void sendTextMsg(){
    	if(TextUtils.isEmpty(mEmotEdit.getText().toString().trim())){
    		ToastUtil.showToast(mFragmentActivity,"内容不能为空!");
    		mEmotEdit.setText("");
    		return ;
    	}
    	int length = mEmotEdit.getEditableText().toString().length();
    	if(length > EgmConstants.PRIVATE_MSG_EDIT_MAX){
    		return ;
    	}
    	mFragment.sendTextMsg(mEmotEdit.getEditableText().toString());
    
    	mEmotEdit.setText("");
    }
    
    /**
     * 发送表情消息
     */
    private void sendFaceMsg(String faceId, String faceDesc) {
    	if(!TextUtils.isEmpty(faceId)){
    		mFragment.sendFaceMsg(faceId, faceDesc);
    	}
    }
    
    /** 是否显示软键盘 */
    public void immControl(boolean bShow){
        if(mImm == null)
            return;
        if(bShow){
            mEmoticonGridControl(false);//隐藏表情框
            mImm.showSoftInput(mEmotEdit, 0);
        }
        else{
            mImm.hideSoftInputFromWindow(mEmotEdit.getWindowToken(), 0);
        }
    }
    
    /** 是否显示表情框 */
    private void mEmoticonGridControl(boolean bShow){
        if(bShow){
            immControl(false);
            mEmoticonView.setVisibility(View.VISIBLE);
        }
        else{
            mEmoticonView.setVisibility(View.GONE);
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode == Activity.RESULT_OK){
    		String filePath = null ;
    		long duration = 0 ;
    		switch(requestCode){
	    		case EgmConstants.REQUEST_SELECT_PICTURE :
	    			/**
	    			 * 发送本地图片
	    			 */
	    			if(data == null || data.getData() == null){
	    				return;
	    			}
	    			Uri uri = data.getData() ;
	    			mFragment.sendLocalImage(uri.toString(),EgmConstants.IsCameraPhotoFlag.OtherPhoto);
	    			break;
	    		case EgmConstants.REQUEST_CAPTURE_PHOTO:
	    		    NTLog.i("RenderEmotView", "onActivityResult EgmConstants.REQUEST_CAPTURE_PHOTO mId is " + mId);
	    			mFragment.sendLocalImage(Uri.fromFile(new File(EgmUtil.getFilePathByType(EgmUtil.TYPE_CAMERA,mId))).toString(),EgmConstants.IsCameraPhotoFlag.CameraPhoto);
	    			break;
	    		case EgmConstants.REQUEST_SELECT_VIDEO:
	    			/**
	    			 * 选择相册中的视频
	    			 */
	    			if(data == null){
	    				return ;
	    			}
	    			filePath = data.getStringExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_PATH);
	    			duration = data.getLongExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_DURATION,0);
	    			if(!TextUtils.isEmpty(filePath) && duration > 0){
	    				mFragment.sendVideo(filePath,String.valueOf(duration/1000));
	    			}
	    			break;
	    		case EgmConstants.REQUEST_RECORD_VIDEO:
	    			if(data == null){
	    				return ;
	    			}
	    			filePath = data.getStringExtra(EgmConstants.EXTRA_PATH);
	    			duration = data.getIntExtra(EgmConstants.EXTRA_DURATION,0);
	    			if(!TextUtils.isEmpty(filePath) && duration > 0){
	    				mFragment.sendVideo(filePath,String.valueOf(duration));
	    			}
	    			break;
	    		}
    	}
	}
    
    public void onSaveInstanceState(Bundle outState) {
        NTLog.i("RenderEmotView", "onSaveInstanceState mId is " + mId);
        if(!TextUtils.isEmpty(mId)){
            outState.putString(KEY_CAPTURE_ID, mId);
        }
    }
    public void onRestoreId(Bundle savedInstanceState) {
        NTLog.i("RenderEmotView", "onRestoreId ");
        if(savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString(KEY_CAPTURE_ID))){
            mId = savedInstanceState.getString(KEY_CAPTURE_ID);
            NTLog.i("RenderEmotView", "onRestoreId mId is " + mId);
        }
    }
    
    /**
     * 隐藏所有键盘
     */
    public void hideAll(){
    		ViewCompat.setBackground(mSendAudio,
    				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_voice_selector));
    		ViewCompat.setBackground(mSendEmot,
    				mFragmentActivity.getResources().getDrawable(R.drawable.icon_chat_emot_selector));
    		mEmot = true ;
    		mAudio = true ;
    		immControl(false);
	    	mRecordingLayout.setVisibility(View.GONE);
	    	mGiftKeyboardView.setVisibility(View.GONE);
	    	mEmoticonGridControl(false);
	    	
    }
    
	/**
	 * 引导女性用户不直接索要礼物
	 */
	public void showGiftsTipGForWoman() {
		mRecordingLayout.setVisibility(View.GONE);
		mGiftKeyboardView.setVisibility(View.GONE);
		mEmoticonGridControl(false);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if(mSendBottomLayout != null){
					showGiftTipsPopupWindow(mSendBottomLayout);
				}
			}
		}, 200);
	}

	/**
	 * 创建并显示PopWindow，注意TextView高度的计算 （StaticLayout）
	 */
	private void showGiftTipsPopupWindow(final View anchor) {
		
		final LinearLayout container = new LinearLayout(mFragmentActivity);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setGravity(Gravity.CENTER);
		final View tipsLayout = View.inflate(mFragmentActivity, R.layout.view_chat_gift_tip_anim_layout, null);
		container.addView(tipsLayout );

		// 添加监听，点击文字外的范围时消失，点击文字不消失
		tipsLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});
		TextView tipTxt = (TextView) tipsLayout.findViewById(R.id.gift_tips_view);
		tipTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
		
		// 创建PopupWindow
		mPopupWindow = new PopupWindow(container, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setClippingEnabled(false);

		// 计算TextView所占高度		
		TextPaint paint = tipTxt.getPaint();
		Resources rs= mFragmentActivity.getResources();
		StaticLayout layout = new StaticLayout(tipTxt.getText(), paint,
				rs.getDisplayMetrics().widthPixels - 2* rs.getDimensionPixelSize(R.dimen.info_margin_60dp), 
				Alignment.ALIGN_NORMAL, 1, 1, true);
		LinearLayout.LayoutParams tipTxtLp = (LayoutParams) tipTxt.getLayoutParams();
		int totlaHeight = layout.getHeight() + tipTxtLp.topMargin + tipTxtLp.bottomMargin + tipTxt.getPaddingTop()
				+ tipTxt.getPaddingBottom();
		
		// 设置PopupWindow的位置
		mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
		final int anchorLocation[] = new int[2];
		anchor.getLocationInWindow(anchorLocation);
		mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, 0, anchorLocation[1] - totlaHeight);
		
		new Handler().postDelayed(new Runnable() {
			// 渐现效果
			@Override
			public void run() {
                AlphaAnimation anim = getAlphAnimation(true);
				tipsLayout.startAnimation(anim);
				tipsLayout.setVisibility(View.VISIBLE);
			}
		}, 10);
	}
}
