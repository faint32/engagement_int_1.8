package com.netease.engagement.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityVideoPlay;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.MessageInfo;

public class FragmentFireVideo extends FragmentBase {
	
	public static FragmentFireVideo newInstance(MessageInfo msgInfo){
		FragmentFireVideo fragment = new FragmentFireVideo();
		Bundle bundle = new Bundle();
		bundle.putSerializable(EgmConstants.BUNDLE_KEY.MESSAGE_INFO, msgInfo);
		fragment.setArguments(bundle);
		return fragment ;
	}

	private MessageInfo msgInfo;
	private String mediaUrl;
	
	private LoadingImageView videoIv;
	private TextView fireTv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		msgInfo = (MessageInfo) this.getArguments().getSerializable(EgmConstants.BUNDLE_KEY.MESSAGE_INFO);
		
		EgmService.getInstance().addListener(mCallBack);
		
		EgmService.getInstance().doGetFireMessageMediaUrl(msgInfo);
		showWatting("加载中");
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
//	    CustomActionBar mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
//	    mCustomActionBar.hide();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = (View) inflater.inflate(R.layout.fragment_fire_video_layout,container,false);
		init(view);
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
	
	private void init(View root){
		if(root == null){
			return ;
		}
		
		videoIv = (LoadingImageView) root.findViewById(R.id.videoIv);
		fireTv = (TextView) root.findViewById(R.id.fireTv);
		
		videoIv.setOnClickListener(clickListener);
		fireTv.setOnClickListener(clickListener);
		
		String uri = msgInfo.msgContent ;
		videoIv.setLoadingImage(uri);
		
		if (TextUtils.isEmpty(mediaUrl)) {
			videoIv.setClickable(false);
		}
	}
	
	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.videoIv) {
				ActivityVideoPlay.startActivityForFire(v.getContext(), mediaUrl);
			} else if (v.getId() == R.id.fireTv) {
				FragmentFireVideo.this.getActivity().finish();
			}
		}
	};
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetFireMessageMediaUrlSucess(int transactionId, String mediaUrl) {
			FragmentFireVideo.this.mediaUrl = mediaUrl;
			videoIv.setClickable(true);
			FragmentFireVideo.this.stopWaiting();
		}
		
		@Override
		public void onGetFireMessageMediaUrlError(int transactionId, int errCode, String err) {
			FragmentFireVideo.this.stopWaiting();
			Toast.makeText(FragmentFireVideo.this.getActivity(), err, Toast.LENGTH_SHORT).show();
		}
	};
}
