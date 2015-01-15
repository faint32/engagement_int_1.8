package com.netease.engagement.itemview;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.engagement.adapter.MsgListCursorAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.protocol.meta.MessageInfo;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RenderMsgListItemSystem extends RenderMsgListItemBase {

	private LinearLayout mSysLayout ;
	private TextView mSysTime ;
	private ImageView mSysProfile ;
	private LinearLayout mSysMsg ;
	
	public RenderMsgListItemSystem(View root, MsgListCursorAdapter adapter) {
		super(root, adapter);
		
		mSysLayout = (LinearLayout)root.findViewById(R.id.system_msg_layout);
		mSysTime = (TextView)root.findViewById(R.id.system_msg_time);
		mSysProfile = (ImageView)root.findViewById(R.id.system_profile);
		mSysMsg = (LinearLayout)root.findViewById(R.id.sys_msg);
	}
	
	public void renderView(MessageInfo msgInfo,boolean timeShow, boolean isOpenFire, String nick){
		super.renderView(msgInfo, timeShow, isOpenFire, nick);
		
		initSysLayout(timeShow);
		getSysMsgRender(mSysMsg).renderView(msgInfo, nick);
	}
	
	/**
	 * 初始化系统布局
	 * @param msgInfo
	 * @param timeShow
	 */
	private void initSysLayout(boolean timeShow){
		mSysLayout.setVisibility(View.VISIBLE);
		if(mMsgInfo.sender == EgmConstants.System_Sender_Id.TYPE_XIAOAI){
			ViewCompat.setBackground(mSysProfile,mSysLayout.getContext().getResources().getDrawable(R.drawable.icon_mesg_portrait_ai));
		}else if(mMsgInfo.sender == EgmConstants.System_Sender_Id.TYPE_YIXIN){
			ViewCompat.setBackground(mSysProfile,mSysLayout.getContext().getResources().getDrawable(R.drawable.icon_mesg_portrait_yixin));
		}
		
		if(timeShow){
			mSysTime.setVisibility(View.VISIBLE);
			mSysTime.setText(TimeFormatUtil.covert2DisplayTime(mMsgInfo.time));
		}else{
			mSysTime.setVisibility(View.GONE);
		}
	}
	
	private RenderSysMsgItem getSysMsgRender(View view){
		RenderSysMsgItem render = null ;
		if(view.getTag() != null && view.getTag() instanceof RenderSysMsgItem){
			render = (RenderSysMsgItem)view.getTag();
		}else{
			render = new RenderSysMsgItem(view);
			render.setAdapter(adapter);
			render.setOuterContinaer(this);
			render.setUploadPictureHelper(mUploadPictureHelper);
			view.setTag(render);
		}
		return render ;
	}
}
