package com.netease.engagement.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.common.cache.CacheManager;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityAppCenter;
import com.netease.engagement.activity.ActivityDownload;
 
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.SlideSwitch;
import com.netease.engagement.widget.SlideSwitch.OnChangedListener;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.VersionInfo;


public class FragmentSettingMain extends FragmentBase implements OnClickListener{
    private FragmentManager mFragmentManager;
    private TextView mMsgSetContent;
    private AlertDialog mDialog;
    private long mUid;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();
        EgmService.getInstance().addListener(mCallBack);
        mUid = ManagerAccount.getInstance().getCurrentId();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_main, container, false);
        initViews(view);
        return view;
    }
    private void initViews(View view) { 
        LinearLayout item  = null;
        TextView tag_name = null;
        TextView tag_content = null;
        
        item = (LinearLayout)view.findViewById(R.id.setting_msg);
        tag_name = (TextView) item.findViewById(R.id.tag_title);
        mMsgSetContent = (TextView) item.findViewById(R.id.tag_content);
        tag_name.setText(R.string.setting_msg);
        refreshMsgSetContent();
        mMsgSetContent.setVisibility(View.VISIBLE);
        item.setOnClickListener(this);
        
        item = (LinearLayout) view.findViewById(R.id.setting_anti_harassment);
        if (ManagerAccount.getInstance().isMale()) {
			tag_name = (TextView) item.findViewById(R.id.tag_title);
			tag_name.setText(R.string.setting_anti_harassment);
			item.setOnClickListener(this);
		}
		else {
			item.setVisibility(View.GONE);
		}
        
        item = (LinearLayout)view.findViewById(R.id.setting_feedback);
        tag_name = (TextView) item.findViewById(R.id.tag_title);
        tag_content = (TextView) item.findViewById(R.id.tag_content);
        tag_name.setText(R.string.setting_feedback);
        item.setOnClickListener(this);
        
        
        item = (LinearLayout)view.findViewById(R.id.setting_checkversion);
        tag_name = (TextView) item.findViewById(R.id.tag_title);
        tag_content = (TextView) item.findViewById(R.id.tag_content);
        tag_name.setText(R.string.setting_checkversion);
        tag_content.setText(getResources().getString(R.string.setting_version_current,EgmUtil.getNumberVersion(getActivity())));
        tag_content.setVisibility(view.VISIBLE);
        item.setOnClickListener(this);
        
        item = (LinearLayout)view.findViewById(R.id.setting_clearcache);
        tag_name = (TextView) item.findViewById(R.id.tag_title);
        tag_content = (TextView) item.findViewById(R.id.tag_content);
        tag_name.setText(R.string.setting_clearcache);
        item.setOnClickListener(this);
        
        item = (LinearLayout)view.findViewById(R.id.setting_helpcenter);
        tag_name = (TextView) item.findViewById(R.id.tag_title);
        tag_content = (TextView) item.findViewById(R.id.tag_content);
        tag_name.setText(R.string.setting_helpcenter);
        item.setOnClickListener(this);
      
        item = (LinearLayout) view.findViewById(R.id.setting_gifts_pic_tone);
		if (ManagerAccount.getInstance().isMale()) {
			tag_name = (TextView) item.findViewById(R.id.tag_title);
			tag_name.setText(R.string.setting_gits_pric_tone);
//	        mUid=Long.parseLong(ManagerAccount.getInstance().getCurrentAccount().mUserId) ;
			SlideSwitch msSlideSwitch = (SlideSwitch) item.findViewById(R.id.tag_switch);
			msSlideSwitch.setCheck(EgmPrefHelper.getGiftsPicOn(getActivity(), mUid));
			msSlideSwitch.setOnChangedListener(new OnChangedListener() {

				@Override
				public void OnChanged(View v, boolean checkState) {
					EgmPrefHelper.putGiftsPicOn(getActivity(), checkState, mUid);
				}
			});
		}
		else {
			item.setVisibility(View.GONE);
		}
		
        item = (LinearLayout)view.findViewById(R.id.setting_app_center);
        tag_name = (TextView)item.findViewById(R.id.tag_title);
        tag_content = (TextView)item.findViewById(R.id.tag_content);
        tag_name.setText(R.string.setting_app_center);
        item.setOnClickListener(this);
        
        TextView logout = (TextView) view.findViewById(R.id.logout);
        logout.setOnClickListener(this);
        
    }
    private void refreshMsgSetContent(){
        if (mMsgSetContent != null) {
            if (EgmPrefHelper.getPushOn(getActivity())) {
                String content = getResources().getString(R.string.setting_msg_set_on);
                if (EgmPrefHelper.getSoundOn(getActivity())
                        && EgmPrefHelper.getShockOn(getActivity())
                        && EgmPrefHelper.getNoDisturbingOn(getActivity())) {
                    content += ": " + "声音、" + "振动、" + "免打扰";
                } else if (EgmPrefHelper.getSoundOn(getActivity())
                        && EgmPrefHelper.getShockOn(getActivity())) {
                    content += ": " + "声音、" + "振动";
                } else if (EgmPrefHelper.getSoundOn(getActivity())
                        && EgmPrefHelper.getNoDisturbingOn(getActivity())) {
                    content += ": " + "声音、" + "免打扰";
                } else if (EgmPrefHelper.getShockOn(getActivity())
                        && EgmPrefHelper.getNoDisturbingOn(getActivity())) {
                    content += ": " + "振动、" + "免打扰";
                } else if (EgmPrefHelper.getSoundOn(getActivity())) {
                    content += ": " + "声音";
                } else if (EgmPrefHelper.getShockOn(getActivity())) {
                    content += ": " + "振动";
                } else if (EgmPrefHelper.getNoDisturbingOn(getActivity())) {
                    content += ": " + "免打扰";
                }
                mMsgSetContent.setText(content);
            } else {
                mMsgSetContent.setText(R.string.setting_msg_set_off);
            }
        }
        
    }
    @Override
    public void onResume() {
        super.onResume();
        CustomActionBar customActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        customActionBar.setLeftAction(R.string.back);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        customActionBar.setMiddleTitle(R.string.setting);
        customActionBar.setMiddleTitleSize(20);
        customActionBar.hideRightTitle();
        customActionBar.setLeftClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        refreshMsgSetContent();
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.setting_msg:  
                Fragment fragMsg = new FragmentSettingMsg();
                mFragmentManager.beginTransaction().replace(R.id.activity_setting_container_id, fragMsg)
                .addToBackStack(null).commit();
                break;
            case R.id.setting_feedback:
                Fragment fragFeedback = new FragmentFeedBack();
                mFragmentManager.beginTransaction().replace(R.id.activity_setting_container_id, fragFeedback)
                        .addToBackStack(null).commit();
                break;
            case R.id.setting_checkversion:
                showWatting("");
                EgmService.getInstance().doCheckVersion();
              
                break;
            case R.id.setting_clearcache:
                mDialog = EgmUtil.createEgmBtnDialog(getActivity(), null, getResources().getString(R.string.setting_clearcache_tip),
                            getResources().getString(R.string.setting_clearcache_no),  getResources().getString(R.string.setting_clearcache_yes),
                            new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    int which = (Integer)view.getTag();
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            showWatting(getResources().getString(R.string.setting_clearcache_ing));
                                            long cache = CacheManager.getCacheSize();
                                            CacheManager.deleteCache();
                                            if(cache < 10*EgmUtil.SIZE_UINT_MB){
                                                new Handler().postDelayed(mMyRunnable, 1000);
                                            } else if(cache >= 10*EgmUtil.SIZE_UINT_MB && cache < 50*EgmUtil.SIZE_UINT_MB){
                                                new Handler().postDelayed(mMyRunnable, 3000);
                                            } else {
                                                new Handler().postDelayed(mMyRunnable, 5000);
                                            }
                                            break;
                                        default:
                                            break;
                                    }

                                    if (mDialog.isShowing()) {
                                        mDialog.dismiss();
                                        mDialog = null;
                                    }
                                }
                            });
                mDialog.show();

                break;
            case R.id.setting_helpcenter:
                Fragment fragHelp = new FragmentHelpCenter();
                mFragmentManager.beginTransaction().replace(R.id.activity_setting_container_id, fragHelp)
                        .addToBackStack(null).commit();
                break;
            case R.id.setting_app_center:
                // Fragment fragAppCenter = new FragmentAppCenter();
                // mFragmentManager.beginTransaction()
                // .replace(R.id.activity_setting_container_id, fragAppCenter)
                // .addToBackStack(null).commit();
                // 为了WebView中监听返回键方便，采用Activity而不是Fragment
                ActivityAppCenter.startActivity(getActivity());
                break;
            case R.id.logout:
                mDialog = EgmUtil.createEgmBtnDialog(getActivity(), null, getResources().getString(R.string.setting_logout_tip),
                        getResources().getString(R.string.logout_no),  getResources().getString(R.string.logout_yes),
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                int which = (Integer)view.getTag();
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        LoopBack lb = new LoopBack();
                                        lb.mType = EgmConstants.LOOPBACK_TYPE.acc_logout;
                                        EgmService.getInstance().doLoopBack(lb);
                                        EngagementApp.getAppInstance().doLogout();
                                        getActivity().finish();
                                        break;
                                    default:
                                        break;
                                }

                                if (mDialog.isShowing()) {
                                    mDialog.dismiss();
                                    mDialog = null;
                                }
                            }
                        });
                mDialog.show();
               
                break;
            case R.id.setting_anti_harassment:
            	int level = EgmPrefHelper.getUserLevel(getActivity(), mUid);
            	if(level < 5){
            		showToast(R.string.setting_anti_harassment_closed);
            	}else{
            		Fragment fragAntiHarassment = new FragmentSettingAntiHarassment();
                    mFragmentManager.beginTransaction().replace(R.id.activity_setting_container_id, fragAntiHarassment)
                            .addToBackStack(null).commit();
            	}

            	break;
        }
    }
    private Runnable mMyRunnable = new Runnable() {
        @Override
        public void run() {
            showToast(R.string.setting_clearcache_done);
            stopWaiting();
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialog !=null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    };
 private EgmCallBack mCallBack = new EgmCallBack(){
        @Override
        public void onCheckVersion(int transactionId,VersionInfo version) {
            stopWaiting();
            if (version != null && version.hasNew) {
            		EgmUtil.showUpdateDialog(getActivity(), version);
            } else {
                showToast(R.string.setting_newest_version);
            }
        }
        @Override
        public void onCheckVersionError(int transactionId, int errCode, String err) {
            stopWaiting();
            showToast(err);
        };
    };
}
