package com.netease.engagement.fragment;


import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentWhoInviteMe extends FragmentBase {
	
	private TextView mQurey;
	private EditText mInviteCodeEdit;
	
	private String mInviteCode;

	private CustomActionBar mCustomActionBar ;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
		mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
		mCustomActionBar.setLeftVisibility(View.INVISIBLE);
		
		mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
		mCustomActionBar.setMiddleTitle(R.string.who_invite_me);
		mCustomActionBar.setMiddleTitleSize(20);
		
		mCustomActionBar.setRightBackgroundResource(R.drawable.titlebar_a_selector);
		mCustomActionBar.setRightTitleColor(getResources().getColor(R.color.purple_dark));
		mCustomActionBar.setRightAction(-1, R.string.close);
		mCustomActionBar.setRightVisibility(View.VISIBLE);
		mCustomActionBar.setRightClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clickBack();
			}
		});	
		EgmService.getInstance().addListener(mCallBack);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout root = (LinearLayout)inflater.inflate(R.layout.fragment_who_invite_me, container, false);
		init(root);
		return root;
	}
	
	private void init(View root) {
		mQurey = (TextView) root.findViewById(R.id.invite_code_query);
		mQurey.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				doQueryInvitor();
			}
		});
		
		mInviteCodeEdit = (EditText) root.findViewById(R.id.invite_code_edit);
	}
	
	private void doQueryInvitor() {
		mInviteCode = mInviteCodeEdit.getText().toString();
		if(TextUtils.isEmpty(mInviteCode)) {
			ToastUtil.showToast(getActivity(), "请输入邀请码");
		} else if(Long.valueOf(mInviteCode) <= 10000) { 
			ToastUtil.showToast(getActivity(), "邀请码格式不对");
		}
		else {
			EgmService.getInstance().doQueryInvitor(Long.valueOf(mInviteCode));
			showWatting(null, "查询中", true);
		}
	}
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        EgmService.getInstance().removeListener(mCallBack);
    }
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onQueryInvitor(int transactionId, Boolean exist) {
			stopWaiting();
			if(exist) {
				InputMethodManager mImm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				mImm.hideSoftInputFromWindow(mInviteCodeEdit.getWindowToken(), 0);
				ActivityUserPage.startActivity(FragmentWhoInviteMe.this, mInviteCode, Integer.toString(EgmConstants.SexType.Female));
			} else {
				ToastUtil.showToast(getActivity(), "没有查询到会员");
			}
		}

		@Override
		public void onQueryInvitorError(int transactionId, int errCode,
				String err) {
			stopWaiting();
			showToast(err);
		}
		
	};
	
}
