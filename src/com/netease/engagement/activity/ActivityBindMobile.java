package com.netease.engagement.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
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
import com.netease.engagement.widget.ProgressTextBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmLocationManager;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.LoginUserInfo;
import com.netease.service.transactions.GetMobileVerifyTransaction;

/**
 * 绑定手机号界面
 * @version 1.0
 */
public class ActivityBindMobile extends ActivityEngagementBase {
    public static final String EXTRA_USER_NAME = "extra_user_name";
    public static final String EXTRA_PASSWORD = "extra_password";
    public static final String EXTRA_TOKEN = "extra_token";
    public static final String EXTRA_NEED_INVITECODE = "extra_need_invite_code";
    
    /** 重发倒计时60秒 */
    private final int TIME_COUNTER = 60;
    
    /** 手机号输入框 */
    private EditText mPhoneEt;
    /** 验证按钮，组合了文字和进度条的控件 */
    private ProgressTextBar mVerifyBtn;
    /** 验证码输入框 */
    private EditText mVerifyEt;
    /** 邀请码 */
    private EditText mInviteEt;
    
    private Activity mActivity;
    /** 定位 */
    private EgmLocationManager mLocation;
    private boolean mIsNeedInviteCode = false;

    private int mVerifyTid;
    private int mBindTid;
    private int mGetUserTid;
    
    private String mUserName;
    private String mPassword;
    private String mToken;

    public static void startActivity(Context context, String userName, String password, String token, boolean needInviteCode) {
        Intent intent = new Intent(context, ActivityBindMobile.class);
        
        intent.putExtra(EXTRA_USER_NAME, userName);
        intent.putExtra(EXTRA_PASSWORD, password);
        intent.putExtra(EXTRA_TOKEN, token);
        intent.putExtra(EXTRA_NEED_INVITECODE, needInviteCode);
        
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extra = this.getIntent().getExtras();
        mUserName = extra.getString(EXTRA_USER_NAME);
        mPassword = extra.getString(EXTRA_PASSWORD);
        mToken = extra.getString(EXTRA_TOKEN);
        mIsNeedInviteCode = extra.getBoolean(EXTRA_NEED_INVITECODE, false);
        
        mActivity = this;
        mLocation = new EgmLocationManager(mActivity);
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        this.getWindow().setBackgroundDrawableResource(R.color.app_background);
        this.getActionBar().hide();
        setContentView(R.layout.fragment_bind_mobile_layout);
        initView();
        
        EgmService.getInstance().addListener(mEgmCallBack);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        // 自动弹出软键盘
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (mActivity != null) {
                    InputMethodManager imm = (InputMethodManager)EngagementApp.getAppInstance()
                            .getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mPhoneEt, 0);
                }
            }
        }, 300);
        mPhoneEt.requestFocus();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EgmService.getInstance().removeListener(mEgmCallBack);
        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        
        mLocation.stop();
    }
    
    private void initView(){
        // 返回
        View back = findViewById(R.id.title_back);
        back.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // 标题
        TextView titleTv = (TextView)findViewById(R.id.title_title);
        titleTv.setText(R.string.reg_title_bind_mobile);
        
        // 绑定
        View right = findViewById(R.id.title_right);
        right.setOnClickListener(mClickBind);
        
        mPhoneEt = (EditText)findViewById(R.id.input_phone_number);
        mVerifyBtn = (ProgressTextBar)findViewById(R.id.verify_btn);
        mVerifyBtn.setMax(TIME_COUNTER);
        
        mVerifyEt = (EditText)findViewById(R.id.input_verify_number);
        
        mPhoneEt.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0){
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
        });

        mVerifyBtn.setOnClickListener(mClickVerify);
        setVerifyBtnState(-1);
        mVerifyBtn.setClickable(false);
        
        mInviteEt = (EditText)findViewById(R.id.bind_invite);
        if(mIsNeedInviteCode){
            mInviteEt.setVisibility(View.VISIBLE);
        }
        else{
            mInviteEt.setVisibility(View.GONE);
        }
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
    
    /** 进入推荐页面 */
    private void gotoHome(){
        ActivityHome.startActivity(mActivity, false, -1);
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
    
    /** 处理获取验证码时手机号已注册网易通行证帐号的情况 */
    private void dealRegisterNeteaseAlready(){
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setMessage(R.string.reg_tip_register_netease_already)
                .setNegativeButton(R.string.login, new DialogInterface.OnClickListener(){  // 去登录
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityLogin.startActivity(mActivity, EgmProtocolConstants.AccountType.Mobile);
                    }
                })
                .setPositiveButton(R.string.back, null)    // 返回
                .create();
        
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    
    private void doGetUserInfo(){
        mGetUserTid = EgmService.getInstance().doLoginGetUserInfo(mUserName, mPassword, mToken, EgmProtocolConstants.AccountType.Yuehui, 
                mLocation.mLatitude > 0 ? String.valueOf(mLocation.mLatitude) : null, 
                mLocation.mLongtitude > 0 ? String.valueOf(mLocation.mLongtitude) : null, 
                mLocation.mProvinceCode > 0 ? String.valueOf(mLocation.mProvinceCode) : null, 
                mLocation.mCityCode > 0 ? String.valueOf(mLocation.mCityCode) : null, 
                mLocation.mDistrictCode > 0 ? String.valueOf(mLocation.mDistrictCode) : null);
    }
    
    private View.OnClickListener mClickVerify = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String mobile = mPhoneEt.getText().toString();
            
            if(checkMobile(mobile)){
                showWatting(null, getString(R.string.common_tip_is_waitting), false);
                mVerifyTid = EgmService.getInstance().doGetMobileVerifyCode(GetMobileVerifyTransaction.TYPE_BIND, mobile);
            }
        }
    };
    
    private View.OnClickListener mClickBind = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String mobile = mPhoneEt.getText().toString();
            String verify = mVerifyEt.getText().toString();
            String invite = "";
            if(mIsNeedInviteCode){
                invite = mInviteEt.getText().toString();
            }
            
            if(checkMobile(mobile) && checkVerifyCode(verify)){
                showWatting(null, getString(R.string.reg_tip_waitting_bind), false);
                mBindTid = EgmService.getInstance().doBindMobile(mobile, verify, invite);
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
        public void onGetMobileVerify(int transactionId, Object nullObj){
            if(transactionId != mVerifyTid)
                return;
            
            stopWaiting();
            mCountDownTimer.start();
            ToastUtil.showToast(mActivity, R.string.reg_tip_verify_code_send);
        }
        
        @Override
        public void onGetMobileVerifyError(int transactionId, int errCode, String err){
            if(transactionId != mVerifyTid)
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
                case EgmServiceCode.TRANSACTION_SMS_REQUIRE_TOO_MANY:
                    ToastUtil.showToast(mActivity, R.string.reg_tip_verify_code_too_many);
                    break;
                default:
                    ToastUtil.showToast(mActivity, err);
                    break;
            }
        }
        
        // 绑定手机号
        @Override
        public void onBindMobile(int transactionId){
            if(transactionId != mBindTid)
                return;
            
            ToastUtil.showToast(mActivity, R.string.reg_tip_mobile_bind_success);
            doGetUserInfo();
        }
        @Override
        public void onBindMobileError(int transactionId, int errCode, String err){
            if(transactionId != mBindTid)
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
        
        @Override
        public void onLoginGetUserInfo(int transactionId, LoginUserInfo loginUserInfo){
            if(transactionId != mGetUserTid)
                return;
            
            stopWaiting();
            ToastUtil.showToast(mActivity, getString(R.string.reg_tip_login_success));
            finish(); // 结束当前界面
            gotoHome();
        }
        @Override
        public void onLoginGetUsrInfoError(int transactionId, int errCode, String err){
            if(transactionId != mGetUserTid)
                return;
            
            stopWaiting();
            switch(errCode){
                case EgmServiceCode.TRANSACTION_COMMON_NOT_REGISTER:
                    ToastUtil.showToast(mActivity, getString(R.string.reg_tip_no_register_error));
                    break;
                case EgmServiceCode.TRANSACTION_COMMON_USER_BLOCK:
                    ToastUtil.showToast(mActivity, R.string.reg_tip_login_be_blacked);
                    break;
                default:
                    ToastUtil.showToast(mActivity, err);
                    break;
            }
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
