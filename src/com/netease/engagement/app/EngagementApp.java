package com.netease.engagement.app;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Handler;
import android.text.TextUtils;

import com.netease.android.activity.LofterApplication;
import com.netease.common.cache.CacheManager;
import com.netease.common.log.NTLog;
import com.netease.common.service.BaseService;
import com.netease.date.R;
import com.netease.engagement.pushMsg.MessagePushUtil;
import com.netease.engagement.pushMsg.NotificationBarMgr;
import com.netease.framework.activity.FrameworkActivityManager;
import com.netease.framework.skin.SkinConfig;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.MsgDBOpenHelper;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.VersionInfo;
import com.netease.share.sticker.util.DependentUtils;
import com.netease.share.sticker.util.ScreenUtil;


public class EngagementApp extends BaseApplication {
	public static final String TAG = "EngagementApp";
	private static EngagementApp sApp;
	private long mLastUploadTime = 0;//记录上次上传统计时间
	private int mGender = -1;//记录性别选择的状态,应用运行时临时变量
	private int mCheckVersionTid = -1;//强制升级时检查版本更新的id
	private boolean mForceUpProcessing = false;

	static public EngagementApp getAppInstance() {
		if (sApp == null)
			throw new NullPointerException("sApp not create or be terminated!");
		return sApp;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sApp = this;
		BaseService.initServiceContext(this);
		initStatic();
		String packetName = getPackageName();
//		NTLog.init(packetName);
		NTLog.initPath(CacheManager.getRoot() + "/");
		SkinConfig.initSkinPath(packetName);
		NTLog.i(TAG, " onCreate");
		
		// 易信表情组建初始化配置
		ScreenUtil.init(this);
		DependentUtils.init(this, "xxx");
		LofterApplication.init(this);
		EgmService.getInstance().addListener(mEgmCallBack);
	}
	private void initStatic(){
//	    SharedKeyMgr.initSharedKey();
	    CacheManager.FileCacheRoot = EgmConstants.CACHE_CHILD_DIR;	    
	}
	
	@Override
	public void onExceptionExit(Throwable exception) {
		if (exception != null) {
			StringWriter sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			NTLog.d(TAG, "onExceptionExit " + sw.toString());
//			MobileAgent.getErrorOnRunning(sApp, sw.toString());
//			String title = XoneConsts.APP_OS_NAME + XoneUtil.getNumberVersion(this) + "异常日志"; 
//			String user = ManagerAccount.getInstance().getCurrentAccountName();
//			XoneService.getInstance().doFeedBack(XoneConsts.FEEDBACK_TYPE.Crash, user, title, "\n\n" + sw.toString(), null, user, this);
		}
		finishApp();
	}
	
	public long getLastUploadTime(){
	    return mLastUploadTime;
	}
	
	public void setLastUploadTime(long time){
	    mLastUploadTime = time;
	}
	
	public void setGender(int gender){
		mGender = gender;
	}
	public int getGender(){
		return mGender;
	}
	
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory,
	        DatabaseErrorHandler errorHandler) {
	    NTLog.i(TAG, " openOrCreateDatabase name " + name);
	    if(name != null && name.equals(MsgDBOpenHelper.DataBaseName)){
	        String uid = ManagerAccount.getInstance().getCurrentAccountId();
	        NTLog.i(TAG, " openOrCreateDatabase uid " + uid);
	        File f = MsgDBOpenHelper.getDbFile(this,uid,name);
	        int flags = SQLiteDatabase.CREATE_IF_NECESSARY;
	        if ((mode & MODE_ENABLE_WRITE_AHEAD_LOGGING) != 0) {
	            flags |= SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING;
	        }
	        SQLiteDatabase db = SQLiteDatabase.openDatabase(f.getPath(), factory, flags, errorHandler);
	        return db;
	    }
	  
	    return super.openOrCreateDatabase(name, mode, factory, errorHandler);
	}
	
	
	
	public void onExitApp() {
		finishApp();
	}
	public void finishApp(){
        FrameworkActivityManager.getInstance().FinishAllActivity();
        NotificationBarMgr.getInstance(sApp).cancleAll();
//        EgmService.getInstance().doLogout(this);
        
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
//                KillApplication.killApplicationByPackageName(getApplicationContext(), getPackageName());
            }
        }, 1000);

    }
	//用户手动退出帐号使用
	public void doLogout(){
	    MessagePushUtil.cancelBind(getApplicationContext());
	    NotificationBarMgr.getInstance(sApp).cancleAll();
	    ManagerAccount manager = ManagerAccount.getInstance();
        manager.logout();
        EgmService.getInstance().doLogout(this);
        
        EgmPrefHelper.deleteWeiboAcc(this);
        clearAccountConfig();
	}
	//本地清理登录状态使用
	public void doRelogin(){
	    MessagePushUtil.cancelBind(getApplicationContext());
        NotificationBarMgr.getInstance(sApp).cancleAll();
        ManagerAccount manager = ManagerAccount.getInstance();
        manager.logout();
        
        EgmPrefHelper.deleteWeiboAcc(this);
        clearAccountConfig();
    }
	
	/** 退出登录或者重新登录后要将token等和帐号相对应的配置数据也清掉 */
	public void clearAccountConfig(){
        EgmProtocol.getInstance().setUrsToken("");
//        Log.d("Egm", "clear token");
//        EgmPrefHelper.putURSToken(this, "");    
        EgmPrefHelper.putNonce(this, "");
        EgmPrefHelper.putExpire(this, 0);
        EgmPrefHelper.putSignature(this, "");
        
        EgmPrefHelper.putOpenAppTime(this, 0);
        EgmPrefHelper.putTipUploadPicTime(this, 0);
        EgmPrefHelper.putTipOpenYuanfenTime(this, 0);
        EgmPrefHelper.putTipSelfInfoTime(this, 0);
        EgmPrefHelper.putTipUpdatePicTime(this, 0);
        EgmPrefHelper.putUpdatePicTime(this, 0);
	}
	
	 private EgmCallBack mEgmCallBack = new EgmCallBack(){
		 @Override
		public void onForceUpdate(int transactionId, int errCode, String err) {
			 if(!mForceUpProcessing){
				 mForceUpProcessing = true;
				 mCheckVersionTid = EgmService.getInstance().doCheckVersion();
			 }
		 };
		 @Override
	     public void onCheckVersion(int transactionId,VersionInfo version) {
			 if(mCheckVersionTid != transactionId)
     			return;
	            if (version != null && !TextUtils.isEmpty(version.downUrl)) {
	            		Activity top = FrameworkActivityManager.getInstance().getTopActivity();
	            		if(top != null){
	            			mForceUpProcessing = false;
	            			EgmUtil.showUpdateDialog(top, version);
	            		}
	            } 
		 }
		 @Override
	     public void onCheckVersionError(int transactionId, int errCode, String err) {
			 if(mCheckVersionTid != transactionId)
     			return;
	          ToastUtil.showToast(sApp, err);
	     };
		 
	 };
}
