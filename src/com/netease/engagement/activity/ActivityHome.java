package com.netease.engagement.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.netease.android.video.ui.VideoFileUtil;
import com.netease.common.log.NTLog;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.dataMgr.EmotConfigManager;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.MsgDataManager;
import com.netease.engagement.dataMgr.TopicDataManager;
import com.netease.engagement.fragment.FragmentHome;
import com.netease.engagement.pushMsg.MessagePushUtil;
import com.netease.engagement.view.CustomViewPager;
import com.netease.engagement.view.HomeTabView;
import com.netease.engagement.view.ShareDialog;
import com.netease.framework.activity.FrameworkActivityManager;
import com.netease.mobileanalysis.MobileAgent;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MsgExtra;
import com.netease.service.protocol.meta.VersionInfo;

/**
 * 主界面，包含推荐、排行、聊天和我四个Fragment。
 * @version 1.0
 */
public class ActivityHome extends ActivityEngagementBase{
    private static final String TAG = "ActivityHome";
    public static final int CONTAINER_ID = R.id.activity_home_container_id;
    public static final String EXTRA_IS_FROM_REGISTER = "extra_is_from_register";
    public static final String EXTRA_TAB_INDEX = "extra_tab_index";
    public static final String EXTRA_PORTRAIT_STATUS = "extra_portrait_status";
    
    /** Tab导航栏 */
    private FragmentHome mFragment;
    
    private boolean mbBackKeyDown = false;
//    private int mCurrentTab = EgmConstants.INDEX_RECOMMEND;
    private AlertDialog mDialog;
    private static int UPDATE_ALERT_TIMES = 10;
    private static int mCheckVersionTid = -1;
    
    
    public static void startActivity(Context context, boolean isFromRegister, int portraitStatus){
        Intent intent = new Intent(context, ActivityHome.class);
        intent.putExtra(EXTRA_IS_FROM_REGISTER, isFromRegister);
        intent.putExtra(EXTRA_PORTRAIT_STATUS, portraitStatus);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity)){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        
        context.startActivity(intent);
    }
    
    public static void startActivity(Context context, int tabIndex){
        Intent intent = new Intent(context, ActivityHome.class);
        intent.putExtra(EXTRA_TAB_INDEX, tabIndex);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        super.setCustomActionBar();
        this.getWindow().setBackgroundDrawableResource(R.color.app_background);
        
        boolean isFromRegister = false;
        int currentTab = FragmentHome.TAB_INDEX_RECOMMEND;
        int portraitStatus = -1;
        
        try {
			Bundle extra = this.getIntent().getExtras();
			if(extra != null){
			    isFromRegister = extra.getBoolean(EXTRA_IS_FROM_REGISTER, false);
			    currentTab = extra.getInt(EXTRA_TAB_INDEX, FragmentHome.TAB_INDEX_RECOMMEND);
			    portraitStatus = extra.getInt(EXTRA_PORTRAIT_STATUS, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        Intent intent = getIntent();
        if(intent != null){
            redirectToPage(intent);
        }
        
        EgmPrefHelper.putOpenAppTime(this, System.currentTimeMillis()); // 记录打开app的时间
        new Handler().postDelayed(mMyRunnable, 2000); 
        
        LinearLayout linear = new LinearLayout(this);
        linear.setId(CONTAINER_ID);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);
        setContentView(linear);

        if (findViewById(CONTAINER_ID) != null && savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            mFragment = new FragmentHome();
            mFragment.setIsFromRegister(isFromRegister);
            mFragment.setPortraitStatus(portraitStatus);
            mFragment.setCurrentTab(currentTab);
            
            ft.add(CONTAINER_ID, mFragment);
            ft.commit();
        }
        EgmService.getInstance().addListener(mEgmCallBack);
        EngagementApp.getAppInstance().setGender(-1);
        NTLog.i(TAG, "onCreate");
        // 初始化
        InitTask initTask = new InitTask(this);
        initTask.execute();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        redirectToPage(intent);
        NTLog.i(TAG, "onNewIntent");
        
        // ActivityHome是单例，从别的地方要跳回ActivityHome并指定tab，就会进来这里
        try {
			Bundle extra = intent.getExtras();
			if(extra != null && extra.containsKey(EXTRA_TAB_INDEX)){
			    final int currentTab = extra.getInt(EXTRA_TAB_INDEX);
			    if(mFragment != null){
			        new Handler().postDelayed(new Runnable() {
			            
			            @Override
			            public void run() {
			                mFragment.setCurrentTab(currentTab);
			            }
			        }, 200);
			        
			    } 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    
    /** push消息跳转 */
    private void redirectToPage(Intent intent) {
        String tag = intent.getAction();
        if (!TextUtils.isEmpty(tag)) {
           //push消息跳转
            try {
                int type = Integer.valueOf(tag);
                if (type > 0) {
                    NTLog.i(TAG, "redirectToPage push type is " + type);
                    switch (type) {
                        case EgmConstants.NOTIFICATION_CHAT_TYPE_ONE:
                            ChatItemUserInfo userInfo = intent.getParcelableExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO);
                            if(userInfo != null){
                                NTLog.i(TAG, "redirectToPage start chat ");
                              //进入聊天界面
                                ActivityPrivateSession.startActivity(this, userInfo);
                            }
                            break;
                        case EgmConstants.NOTIFICATION_CHAT_TYPE_MULTI:
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        NTLog.i(TAG, "redirectToPage EgmConstants.INDEX_CHAT ");
                                        if(mFragment != null){
                                            mFragment.setCurrentTab(EgmConstants.INDEX_CHAT);
                                        }
                                    }
                                }, 200);
                            break;
                        case EgmConstants.NOTIFICATION_ACTIVITIES:
                            if(mFragment != null){
                                mFragment.setCurrentTab(EgmConstants.INDEX_RECOMMEND);
                            }
                            String content = intent.getStringExtra(EgmConstants.BUNDLE_KEY.PUSH_ACTIVITY_CONTENT);
                            MsgExtra extra = intent.getParcelableExtra(EgmConstants.BUNDLE_KEY.PUSH_ACTIVITY_EXTRA);
                            if(extra != null){
                                showActionDialog(content, extra);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
            }
        }
    }
    
    private Runnable mMyRunnable = new Runnable() {
        @Override
        public void run() {
        	 	if(!MessagePushUtil.isBind) {
                 MessagePushUtil.bindAccount(getApplicationContext());;  // 绑定消息推送帐号
             }
        	 	mCheckVersionTid = EgmService.getInstance().doCheckVersion();
       }
    };
    
    @Override
    public void onRestart() {
        super.onRestart();
        if(!MessagePushUtil.isBind) {
            MessagePushUtil.bindAccount(getApplicationContext());;  // 绑定消息推送帐号
        }
        MessagePushUtil.restartService(getApplicationContext());
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialog !=null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
        MobileAgent.sessionEnd(this.getApplicationContext());
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK) {
                mbBackKeyDown = true;
                return true;
        }
        mbBackKeyDown = false;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mbBackKeyDown){
                mbBackKeyDown = false;
//              onExit();
                moveTaskToBack(true);
                return true;
            }
        }
        mbBackKeyDown = false;
        return super.onKeyUp(keyCode, event);
    };
    
    public void onExit() {
        
        mDialog = EgmUtil.createEgmBtnDialog(this, getResources().getString(R.string.tip), getResources().getString(R.string.exit_app),
                getResources().getString(R.string.cancel),  getResources().getString(R.string.confirm),
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int which = (Integer)view.getTag();
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                finish();
                                EngagementApp.getAppInstance().onExitApp();
                                break;
                            default:
                                break;
                        }

                        if (mDialog !=null && mDialog.isShowing()) {
                            mDialog.dismiss();
                            mDialog = null;
                        }
                    }
                });
        mDialog.show(); 
        
    }
    private void showActionDialog(String msgContent,MsgExtra msgExtra){
        final String url =  msgExtra.url;
        final String title = msgExtra.title;
        mDialog = EgmUtil.createEgmBtnDialog(this, msgExtra.title, msgContent,
                msgExtra.button2,  msgExtra.button1,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int which = (Integer)view.getTag();
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                ActivityAction.startActivity(ActivityHome.this,url,title);
                                break;
                            default:
                                break;
                        }

                        if (mDialog !=null && mDialog.isShowing()) {
                            mDialog.dismiss();
                            mDialog = null;
                        }
                    }
                });
        mDialog.show(); 
        
            
    }
    private EgmCallBack mEgmCallBack = new EgmCallBack() {

        @Override
        public void onLoopBack(int transactionId, LoopBack lb) {
            if(lb != null ){
                switch (lb.mType) {
                    case EgmConstants.LOOPBACK_TYPE.acc_logout:
                        NTLog.i(TAG, "switch_account");
                        EngagementApp.getAppInstance().clearAccountConfig();
                        ActivityAccountEntrance.startActivity(ActivityHome.this);
                        finish();   
                }
            }
        };
        @Override
        public void onRelogin(int transactionId, int errCode, String err) {
            EngagementApp.getAppInstance().doRelogin();
            FrameworkActivityManager.getInstance().FinishAllActivity();
            ActivityAccountEntrance.startActivity(ActivityHome.this);
        };
        @Override
        public void onCheckVersion(int transactionId,VersionInfo version) {
        		if(mCheckVersionTid != transactionId)
        			return;
            stopWaiting();
            boolean showAlert = false;

            if (version != null && version.hasNew) {
                String pre = EgmPrefHelper.getPreUpdateVersion(getApplicationContext());
                if(version.version.equals(pre)){
                    int times = EgmPrefHelper.getUpdateAlertTime(getApplicationContext());
                    if(times > 0 && times < UPDATE_ALERT_TIMES){
                        EgmPrefHelper.putUpdateAlertTimes(getApplicationContext(),++times);
                    } else {
                        showAlert = true;
                        EgmPrefHelper.putUpdateAlertTimes(getApplicationContext(),1);
                    }  
                } else {
                    showAlert = true;
                    EgmPrefHelper.putPreUpdateVersion(getApplicationContext(), version.version);
                    EgmPrefHelper.putUpdateAlertTimes(getApplicationContext(),1);
                }
                
                if (version.forceUpdate) { // 如果是强制升级，每次都弹框
                		showAlert = true;
                }
                if (showAlert) {
                 	EgmUtil.showUpdateDialog(ActivityHome.this, version);
                }
            }
        }
    };
    
    public CustomViewPager getViewPager(){
    	if(mFragment != null){
    		return mFragment.getViewPager();
    	}
    	return null ;
    }
    
    public HomeTabView getChatTab(){
    	if(mFragment != null){
    		return mFragment.mChatTab ;
    	}
    	return null ;
    }
    
    public HomeTabView getMyselfTab(){
    	if(mFragment != null){
    		return mFragment.mMySelfTab ;
    	}
    	return null ;
    }
    
    public void setHomeFragment(FragmentHome homeFragment){
        NTLog.i(TAG, "setHomeFragment homeFragment is  " + homeFragment);
        if(homeFragment != null){
            mFragment = homeFragment;
        }
    }
    
    public FragmentHome getHomeFragment(){
        return mFragment;
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if(mFragment.mViewPagerScrollState != ViewPager.SCROLL_STATE_IDLE && ev.getAction() == MotionEvent.ACTION_DOWN) {
    		Log.e("dispatchTouchEvent", "1");
    		return false;
    	} else {
    		return super.dispatchTouchEvent(ev);
    	}
    }
    
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (ShareDialog.mSsoHandler != null) {
        	ShareDialog.mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
    /*
     * 1,统计初始化
     */
    static class InitTask extends AsyncTask<Object, Object, Object> {
        private Context mContext;
        
        InitTask(Context context) {
            mContext = context.getApplicationContext();
        }
        
        @Override
        protected Object doInBackground(Object... params) {
            NTLog.i("InitTask", "doInBackground");
            
            MsgDataManager.getInstance();
            //检查配置数据
            long now = System.currentTimeMillis() ;
            if(now - EgmPrefHelper.getCheckConfigDataTime(mContext) >= EgmConstants.CHECK_CONFIG_DURATION){
                EgmPrefHelper.putCheckConfigDataTime(mContext, now);
                Handler handler = new Handler(Looper.getMainLooper());
                //获取用户配置数据
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        ConfigDataManager.getInstance().getUserConfig();
                    }
                }, 500);
                
                //获取礼物数据
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        GiftInfoManager.getGiftConfig();
                    }
                }, 1000);
                
                //获取表情配置信息
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        EmotConfigManager.getInstance().getEmotConfig();
                    }
                }, 1500);
                
                //获取话题数据
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        TopicDataManager.getInstance().getTipicConfig(null);
                    }
                }, 2000);
             
            }
            //删除易信通知相关
            LastMsgDBManager.delMsgByUid(1);
            MsgDBManager.delMsgByUid(1);
            //初始化统计
            try{
                MobileAgent.getErrorOnCreate(mContext);
                MobileAgent.sessionStart(mContext);
            }catch(Exception e) {
                NTLog.e("MobileAgent", e.toString());
            }
            
            VideoFileUtil.deleteOldFiles();
            
            return null;
        }
        
    } 
}
