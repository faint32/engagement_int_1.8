package com.netease.engagement.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.SlideSwitch;
import com.netease.engagement.widget.SlideSwitch.OnChangedListener;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.AntiHarassmentInfo;

public class FragmentSettingAntiHarassment extends FragmentBase {
	private LinearLayout mNoAvatarSetLay;
	private LinearLayout mLowLelSetLay;
	private SlideSwitch mNoAvatarSetSw;
	private SlideSwitch mLowLelSetSw;
	private boolean mBNoAvatarOn;
	private boolean mBLowLevelOn;
	private int mGetAntiHarassmentId;
	private int mUpdateAntiHarassmentId;
	private  CustomActionBar customActionBar;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        customActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        customActionBar.setLeftAction(R.string.back);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        customActionBar.setMiddleTitle(R.string.setting_anti_harassment);
        customActionBar.setMiddleTitleSize(20);
//        customActionBar.hideRightTitle();
        customActionBar.setLeftClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        customActionBar.setRightAction(-1, R.string.save);
        customActionBar.setRightClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AntiHarassmentInfo info = new AntiHarassmentInfo();
				info.setNoPortait(mBNoAvatarOn);
				info.setLevelLimit(mBLowLevelOn);
				mUpdateAntiHarassmentId = EgmService.getInstance().doUpdateAntiHarassment(info);
			}
		});
        EgmService.getInstance().addListener(mEgmCallBack);
    }
	 @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_set_anti_harassment,container,false);
        initViews(root);
        return root;
    }

	private void initViews(View view) {
		TextView tag_name = null;
		mNoAvatarSetLay = (LinearLayout) view.findViewById(R.id.setting_no_avatar);
		tag_name = (TextView) mNoAvatarSetLay.findViewById(R.id.tag_title);
		tag_name.setText(R.string.setting_no_avatar_set);
		mNoAvatarSetSw = (SlideSwitch) mNoAvatarSetLay
				.findViewById(R.id.tag_switch);
		mNoAvatarSetSw.setOnChangedListener(new OnChangedListener() {
			@Override
			public void OnChanged(View v, boolean checkState) {
				mBNoAvatarOn = checkState;
			}
		});

		mNoAvatarSetLay.setVisibility(View.GONE);
		mLowLelSetLay = (LinearLayout) view.findViewById(R.id.setting_low_level);
		tag_name = (TextView) mLowLelSetLay.findViewById(R.id.tag_title);
		tag_name.setText(R.string.setting_lowlevel_set);
		mLowLelSetSw = (SlideSwitch) mLowLelSetLay
				.findViewById(R.id.tag_switch);
		mLowLelSetSw.setOnChangedListener(new OnChangedListener() {
			@Override
			public void OnChanged(View v, boolean checkState) {
				mBLowLevelOn = checkState;
			}
		});

		mLowLelSetLay.setVisibility(View.GONE);
		mGetAntiHarassmentId = EgmService.getInstance().doGetAntiHarassment();

	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mEgmCallBack);
	};
	
	private void UpdateState(AntiHarassmentInfo info){
		if(info == null)
			return;
		if(mNoAvatarSetSw != null){
			mNoAvatarSetSw.setCheck(info.noPortaitOn());
			mBNoAvatarOn = info.noPortaitOn();
			mNoAvatarSetLay.setVisibility(View.VISIBLE);
		}
		if(mLowLelSetSw != null){
			mLowLelSetSw.setCheck(info.levelLimitOn());
			mBLowLevelOn = info.levelLimitOn();
			mLowLelSetLay.setVisibility(View.VISIBLE);
		}
	}
	
	EgmCallBack mEgmCallBack =  new EgmCallBack(){
		
		@Override
		public void onGetAntiHarassment(int transactionId, AntiHarassmentInfo info) {
			if(transactionId != mGetAntiHarassmentId)
                return;
            stopWaiting();
            UpdateState(info);
		};
		@Override
		public void onGetAntiHarassmentError(int transactionId, int errCode, String err) {
			 if(transactionId != mGetAntiHarassmentId)
	                return;
	            stopWaiting();
	            showToast(err);
	            if(customActionBar != null){
	            	customActionBar.hideRightTitle();
	            }
		};
		@Override
		public void onUpdateAntiHarassment(int transactionId, int code) {
			 stopWaiting();
			 showToast(getString(R.string.setting_success));
			 getFragmentManager().popBackStack();
		};
		@Override
		public void onUpdateAntiHarassmentError(int transactionId, int errCode, String err) {
			if(transactionId != mUpdateAntiHarassmentId)
                return;
			 stopWaiting();
	         showToast(err);
		};
		
		
	};
}
