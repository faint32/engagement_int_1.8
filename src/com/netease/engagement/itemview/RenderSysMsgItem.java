package com.netease.engagement.itemview;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityAction;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityInvite;
import com.netease.engagement.activity.ActivityMoneyAccount;
import com.netease.engagement.activity.ActivityMyShow;
import com.netease.engagement.activity.ActivityPageInfo;
import com.netease.engagement.activity.ActivityPicShowOffForFemale;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.activity.ActivityYuanfen;
import com.netease.engagement.adapter.UploadPictureHelper;
import com.netease.engagement.adapter.YixinHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentHome;
import com.netease.engagement.widget.richtext.view.RichTextView;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.service.protocol.meta.MsgExtra;



public class RenderSysMsgItem extends RenderBase {
	private View mRoot ;
	private RichTextView mRichTextView ;
	private TextView mActionText ;
	
	private MsgExtra mMsgExtra ;
	private MessageInfo mMsgInfo ;
	
	private YixinHelper mYixinHelper;
	private UploadPictureHelper mUploadPictureHelper;
	
	public RenderSysMsgItem(View root){
	    
		mRoot = root ;
		mRichTextView = (RichTextView)root.findViewById(R.id.sys_text_content);
		mActionText = (TextView)root.findViewById(R.id.sys_action_button);
		
		mYixinHelper = new YixinHelper(root.getContext(), YixinHelper.TYPE_BE_ADD_FRIEND);
	}
	
	@Override
	public void renderView(MessageInfo msgInfo, String nick){
		if(msgInfo == null){
			return ;
		}
		mMsgInfo = msgInfo ;
		
		String msgExtra = msgInfo.extraString ;
		mMsgExtra = MsgExtra.toMsgExtra(msgExtra);
		
		mRichTextView.setRichText(mMsgInfo.msgContent);
//		mActionText.setText(mMsgExtra.button1);   
		setActionText(mMsgExtra);
		mActionText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				action(mMsgExtra.sysMsgType, mMsgExtra);
			}
		});
	}
	
    public void setUploadPictureHelper(UploadPictureHelper helper){
        mUploadPictureHelper = helper;
    }
	
	private void setActionText(MsgExtra mExtra){
	    switch (mMsgExtra.sysMsgType) {
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_ACTIVITIES:
                if(!TextUtils.isEmpty(mMsgExtra.button1)){
                    mActionText.setVisibility(View.VISIBLE);
                    mActionText.setText(mMsgExtra.button1);
                }else{
                    mActionText.setVisibility(View.GONE);
                }
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_CASH_WILL_EXPIRE:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_cash_will_expire);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_FATE:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_fate);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_GOLD_WILL_EXPIRE:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_gold_will_expire);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_INTRODUCE:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_introduce);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_INTRODUCE_AUDIO:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_introduce_audio);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_PHOTO_NOT_PASS:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_photo_not_pass);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_WITHDRAW_FAILED:
                //提现申请被驳回，
            	mActionText.setText(R.string.sysmsg_type_withdraw_failed);
                mActionText.setVisibility(View.VISIBLE);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_WITHDRAW_SUCCESS:
                //提现成功;操作：跳转进网易宝账户界面（webview界面)
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_withdraw_success);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_COMMON_TEXT:
                //普通文本消息
                mActionText.setVisibility(View.GONE);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_ADD_YIXIN:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_add_yixin);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_INFORM_FEMALE_FRIEND:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_invite_friends);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_INTRODUCE_VIDEO:
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(R.string.sysmsg_type_introduce_video);
                break;
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_GET_SNOW_TIPS:
                String tips = mExtra.button1;
                mActionText.setVisibility(View.VISIBLE);
                mActionText.setText(tips);
            default:
            	mActionText.setVisibility(View.GONE);
        }
	    
	}
	
    private void action(int sysMsgType, MsgExtra mExtra) {
        
	    String url=null;
	    
        if (mExtra != null) {
            url = mExtra.url;
        }
	    
		switch (sysMsgType) {
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_ACTIVITIES:
				//活动
				ActivityAction.startActivity(mRoot.getContext(),mMsgExtra.url,mMsgExtra.title);
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_CASH_WILL_EXPIRE:
				//现金收入30天后过期; 操作：进入推荐页面
			    ActivityHome.startActivity(mRoot.getContext(), FragmentHome.TAB_INDEX_RECOMMEND);
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_FATE:
				//条件：碰缘分内容审核不通过； 跳转：碰缘分界面
				ActivityYuanfen.startActivity(mRoot.getContext(), false);
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_GOLD_WILL_EXPIRE:
				//金币在30天后过期; 操作： 进入充值界面（跳浏览器）
			    ActivityWeb.startCoinCharge(mRoot.getContext());
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_INTRODUCE:
				//自我介绍文字审核不通过；跳转：自我介绍文字填写界面
				ActivityPageInfo.startActivity(mRoot.getContext(), String.valueOf(ActivityPageInfo.INTRODUCE), null);
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_INTRODUCE_AUDIO:
				//自我介绍语音审核不通过; 跳转：个人展示界面
				ActivityMyShow.startActivity(mRoot.getContext(),false);
//				ActivityAudioRecorder.startActivity(mRoot.getContext());
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_PHOTO_NOT_PASS:
				//照片/私照审核不通过; 操作：进入上传照片/私照流程（无需跳界面）
			    if(mUploadPictureHelper != null){
			    	if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female){
			    		mUploadPictureHelper.showUploadUpPicEntrance();
			    	} else {
			    		mUploadPictureHelper.showUploadUpPicSource(EgmConstants.Photo_Type.TYPE_PUBLIC);
			    	}
			    }
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_WITHDRAW_FAILED:
				//提现申请被驳回，无跳转
				ActivityMoneyAccount.startActivity(mRoot.getContext());
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_WITHDRAW_SUCCESS:
				//提现成功;操作：跳转进网易宝账户界面（webview界面)
			    ActivityWeb.startCheckBalance(mRoot.getContext());
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_COMMON_TEXT:
				//普通文本消息
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_ADD_YIXIN:
				//加易信好友
			    mYixinHelper.checkYixinAddFriend();
				break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_INFORM_FEMALE_FRIEND:
			    ActivityInvite.launch(mRoot.getContext());
			    break;
			case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_INTRODUCE_VIDEO:
				//自我介绍视频审核不通过; 跳转：个人展示界面
				ActivityMyShow.startActivity(mRoot.getContext(),true);
            case EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_GET_SNOW_TIPS:
                //跳转活动页
                Bundle bundle = new Bundle();
                bundle.putString("des_url", url);
                ActivityPicShowOffForFemale.startActivity(mRoot.getContext(), bundle);
			    break;
			    
		}
	}
}
