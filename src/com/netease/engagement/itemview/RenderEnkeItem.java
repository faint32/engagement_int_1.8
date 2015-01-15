package com.netease.engagement.itemview;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.view.HeadView;
import com.netease.engagement.view.ProfileView;
import com.netease.service.protocol.meta.AdmirerUserInfo;

public class RenderEnkeItem {
	private View rootView ;
	private AdmirerUserInfo enkeInfo ;
	private int rank ;
	public RenderEnkeItem(View root,AdmirerUserInfo enkeInfo,int rank){
		rootView = root ;
		this.enkeInfo = enkeInfo ;
		this.rank = rank ;
	}
	public void renderView(){
		TextView txtRank = (TextView)rootView.findViewById(R.id.enke_rank);
		txtRank.setText(String.valueOf(rank));
		if(rank <= 3){
			txtRank.setTextColor(Color.parseColor("#bd9fdf"));
		}else{
			txtRank.setTextColor(Color.parseColor("#C6C6C6"));
		}
		if(rank%2 == 0){
			rootView.setBackgroundColor(Color.parseColor("#f8f8f8"));
		}else{
			rootView.setBackgroundColor(Color.parseColor("#ffffff"));
		}
		
		HeadView profile = (HeadView)rootView.findViewById(R.id.enke_profile);
		profile.setImageUrl(false,HeadView.PROFILE_SIZE_SMALL, enkeInfo.portraitUrl192,
				EgmConstants.SexType.Male);
		profile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityUserPage.startActivity(EngagementApp.getAppInstance().getApplicationContext(), 
						String.valueOf(enkeInfo.uid), 
						String.valueOf(EgmConstants.SexType.Male));
			}
		});
		
		TextView txtNick = (TextView)rootView.findViewById(R.id.enke_nick);
		if(!TextUtils.isEmpty(enkeInfo.nick)){
			txtNick.setText(enkeInfo.nick);
		}
		TextView txtPrivacy = (TextView)rootView.findViewById(R.id.enke_privacy);
		txtPrivacy.setText("亲密度："+enkeInfo.intimacy);
	}
}
