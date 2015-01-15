package com.netease.engagement.itemview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.adapter.MsgListCursorAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.view.HeadView;
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.MessageInfo;

public class RenderMsgListItemAnother extends RenderMsgListItemBase {

	private  LinearLayout mUserLayout ;
	private  TextView mUserTime ;
	private  HeadView mUserProfile ;
	private  RelativeLayout mUserContainerLayout ;
	private  ProgressBar mFireProgress;
	private  ImageView mFireIcon;
	private  String otherProfile ;
	private  LinearLayout mKeyWordsLay;
	
	public RenderMsgListItemAnother(View root, MsgListCursorAdapter adapter) {
		super(root, adapter);
		
		//对方部分
		mUserLayout = (LinearLayout)root.findViewById(R.id.user_layout);
		mUserTime = (TextView)root.findViewById(R.id.user_msg_time);
		mUserProfile = (HeadView)root.findViewById(R.id.user_profile);
		mUserContainerLayout = (RelativeLayout)root.findViewById(R.id.user_container_layout);
		mFireProgress = (ProgressBar) root.findViewById(R.id.fire_progressbar);
		mKeyWordsLay = (LinearLayout)root.findViewById(R.id.keywords_match_tip);
		mFireIcon = (ImageView)root.findViewById(R.id.fire_icon);
		
		mFireIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(RenderMsgListItemAnother.this.root.getContext(), 
						R.string.this_is_fire_message, Toast.LENGTH_SHORT).show();
			}
		});
		
		if(MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CHAT_OTHER_PROFILE) != null){
			otherProfile = ((String)MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CHAT_OTHER_PROFILE));
		}
	}
	
	@Override
	public void renderView(MessageInfo msgInfo,boolean timeShow, boolean isOpenFire, String nick){
		super.renderView(msgInfo, timeShow, isOpenFire, nick);
		
		initUserLayout(timeShow);
		mKeyWordsLay.setVisibility(View.GONE);
		switch (msgInfo.type) {
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT:
			RenderChatText renderChatText = null;
			if (mUserContainerLayout.getTag() != null 
					&& mUserContainerLayout.getTag() instanceof RenderChatText) {
				renderChatText = (RenderChatText) mUserContainerLayout.getTag();
			} else {
				View view = View.inflate(root.getContext(), R.layout.view_chat_text_other, null);
				renderChatText = new RenderChatText(view);
				
				addRenderView(renderChatText, view);
			}
			if(msgInfo.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_COMMON){
				matchKeyWords(msgInfo);
			}
			renderChatText.renderView(msgInfo, mNick);
			break ;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
			RenderOtherAudioItem renderOtherAudioItem = null;
			if (mUserContainerLayout.getTag()!=null && mUserContainerLayout.getTag() instanceof RenderOtherAudioItem) {
				renderOtherAudioItem = (RenderOtherAudioItem) mUserContainerLayout.getTag();
			} else {
				View view = View.inflate(root.getContext(), R.layout.item_view_audio_other, null);
				renderOtherAudioItem = new RenderOtherAudioItem(view);
				
				addRenderView(renderOtherAudioItem, view);
			}
			renderOtherAudioItem.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
			RenderGiftItem renderGiftItem = null;
			if (mUserContainerLayout.getTag()!=null && mUserContainerLayout.getTag() instanceof RenderGiftItem) {
				renderGiftItem = (RenderGiftItem) mUserContainerLayout.getTag();
			} else {
				View view = View.inflate(root.getContext(), R.layout.view_chat_gift_other_layout, null);
				renderGiftItem = new RenderGiftItem(view);
				
				addRenderView(renderGiftItem, view);
			}
			renderGiftItem.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
			RenderImageItem renderImageItem = null;
			if (mUserContainerLayout.getTag()!=null && mUserContainerLayout.getTag() instanceof RenderImageItem) {
				renderImageItem = (RenderImageItem) mUserContainerLayout.getTag();
			} else {
				View view = View.inflate(root.getContext(), R.layout.item_view_image_other, null);
				renderImageItem = new RenderImageItem(view);
				
				addRenderView(renderImageItem, view);
			}
			renderImageItem.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
			RenderImageItem renderImageItem2 = null;
			if (mUserContainerLayout.getTag()!=null && mUserContainerLayout.getTag() instanceof RenderImageItem) {
				renderImageItem2 = (RenderImageItem) mUserContainerLayout.getTag();
			} else {
				View view = View.inflate(root.getContext(), R.layout.item_view_image_other, null);
				renderImageItem2 = new RenderImageItem(view);
				
				addRenderView(renderImageItem2, view);
			}
			renderImageItem2.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
			RenderVideoItem renderVideoItem = null;
			if (mUserContainerLayout.getTag()!=null && mUserContainerLayout.getTag() instanceof RenderVideoItem) {
				renderVideoItem = (RenderVideoItem) mUserContainerLayout.getTag();
			} else {
				View view = View.inflate(root.getContext(), R.layout.item_view_video_other, null);
				renderVideoItem = new RenderVideoItem(view);
				
				addRenderView(renderVideoItem, view);
			}
			renderVideoItem.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE:
			RenderFaceItem renderFaceItem = null;
			if (mUserContainerLayout.getTag()!=null && mUserContainerLayout.getTag() instanceof RenderFaceItem) {
				renderFaceItem = (RenderFaceItem) mUserContainerLayout.getTag();
			} else {
				View view = View.inflate(root.getContext(), R.layout.item_view_face_other, null);
				renderFaceItem = new RenderFaceItem(view);
				
				addRenderView(renderFaceItem, view);
			}
			renderFaceItem.renderView(msgInfo, mNick);
			break;
		default:
			return;
		}
		
	}
	
	@Override
	public ProgressBar getFireProgressBar() {
		return mFireProgress;
	}
	
	private void addRenderView(RenderBase renderBase, View view) {
		if (mUserContainerLayout.getTag()!=null) {
			mUserContainerLayout.setTag(null);
			mUserContainerLayout.removeAllViews();
		}
		
		renderBase.setAdapter(adapter);
		renderBase.setOuterContinaer(this);
		mUserContainerLayout.setTag(renderBase);
		mUserContainerLayout.addView(view);
	}

	/**
	 * 初始化对方布局
	 * @param timeShow
	 */
	private void initUserLayout(boolean timeShow){
		mUserLayout.setVisibility(View.VISIBLE);
		if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female) {
			mUserProfile.setImageUrl(false,HeadView.PROFILE_SIZE_SMALL,otherProfile, 
					EgmConstants.SexType.Male);
		} else {
			mUserProfile.setImageUrl(false,HeadView.PROFILE_SIZE_SMALL,otherProfile, 
					EgmConstants.SexType.Female);
		}
		mUserProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int gender = ManagerAccount.getInstance().getCurrentGender() ^ 1 ;
				if (isFromUserInfo) {
					Context context = mUserProfile.getContext();
			        if(context instanceof Activity){
			        	Activity activity = (Activity) context;
			        	activity.finish();
			        }
				} else {
					ActivityUserPage.startActivity(
							mUserProfile.getContext(),
							String.valueOf(mMsgInfo.sender), 
							String.valueOf(gender));		
				}
			}
		});
		if(timeShow){
			mUserTime.setVisibility(View.VISIBLE);
			mUserTime.setText(TimeFormatUtil.covert2DisplayTime(mMsgInfo.time));
			if (isOpenFire) {
				mUserTime.setTextColor(Color.BLACK);
				Drawable left = mUserTime.getContext().getResources().getDrawable(R.drawable.icon_clock_black);
				mUserTime.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
			} else {
				mUserTime.setTextColor(mUserTime.getContext().getResources().getColor(R.color.info_audio_txt_color));
				Drawable left = mUserTime.getContext().getResources().getDrawable(R.drawable.icon_clock_gray);
				mUserTime.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
			}
		} else {
			mUserTime.setVisibility(View.GONE);
		}
		
		if (mMsgInfo.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
			mFireIcon.setVisibility(View.VISIBLE);
			if (isOpenFire) {
				mFireIcon.setBackgroundResource(R.drawable.icon_pgchat_fire_s_white);
			} else {
				mFireIcon.setBackgroundResource(R.drawable.icon_pgchat_fire_s_gray);
			}
		} else {
			mFireIcon.setVisibility(View.GONE);
		}
	}
	@Override
	public void visibleKeywordsView() {
		if(mMsgInfo.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT){
			matchKeyWords(mMsgInfo);
		} else{
			mKeyWordsLay.setVisibility(View.GONE);
		}
	}
	private boolean matchKeyWords(final MessageInfo msgInfo){
		boolean isMatched = false;
		if(!TextUtils.isEmpty(msgInfo.tips)){
			TextView tv = (TextView)mKeyWordsLay.findViewById(R.id.keywords_match_text);
			tv.setText(msgInfo.tips);
			mKeyWordsLay.setVisibility(View.VISIBLE);
			isMatched = true;
		} else{
			mKeyWordsLay.setVisibility(View.GONE);
		}
		return isMatched;
	}

}
