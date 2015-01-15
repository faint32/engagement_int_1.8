package com.netease.engagement.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmLocationManager;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.LoginUserInfo;

/**
 * 登录界面
 * <p>登录流程：
 * <br>1、初始化URS，获取urs_id, urs_key，如果已初始化，则直接进入2
 * <br>2、登录URS，获取token
 * <br>3、如果是同城约会帐号，则判断是否已经注册过；如果是手机号，进入6
 * <br>4、同城约会未注册，则结束；已注册且未绑定手机号，进入5；已注册且已绑定手机号，进入6
 * <br>5、同城约会帐号绑定手机号
 * <br>6、上传地理位置信息并获取UserInfo，如果未完成注册（信息不全），进入7，否则进入8
 * <br>7、进入补全信息界面
 * <br>8、完成
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class ActivityLogin extends ActivityEngagementBase {
    public static final String EXTRA_ACCOUNT_TYPE = "extra_account_type";
    public static final String EXTRA_RESULT_LOGIN_SUCCESS = "extra_result_login_success";
    
    private final int REQUEST_CODE_SELECT_SEX = 1;
    
    private Activity mActivity;
    
    /** 帐号 */
    private EditText mAccountEt;
    /** 密码 */
    private EditText mPasswordEt;
    
    private TextView loginBtn;

    /** 检查是否是同城帐号的Transaction id */
    private int mCheckTid;
    /** 登录后获取用户信息的Transaction id */
    private int mGetUserTid;
    /** 获取手机绑定的通行证帐号的Transaction id*/
    private int mQueryAccountTid;
    
    /** 帐号类型 */
    private int mAccountType = EgmProtocolConstants.AccountType.Mobile;
    /** 帐号 */
    private String mUserName;
    /** 密码 */
    private String mPassword;
    /** 定位 */
    private EgmLocationManager mLocation;
    private int mLoginTid;
    private String mToken;
    
    private AlertDialog mDialog;
    private boolean mBIsNewReg = false;
    
	public static void startActivity(Context context, int accountType){
		Intent intent = new Intent(context, ActivityLogin.class);
		intent.putExtra(EXTRA_ACCOUNT_TYPE, accountType);
		
		//非activity的context启动activity会出错
		if(!(context instanceof Activity)){
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		
		context.startActivity(intent);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.getWindow().setBackgroundDrawableResource(R.color.app_background);
        this.getActionBar().hide();
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        Bundle extra = this.getIntent().getExtras();
        if(extra != null && extra.containsKey(EXTRA_ACCOUNT_TYPE)){
            mAccountType = extra.getInt(EXTRA_ACCOUNT_TYPE);
        }
        
        mActivity = this;
        setContentView(R.layout.fragment_login);
        initViews();
        
        mLocation = new EgmLocationManager(mActivity);
        
        EgmService.getInstance().addListener(mEgmCallback);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        if(mAccountType == EgmProtocolConstants.AccountType.Yuehui){
            // 自动弹出软键盘
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mActivity != null) {
                        InputMethodManager imm = (InputMethodManager)EngagementApp.getAppInstance()
                                .getSystemService(Service.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mAccountEt, 0);
                    }
                }
            }, 300);
            mAccountEt.requestFocus();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EgmService.getInstance().removeListener(mEgmCallback);
        mLocation.stop();
    }
    
    private void initViews() {
        findViewById(R.id.title_back).setOnClickListener(mClickBack);
        findViewById(R.id.title_right).setVisibility(View.INVISIBLE);
        
        TextView title = (TextView)findViewById(R.id.title_title);
        title.setText(R.string.login);
        
        loginBtn = (TextView) findViewById(R.id.login);
        loginBtn.setOnClickListener(mClickLogin);
        
        mAccountEt = (EditText)findViewById(R.id.login_user_name);
        mPasswordEt = (EditText)findViewById(R.id.login_password);
        
        if(mAccountType == EgmProtocolConstants.AccountType.Mobile){
            mAccountEt.setInputType(InputType.TYPE_CLASS_PHONE);
        }
        
        mAccountEt.addTextChangedListener(textWatcher);
        mPasswordEt.addTextChangedListener(textWatcher);
        
        // 忘记密码
        TextView forgetPasswordTV = (TextView) findViewById(R.id.login_forget_password);
        forgetPasswordTV.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); 
        forgetPasswordTV.findViewById(R.id.login_forget_password).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
					Uri uri = Uri.parse(EgmConstants.ACCOUNT_FORGET_PASSWORD_LINK);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				} catch (Exception e) {
				}
            }
        });
        View externallogin = findViewById(R.id.external_login_layout);


        TextView wechat = (TextView)findViewById(R.id.icon_wechat);
        
     // 同城1.0帐号登录
		View loginYuehui = findViewById(R.id.lay_yuehui);
		loginYuehui.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityLogin.startActivity(mActivity,
						EgmProtocolConstants.AccountType.Yuehui);
			}
		});
		// 易信帐号登录
		View yixin = findViewById(R.id.lay_yixin);
		yixin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityExternalLogin.startActivity(mActivity,
						EgmProtocolConstants.AccountType.YiXin);
			}
		});
		
        if(mAccountType == EgmProtocolConstants.AccountType.Mobile){
            mAccountEt.setHint(R.string.reg_input_mobile);
            findViewById(R.id.login_others).setVisibility(View.VISIBLE);
            externallogin.setVisibility(View.VISIBLE);
        }
        else{
            mAccountEt.setHint(R.string.reg_input_yuehui_account);
            findViewById(R.id.login_others).setVisibility(View.GONE);
            externallogin.setVisibility(View.GONE);
        }
    }
    
    private TextWatcher textWatcher = new TextWatcher(){
        @Override
        public void afterTextChanged(Editable s) {
        	String s1 = mAccountEt.getText().toString();
        	String s2 = mPasswordEt.getText().toString();
        	if (TextUtils.isEmpty(s1) || TextUtils.isEmpty(s2)) {
        		loginBtn.setEnabled(false);
        	} else {
        		loginBtn.setEnabled(true);
        	}
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    };


    private void doLoginURS() {
        String user = mAccountEt.getText().toString().trim();
        String password = mPasswordEt.getText().toString();

        switch(mAccountType){
            case EgmProtocolConstants.AccountType.Mobile:
                if(!checkMobile(user) || !checkPassword(password)){
                    return;
                }
                break;
            case EgmProtocolConstants.AccountType.Yuehui:
                if (TextUtils.isEmpty(user)) {
                    ToastUtil.showToast(mActivity, R.string.reg_tip_yuehui_is_empty);
                    return;
                }
                if(!checkPassword(password)){
                    return;
                }
                break;
        }
        
        mUserName = user;
        mPassword = password;
        
        showWatting(null, getString(R.string.reg_tip_logining), false);
        mLoginTid = EgmService.getInstance().doLoginURS(mUserName, mPassword);
        
    }
    
    /** 判断输入的手机号是否合法 */
    private boolean checkMobile(String mobile){
        boolean legal = false;
        
        if(!TextUtils.isEmpty(mobile)){
            if(EgmUtil.isValidatePhoneNum(mobile)){
                legal = true;
            }
            else{
                ToastUtil.showToast(mActivity, R.string.reg_tip_mobile_format_invalid);
            }
        }
        else{
            ToastUtil.showToast(mActivity, R.string.reg_tip_mobile_is_empty);
        }
        
        return legal;
    }
    
    /** 检查密码的合法性 */
    private boolean checkPassword(String psw){
        boolean legal = false;
        
        if(!TextUtils.isEmpty(psw)){
            legal = true;
        }
        else{
            ToastUtil.showToast(mActivity, R.string.reg_tip_password_is_empty);
        }
        
        return legal;
    }
    
    private void doCheckIsMember(){
        mCheckTid = EgmService.getInstance().doGetIsYuehuiAccount();
    }
    
    private void doGetUserInfo(){
        mGetUserTid = EgmService.getInstance().doLoginGetUserInfo(mUserName, mPassword, mToken, mAccountType, 
                mLocation.mLatitude > 0 ? String.valueOf(mLocation.mLatitude) : null, 
                mLocation.mLongtitude > 0 ? String.valueOf(mLocation.mLongtitude) : null, 
                mLocation.mProvinceCode > 0 ? String.valueOf(mLocation.mProvinceCode) : null, 
                mLocation.mCityCode > 0 ? String.valueOf(mLocation.mCityCode) : null, 
                mLocation.mDistrictCode > 0 ? String.valueOf(mLocation.mDistrictCode) : null);
    }
    
    private void doQueryAccount() {
    	mQueryAccountTid = EgmService.getInstance().doQueryAccount(mUserName); //参数为手机号码
    }
    
    /** 进入推荐页面 */
    private void gotoRecommend(int portraitStatus){
        ActivityHome.startActivity(mActivity, mBIsNewReg, portraitStatus);
        finish();
    }
    
//    /** 进入绑定手机号界面 */
//    private void gotoBindMobile(boolean needInviteCode){
//        ActivityBindMobile.startActivity(mActivity, mUserName, mPassword, mLoginHelper.getToken(), needInviteCode);
//        finish();
//    }
    
    /** 进入选择性别界面（欢迎页） */
    private void gotoSelectSex(){
        ActivityWelcome.startActivityForResult(mActivity, REQUEST_CODE_SELECT_SEX, ActivityWelcome.TYPE_LOGIN);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK || data == null)
            return;
        
        if(requestCode == REQUEST_CODE_SELECT_SEX){
            int sexType = EgmConstants.SexType.Female;
            Bundle extra = data.getExtras();
            
            // 从选性别界面获取性别后再跳去注册界面
            if(extra != null){
                sexType = extra.getInt(ActivityWelcome.EXTRA_SEX_RESULT);
                gotoRegisterDetail(sexType);
                finish();
            }
        }
    }
    
    /** 前往补充资料界面 */
    private void gotoRegisterDetail(int sexType){
        ActivityRegisterDetail.startActivity(mActivity, 
                sexType, mUserName, mPassword, mAccountType, false);
    }
    
    private final View.OnClickListener mClickBack = new OnClickListener(){
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    
    /** 登录 */
    private final View.OnClickListener mClickLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doLoginURS();
        }
    };
    
    /* 点击软键盘外的区域使软键盘隐藏 */
    private InputMethodManager mManager;
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        if(event.getAction() == MotionEvent.ACTION_DOWN){  
            if(mManager != null && getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null){  
                mManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }  
        }  
        return super.onTouchEvent(event);  
    }

    private EgmCallBack mEgmCallback = new EgmCallBack(){
    	 /** 登录URS成功 */
        @Override
        public void onLoginURS(int transactionId, String token){
            if(transactionId != mLoginTid)
                return;
            mBIsNewReg = false;
            mToken = token;
            if(mAccountType == EgmProtocolConstants.AccountType.Mobile){
                doGetUserInfo();
            }
            else{
                doCheckIsMember();
            }
        }
        @Override
        public void onLoginURSError(int transactionId, int errCode, String errStr){
            if(transactionId != mLoginTid)
                return;

                String err = getString(R.string.reg_tip_login_error);
                switch(errCode){
                    case EgmServiceCode.TRANSACTION_COMMON_NOT_REGISTER:
                        if(mAccountType == EgmProtocolConstants.AccountType.Mobile){
//                        	stopWaiting();
//                        	err = getString(R.string.reg_tip_account_mobile_no_register_error);
                            
                            doQueryAccount();
                            err = "";
                        }
                        else{   // 同城约会1.0帐号
                        	stopWaiting();
                            err = getString(R.string.reg_tip_account_yuehui_no_register_error);
                        }
                        break;
                    case EgmServiceCode.TRANSACTION_COMMON_PASSWORD_ERROR:
                    	stopWaiting();
                        err = getString(R.string.reg_tip_account_error);
                        break;
                    default:
                    	stopWaiting();
                        err = errStr;
                        break;
                }
                
                if(!TextUtils.isEmpty(err)) {
                	ToastUtil.showToast(mActivity, err);
                }
        }
        
        /** 判断是否是同城约会1.0版本帐号，以及是否绑定（验证）过手机号 */
        @Override
        public void onIsYuehuiAccount(int transactionId,boolean isNewReg){
            if(transactionId != mCheckTid)
                return;
            mBIsNewReg = isNewReg;
            doGetUserInfo();
        }
        @Override
        public void onIsYuehuiAccountError(int transactionId, int errCode, String err){
            if(transactionId != mCheckTid)
                return;
            
            stopWaiting();
            switch(errCode){
//                case EgmServiceCode.TRANSACTION_ACCOUNT_NOT_BIND_MOBILE:
//                    gotoBindMobile(false);
//                    break;
//                case EgmServiceCode.TRANSACTION_ACCOUNT_NOT_BIND_MOBILE2:
//                    gotoBindMobile(true);
//                    break;
                case EgmServiceCode.TRANSACTION_ACCOUNT_NOT_YUEHUI_MENBER:
                    ToastUtil.showToast(mActivity, getString(R.string.reg_tip_is_not_yuehui_menber));
                    break;
                default:
                    ToastUtil.showToast(mActivity, err);
                    break;
            }
        }
        
        @Override
        public void onLoginGetUserInfo(int transactionId, LoginUserInfo loginUserInfo){
            if(transactionId != mGetUserTid)
                return;
            
            stopWaiting();
            ToastUtil.showToast(mActivity, getString(R.string.reg_tip_login_success));
            
            gotoRecommend(loginUserInfo.userInfo.portraitStatus);
        }
        @Override
        public void onLoginGetUsrInfoError(int transactionId, int errCode, String err){
            if(transactionId != mGetUserTid)
                return;
            
            stopWaiting();
            switch(errCode){
                case EgmServiceCode.TRANSACTION_COMMON_REGISTER_NOT_FINISH:
                    gotoSelectSex();
                    break;
                case EgmServiceCode.TRANSACTION_COMMON_NOT_REGISTER:
                    ToastUtil.showToast(mActivity, getString(R.string.reg_tip_no_register_error));
                    break;
//                case EgmServiceCode.TRANSACTION_ACCOUNT_NOT_BIND_MOBILE:
//                    gotoBindMobile(false);
//                    break;
//                case EgmServiceCode.TRANSACTION_ACCOUNT_NOT_BIND_MOBILE2:
//                    gotoBindMobile(true);
//                    break;
                case EgmServiceCode.TRANSACTION_COMMON_USER_BLOCK:
                	//被系统加黑的用户，登录时弹出一个对话框
//                    ToastUtil.showToast(mActivity, R.string.reg_tip_login_be_blacked);
                	 mDialog = EgmUtil.createEgmNoticeDialog(mActivity, err,
                			new OnClickListener() {
								@Override
								public void onClick(View v) {
									if(mDialog != null && mDialog.isShowing()) {
										mDialog.dismiss();
									}
								}
							});
                	 mDialog.show();
                    break;
                default:
                    ToastUtil.showToast(mActivity, err);
                    break;
            }
        }
		@Override
		public void onQueryAccount(int transactionId, String account) {
			if(mQueryAccountTid != transactionId) {
				return;
			}
			
			if(TextUtils.isEmpty(account)) { //帐号为空，证明该手机号没有urs账户，需要提示，并由用户返回上一级页面，自主注册
				ToastUtil.showToast(mActivity, getString(R.string.reg_tip_account_mobile_no_register_error));
			} else { //帐号不为空，绑定了163.com以外的域名，利用该帐号帮用户登录urs
				String password = mPasswordEt.getText().toString();
				
				if(!checkPassword(password)) {
					return;
				}
				
				mUserName = account;
				mPassword = password;
				
//				mLoginHelper.login(account, password);
				mLoginTid = EgmService.getInstance().doLoginURS(mUserName, mPassword);
			}
		}
		@Override
		public void onQueryAccountError(int transactionId, int errCode,
				String err) {
			if(mQueryAccountTid != transactionId) {
				return;
			}
			stopWaiting();
			switch (errCode) {
			case EgmServiceCode.TRANSACTION_COMMON_NOT_REGISTER:
				ToastUtil.showToast(mActivity, getString(R.string.reg_tip_no_register_error));
				break;

			default:
				ToastUtil.showToast(mActivity, err);
				break;
			}
			
		}
        
        
    };
}
