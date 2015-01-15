package com.netease.engagement.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.ProgressTextBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.Utils.SmsObserver;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.transactions.GetMobileVerifyTransaction;


public class ActivityRegisterEntrance extends ActivityEngagementBase {
    public static final String EXTRA_REGISTER_SEX_TYPE = "extra_register_sex_type";
    
    /** 重发倒计时60秒 */
    private final int TIME_COUNTER = 60;
    
    /** 手机号输入框 */
    private EditText mPhoneEt;
    /** 验证按钮，组合了文字和进度条的控件 */
    private ProgressTextBar mVerifyBtn;
    /** 验证码输入框 */
    private EditText mVerifyEt;
    /** 密码输入框 */
    private EditText mPswEt;
    /** 同意协议 */
    private CheckBox mAgreeCheck;
    /** 注册按钮 */
    private TextView mRegisterBtn;

    
    private Context mActivity;
    private int mSexType;

    private int mVerifyTid;
    private int mRegisterTid;
    
    private String mAesKey;
    private SmsObserver  mSmsObserver;//监听验证码短信

    public static void startActivity(Context context, int sexType) {
        Intent intent = new Intent(context, ActivityRegisterEntrance.class);
        intent.putExtra(EXTRA_REGISTER_SEX_TYPE, sexType);
        
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extra = this.getIntent().getExtras();
        mSexType = extra.getInt(EXTRA_REGISTER_SEX_TYPE);
        
        mActivity = this;
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.getWindow().setBackgroundDrawableResource(R.color.app_background);
        this.getActionBar().hide();
        setContentView(R.layout.fragment_register_entrance_layout);
        initView();
        
        EgmService.getInstance().addListener(mEgmCallBack);
        mSmsObserver = new SmsObserver(this, smsHandler);  
        getContentResolver().registerContentObserver(SmsObserver.SMS_INBOX, true, mSmsObserver);
    }

	public Handler smsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SmsObserver.MSG_VERIFYCODE:
				if(mVerifyEt != null && mVerifyEt.getVisibility() == View.VISIBLE){
					String code = (String)msg.obj;
					mVerifyEt.setText(code.trim());
				}
				break;
			}
		};
	};

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EgmService.getInstance().removeListener(mEgmCallBack);
        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        getContentResolver().unregisterContentObserver(mSmsObserver);
    }
    
    private void initView(){
    	findViewById(R.id.title_back).setOnClickListener(mClickBack);
        findViewById(R.id.title_right).setVisibility(View.INVISIBLE);
        
        TextView title = (TextView)findViewById(R.id.title_title);
        title.setText(R.string.register);
        
        mRegisterBtn = (TextView) findViewById(R.id.register);
        mRegisterBtn.setOnClickListener(mClickRegister);
        
        mPhoneEt = (EditText)findViewById(R.id.input_phone_number);
        mVerifyBtn = (ProgressTextBar)findViewById(R.id.verify_btn);
        mVerifyBtn.setMax(TIME_COUNTER);
        
        mVerifyEt = (EditText)findViewById(R.id.input_verify_number);
        mPswEt = (EditText)findViewById(R.id.input_password);
        
        mPhoneEt.addTextChangedListener(textWatcher);
        mVerifyEt.addTextChangedListener(textWatcher);
        mPswEt.addTextChangedListener(textWatcher);


        mVerifyBtn.setOnClickListener(mClickVerify);
        setVerifyBtnState(-1);
        mVerifyBtn.setClickable(false);
        
//        // 同城1.0版帐号登录
//        TextView external = (TextView)findViewById(R.id.external_login_citydate);
//        external.setOnClickListener(mClickExternalLogin);
        
        // 同意协议
        mAgreeCheck = (CheckBox)findViewById(R.id.reg_agree_items_check);
        
        findViewById(R.id.reg_agree_items_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCheckTerms();
            }
        });
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
    }
    
    private TextWatcher textWatcher = new TextWatcher(){
        @Override
        public void afterTextChanged(Editable s) {
        	String s1 = mPhoneEt.getText().toString();
        	String s2 = mVerifyEt.getText().toString();
        	String s3 = mPswEt.getText().toString();
        	if (TextUtils.isEmpty(s1) || TextUtils.isEmpty(s2) || TextUtils.isEmpty(s3)) {
        		mRegisterBtn.setEnabled(false);
        	} else {
        		mRegisterBtn.setEnabled(true);
        	}
        	
        	if(s1.length() == 0){
                mVerifyBtn.setClickable(false);
            }
            else{
                mVerifyBtn.setClickable(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    };

    
    /** 查看协议 */
    private void gotoCheckTerms(){
        ActivityWeb.startActivity(mActivity, EgmConstants.URL_SERVICE_TERMS, true);
    }

    /**
     * 设置验证按钮的状态。
     * @param progress 小于零则为待验证状态，否则为重发状态，值为等待下次重发的进度。
     */
    private void setVerifyBtnState(int progress){
        if(mVerifyBtn == null)
            return;
        
        if(progress < 0){   // 验证
            mVerifyBtn.setClickable(true);
            mVerifyBtn.setText(getString(R.string.phone_verify));
            mVerifyBtn.setProgress(TIME_COUNTER);
        }
        else{   // 重发
            mVerifyBtn.setText(getString(R.string.resend));
            progress = TIME_COUNTER - progress;
            mVerifyBtn.setProgress(progress); 
            if(mVerifyBtn.getMax() == progress){   // 进度满了
                mVerifyBtn.setClickable(true);
            }
            else{
                mVerifyBtn.setClickable(false);
            }
        }
    }
    
    /** 跳转到注册补全资料页面 */
    private void gotoRegisterDetail(){
        ActivityRegisterDetail.startActivity(mActivity, 
                mSexType, 
                mPhoneEt.getText().toString(), 
                mPswEt.getText().toString(), 
                EgmProtocolConstants.AccountType.Mobile, 
                true);
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
            int length = psw.length();
            if(length >= 6 && length <=16){
                legal = true;
            }
            else{
                ToastUtil.showToast(mActivity, R.string.reg_tip_password_is_illegal);
            }
        }
        else{
            ToastUtil.showToast(mActivity, R.string.reg_tip_password_is_empty);
        }
        
        return legal;
    }
    
    private boolean checkLicence(){
        boolean legal = false;
        
        if(mAgreeCheck.isChecked()){
            legal = true;
        }
        else{
            ToastUtil.showToast(mActivity, R.string.reg_tip_licence_empty);
        }
        
        return legal;
    }
    
    private boolean checkVerifyCode(String verify){
        boolean legal = false;
        
        if(!TextUtils.isEmpty(verify)){
            legal = true;
        }
        else{
            ToastUtil.showToast(mActivity, R.string.reg_tip_verify_is_empty);
        }
        
        return legal;
    }
    
    private final View.OnClickListener mClickBack = new OnClickListener(){
        @Override
        public void onClick(View v) {
            finish();
//            gotoRegisterDetail();
        }
    };
    
    private final View.OnClickListener mClickExternalLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ActivityLogin.startActivity(mActivity, EgmProtocolConstants.AccountType.Yuehui);
        }
    };
    
    private AlertDialog mRegisterAlreadyDialog;
    /** 处理获取验证码时手机号已注册网易通行证帐号的情况 */
    private void dealRegisterNeteaseAlready(){
        mRegisterAlreadyDialog = EgmUtil.createEgmBtnDialog(mActivity, null, 
                getString(R.string.reg_tip_register_netease_already), 
                getString(R.string.login), 
                getString(R.string.reg_btn_user_other_mobile), 
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int which = (Integer)v.getTag();
                        switch (which) {
                            case DialogInterface.BUTTON_NEGATIVE:   // 左 登录
                                ActivityLogin.startActivity(mActivity, EgmProtocolConstants.AccountType.Mobile);
                                break;
                            case DialogInterface.BUTTON_POSITIVE:
                                mRegisterAlreadyDialog.dismiss();
                                break;
                        }    
                    }
                });
        
        mRegisterAlreadyDialog.setCanceledOnTouchOutside(false);
        mRegisterAlreadyDialog.show();
    }
    
    private AlertDialog mBindAlreadyDialog;
    /** 处理获取验证码时手机号已被同城1.0帐号绑定的情况 */
    private void dealBindAlready(){
        mBindAlreadyDialog = EgmUtil.createEgmMenuDialog(mActivity, 
                getString(R.string.reg_tip_bind_yuehui_already), 
                new CharSequence[]{getString(R.string.reg_btn_user_other_mobile)}, // 使用其它手机号
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBindAlreadyDialog.dismiss();
                    }
                });
        
        mBindAlreadyDialog.setCanceledOnTouchOutside(false);
        mBindAlreadyDialog.show();
    }
    
    private View.OnClickListener mClickVerify = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String mobile = mPhoneEt.getText().toString();
            
            if(checkMobile(mobile)){
                showWatting(null, getString(R.string.common_tip_is_waitting), false);
                mVerifyTid = EgmService.getInstance().doGetMobileVerifyCode(GetMobileVerifyTransaction.TYPE_REGISTER, mobile);
            }
        }
    };
    
    private View.OnClickListener mClickRegister = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String mobile = mPhoneEt.getText().toString();
            String verify = mVerifyEt.getText().toString();
            String psw = mPswEt.getText().toString();
            
            if(checkMobile(mobile) && checkVerifyCode(verify) && checkPassword(psw) && checkLicence()){
                showWatting(null, getString(R.string.reg_tip_waitting_register), false);
                mRegisterTid = EgmService.getInstance().doRegisterMobile(mobile, psw, verify, mAesKey);
            }
        	
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
    
    private EgmCallBack mEgmCallBack = new EgmCallBack() {
        // 验证码
        @Override
        public void onGetMobileVerify(int transactionId, Object key){
            if(transactionId != mVerifyTid)
                return;
            
            stopWaiting();
            mCountDownTimer.start();    // 开始倒计时
            mAesKey = (String)key;
            ToastUtil.showToast(mActivity, R.string.reg_tip_verify_code_send);
        }
        
        @Override
        public void onGetMobileVerifyError(int transactionId, int errCode, String err){
            if(transactionId != mVerifyTid)
                return;
            
            stopWaiting();
            mAesKey = null;
            switch(errCode){
                case EgmServiceCode.TRANSACTION_COMMON_REGISTER_ALREADY:
                case EgmServiceCode.TRANSACTION_MOBILE_REGISTER_NETEASE_ALREADY:
                    dealRegisterNeteaseAlready();
                    break;
                case EgmServiceCode.TRANSACTION_MOBILE_BIND_YUEHUI_ALREADY: 
                    dealBindAlready();
                    break;
                case EgmServiceCode.TRANSACTION_SMS_REQUIRE_TOO_MANY:
                    ToastUtil.showToast(mActivity, R.string.reg_tip_verify_code_too_many);
                    break;
                
                default:
                    ToastUtil.showToast(mActivity, err);
                    break;
            }
        }
        
        // 注册
        @Override
        public void onRegisterMobile(int transactionId){
            if(transactionId == mRegisterTid){
                stopWaiting();
                gotoRegisterDetail();
            }
        }
        
        @Override
        public void onRegisterMobileError(int transactionId, int errCode, String err){
            if(transactionId != mRegisterTid)
                return;
            
            stopWaiting();
            switch(errCode){
                case EgmServiceCode.TRANSACTION_COMMON_REGISTER_ALREADY:
                case EgmServiceCode.TRANSACTION_MOBILE_REGISTER_NETEASE_ALREADY:
                    dealRegisterNeteaseAlready();
                    break;
                case EgmServiceCode.TRANSACTION_MOBILE_BIND_YUEHUI_ALREADY: 
                    ToastUtil.showToast(mActivity, R.string.reg_tip_register_already);
                    break;
                case EgmServiceCode.TRANSACTION_VERIFY_CODE_ERROR: 
                    ToastUtil.showToast(mActivity, R.string.reg_tip_verify_error);
                    break;
                case EgmServiceCode.TRANSACTION_MOBILE_FORMAT_ERROR: 
                    ToastUtil.showToast(mActivity, R.string.reg_tip_mobile_format_invalid);
                    break;
                
                default:
                    ToastUtil.showToast(mActivity, err);
                    break;
            }
            
        }
        
        // 注册后登录成功，把本界面关掉
        @Override
        public void onFillUserInfo(int transactionId, UserInfo userInfo){
            finish();
        }
    };

    /** 倒计时器，倒计时60秒，每隔一秒给出一个通知 */
    private CountDownTimer mCountDownTimer = new CountDownTimer(TIME_COUNTER * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            int progress = (int)(millisUntilFinished / 1000);
            setVerifyBtnState(progress);
        }

        @Override
        public void onFinish() {
            setVerifyBtnState(0);
        }
    };
}
