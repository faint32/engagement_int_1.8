package com.netease.engagement.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.common.log.NTLog;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.dataMgr.EmotConfigManager;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.TopicDataManager;
import com.netease.engagement.pushMsg.MessagePushUtil;
import com.netease.mobileanalysis.MobileAgent;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;


/**
 * App启动进入的界面。
 * 在这里自动登录。
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class ActivitySplash extends ActivityEngagementBase {
    private ManagerAccount.Account mAccount;
//    private LoginHelper mLoginHelper;
    private int mTid;
    
    /** 首发包的渠道图标 */
    private ImageView mChannelIcon;
    /** 首发包的渠道文字 */
    private TextView mChannelText;
    private long splashDuration = 1*1000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        this.getActionBar().hide();
        setContentView(R.layout.activity_splash_layout);
        mChannelIcon = (ImageView)findViewById(R.id.splash_channel_icon);
        mChannelText = (TextView)findViewById(R.id.splash_channel_text);
        if(EgmUtil.isShoufa(this)){
            mChannelIcon.setVisibility(View.VISIBLE);
            mChannelText.setVisibility(View.VISIBLE);
            if(EgmUtil.getAppChannelID(this).equals(EgmConstants.CHANNEL_360)){
                mChannelIcon.setBackgroundResource(R.drawable.icon_platform_logo_360);
                mChannelText.setText(R.string.splash_channel_360);
            }else if(EgmUtil.getAppChannelID(this).equals(EgmConstants.CHANNEL_ANDROID91)){
                mChannelIcon.setBackgroundResource(R.drawable.icon_platform_logo_91);
                mChannelText.setText(R.string.splash_channel_91);
            }else if(EgmUtil.getAppChannelID(this).equals(EgmConstants.CHANNEL_NDUO)){
                mChannelIcon.setBackgroundResource(R.drawable.icon_platform_logo_nduoa);
                mChannelText.setText(R.string.splash_channel_nduo);
            }else if(EgmUtil.getAppChannelID(this).equals(EgmConstants.CHANNEL_GOAPK)){
                mChannelIcon.setBackgroundResource(R.drawable.icon_platform_logo_anzhi);
                mChannelText.setText(R.string.splash_channel_anzhi);
            }else if(EgmUtil.getAppChannelID(this).equals(EgmConstants.CHANNEL_BAIDU)){
                mChannelIcon.setBackgroundResource(R.drawable.icon_platform_logo_baidu);
                mChannelText.setVisibility(View.GONE);
            }else if(EgmUtil.getAppChannelID(this).equals(EgmConstants.CHANNEL_HIAPK)){
                mChannelIcon.setBackgroundResource(R.drawable.icon_platform_logo_91);
                mChannelText.setText(R.string.splash_channel_91);
            } else if(EgmUtil.getAppChannelID(this).equals(EgmConstants.CHANNEL_QQ)){
                mChannelIcon.setBackgroundResource(R.drawable.icon_platform_logo_yingyongbao);
                mChannelText.setText(R.string.splash_channel_qq);
            } else{
                mChannelIcon.setVisibility(View.GONE);
                mChannelText.setVisibility(View.GONE);
            }
            
        }else{
            mChannelIcon.setVisibility(View.GONE);
            mChannelText.setVisibility(View.GONE);
        }
        
        ManagerAccount accountManager = ManagerAccount.getInstance();
        mAccount = accountManager.getCurrentAccount();
        NTLog.e("ActivitySplash","mAccount is " + mAccount);
        
        
        doComeinApp();  // 改成登录urs返回结果后再进主界面，这样就不需要延迟2秒了
        
        // 消息推送平台初始化
        NTLog.i("MessagePushUtil","init push");
        MessagePushUtil.init(getApplicationContext());
        MessagePushUtil.startService(getApplicationContext());
        MessagePushUtil.register(getApplicationContext());
        
        // 统计平台初始化
        MobileAgentTask mobileAgentTask = new MobileAgentTask(this);
        mobileAgentTask.execute();
        
        //检查配置数据
        long now = System.currentTimeMillis() ;
        //if(now - EgmPrefHelper.getCheckConfigDataTime(getApplicationContext()) >= EgmConstants.CHECK_CONFIG_DURATION){
        	//获取用户配置数据
        	new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					ConfigDataManager.getInstance().getUserConfig();
				}
        	}, 500);
        	
        	//获取礼物数据
        	new Handler().postDelayed(new Runnable(){
        		@Override
        		public void run() {
        			GiftInfoManager.getGiftConfig();
        		}
        	}, 1000);
        	
        	//获取表情配置信息
        	new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					EmotConfigManager.getInstance().getEmotConfig();
				}
        	}, 1500);
        	
        	//获取话题数据
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    TopicDataManager.getInstance().getTipicConfig(null);
                }
            }, 2000);
        	
        	EgmPrefHelper.putCheckConfigDataTime(getApplicationContext(), now);
        //}
    }
    
    /** 处理进入app的逻辑 */
    private void doComeinApp(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(ManagerAccount.getInstance().hasAccount()){  // 有帐号，也就是已经有过登录注册
                    if(mAccount != null){ // 已登录，进入主界面
                        gotoHome(-1);
                    }
                    else{   // 未登录，那就去登录注册入口
                        gotoAccountEntrance();
                    }
                } else{   // 没有帐号，也就是第一次使用，需要先选择性别
                    ActivityWelcome.startActivity(ActivitySplash.this, ActivityWelcome.TYPE_FIRST);
                    finish();
                }
                
            }
        }, splashDuration);//强制splash显示1秒
    }
    
    private void gotoHome(int portaitStatus){
        ActivityHome.startActivity(this, false, portaitStatus);
        this.finish();
    }
    
    private void gotoAccountEntrance(){
        ActivityAccountEntrance.startActivity(this);
        this.finish();
    }
    
    static class MobileAgentTask extends AsyncTask<Object, Object, Object> {
        private Context mContext;
        
        MobileAgentTask(Context context) {
            mContext = context.getApplicationContext();
        }
        
        @Override
        protected Object doInBackground(Object... params) {
            try{
                MobileAgent.getErrorOnCreate(mContext);
                MobileAgent.sessionStart(mContext);
            }catch(Exception e) {
                NTLog.e("MobileAgent", e.toString());
            }
            return null;
        }
        
    }  
    
    @Override
    protected void onDestroy() {
        MobileAgent.sessionEnd(this.getApplicationContext());
        super.onDestroy();
        
    }
}
